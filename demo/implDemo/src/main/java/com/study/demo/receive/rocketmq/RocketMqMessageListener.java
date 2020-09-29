//package com.study.demo.receive.rocketmq;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
//import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
//import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
//import org.apache.rocketmq.common.message.MessageExt;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
///**
// * @author
// */
//@Slf4j
//@Component
//public class RocketMqMessageListener implements MessageListenerConcurrently {
//
//
//
//
//    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
//
//        /*
//         * 解析成 topic key value后交给处理器
//         */
//        for (MessageExt msg : msgs) {
//            try {
//                String key = msg.getKeys();
//                String topic = msg.getTopic();
//                String value = new String(msg.getBody(), "UTF-8");
//                log.info("rocketMQ receive message:{}", value);
//
//            } catch (Exception e) {
//                log.error("rocket MQ consumer error", e);
//            }
//        }
//        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
//    }
//}
