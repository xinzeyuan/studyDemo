//package com.study.demo.message.consumer;
//
//import com.oppo.iot.smarthome.server.provider.message.CommonRunner;
//import com.oppo.iot.smarthome.server.provider.message.TransactionRunner;
//import com.oppo.trace.springaop.WithTrace;
//import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
//import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
//import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
//import org.apache.rocketmq.common.message.MessageExt;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.BeanFactoryUtils;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//
//import java.util.List;
//import java.util.Map;
//
///**
// * @author 80249849
// * @date 2019-05-05
// */
//@WithTrace(recordMethodArgs = true)
//public class MessageDispatcher implements MessageListenerConcurrently, ApplicationContextAware {
//    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDispatcher.class);
//
//    private Map<String, TransactionRunner> txRunnerMap;
//    private Map<String, CommonRunner> runnerMap;
//
//    @Override
//    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
//                                                    ConsumeConcurrentlyContext context) {
//
//        // 根据 msg tag 派发消费者
//        for (MessageExt msg : msgs) {
//            String tags = msg.getTags();
//            TransactionRunner txRunner = getTxRunner(tags);
//            if (txRunner != null) {// 事务消息
//                LOGGER.debug("message received from queue [{}], messageKey={}, " +
//                                "try to dispatch it to {}.",
//                        context.getMessageQueue().getQueueId(),
//                        msg.getKeys(),
//                        txRunner.getClass().getName());
//                try {
//                    txRunner.runTx(msg);
//                    LOGGER.debug("tx message consume from queue success,messageKey:{}",msg.getKeys());
//                } catch (Exception e) {
//                    LOGGER.error("MessageDispatcher runTx error", e);
//                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
//                }
//            } else {// 普通消息
//                try {
//                    CommonRunner runner = getRunner(tags);
//                    LOGGER.debug("message received from queue [{}], messageKey={}, " +
//                                    "try to dispatch it to {}.",
//                            context.getMessageQueue().getQueueId(),
//                            msg.getKeys(),
//                            runner.getClass().getName());
//                    runner.run(msg);
//                    LOGGER.debug("message consume from queue success,messageKey:{}",msg.getKeys());
//                } catch (Exception e) {
//                    LOGGER.error("MessageDispatcher run error", e);
//                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
//                }
//            }
//        }
//        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
//    }
//
//    private CommonRunner getRunner(String tags) {
//        return runnerMap.get(tags);
//    }
//
//    private TransactionRunner getTxRunner(String tags) {
//        return txRunnerMap.get(tags);
//    }
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        txRunnerMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, TransactionRunner.class);
//        runnerMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, CommonRunner.class);
//    }
//}
