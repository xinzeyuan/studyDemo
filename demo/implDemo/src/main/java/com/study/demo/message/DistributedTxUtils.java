//package com.study.demo.message;
//
//import com.oppo.iot.smarthome.common.utils.JSONUtils;
//import com.oppo.iot.smarthome.server.provider.exception.SendMessageException;
//import com.oppo.iot.smarthome.server.service.message.producer.TransactionMessageProducerWrapper;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.client.exception.MQClientException;
//import org.apache.rocketmq.client.producer.LocalTransactionState;
//import org.apache.rocketmq.client.producer.TransactionSendResult;
//import org.apache.rocketmq.common.message.Message;
//
///**
// * @author 80249849
// * @date 2019-11-26
// */
//@Slf4j
//public class DistributedTxUtils {
//
//    private DistributedTxUtils() {
//    }
//
//    /**
//     * 发送半消息，执行本地事务
//     *
//     * @param msg 消息体
//     */
//    public static void sendHalfMsgAndExecLocalTrans(TransactionMessageProducerWrapper producer, Message msg) {
//        TransactionSendResult result;
//        try {
//            result = producer.getProducer().sendMessageInTransaction(msg, "");
//        } catch (MQClientException e) {
//            throw new SendMessageException(e.getMessage(), e.getCause());
//        }
//        LocalTransactionState state = result.getLocalTransactionState();
//        if (!LocalTransactionState.COMMIT_MESSAGE.equals(state)) {
//            String errMsg = "local transaction not committed!";
//            throw new SendMessageException(String.format("%s\n%s", errMsg, JSONUtils.toJSONString(result)));
//        }
//    }
//}
