//package com.study.demo.message.producer;
//
//import com.oppo.iot.smarthome.common.utils.JSONUtils;
//import com.oppo.iot.smarthome.server.provider.exception.TransactionProcessorNotFoundException;
//import com.oppo.iot.smarthome.server.provider.message.LocalTransactionChecker;
//import com.oppo.iot.smarthome.server.provider.message.LocalTransactionRunner;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.client.producer.LocalTransactionState;
//import org.apache.rocketmq.client.producer.TransactionListener;
//import org.apache.rocketmq.common.message.Message;
//import org.apache.rocketmq.common.message.MessageExt;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.BeanFactoryUtils;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//
//import java.util.Map;
//
///**
// * @author 80249849
// * @date 2019-05-05
// */
//@Slf4j
//public class TransactionListenerImpl implements TransactionListener, ApplicationContextAware {
//
//    private Map<String, LocalTransactionRunner> runnerMap;
//    private Map<String, LocalTransactionChecker> checkerMap;
//
//    @Override
//    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
//        // 根据 msg tag 进行事务派发
//        try {
//            log.debug("run local transaction in message, messageKey={}", msg.getKeys());
//            getRunner(msg.getTags()).runLocal(msg, arg);
//        } catch (Exception e) {
//            log.error("TransactionListenerImpl runLocal error", e);
//            log.warn("local transaction rollback, messageKey={}", msg.getKeys());
//            return LocalTransactionState.ROLLBACK_MESSAGE;
//        }
//        log.debug("local transaction committed, messageKey={}", msg.getKeys());
//        return LocalTransactionState.COMMIT_MESSAGE;
//    }
//
//    @Override
//    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
//        // 根据 msg tag 进行回查逻辑派发
//        Boolean success;
//        try {
//            log.debug("check result of local transaction, messageKey={}", msg.getKeys());
//            success = getChecker(msg.getTags()).runCheck(msg);
//        } catch (Exception e) {
//            log.error("TransactionListenerImpl runCheck error", e);
//            success = Boolean.FALSE;
//        }
//        if (success) {
//            log.debug("transaction already done, messageKey={}.", msg.getKeys());
//            return LocalTransactionState.COMMIT_MESSAGE;
//        } else {
//            log.warn("transaction not done yet, but MQ checked it, messageKey={} ,please be aware!",
//                    msg.getKeys());
//            return LocalTransactionState.ROLLBACK_MESSAGE;
//        }
//    }
//
//    private LocalTransactionRunner getRunner(String tags) {
//        LocalTransactionRunner runner = runnerMap.get(tags);
//        if (runner == null) {
//            throw new TransactionProcessorNotFoundException("no processor found for message tag [" + tags + "]");
//        }
//        return runner;
//    }
//
//    private LocalTransactionChecker getChecker(String tags) {
//        LocalTransactionChecker checker = checkerMap.get(tags);
//        if (checker == null) {
//            throw new TransactionProcessorNotFoundException("no processor found for message tag [" + tags + "]");
//        }
//        return checker;
//    }
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        runnerMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, LocalTransactionRunner.class);
//        checkerMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, LocalTransactionChecker.class);
//    }
//}
