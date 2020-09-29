//package com.study.demo.config;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.google.common.collect.Lists;
//import com.mongodb.client.MongoCollection;
//import com.mongodb.client.MongoCursor;
//import com.mongodb.client.MongoIterable;
//import com.mongodb.client.model.Filters;
//import com.mongodb.client.model.IndexModel;
//import com.mongodb.client.model.IndexOptions;
//import com.mongodb.client.result.DeleteResult;
//import com.mongodb.client.result.UpdateResult;
//import com.oppo.iot.shadow.bo.ShadowUpdateResult;
//import com.oppo.iot.shadow.dao.ShadowUpdateDao;
//import com.oppo.iot.shadow.dao.entity.DeviceShadow;
//import com.oppo.iot.smarthome.common.utils.JacksonUtils;
//import com.oppo.trace.springaop.WithTrace;
//import org.bson.Document;
//import org.bson.conversions.Bson;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Repository;
//
//import javax.annotation.PostConstruct;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicLong;
//import java.util.stream.IntStream;
//
///**
// * @author:
// * @Date: 2019/7/29
// */
//@Repository
//@WithTrace(recordMethodArgs = true)
//public class MongoDBUpdateDaoImpl implements ShadowUpdateDao {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBUpdateDaoImpl.class);
//    private final int SHARDING_SIZE = 100;
//    private static final String SHARDING_PREFIX = "shadow_deviceId_prefix_";
//    private static final String DEVICE_ID_FIELD = "deviceId";
//    private static final String PRODUCT_ID_FIELD = "productId";
//    private static final String SSO_ID_FIELD = "ssoId";
//    private static final String FIRMWARE_INFO = "0.5";
//
//    @Autowired
//    private MongoDBClient client;
//
//    @PostConstruct
//    public void init(){
//        MongoIterable<String> collectionNameTterable = client.getDataBase().listCollectionNames();
//
//        //检查创建索引
//        IntStream.range(0,SHARDING_SIZE).parallel().forEach(i->{
//            if(collectionNameTterable != null){
//                String collectionName = SHARDING_PREFIX + i;
//
//                MongoCursor<String> cursor = collectionNameTterable.iterator();
//                if(cursor != null){
//                    while (cursor.hasNext()){
//                        //集合已创建，则直接返回
//                        if(cursor.next().equals(collectionName)){
//                           return;
//                        }
//                    }
//                    //创建索引
//                    MongoCollection mongoCollection = client.getDataBase().getCollection(collectionName);
//                    createIndex(mongoCollection);
//                }
//            }
//        });
//    }
//
//    @Override
//    public ShadowUpdateResult update(String deviceId, Map<String, Object> updateData) {
//        //计算所在集合
//        MongoCollection mongoCollection = getCollection(deviceId);
//
//        //查找影子文档
//        Bson bson = Filters.eq(DEVICE_ID_FIELD, deviceId);
//
//        //构建更新数据文档
//        Document updateDocument = buildUpdateDocument(updateData);
//
//        UpdateResult result = mongoCollection.updateMany(bson, updateDocument);
//
//        ShadowUpdateResult shadowUpdateResult = new ShadowUpdateResult();
//        shadowUpdateResult.setMatchCount(result.getMatchedCount());
//        shadowUpdateResult.setModifiedCount(result.getModifiedCount());
//
//        return shadowUpdateResult;
//    }
//
//    @Override
//    public void insert(DeviceShadow shadow) {
//        //计算所在集合
//        MongoCollection mongoCollection = getCollection(shadow.getDeviceId());
//        String shadowStr = null;
//        try {
//            shadowStr = JacksonUtils.OBJECT_MAPPER.writeValueAsString(shadow);
//        } catch (JsonProcessingException e) {
//            LOGGER.error("parse shadow error",e);
//        }
//        Document document = Document.parse(shadowStr);
//        mongoCollection.insertOne(document);
//    }
//
//    @Override
//    public boolean delete(String deviceId) {
//        //计算所在分表
//        MongoCollection mongoCollection = getCollection(deviceId);
//
//        //查找影子文档
//        Bson bson = Filters.eq(DEVICE_ID_FIELD, deviceId);
//
//        DeleteResult result = mongoCollection.deleteMany(bson);
//
//        return result.wasAcknowledged();
//    }
//
//    @Override
//    public ShadowUpdateResult updateBySsoId(String ssoId, Map<String,Object> updateData) throws Exception {
//        AtomicLong matchedCount = new AtomicLong(0);
//        AtomicLong modifiedCount = new AtomicLong(0);
//
//        IntStream.range(0,SHARDING_SIZE).parallel().forEach(i->{
//            String collectionName = SHARDING_PREFIX + i;
//            MongoCollection mongoCollection = client.getDataBase().getCollection(collectionName);
//
//            //查找影子文档
//            Bson userCondition = Filters.eq(SSO_ID_FIELD, ssoId);
//            Document updateDocument = buildUpdateDocument(updateData);
//
//            //更新
//            UpdateResult updateResult =  mongoCollection.updateMany(userCondition,updateDocument);
//            matchedCount.addAndGet(updateResult.getMatchedCount());
//            modifiedCount.addAndGet(updateResult.getModifiedCount());
//        });
//
//        ShadowUpdateResult shadowUpdateResult = new ShadowUpdateResult();
//        shadowUpdateResult.setMatchCount(matchedCount.get());
//        shadowUpdateResult.setModifiedCount(modifiedCount.get());
//
//        return shadowUpdateResult;
//    }
//
//    /**
//     * 计算文档所在集合
//     *
//     * @param deviceId
//     * @return
//     */
//    private MongoCollection getCollection(String deviceId) {
//        int shard = Math.abs(deviceId.hashCode()) % SHARDING_SIZE;
//        String collectionName = SHARDING_PREFIX + shard;
//        MongoCollection mongoCollection = client.getDataBase().getCollection(collectionName);
//        return mongoCollection;
//    }
//
//    private void createIndex(MongoCollection mongoCollection) {
//        List<IndexModel> indexModelList = Lists.newArrayList();
//
//        //设备id索引
//        IndexModel idIndexModel = getIndexModel(DEVICE_ID_FIELD,true);
//        indexModelList.add(idIndexModel);
//
//        //产品id索引
//        IndexModel productIndexModel = getIndexModel(PRODUCT_ID_FIELD,false);
//        indexModelList.add(productIndexModel);
//
//        //用户id索引
//        IndexModel ssoIdIndexModel = getIndexModel(SSO_ID_FIELD,false);
//        indexModelList.add(ssoIdIndexModel);
//
//        //固件索引
//        IndexModel targetIndexModel = getIndexModel(FIRMWARE_INFO,false);
//        indexModelList.add(targetIndexModel);
//
//        //索引创建
//        mongoCollection.createIndexes(indexModelList);
//    }
//
//    private IndexModel getIndexModel(String field,boolean unique) {
//        IndexOptions idIndexOptions = new IndexOptions()
//                .background(true)
//                .unique(unique);
//        Document indexKey = new Document();
//        indexKey.put(field,1);
//        return new IndexModel(indexKey,idIndexOptions);
//    }
//
//    /**
//     * 构建更新数据
//     *
//     * @param updateData
//     * @return
//     */
//    private Document buildUpdateDocument(Map<String, Object> updateData) {
//        Document data = new Document();
//        if (updateData != null) {
//            for (Map.Entry<String, Object> entry : updateData.entrySet()) {
//                data.append(entry.getKey(), entry.getValue());
//            }
//        }
//
//        Document document = new Document("$set", data);
//        return document;
//    }
//}
