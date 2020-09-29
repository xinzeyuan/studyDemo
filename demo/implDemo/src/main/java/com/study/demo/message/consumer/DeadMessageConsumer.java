//package com.study.demo.message.consumer;
//
//import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
//import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
//import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
//import org.apache.rocketmq.common.message.MessageExt;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.List;
//
///**
// * @author 80249849
// * @date 2019-05-05
// */
//public class DeadMessageConsumer implements MessageListenerConcurrently {
//    private static final Logger LOGGER = LoggerFactory.getLogger(DeadMessageConsumer.class);
//
//    @Override
//    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
//                                                    ConsumeConcurrentlyContext context) {
//
//        for (MessageExt msg : msgs) {
//            // 日志告警
//            LOGGER.error("message from dead letter queue ==> bornHostString={}, msgId={}, body=【{}】",
//                    msg.getBornHostString(),
//                    msg.getMsgId(),
//                    new String(msg.getBody())
//            );
//        }
//        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
//    }
//}
