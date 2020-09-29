//package com.study.demo.message.producer;
//
//import com.oppo.iot.smarthome.server.provider.exception.ParamCheckException;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.rocketmq.client.exception.MQClientException;
//import org.apache.rocketmq.client.producer.TransactionListener;
//import org.apache.rocketmq.client.producer.TransactionMQProducer;
//import org.springframework.beans.factory.BeanInitializationException;
//
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//
///**
// * @author 80249849
// * @date 2019-05-05
// */
//@Slf4j
//public class TransactionMessageProducerWrapper {
//
//    private String producerGroup;
//    private String namesrvAddr;
//    private Integer retryTimes;
//    private Integer timeout;
//    private TransactionListener transactionListener;
//    private TransactionMQProducer producer;
//
////    public TransactionMessageProducerWrapper(String producerGroup, String namesrvAddr, Integer retryTimes, Integer timeout, TransactionListener transactionListener) {
////        this.producerGroup = producerGroup;
////        this.namesrvAddr = namesrvAddr;
////        this.retryTimes = retryTimes;
////        this.timeout = timeout;
////        this.transactionListener = transactionListener;
////    }
//
//    public TransactionMQProducer getProducer() {
//        return producer;
//    }
//
//    public void start() {
//        log.info("starting transaction message producer wrapper...");
//        try {
//            log.info("checking configurations.");
//            checkConfigs();
//            log.info("configurations checked.");
//            log.info("initializing producer...");
//            initProducer();
//            log.info("producer initialized.");
//        } catch (Exception e) {
//            unchecked(e);
//        }
//        log.info("transaction message producer wrapper started.");
//    }
//
//    public void shutdown() {
//        log.info("stopping transaction message producer...");
//        try {
//            producer.shutdown();
//        } catch (Exception e) {
//            unchecked(e);
//        }
//        log.info("transaction message producer stopped.");
//    }
//
//    private void checkConfigs() {
//        if (StringUtils.isEmpty(producerGroup) ||
//                StringUtils.isEmpty(namesrvAddr) ||
//                retryTimes == null ||
//                timeout == null ||
//                transactionListener == null) {
//            throw new ParamCheckException("lost params for initializing transaction message producer!");
//        }
//    }
//
//    private void initProducer() throws MQClientException {
//        producer = new TransactionMQProducer();
//        producer.setProducerGroup(producerGroup);
//        producer.setNamesrvAddr(namesrvAddr);
//        producer.setTransactionListener(transactionListener);
//        producer.setExecutorService(new ThreadPoolExecutor(
//                1,
//                5,
//                100L,
//                TimeUnit.SECONDS,
//                new ArrayBlockingQueue<>(2000)
//                , r -> new Thread(r, "family-tx-message-producer-thread")));
//        producer.setSendMsgTimeout(timeout);
//        producer.setRetryTimesWhenSendFailed(retryTimes);
//        producer.start();
//    }
//
//    private void unchecked(Exception e) {
//        log.error("failed to start or shutdown message producer [TransactionMessageProducerWrapper]!");
//        throw new BeanInitializationException(e.getMessage(), e.getCause());
//    }
//
//    public void setProducerGroup(String producerGroup) {
//        this.producerGroup = producerGroup;
//    }
//
//    public void setNamesrvAddr(String namesrvAddr) {
//        this.namesrvAddr = namesrvAddr;
//    }
//
//    public void setRetryTimes(Integer retryTimes) {
//        this.retryTimes = retryTimes;
//    }
//
//    public void setTimeout(Integer timeout) {
//        this.timeout = timeout;
//    }
//
//    public void setTransactionListener(TransactionListener transactionListener) {
//        this.transactionListener = transactionListener;
//    }
//
//    public void setProducer(TransactionMQProducer producer) {
//        this.producer = producer;
//    }
//}
