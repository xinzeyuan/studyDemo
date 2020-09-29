//package com.study.demo.config;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.oppo.iot.ota.silent.common.enums.UpgradeResultType;
//import com.oppo.iot.ota.silent.common.exception.EsNoResourceException;
//import com.oppo.iot.ota.silent.common.utils.StrategyTableUtil;
//import com.oppo.iot.ota.silent.record.bo.*;
//import com.oppo.iot.ota.silent.record.bo.personas.GetPersonasReq;
//import com.oppo.iot.ota.silent.record.config.EsConfig;
//import com.oppo.iot.ota.silent.record.enums.RecordStatusEnum;
//import com.oppo.iot.ota.silent.record.utils.ESCriteria;
//import org.apache.http.HttpHost;
//import org.elasticsearch.action.ActionListener;
//import org.elasticsearch.action.bulk.BulkItemResponse;
//import org.elasticsearch.action.bulk.BulkRequest;
//import org.elasticsearch.action.bulk.BulkResponse;
//import org.elasticsearch.action.index.IndexRequest;
//import org.elasticsearch.action.search.SearchRequest;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.action.search.SearchScrollRequest;
//import org.elasticsearch.action.update.UpdateRequest;
//import org.elasticsearch.client.RestClient;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.common.unit.TimeValue;
//import org.elasticsearch.common.xcontent.XContentType;
//import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.index.query.RangeQueryBuilder;
//import org.elasticsearch.index.query.TermQueryBuilder;
//import org.elasticsearch.script.Script;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.SearchHits;
//import org.elasticsearch.search.aggregations.AggregationBuilders;
//import org.elasticsearch.search.aggregations.bucket.terms.Terms;
//import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
//import org.elasticsearch.search.builder.SearchSourceBuilder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.util.CollectionUtils;
//import org.springframework.util.StringUtils;
//
//import java.io.IOException;
//import java.util.*;
//import java.util.concurrent.Semaphore;
//import java.util.concurrent.TimeUnit;
//
//@Component
//public class ESProcessor implements InitializingBean {
//
//    public static final String SSOID = "ssoid";
//    public static final String DEVICE_ID = "deviceId";
//    private static final Logger LOGGER = LoggerFactory.getLogger(ESProcessor.class);
//    public static final String FIRMWARE_INFO_ID = "firmwareInfoId";
//
//    @Autowired
//    private EsConfig esConfig;
//
//    private Semaphore semaphore;
//
//    private RestHighLevelClient client = null;
//
//    /**
//     * 异步写入
//     * @param upgradeHistoryBaseData
//     */
//    public void insertAsync(EsData<BaseBo> upgradeHistoryBaseData){
//        if(!upgradeHistoryBaseData.validData()){
//            return;
//        }
//        tryAcquireSemaphore();
//        BulkRequest request = buildInsertBulkRequest(upgradeHistoryBaseData);
//        client.bulkAsync(request, new ActionListener<BulkResponse>() {
//            @Override
//            public void onResponse(BulkResponse bulkItemResponses) {
//                releaseSemaphore();
//                if(bulkItemResponses.hasFailures()){
//                    for (BulkItemResponse bulkItemResponse : bulkItemResponses) {
//                        if (bulkItemResponse.isFailed()) {
//                            LOGGER.error("es insert error,index[{}] type[{}] cause:{}", bulkItemResponse.getIndex(), bulkItemResponse.getType(), bulkItemResponse.getFailureMessage());
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                releaseSemaphore();
//                LOGGER.error("insert es error", e);
//            }
//        });
//    }
//
//    /**
//     * 同步写入
//     * @param upgradeHistoryBaseData
//     * @throws IOException
//     */
//    public void insert(EsData<BaseBo> upgradeHistoryBaseData) throws IOException {
//        BulkRequest request = buildInsertBulkRequest(upgradeHistoryBaseData);
//        bulkHandle(request);
//    }
//
//    /**
//     * 同步更新升级记录状态
//     * @param upgradeHistoryEsData
//     * @throws IOException
//     */
//    public void updateUpgradeHistoryStatus(EsData<FirmwareUpgradeHistory> upgradeHistoryEsData) throws IOException {
//        BulkRequest request = buildUpdateBulkRequest(upgradeHistoryEsData);
//        bulkHandle(request);
//    }
//
//    public void updateUpgradeHistoryStatusNew(EsData<FirmwareUpgradeHistory> upgradeHistoryEsData) throws IOException {
//        BulkRequest request = buildUpdateBulkRequest(upgradeHistoryEsData);
//        bulkHandle(request);
//    }
//
//    private final static String AGGREGATION_NAME = "group_by_status";
//    /**
//     *
//     * @param index
//     * @param type
//     * @return
//     * @throws IOException
//     */
//    public Map<UpgradeResultType,Long> countUpgradeHistoryByStatus(String index, String type) throws Throwable {
//        SearchRequest searchRequest = buildCountUpgradeHistoryByStatusReq(index, type);
//        SearchResponse response = null;
//        try {
//            tryAcquireSemaphore();
//            LOGGER.debug("countUpgradeHistoryByStatus >>> source (es query)>> "+ searchRequest.source().toString());
//            response = client.search(searchRequest);
//        }catch (Exception e){
//            LOGGER.error("countUpgradeHistoryByStatus>>> client.search >> 【error】 +index>>" + index + "   type>>> " + type + "     source>> "+ searchRequest.source().toString(),e);
//            return Maps.newHashMap();
//        }finally {
//            releaseSemaphore();
//        }
//        Terms groupByStatusTerms = response.getAggregations().get(AGGREGATION_NAME);
//        Map<UpgradeResultType,Long> resultTypeLongMap = new HashMap<>();
//        for(Terms.Bucket bucket: groupByStatusTerms.getBuckets()){
//            int key = bucket.getKeyAsNumber().intValue();
//            long count = bucket.getDocCount();
//            UpgradeResultType upgradeResultType = UpgradeResultType.getType(key);
//            if(null!=upgradeResultType){
//                Long lastCount = resultTypeLongMap.getOrDefault(upgradeResultType,0L);
//                resultTypeLongMap.put(upgradeResultType,lastCount+count);
//            }
//        }
//        return resultTypeLongMap;
//    }
//
//
//    public PersonasRecordResp getPersonas(GetPersonasReq req) throws IOException {
//        List<Personas> personaList = new LinkedList<>();
//        SearchResponse search = null;
//        if(null == req.getSearchId()) {
//            SearchRequest searchRequest = buildGetPersonasReq(req);
//            search = client.search(searchRequest);
//        }
//        else {
//            SearchScrollRequest searchScrollRequest = new SearchScrollRequest();
//            searchScrollRequest.scrollId(req.getSearchId());
//            searchScrollRequest.scroll(new TimeValue(300,TimeUnit.SECONDS));
//            search = client.searchScroll(searchScrollRequest);
//        }
//        SearchHits hits = search.getHits();
//        for(SearchHit hit : hits){
//            hit.getId();
//            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
//            Integer activeUser = (Integer) sourceAsMap.get("activeUser");
//            Boolean betaUser = (Boolean) sourceAsMap.get("betaUser");
//            Integer deviceActiveUser = (Integer) sourceAsMap.get("deviceActiveUser");
//            String country = (String)sourceAsMap.get("country");
//            String province = (String)sourceAsMap.get("province");
//            String city = (String)sourceAsMap.get("city");
//            String ssoid = (String)sourceAsMap.get("ssoid");
//            personaList.add(Personas.builder().ssoid(ssoid).activeUser(activeUser).betaUser(betaUser).deviceActiveUser(deviceActiveUser).country(country)
//            .province(province).city(city).build());
//        }
//
//        String scrollId = hits.getHits().length > 0 ? search.getScrollId() : null;
//        return PersonasRecordResp.builder().personasBoList(personaList).searchId(scrollId).build();
//    }
//
//    /**
//     * 更新用户画像公测用户标签
//     * @param ssoid2BetaUserMap
//     * @throws IOException
//     */
//    public void updatePersonasBetaUserTag(Map<String,Boolean> ssoid2BetaUserMap) throws IOException {
//        BulkRequest request = buildUpdatePersonasRequest(ssoid2BetaUserMap);
//        bulkHandle(request);
//    }
//
//    //==================private method===================//
//    /**
//     * 批量请求处理
//     * @param request
//     */
//    private void bulkHandle(BulkRequest request) throws EsNoResourceException,IOException{
//        try {
//            tryAcquireSemaphore();
//            client.bulk(request);
//        } finally {
//            releaseSemaphore();
//        }
//    }
//
////    protected interface BulkProcess{
////        void process(IndexRequest request, EsData<BaseBo> esData);
////    }
//
//    /**
//     * 构建写入记录请求
//     * @param baseBoEsData
//     * @return
//     */
//    private BulkRequest buildInsertBulkRequest(EsData<BaseBo> baseBoEsData){
//        BulkRequest request = new BulkRequest();
//        for(BaseBo baseBo : baseBoEsData.getDocList()){
//            IndexRequest indexRequest = new IndexRequest();
//            indexRequest.index(baseBoEsData.getIndexName());
//            indexRequest.type(baseBoEsData.getType());
//            indexRequest.id(baseBo.getId());
//            indexRequest.source(JSONObject.toJSONString(baseBo), XContentType.JSON);
//            request.add(indexRequest);
//        }
//        return request;
//    }
//
//    /**
//     * 构建更新升级历史请求
//     * @param upgradeHistoryEsData
//     * @return
//     */
//    private BulkRequest buildUpdateBulkRequest(EsData<FirmwareUpgradeHistory> upgradeHistoryEsData){
//        BulkRequest request = new BulkRequest();
//        for(FirmwareUpgradeHistory doc : upgradeHistoryEsData.getDocList()){
//            UpdateRequest updateRequest = new UpdateRequest();
//
//            updateRequest.index(upgradeHistoryEsData.getIndexName());
//            updateRequest.type(upgradeHistoryEsData.getType());
//            updateRequest.id(doc.getId());
//            String script = "ctx._source.status=" + doc.getStatus();
//            updateRequest.script(new Script(script));
//            request.add(updateRequest);
//        }
//        LOGGER.debug("【buildUpdateBulkRequest】 req:{}", JSON.toJSONString(request));
//        return request;
//    }
//
//    /**
//     * 构建统计升级历史记录状态请求
//     * @param index
//     * @param type
//     * @return
//     */
//    private SearchRequest buildCountUpgradeHistoryByStatusReq(String index, String type){
//        SearchRequest searchRequest = new SearchRequest();
//        searchRequest.indices(index);
//        searchRequest.types(type);
//        TermsAggregationBuilder aggregation = AggregationBuilders.terms(AGGREGATION_NAME)
//                .field("status");
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        searchSourceBuilder.aggregation(aggregation);
//        searchSourceBuilder.size(0);
//        searchRequest.source(searchSourceBuilder);
//        return searchRequest;
//    }
//
//    /**
//     * 构建用户画像请求
//     * @param req
//     * @return
//     */
//    private SearchRequest buildGetPersonasReq(GetPersonasReq req){
//        SearchSourceBuilder searchSourceBuilder =  new SearchSourceBuilder();
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//        if(!CollectionUtils.isEmpty(req.getSsoidList())){
//            TermQueryBuilder idTermQueryBuilder = QueryBuilders.termQuery("_id", req.getSsoidList());
//            boolQueryBuilder.filter(idTermQueryBuilder);
//        }
//        if(req.getActiveUser()!=null){
//            RangeQueryBuilder activeUserQueryBuilder = QueryBuilders.rangeQuery("activeUser").gte(req.getActiveUser());
//            boolQueryBuilder.filter(activeUserQueryBuilder);
//        }
//        if(req.getDeviceActiveUser()!=null){
//            RangeQueryBuilder deviceActiveUserQueryBuilder = QueryBuilders.rangeQuery("deviceActiveUser").gte(req.getDeviceActiveUser());
//            boolQueryBuilder.filter(deviceActiveUserQueryBuilder);
//        }
//        if(req.getBetaUser()!=null){
//            TermQueryBuilder betaUserTermQueryBuilder = QueryBuilders.termQuery("betaUser", req.getBetaUser());
//            boolQueryBuilder.filter(betaUserTermQueryBuilder);
//        }
//        searchSourceBuilder.query(boolQueryBuilder);
//        searchSourceBuilder.from(req.getFrom());
//        searchSourceBuilder.size(req.getPageSize());
//        searchSourceBuilder.fetchSource(new String[]{"id","activeUser","betaUser","deviceActiveUser","country","province","city","ssoid"},new String[]{});
//        SearchRequest searchRequest = new SearchRequest();
//        searchRequest.indices(Personas.INDEX_NAME);
//        searchRequest.types(Personas.TYPE);
//        searchRequest.source(searchSourceBuilder);
//        searchRequest.scroll(new TimeValue(300,TimeUnit.SECONDS));
//        return searchRequest;
//    }
//
//    /**
//     * 构建更新用户画像请求
//     * @param ssoid2BetaUserMap
//     * @return
//     */
//    private BulkRequest buildUpdatePersonasRequest(Map<String,Boolean> ssoid2BetaUserMap){
//        BulkRequest request = new BulkRequest();
//        for(Map.Entry<String,Boolean> ssoid2BetaUserEntry : ssoid2BetaUserMap.entrySet()){
//            UpdateRequest updateRequest = new UpdateRequest();
//            updateRequest.index(Personas.INDEX_NAME);
//            updateRequest.type(Personas.TYPE);
//            updateRequest.id(ssoid2BetaUserEntry.getKey());
//            String script = "ctx._source.betaUser=" + ssoid2BetaUserEntry.getValue();
//            updateRequest.script(new Script(script));
//            request.add(updateRequest);
//        }
//        return request;
//    }
//
//    private void tryAcquireSemaphore(){
//        if(!semaphore.tryAcquire()){
//            throw new EsNoResourceException("es over max concurrent request");
//        }
//    }
//
//    private void releaseSemaphore(){
//        semaphore.release();
//    }
//
//    private void init(String[] nodes, int maxConcurrentRequest){
//        HttpHost[] httpHosts = new HttpHost[nodes.length];
//        if(nodes != null){
//            for(int i = 0; i < nodes.length ; i++){
//                String node = nodes[i] ;
//                String[] addrInfo = node.split(":");
//                httpHosts [i] = new HttpHost(addrInfo[0],Integer.parseInt(addrInfo[1]),"http");
//            }
//        }
//        client = new RestHighLevelClient(RestClient.builder(httpHosts));
//        semaphore = new Semaphore(maxConcurrentRequest);
//    }
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
//        init(esConfig.getEsNodes(),esConfig.getMaxConnection());
//    }
//
//    public Integer queryNewRecordsResp(String indexName, String type,Long  firmInfoId, String  ssoId, String  deviceId) {
//        try{
//            //1、用户id过滤  2、固件id过滤 3、deviceId过滤       firmware_info_id    sssoid   device_id
//            //--------------------es查询-0----------------------------------
//            SearchSourceBuilder queryBuilder = ESCriteria.create().addTermQuery(FIRMWARE_INFO_ID, firmInfoId).addTermQuery(SSOID, ssoId.toString())
//                    .addTermQuery(DEVICE_ID,deviceId.toString()).builder();
//
//
////            BoolQueryBuilder boolQueryBuilder1 =  new  BoolQueryBuilder();
////            boolQueryBuilder1.filter(QueryBuilders.termQuery(FIRMWARE_INFO_ID, firmInfoId));
////
////            BoolQueryBuilder boolQueryBuilder2 =  new  BoolQueryBuilder();
////            boolQueryBuilder2.filter(QueryBuilders.termQuery(DEVICE_ID, deviceId.toString()));
////            boolQueryBuilder1.must(boolQueryBuilder2);
////            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
////            searchSourceBuilder.query(boolQueryBuilder1);
//
//            queryBuilder.fetchSource(new String[]{"id",FIRMWARE_INFO_ID,"ssoid", DEVICE_ID},new String[]{});
//            SearchRequest searchRequest = new SearchRequest();
//            searchRequest.indices(indexName);
//            searchRequest.types(type);
//            searchRequest.source(queryBuilder);
//
//            LOGGER.debug("查询的ES 的query语句为  : {}", searchRequest.source());
//
//            SearchResponse search = client.search(searchRequest);
//
//            SearchHits hits = search.getHits();
//            for (SearchHit hit : hits) {
//                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
//                LOGGER.debug("查询到的ES结果为 >>: {}", searchRequest.source());
//            }
//            return hits.getHits().length;
//        }catch (Exception e){
//            LOGGER.error("queryNewRecordsResp error" + e);
//            e.printStackTrace();
//            return 0;
//        }
//    }
//
//    public List<FirmwareUpgradeHistory> getTargetSatusDeviceRecord(String indexName, String type, List<Byte> statuses) throws Exception{
//        SearchSourceBuilder queryBuilder = ESCriteria.create().addTermsQuery("status", statuses).builder();
//
//        SearchRequest searchRequest = new SearchRequest();
//        searchRequest.indices(indexName);
//        searchRequest.types(type);
//        searchRequest.source(queryBuilder);
//
//        SearchResponse search = client.search(searchRequest);
//        SearchHits hits = search.getHits();
//        List<FirmwareUpgradeHistory> histories = new ArrayList<>();
//        for (SearchHit hit : hits) {
//            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
//            Long firmwareInfoId = (Long) sourceAsMap.get("firmwareInfoId");
//            String productId = (String) sourceAsMap.get("productId");
//            String ssoid = (String) sourceAsMap.get("ssoid");
//            String deviceId = (String)sourceAsMap.get("deviceId");
//            String target = (String)sourceAsMap.get("target");
//            Integer status = (Integer)sourceAsMap.get("status");
//            histories.add(FirmwareUpgradeHistory.builder().firmwareInfoId(firmwareInfoId)
//                    .productId(productId).ssoid(ssoid).deviceId(deviceId).target(target).status(status)
//                .build());
//        }
//        return histories;
//    }
//
//    public boolean getIsUpgradingOfThisDevice(String indexName, String deviceId, String target, String productId) throws  Exception{
//        SearchSourceBuilder queryBuilder = ESCriteria.create()
//                .addMatchPhraseQuery("status", "0")
//                .addMatchPhraseQuery("deviceId", deviceId)
//                .addMatchPhraseQuery("target", target)
//                .addMatchPhraseQuery("productId", productId)
//                .builder();
//
//        SearchRequest searchRequest = new SearchRequest();
//        searchRequest.indices(indexName);
//        searchRequest.source(queryBuilder);
//        searchRequest.types(StrategyTableUtil.UPGRADE_RECORD_TABLE_INDEX_TYPE);
//        SearchResponse search = client.search(searchRequest);
//        SearchHits hits = search.getHits();
//
//        return hits.getHits().length > 0;
//    }
//
//    public Map getTargetSatusDeviceRecord(String scrollId, String indexName, String type, List<Byte> statuses) throws  Exception{
//        SearchResponse search = null;
//        if(StringUtils.isEmpty(scrollId)) {
//            SearchRequest searchRequest = buildStatusDeviceRecordCondition(indexName, type, statuses);
//            search = client.search(searchRequest);
//        }else {
//            SearchScrollRequest searchScrollRequest = new SearchScrollRequest();
//            searchScrollRequest.scrollId(scrollId);
//            searchScrollRequest.scroll(new TimeValue(300,TimeUnit.SECONDS));
//            search = client.searchScroll(searchScrollRequest);
//        }
//        SearchHits hits = search.getHits();
//        List<FirmwareUpgradeHistory> histories = new ArrayList<>();
//        for (SearchHit hit : hits) {
//            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
//            Long firmwareInfoId = (Long) sourceAsMap.get("firmwareInfoId");
//            String productId = (String) sourceAsMap.get("productId");
//            String ssoid = (String) sourceAsMap.get("ssoid");
//            String deviceId = (String)sourceAsMap.get("deviceId");
//            String target = (String)sourceAsMap.get("target");
//            Integer status = (Integer)sourceAsMap.get("status");
//            histories.add(FirmwareUpgradeHistory.builder().firmwareInfoId(firmwareInfoId)
//                    .productId(productId).ssoid(ssoid).deviceId(deviceId).target(target).status(status)
//                    .build());
//        }
//        Map resultMap = new HashMap();
//        resultMap.put("resultList",histories);
//        scrollId = hits.getHits().length > 0 ? search.getScrollId() : null;
//        resultMap.put("scrollId",scrollId);
//
//        return resultMap;
//
//    }
//
//    private SearchRequest buildStatusDeviceRecordCondition(String indexName, String type, List<Byte> statuses) throws  Exception{
//        SearchSourceBuilder queryBuilder = ESCriteria.create().addTermsQuery("status", statuses).builder();
//        SearchRequest searchRequest = new SearchRequest();
//        searchRequest.indices(indexName);
//        searchRequest.types(type);
//        searchRequest.source(queryBuilder);
//        searchRequest.scroll(new TimeValue(300,TimeUnit.SECONDS));
//        return searchRequest;
//    }
//
//
//    public boolean existsOfThisDevice(String indexName, String deviceId, String target, String productId) throws  Exception{
//        SearchSourceBuilder queryBuilder = ESCriteria.create()
//                .addMatchPhraseQuery("deviceId", deviceId)
//                .addMatchPhraseQuery("target", target)
//                .addMatchPhraseQuery("productId", productId)
//                .builder();
//
//        SearchRequest searchRequest = new SearchRequest();
//        searchRequest.indices(indexName);
//        searchRequest.source(queryBuilder);
//        searchRequest.types(StrategyTableUtil.UPGRADE_RECORD_TABLE_INDEX_TYPE);
//        SearchResponse search = client.search(searchRequest);
//        SearchHits hits = search.getHits();
//
//        return hits.getHits().length > 0;
//    }
//
//    public boolean getHaveAllUpgradeSuccessForDetail(String indexname, String type) throws Exception{
//        SearchSourceBuilder queryBuilder = ESCriteria.create()
//                .addNotTermsQuery("status", Lists.newArrayList(RecordStatusEnum.SUCCESS.getStatus()))
//                .builder();
//
//        SearchRequest searchRequest = new SearchRequest();
//        searchRequest.indices(indexname);
//        searchRequest.source(queryBuilder);
//        searchRequest.types(StrategyTableUtil.UPGRADE_RECORD_TABLE_INDEX_TYPE);
//        SearchResponse search = client.search(searchRequest);
//        SearchHits hits = search.getHits();
//
//        return hits.getHits().length == 0;
//    }
//}
