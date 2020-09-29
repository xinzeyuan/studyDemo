package com.study.demo.impl;

//import com.study.dao.mapper.ConsumerMapper;
//import com.study.dao.model.Consumer;
//import org.apache.rocketmq.client.producer.DefaultMQProducer;
//import org.apache.rocketmq.client.producer.SendCallback;
//import org.apache.rocketmq.client.producer.SendResult;
//import org.apache.rocketmq.common.message.Message;
import org.junit.Test;
//import org.redisson.api.RBucket;
//import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class testDao extends TestApplication{

  /*  @Resource
    public ConsumerMapper consumerMapper;

    @Test
    public void testMysql(){
        Consumer axin = Consumer.builder().name("axin").age(30).ssoid(11).build();
        int i = consumerMapper.insertSelective(axin);
        System.out.println("********** "+ i);
    }
*/
  /*  @Autowired
    DefaultMQProducer mqProducer;
    @Test
    public void testRocketMQ() throws Exception {
        String data = "{\n" +
                "  \"deviceId\": \"2JjPdxE\",\n" +
                "  \"properties\": {\n" +
                "    \"power\": \"ON\",\n" +
                "    \"light\": 100\n" +
                "  },\n" +
                "  \"status\": \"1\",\n" +
                "  \"msgId\": \"1244f98218654542bc62736be277592a\",\n" +
                "  \"ssoId\": \"100000\"\n" +
                "}";

        Message msg = new Message();
        msg.setTopic("DEVICE_STATUS_TOPIC");
        msg.setBody(data.getBytes("UTF-8"));
        System.err.println("rocketMQ send start...");
        mqProducer.send(msg, new SendCallback() {

            public void onSuccess(SendResult sendResult) {
                System.out.println("rocketMQ send success...");
            }


            public void onException(Throwable e) {
                System.out.println("rocketMQ send fail...");
                e.printStackTrace();
            }
        });
    }*/

   /* @Autowired
    RedissonClient redissonClient;
    @Test
    public void testRediss(){
*//*
        RBucket<Long> bucket = redissonClient.getBucket("test:rediss");
//        bucket.set(new Date().getTime());
        System.out.println("bucket = " + bucket.get());
*//*

        RBucket<String> bucket = redissonClient.getBucket("test");
      bucket.set("123");
      *//*    boolean isUpdated = bucket.compareAndSet("123", "4934");
        System.out.println("isUpdated = " + isUpdated);*//*
     *//*   String prevObject = bucket.getAndSet("321");
        System.out.println("prevObject = " + prevObject);*//*

     *//*  boolean isSet = bucket.trySet("901");
        long objectSize = bucket.size();
        System.out.println("objectSize = " + objectSize);*//*


        // set with expiration
         bucket.set("value", 10, TimeUnit.SECONDS);
        boolean isNewSet = bucket.trySet("nextValue", 10, TimeUnit.SECONDS);
        System.out.println("isNewSet = " + isNewSet);

    }*/

    @Autowired
    KafkaTemplate kafkaTemplate;
    @Test
    public void testKafka() {
        String data = "{\n" +
                "  \"deviceId\": \"2JjPdxE\",\n" +
                "  \"properties\": {\n" +
                "    \"power\": \"ON\",\n" +
                "    \"light\": 100\n" +
                "  },\n" +
                "  \"status\": \"1\",\n" +
                "  \"msgId\": \"1244f98218654542bc62736be277592a\",\n" +
                "  \"ssoId\": \"100000\"\n" +
                "}";

        kafkaTemplate.send("test", data).addCallback(new ListenableFutureCallback() {
            //@Override
            public void onFailure(Throwable throwable) {
                System.out.println("kafka send fail...");
            }

            //@Override
            public void onSuccess(Object o) {
                System.out.println("kafka send success...");
            }
        });
    }

}
