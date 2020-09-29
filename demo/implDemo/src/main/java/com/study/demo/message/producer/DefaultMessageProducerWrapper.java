//package com.study.demo.message.producer;
//
//import com.oppo.iot.smarthome.server.provider.exception.ParamCheckException;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.rocketmq.client.exception.MQClientException;
//import org.apache.rocketmq.client.producer.DefaultMQProducer;
//import org.springframework.beans.factory.BeanInitializationException;
//
///**
// * @author 80249849
// * @date 2019-05-09
// */
//@Slf4j
//public class DefaultMessageProducerWrapper {
//
//    private String producerGroup;
//    private String namesrvAddr;
//    private Integer retryTimes;
//    private Integer timeout;
//    private DefaultMQProducer producer;
//
//    public DefaultMQProducer getProducer() {
//        return producer;
//    }
//
//    public void start() {
//        log.info("starting message producer wrapper...");
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
//        log.info("message producer wrapper started.");
//    }
//
//    public void shutdown() {
//        log.info("stopping message producer...");
//        try {
//            producer.shutdown();
//        } catch (Exception e) {
//            unchecked(e);
//        }
//        log.info("message producer stopped.");
//    }
//
//    private void checkConfigs() {
//        if (StringUtils.isEmpty(producerGroup) ||
//                StringUtils.isEmpty(namesrvAddr) ||
//                retryTimes == null ||
//                timeout == null) {
//            throw new ParamCheckException("lost params for initializing message producer!");
//        }
//    }
//
//    private void initProducer() throws MQClientException {
//        producer = new DefaultMQProducer();
//        producer.setProducerGroup(producerGroup);
//        producer.setNamesrvAddr(namesrvAddr);
//        producer.setSendMsgTimeout(timeout);
//        producer.setRetryTimesWhenSendFailed(retryTimes);
//        producer.start();
//    }
//
//    private void unchecked(Exception e) {
//        log.error("failed to start or shutdown message producer [DefaultMessageProducerWrapper]!");
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
//    public void setProducer(DefaultMQProducer producer) {
//        this.producer = producer;
//    }
//}
