///*
//package com.study.demo.config;
//
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.mongodb.client.FindIterable;
//import com.mongodb.client.MongoCollection;
//import com.mongodb.client.MongoCursor;
//import com.mongodb.client.model.Filters;
//import com.oppo.basic.heracles.client.core.spring.annotation.HeraclesDynamicConfig;
//import com.oppo.iot.shadow.bo.*;
//import com.oppo.iot.shadow.dao.ShadowQueryDao;
//import com.oppo.iot.shadow.dao.entity.DeviceInfo;
//import com.oppo.iot.shadow.dao.entity.DeviceShadow;
//import com.oppo.iot.shadow.dao.entity.DeviceState;
//import com.oppo.iot.smarthome.common.utils.JacksonUtils;
//import com.oppo.trace.springaop.WithTrace;
//import org.apache.commons.lang3.StringUtils;
//import org.bson.Document;
//import org.bson.conversions.Bson;
//import org.bson.json.JsonWriterSettings;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Repository;
//import org.springframework.util.CollectionUtils;
//
//import java.util.*;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//*/
///**
// * @author:
// * @Date: 2019/7/29
// *//*
//
//@Repository
//@WithTrace(recordMethodArgs = true)
//public class MongoDBQueryDaoImpl implements ShadowQueryDao {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBQueryDaoImpl.class);
//    private final int SHARDING_SIZE = 100;
//    @Autowired
//    private MongoDBClient client;
//
//    @HeraclesDynamicConfig(key = "shadowTimeout", fileName = "shadow.properties")
//    @Value("${shadowTimeout}")
//    private int shadowTimeout;
//
//    private static final String SHARDING_PREFIX = "shadow_deviceId_prefix_";
//    private static final String DEVICE_ID_FIELD = "deviceId";
//    private static final String TIMESTAMP_FIELD = "timestamp";
//    private static final String PRODUCT_ID_FIELD = "productId";
//    private static final String SSO_ID_FIELD = "ssoId";
//    private static final String PROVINCE_FIELD = "province";
//    private static final String COUNTRY_FIELD = "country";
//    private static final String CITY_FIELD = "city";
//    private static final String DEFAULT_DOC_ID_FIELD = "_id";
//    private static final String STATE_REPORTED_PREFIX = "state.reported.";
//    private static final String FIRMWARE_INFO_TARGET_SERVICE_PREFIX = ".5.currentSoftwareVersion";
//    private static final String FIRMWARE_INFO_TARGET = ".5.id";
//
//    @Override
//    public ShadowBo find(String deviceId) {
//        List<DeviceShadow> deviceShadowList = new ArrayList<>();
//
//        //计算所在分表
//        int collection = Math.abs(deviceId.hashCode()) % SHARDING_SIZE;
//        MongoCollection mongoCollection = client.getDataBase().getCollection(SHARDING_PREFIX + collection);
//
//        //解析查询条件
//        Bson bson = Filters.eq(DEVICE_ID_FIELD, deviceId);
//
//        //查询结果
//        FindIterable iterable = mongoCollection.find(bson);
//        if (iterable != null) {
//            MongoCursor cursor = iterable.iterator();
//            while (cursor.hasNext()) {
//                Document document = (Document) cursor.next();
//                if (document == null) {
//                    continue;
//                }
//                try {
//                    DeviceShadow shadow = JacksonUtils.OBJECT_MAPPER.readValue(
//                            document.toJson(JsonWriterSettings.builder()
//                                    .timestampConverter((value, writer) -> writer.writeNumber(String.valueOf(value.getValue()))).build()),
//                            DeviceShadow.class);
//                    deviceShadowList.add(shadow);
//                } catch (Exception e) {
//                    LOGGER.error("parse mongodb document error", e);
//                }
//            }
//        }
//
//        deviceShadowList.sort((o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
//        if (!CollectionUtils.isEmpty(deviceShadowList)) {
//            DeviceShadow lastDeviceShadow = deviceShadowList.get(0);
//            return buildDeviceShadowBo(lastDeviceShadow);
//        }
//
//        return null;
//    }
//
//    @Override
//    public DeviceQueryResult findDevice(ShadowQueryCondition shadowQueryCondition, int batchSize, Map<String, Iterator> cursorResult) {
//        //初始根据条件查询
//        if (CollectionUtils.isEmpty(cursorResult)) {
//            cursorResult = queryByCondition(shadowQueryCondition);
//        }
//
//        List<Device> devices = new ArrayList<>();
//        //根据游标查询
//        queryByCursor(batchSize, cursorResult, devices);
//
//        DeviceQueryResult result = new DeviceQueryResult();
//        result.setCursorResult(cursorResult);
//        result.setDevice(devices);
//
//        //如果游标为空，则表示结束查询
//        if(CollectionUtils.isEmpty(cursorResult) || CollectionUtils.isEmpty(devices)){
//            result.setEnd(true);
//        }
//        return result;
//    }
//
//    @Override
//    public long countDeviceAfterTs(ShadowQueryCondition shadowQueryCondition, long ts) {
//        long deviceCount;
//        Bson bson = Filters.and(Filters.gte(TIMESTAMP_FIELD, ts),buildCondition(shadowQueryCondition.getProductId(),
//                shadowQueryCondition.getConditionList(),null,null));
//        deviceCount = IntStream.range(0, SHARDING_SIZE).parallel()
//                .mapToObj(i -> client.getDataBase().getCollection(SHARDING_PREFIX + i))
//                .mapToLong(mongoCollection -> mongoCollection.countDocuments(bson)).sum();
//        return deviceCount;
//    }
//
//    @Override
//    public long countAllDevice() {
//        long deviceCount;
//        deviceCount = IntStream.range(0, SHARDING_SIZE).parallel()
//                .mapToObj(i -> client.getDataBase().getCollection(SHARDING_PREFIX + i))
//                .mapToLong(mongoCollection -> mongoCollection.countDocuments()).sum();
//        return deviceCount;
//    }
//
//    @Override
//    public long countDevice(ShadowQueryCondition shadowQueryCondition) {
//        Byte grayDeviceType = shadowQueryCondition.getGrayDeviceType();
//        Bson bson2 = null;
//        if(grayDeviceType != null){
//            long ts = System.currentTimeMillis() - shadowTimeout;
//            bson2 = buildConditionWithDeviceType(grayDeviceType, ts,shadowQueryCondition.getProductId(),
//                    shadowQueryCondition.getConditionList(),
//                    shadowQueryCondition.getSsoId(),
//                    shadowQueryCondition.getRegionConditionList());
//        }else{
//            bson2 = buildCondition(shadowQueryCondition.getProductId(),
//                    shadowQueryCondition.getConditionList(),
//                    shadowQueryCondition.getSsoId(),
//                    shadowQueryCondition.getRegionConditionList());
//        }
//
//        final Bson bson =bson2;
//        long deviceCount;
//        deviceCount = IntStream.range(0, SHARDING_SIZE).parallel()
//                .mapToObj(i -> client.getDataBase().getCollection(SHARDING_PREFIX + i))
//                .mapToLong(mongoCollection -> mongoCollection.countDocuments(bson)).sum();
//        return deviceCount;
//    }
//
//    @Override
//    public Map<String, Long> groupDeviceCountByProvince(ShadowQueryCondition shadowQueryCondition) {
//        Byte grayDeviceType = shadowQueryCondition.getGrayDeviceType();
//        Bson bson2 = null;
//        if(grayDeviceType != null){
//            long ts = System.currentTimeMillis() - shadowTimeout;
//            bson2 = buildConditionWithDeviceType(grayDeviceType, ts,shadowQueryCondition.getProductId(),
//                    shadowQueryCondition.getConditionList(),
//                    shadowQueryCondition.getSsoId(),
//                    shadowQueryCondition.getRegionConditionList());
//        }else{
//            bson2 = buildCondition(shadowQueryCondition.getProductId(),
//                    shadowQueryCondition.getConditionList(),
//                    shadowQueryCondition.getSsoId(),
//                    shadowQueryCondition.getRegionConditionList());
//        }
//
//        final Bson bson =bson2;
//
//        Map<String, Long> result = Maps.newHashMap();
//        List<Map<String,Long>> provinceDeviceCountList =  IntStream.range(0, SHARDING_SIZE).parallel()
//                .mapToObj(i -> client.getDataBase().getCollection(SHARDING_PREFIX + i))
//                .map(mongoCollection -> groupDeviceCount(bson, mongoCollection))
//                .collect(Collectors.toList());
//
//        //汇总数据
//        provinceDeviceCountList.stream().forEach(c->{
//            for(Map.Entry<String, Long> key : c.entrySet()){
//                String province = key.getKey();
//                Long value = key.getValue();
//                Long count = result.get(province) == null ? 0L : result.get(province);
//                result.put(province,count + value);
//            }
//        });
//
//        return result;
//    }
//
//    @Override
//    public List<String> listAllDeviceId() {
//        List<String> deviceIds = Lists.newArrayList();
//        IntStream.range(0,SHARDING_SIZE)
//                .mapToObj(i -> client.getDataBase().getCollection(SHARDING_PREFIX + i))
//                .forEach(mongoCollection -> {
//                    FindIterable<Document> docs = mongoCollection.find();
//                    if(docs != null){
//                        MongoCursor<Document> iterator = docs.iterator();
//                        while (iterator.hasNext()){
//                            Document document = iterator.next();
//                            String deviceId = (String) document.get(DEVICE_ID_FIELD);
//                            deviceIds.add(deviceId);
//                        }
//                    }
//                });
//        return deviceIds;
//    }
//
//    private Map<String, Iterator> queryByCondition(ShadowQueryCondition shadowQueryCondition) {
//        //看是否有在线设备的过滤
//        Byte grayDeviceType = shadowQueryCondition.getGrayDeviceType();
//        Bson bson2 = null;
//        if(grayDeviceType != null){
//            long ts = System.currentTimeMillis() - shadowTimeout;
//            bson2 = buildConditionWithDeviceType(grayDeviceType, ts,shadowQueryCondition.getProductId(),
//                    shadowQueryCondition.getConditionList(),
//                    shadowQueryCondition.getSsoId(),
//                    shadowQueryCondition.getRegionConditionList());
//        }else{
//            bson2 = buildCondition(shadowQueryCondition.getProductId(),
//                    shadowQueryCondition.getConditionList(),
//                    shadowQueryCondition.getSsoId(),
//                    shadowQueryCondition.getRegionConditionList());
//        }
//
//        final Bson bson =bson2;
//        Map<String, Iterator> cursorResult = new HashMap<>(100);
//
//        IntStream.range(0,SHARDING_SIZE).parallel().forEach(i->{
//            MongoCollection mongoCollection = client.getDataBase().getCollection(SHARDING_PREFIX + i);
//            FindIterable iterable = mongoCollection.find(bson);
//            if (iterable != null && iterable.iterator().hasNext()) {
//                cursorResult.put(SHARDING_PREFIX + i, iterable.iterator());
//            }
//        });
//        return cursorResult;
//    }
//
//
//
//    */
///**
//     * 根据游标查询
//     * @param batchSize
//     * @param cursorResult
//     * @param deviceIds
//     *//*
//
//    private void queryByCursor(int batchSize, Map<String, Iterator> cursorResult, List<Device> deviceIds) {
//        List<String> finishedCursor = Lists.newArrayList();
//
//        cursorResult.entrySet().forEach(entry->{
//            Iterator cursor = entry.getValue();
//            while (cursor.hasNext()) {
//                if (deviceIds.size() >= batchSize) {
//                    break;
//                }
//                Document document = (Document) cursor.next();
//                if (document == null) {
//                    continue;
//                }
//                try {
//                    DeviceInfo deviceInfo = JacksonUtils.OBJECT_MAPPER.readValue(document.toJson(), DeviceInfo.class);
//                    Device device = new Device();
//                    device.setDeviceId(deviceInfo.getDeviceId());
//                    device.setSsoId(deviceInfo.getSsoId());
//                    deviceIds.add(device);
//                } catch (Exception e) {
//                    LOGGER.error("==========parse mongodb deviceId document error===========", e);
//                }
//            }
//            if (!cursor.hasNext()) {
//                finishedCursor.add(entry.getKey());
//            }
//        });
//
//        //移除完成游标集
//        finishedCursor.stream().forEach(key -> cursorResult.remove(key));
//    }
//
//    */
///**
//     * 分组设备数量统计
//     * @param bson
//     * @param mongoCollection
//     * @return
//     *//*
//
//    private Map<String, Long> groupDeviceCount(Bson bson, MongoCollection<Document> mongoCollection) {
//        String mapFunction = "function() {emit(this.province,1);}";
//        String reduceFunction = "function(key,values) {return Array.sum(values)}";
//        MongoCursor<Document> resultCursor = mongoCollection.mapReduce(mapFunction,reduceFunction).filter(bson).iterator();
//        Map<String,Long> countMap = Maps.newHashMap();
//        while (resultCursor.hasNext()){
//            Document document = resultCursor.next();
//            String province = document.getString("_id");
//            Double value = document.getDouble("value");
//            Long count = countMap.get(province) == null ? 0L : countMap.get(province);
//            countMap.put(province,count + value.longValue());
//        }
//        return countMap;
//    }
//
//
//    */
///**
//     * 构建查询条件
//     * @param productId
//     * @param condition
//     * @param ssoId
//     * @param regionConditions
//     * @return
//     *//*
//
//    private Bson buildConditionWithDeviceType(Byte onlineType, long ts,String productId, List<FirmwareCondition> condition,
//                                               List<String> ssoId, List<RegionCondition> regionConditions) {
//        List<Bson> conditions = Lists.newArrayList();
//
//        //产品id过滤
//        Bson productIdFilter = buildProductFilter(productId);
//        if(productIdFilter != null) {
//            conditions.add(productIdFilter);
//        }
//
//        //用户过滤
//        Bson ssoIdFilter = buildUserFilter(ssoId);
//        if(ssoIdFilter != null) {
//            conditions.add(ssoIdFilter);
//        }
//
//        //区域条件过滤
//        Bson regionFilter = buildRegionFilter(regionConditions);
//        if(regionFilter != null) {
//            conditions.add(regionFilter);
//        }
//
//        //固件条件过滤
//        Bson firmwareFilter = buildFirmwareFilter(condition);
//        if(firmwareFilter != null) {
//            conditions.add(firmwareFilter);
//        }
//
//        //在线设备过滤
//        Bson onlineTypeFilter = buildOnineFilter(onlineType, ts);
//        if(onlineTypeFilter != null){
//            conditions.add(onlineTypeFilter);
//        }
//        return Filters.and(conditions);
//    }
//
//    private Bson buildOnineFilter(Byte onlineType,Long ts) {
//        Bson onineFilter = null;
//        if (onlineType != null && onlineType == 1) {
//            onineFilter = Filters.gte(TIMESTAMP_FIELD, ts);
//        }
//        return onineFilter;
//    }
//
//    private Bson buildCondition(String productId, List<FirmwareCondition> condition, List<String> ssoId, List<RegionCondition> regionConditions) {
//        List<Bson> conditions = Lists.newArrayList();
//
//        //产品id过滤
//        Bson productIdFilter = buildProductFilter(productId);
//        if(productIdFilter != null) {
//            conditions.add(productIdFilter);
//        }
//
//        //用户过滤
//        Bson ssoIdFilter = buildUserFilter(ssoId);
//        if(ssoIdFilter != null) {
//            conditions.add(ssoIdFilter);
//        }
//
//        //区域条件过滤
//        Bson regionFilter = buildRegionFilter(regionConditions);
//        if(regionFilter != null) {
//            conditions.add(regionFilter);
//        }
//
//        //固件条件过滤
//        Bson firmwareFilter = buildFirmwareFilter(condition);
//        if(firmwareFilter != null) {
//            conditions.add(firmwareFilter);
//        }
//
//        return Filters.and(conditions);
//    }
//
//    private Bson buildProductFilter(String productId) {
//        Bson productIdFilter = null;
//        if (StringUtils.isNotBlank(productId)) {
//            productIdFilter = Filters.eq(PRODUCT_ID_FIELD, productId);
//        }
//        return productIdFilter;
//    }
//
//
//    */
///**
//     * 用户id过滤
//     * @param ssoId
//     * @return
//     *//*
//
//    private Bson buildUserFilter(List<String> ssoId) {
//        Bson ssoIdFilter = null;
//        if(!CollectionUtils.isEmpty(ssoId)){
//            ssoIdFilter = Filters.in(SSO_ID_FIELD,ssoId);
//        }
//        return ssoIdFilter;
//    }
//
//    */
///**
//     * 地域过滤
//     * @param regionConditions
//     * @return
//     *//*
//
//    private Bson buildRegionFilter(List<RegionCondition> regionConditions) {
//        if(CollectionUtils.isEmpty(regionConditions)){
//            return null;
//        }
//
//        List<Bson> filters = Lists.newArrayList();
//        for(RegionCondition regionCondition : regionConditions){
//            Bson tmpFilter = Filters.and();
//            if(StringUtils.isNotBlank(regionCondition.getCountry())){
//                tmpFilter = Filters.and(tmpFilter,Filters.eq(COUNTRY_FIELD,regionCondition.getCity()));
//            }
//            if(StringUtils.isNotBlank(regionCondition.getProvince())){
//                tmpFilter = Filters.and(tmpFilter,Filters.eq(PROVINCE_FIELD,regionCondition.getProvince()));
//            }
//            if(StringUtils.isNotBlank(regionCondition.getCity())){
//                tmpFilter = Filters.and(tmpFilter,Filters.eq(PROVINCE_FIELD,regionCondition.getCity()));
//            }
//            filters.add(tmpFilter);
//        }
//
//        return Filters.or(filters);
//    }
//
//
//    */
///**
//     * 固件条件过滤
//     * @param conditions
//     * @return
//     *//*
//
//    private Bson buildFirmwareFilter(List<FirmwareCondition> conditions) {
//        if (CollectionUtils.isEmpty(conditions)) {
//            return null;
//        }
//
//        List<Bson> filterConditions = Lists.newArrayList();
//        for (FirmwareCondition condition : conditions) {
//            Integer target = Integer.parseInt(condition.getTarget());
//            //升级固件所属目标模块
//            String targetField = new StringBuilder(STATE_REPORTED_PREFIX)
//                    .append(target)
//                    .append(FIRMWARE_INFO_TARGET)
//                    .toString();
//            //目标模块版本
//            String targetVersionField = new StringBuilder(STATE_REPORTED_PREFIX)
//                    .append(target)
//                    .append(FIRMWARE_INFO_TARGET_SERVICE_PREFIX)
//                    .toString();
//            //版本条件
//            String versionCondition = condition.getCondition();
//            //版本号
//            String version = condition.getVersion();
//            if(StringUtils.isNotBlank(version)) {
//                Bson versionFilter = null;
//                if(StringUtils.isBlank(versionCondition)){
//                    versionFilter = Filters.eq(targetVersionField, version);
//                }
//                else {
//                    switch (versionCondition) {
//                        case ">=":
//                            versionFilter = Filters.gte(targetVersionField, version);
//                            break;
//                        case "<=":
//                            versionFilter = Filters.lte(targetVersionField, version);
//                            break;
//                        case "<":
//                            versionFilter = Filters.lt(targetVersionField, version);
//                            break;
//                        case ">":
//                            versionFilter = Filters.gt(targetVersionField, version);
//                            break;
//                        case "!=":
//                            versionFilter = Filters.ne(targetVersionField, version);
//                            break;
//                        //新增 in 的操作符   version多个以 , 隔开
//                        case "in":
//                            String[] versionArr = version.split(",");
//                            versionFilter = Filters.in(targetVersionField, versionArr);
//                            break;
//                        default:
//                            versionFilter = Filters.eq(targetVersionField, version);
//                    }
//                }
//                filterConditions.add(Filters.and(Filters.eq(targetField, target), versionFilter));
//            }
//            else {
//                filterConditions.add(Filters.and(Filters.eq(targetField, target)));
//            }
//        }
//
//        return Filters.or(filterConditions);
//    }
//
//    */
///**
//     * 构建影子
//     *
//     * @param lastDeviceShadow
//     * @return
//     *//*
//
//    private ShadowBo buildDeviceShadowBo(DeviceShadow lastDeviceShadow) {
//        ShadowBo shadowBo = new ShadowBo();
//        shadowBo.setSsoId(lastDeviceShadow.getSsoId());
//        shadowBo.setDeviceId(lastDeviceShadow.getDeviceId());
//        shadowBo.setTimestamp(lastDeviceShadow.getTimestamp());
//        shadowBo.setProductId(lastDeviceShadow.getProductId());
//
//        //设置属性
//        if (lastDeviceShadow.getState() != null) {
//            DeviceState state = lastDeviceShadow.getState();
//            if (state.getReported() != null) {
//                shadowBo.setProperties(state.getReported());
//            }
//        }
//
//        return shadowBo;
//    }
//}
//*/
