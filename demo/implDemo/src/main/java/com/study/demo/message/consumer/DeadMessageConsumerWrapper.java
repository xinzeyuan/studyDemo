//package com.study.demo.message.consumer;
//
//import com.oppo.iot.smarthome.server.provider.exception.ParamCheckException;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
//import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
//import org.apache.rocketmq.client.exception.MQClientException;
//import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.BeanInitializationException;
//
///**
// * @author 80249849
// * @date 2019-05-05
// */
//public class DeadMessageConsumerWrapper {
//    private static final Logger LOGGER = LoggerFactory.getLogger(DeadMessageConsumerWrapper.class);
//
//    private String consumerGroup;
//    private String namesrvAddr;
//    private DefaultMQPushConsumer consumer;
//    private MessageListenerConcurrently messageListener;
//
//    public void start() {
//        LOGGER.info("starting dead message consumer wrapper...");
//        try {
//            LOGGER.info("checking configurations.");
//            checkConfigs();
//            LOGGER.info("configurations checked.");
//            LOGGER.info("initializing consumer...");
//            initConsumer();
//            LOGGER.info("consumer initialized.");
//        } catch (Exception e) {
//            unchecked(e);
//        }
//        LOGGER.info("dead message consumer wrapper started.");
//    }
//
//    private void checkConfigs() {
//        if (StringUtils.isEmpty(consumerGroup) ||
//                StringUtils.isEmpty(namesrvAddr) ||
//                messageListener == null) {
//            throw new ParamCheckException("lost params for initializing dead message consumer!");
//        }
//    }
//
//    private void initConsumer() throws MQClientException {
//        consumer = new DefaultMQPushConsumer();
//        consumer.setConsumerGroup("GROUP_DLQ_" + consumerGroup);
//        consumer.setNamesrvAddr(namesrvAddr);
//        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
//        consumer.subscribe("%DLQ%" + consumerGroup, "*");
//        consumer.registerMessageListener(messageListener);
//        consumer.start();
//    }
//
//    public void shutdown() {
//        LOGGER.info("stopping message consumer wrapper...");
//        try {
//            LOGGER.info("stopping init consumer...");
//            consumer.shutdown();
//            LOGGER.info("consumer stopped.");
//        } catch (Exception e) {
//            unchecked(e);
//        }
//        LOGGER.info("message consumer wrapper stopped.");
//    }
//
//    private void unchecked(Exception e) {
//        LOGGER.error("failed to start or shutdown message consumer [DeadMessageConsumerWrapper]!");
//        throw new BeanInitializationException(e.getMessage(), e.getCause());
//    }
//
//    public void setConsumerGroup(String consumerGroup) {
//        this.consumerGroup = consumerGroup;
//    }
//
//    public void setNamesrvAddr(String namesrvAddr) {
//        this.namesrvAddr = namesrvAddr;
//    }
//
//    public void setConsumer(DefaultMQPushConsumer consumer) {
//        this.consumer = consumer;
//    }
//
//    public void setMessageListener(MessageListenerConcurrently messageListener) {
//        this.messageListener = messageListener;
//    }
//}
