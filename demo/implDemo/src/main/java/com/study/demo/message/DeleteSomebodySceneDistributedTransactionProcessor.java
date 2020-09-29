//package com.study.demo.message;
//
//import com.alibaba.dubbo.config.annotation.Reference;
//import com.oppo.iot.smarthome.common.utils.JSONUtils;
//import com.oppo.iot.smarthome.scene.server.provider.spi.SceneService;
//import com.oppo.iot.smarthome.server.provider.bo.client2server.message.SceneDeleteMessage;
//import com.oppo.iot.smarthome.server.provider.message.LocalTransactionChecker;
//import com.oppo.iot.smarthome.server.provider.message.LocalTransactionRunner;
//import com.oppo.iot.smarthome.server.provider.message.TransactionRunner;
//import com.oppo.trace.springaop.WithTrace;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.common.message.Message;
//import org.apache.rocketmq.common.message.MessageExt;
//import org.springframework.stereotype.Service;
//
///**
// * <p>
// * Description: 设备取消授权（disable instance）
// * </p>
// *
// * @author
// * @version 1.0
// * @Date 2019/7/31
// */
//@Slf4j
//@Service
//@WithTrace(recordMethodArgs = true)
//public class DeleteSomebodySceneDistributedTransactionProcessor implements LocalTransactionRunner, LocalTransactionChecker, TransactionRunner {
//
//    @Reference
//    private SceneService sceneService;
//
//    @Override
//    public void runTx(MessageExt msg) throws Exception {
//        try {
//            SceneDeleteMessage message = JSONUtils.parseObject(msg.getBody(), SceneDeleteMessage.class);
//            log.debug("remote tx: {}", message);
//
//            sceneService.deleteSomebodyAllSceneTx(message.getSsoId(), message.getHomeId());
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
//    }
//
//    @Override
//    public Boolean runCheck(MessageExt msg) throws Exception {
//        // 因为本地事务外置，所以检查无意义
//        return Boolean.TRUE;
//    }
//
//    @Override
//    public void runLocal(Message msg, Object arg) throws Exception {
//        // 本地事务：远端事务 = 1：N，因此外部执行
//    }
//}
