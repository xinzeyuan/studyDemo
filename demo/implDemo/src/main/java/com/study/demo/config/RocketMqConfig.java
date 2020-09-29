//package com.study.demo.config;
//
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
//import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
//import org.apache.rocketmq.client.exception.MQClientException;
//import org.apache.rocketmq.client.producer.DefaultMQProducer;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * @author
// */
//@Slf4j
////@Configuration
//public class RocketMqConfig {
//
//    //@Value("localhost:9876")
//    private String nameServerAddr="localhost:9876";
//
//    //@Value("producerGroupName")
//    private String producerGroupName="producerGroupName";
//    //@Value("3")
//    private Integer retryTimes=3;
//    //@Value("2000")
////    private Integer timeout=2000;
//
//
//    //@Value("consumerGroupName")
//    private String consumerGroupName="consumerGroupName";
//
//    @Autowired
//    private MessageListenerConcurrently messageListener;
//
//    private String[] topics = {"testTopic"};
//
//    //@Bean
//    public DefaultMQProducer defaultMQProducer() {
//        DefaultMQProducer producer = new DefaultMQProducer(producerGroupName);
//        producer.setNamesrvAddr(nameServerAddr);
//        producer.setRetryTimesWhenSendFailed(retryTimes);
//        producer.setSendMsgTimeout(timeout);
//        try {
//            producer.start();
//            log.info("rocketMQ producer start success");
//        } catch (MQClientException e) {
//            log.error("rocketMQ producer start error", e);
//        }
//        return producer;
//    }
//
//
//    //@Bean
//    public DefaultMQPushConsumer defaultMQPushConsumer() {
//
//        for (String topic : topics) {
//            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(producerGroupName + topic);
//            consumer.setNamesrvAddr(nameServerAddr);
//            consumer.registerMessageListener(messageListener);
//            try {
//                consumer.subscribe(topic, "*");
//                consumer.start();
//                log.info("rocketMQ topic={} consumer start success", topic);
//            } catch (MQClientException e) {
//                log.error("rocketMQ topic=" + topic + " consumer start error", e);
//            }
//        }
//        return null;
//    }
//
//}
