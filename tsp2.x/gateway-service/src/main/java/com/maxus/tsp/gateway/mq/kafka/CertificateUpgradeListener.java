//package com.maxus.tsp.gateway.mq.kafka;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.maxus.tsp.common.constant.KafkaMsgConstant;
//import com.maxus.tsp.common.constant.KafkaMsgConstantFota;
//import com.maxus.tsp.common.enums.ResultStatus;
//import com.maxus.tsp.common.util.ByteUtil;
//import com.maxus.tsp.common.util.DateUtil;
//import com.maxus.tsp.common.util.ThrowableUtil;
//import com.maxus.tsp.gateway.common.constant.OperationConstant;
//import com.maxus.tsp.gateway.common.model.BaseFotaCtrlItResp;
//import com.maxus.tsp.gateway.common.model.fota.CertificationUpgradeReqInfo;
//import com.maxus.tsp.gateway.common.ota.CertificationUpgradeData;
//import com.maxus.tsp.gateway.common.ota.GlobalSessionChannel;
//import com.maxus.tsp.gateway.common.ota.OTAMessage;
//import com.maxus.tsp.gateway.ota.TspServiceProc;
//import com.maxus.tsp.gateway.service.CertificationUpgradeUtil;
//import com.maxus.tsp.gateway.service.DataProcessing;
//import com.maxus.tsp.gateway.service.KafkaService;
//import com.maxus.tsp.gateway.service.TboxService;
//import com.maxus.tsp.platform.service.model.AppJsonResult;
//import io.netty.channel.Channel;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.annotation.KafkaListener;
//
//import java.util.Optional;
//
///**
// * @ClassName CertificateUpgradeListener
// * @Description 监听IT下发证书更新kafka消息
// * @Author ssh
// * @Date 2019/1/31 13:46
// * @Version 1.0
// **/
//public class CertificateUpgradeListener extends BaseListener {
//    private static final Logger logger = LogManager.getLogger(CertificateUpgradeListener.class);
//
//    @Autowired
//    private CertificationUpgradeUtil certificationUpgradeUtil;
//
//    @Autowired
//    private KafkaService kafkaService;
//
//    @Autowired
//    private TboxService tboxService;
//
//    @KafkaListener(topics = KafkaMsgConstantFota.TOPIC_IT_CERTIFICATION_UPGRADE_REQ, containerFactory = "KafkaItContainer")
//    public void listenCertificateUpgrade(ConsumerRecord<?, ?> record) {
//        try {
//            Optional<?> kafkaMessage = Optional.ofNullable(record.value());
//            if (kafkaMessage.isPresent()) {
//                Object message = kafkaMessage.get();
//                CertificationUpgradeReqInfo certificationUpgradeReqInfo = JSON.parseObject((String) message, CertificationUpgradeReqInfo.class);
//                if (certificationUpgradeReqInfo != null) {
//                    logger.info("TBox(SN:{})kafka证书更新传递参数为:{}", certificationUpgradeReqInfo.getSn(), JSONObject.toJSONString(certificationUpgradeReqInfo));
//                    asynDoCertificationUpgrade(certificationUpgradeReqInfo);
//                } else {
//                    logger.warn("当前TBox证书更新kafka传递参数为空!");
//                }
//            }
//        } catch (Exception e) {
//            logger.error("当前TBox进行证书更新因异常失败, 异常原因:{}", ThrowableUtil.getErrorInfoFromThrowable(e));
//        }
//    }
//
//
//    /**
//     * @return void
//     * @Description 处理证书更新控制，并将处理结果返回给IT
//     * @Date 2019/1/31 14:40
//     * @Param [certificationUpgradeReqInfo]
//     **/
//    private void asynDoCertificationUpgrade(CertificationUpgradeReqInfo certificationUpgradeReqInfo) {
//        TspServiceProc.getThreadPool().execute(() -> {
//            try {
//                //最终返回给IT的Json
//                AppJsonResult appJsonResult;
//                String serialNumber = certificationUpgradeReqInfo.getSn();
//                //判断处理kafka消息的时间是否超过10s
//                if (DateUtil.timeDifference(certificationUpgradeReqInfo.getEventTime())) {
//                    // 获得证书更新的结果
//                    logger.info("TBox(SN:{})开始执行证书更新!", serialNumber);
//                    CertificationUpgradeUtil certificationUpgradeUtil = this.certificationUpgradeUtil.cloneUtil();
//                    //参数校验
//                    appJsonResult = certificationUpgradeUtil.validCertificationUpgrade(certificationUpgradeReqInfo);
//                    if (appJsonResult == null || appJsonResult.getStatus() == null) {
//                        // 参数校验通过, 开始处理证书更新的业务
//                        appJsonResult = certificationUpgradeUtil.doCertificationUpgrade(certificationUpgradeReqInfo);
//                    }
//                } else {
//                    logger.warn("TBox(SN:{})进行证书更新过程中处理kafka消息时间超过10s!", serialNumber);
//                    appJsonResult = new AppJsonResult(ResultStatus.OP_UNDO_FOR_REQUEST_TIME_EXPIRED,"");
//                }
//                BaseFotaCtrlItResp certificationUpgradeItResp = new BaseFotaCtrlItResp();
//                certificationUpgradeItResp.setSn(certificationUpgradeReqInfo.getSn());
//                certificationUpgradeItResp.setStatus(appJsonResult.getStatus());
//                certificationUpgradeItResp.setDescription(appJsonResult.getDescription());
//                certificationUpgradeItResp.setData(appJsonResult.getData());
//                certificationUpgradeItResp.setSeqNo(certificationUpgradeReqInfo.getSeqNo());
//                certificationUpgradeItResp.setEventTime(certificationUpgradeReqInfo.getEventTime());
//                logger.info("TBox(SN:{})收到回复, 本次证书更新操作回复结果为:{}", serialNumber, JSONObject.toJSONString(certificationUpgradeItResp));
//                kafkaService.transferItData(KafkaMsgConstantFota.TOPIC_IT_CERTIFICATION_UPGRADE_RESP, certificationUpgradeItResp, serialNumber);
//            } catch (Exception e) {
//                logger.error("TBox(SN:{})当前证书更新请求因发生异常失败, 异常原因:{}", certificationUpgradeReqInfo.getSn(), ThrowableUtil.getErrorInfoFromThrowable(e));
//            }
//        });
//    }
//
//    /**
//     * @return void
//     * @Description 消费网关kafka证书更新下行消息，解析消息后组装报文下发给TBox
//     * @Date 2019/1/31 15:41
//     * @Param [record]
//     **/
//    @KafkaListener(topics = KafkaMsgConstantFota.TOPIC_SELF_CERTIFICATION_UPGRADE_DOWN, containerFactory = "KafkaContainer")
//    public void listenCertificationUpgradeDown(ConsumerRecord<?, ?> record) {
//        try {
//            Optional<?> kafkaMessage = Optional.ofNullable(record.value());
//            if (kafkaMessage.isPresent()) {
//                Object message = kafkaMessage.get();
//                String[] cmd = ((String) JSON.parse(message.toString())).split("_");
//                //cmd[0]:serialNumber
//                //cmd[1]:value
//                //cmd[2]:dateTime
//                //检查字段个数
//                if (cmd.length == 3) {
//                    String serialNumber = cmd[KafkaMsgConstant.TOPIC_TBOX_SN_OFFSET];
//                    //检查消息是否过期90s
//                    if (getExpiredTimeByCurTimeAndOffset(OperationConstant.REMOTE_CONFIG_REQ_EXPIRED_TIME)
//                            .compareTo(cmd[2]) > 0) {
//                        logger.warn("TBox(SN:{})证书更新下行消息发生时间为:{}。超过90s, 已经过期, 该消息丢弃!", serialNumber, cmd[2]);
//                        return;
//                    }
//                    //通过在线socket频道检测TBox是否在本节点上线
//                    Channel channel = GlobalSessionChannel.getChannel(serialNumber);
//                    if (channel == null || !DataProcessing.tboxCurrentChannelExist(serialNumber, channel.remoteAddress().toString())) {
//                        logger.warn("TBox(SN:{})没有在本节点上线, 不用处理证书更新请求!", serialNumber);
//                        return;
//                    }
//                    //检测到TBox在本节点上线, 开始组装报文
//                    OTAMessage sendOTAMsg = CertificationUpgradeData.getSendDataForCertificationUpgrade(cmd);
//                    if (sendOTAMsg != null) {
//                        if (sendOTAMsg.CurMessage != null && sendOTAMsg.CurMessage.length > 0) {
//                            String byteToHex = ByteUtil.byteToHex(sendOTAMsg.CurMessage);
//                            logger.info("TBox(SN:{})开始下发证书更新报文:{}", serialNumber, byteToHex);
//                            //写入报文
//                            channel.writeAndFlush(byteToHex);
//                        } else {
//                            logger.warn("TBox(SN:{})证书更新指令下发失败:{}", serialNumber, message);
//                        }
//                    } else {
//                        logger.warn("TBox(SN:{})组装报文出错，当前报文为空!", serialNumber);
//                    }
//                } else {
//                    logger.warn("TBox当前证书更新消息格式不符合规则, 不执行证书更新指令:{}", kafkaMessage.get().toString());
//                }
//            }
//        } catch (Exception e) {
//            logger.error("监听TBox证书更新上行指令时, kafka发生异常:{}", ThrowableUtil.getErrorInfoFromThrowable(e));
//        }
//    }
//
//    /**
//     * @return void
//     * @Description 监听网关kafka证书更新上行消息
//     * @Date 2019/1/31 16:14
//     * @Param [record]
//     **/
//    @KafkaListener(topics = KafkaMsgConstantFota.TOPIC_SELF_CERTIFICATION_UPGRADE_UP, containerFactory = "KafkaContainer")
//    public void listenCertificationUpgradeUp(ConsumerRecord<?, ?> record) {
//        try {
//            Optional<?> kafkaMessage = Optional.ofNullable(record.value());
//            if (kafkaMessage.isPresent()) {
//                Object message = kafkaMessage.get();
//                String[] cmd = ((String) JSON.parse(message.toString())).split("_");
//                //cmd[0]:serialNumber
//                //cmd[1]:result
//                //cmd[2]:dateTime
//                if (cmd.length == 3) {
//                    String serialNumber = cmd[KafkaMsgConstant.TOPIC_TBOX_SN_OFFSET];
//                    if (getExpiredTimeByCurTimeAndOffset(OperationConstant.REMOTECONTROL_RESP_EXPIRED_TIME).compareTo(cmd[2]) > 0) {
//                        logger.warn("TBox(SN:{})证书更新消息回复发生时间为:{}。已经过期, 该条消息丢弃!", serialNumber, cmd[2]);
//                        return;
//                    }
//                    CertificationUpgradeUtil certificationUpgradeUtil = CertificationUpgradeUtil.getCertificationUpgrade(serialNumber);
//                    //从能获取到数据的节点发送kafka
//                    if (certificationUpgradeUtil != null) {
//                        certificationUpgradeUtil.setResult(cmd[1]);
//                        try {
//                            synchronized (certificationUpgradeUtil) {
//                                certificationUpgradeUtil.notifyAll();
//                            }
//                        } catch (Exception e) {
//                            logger.error("TBox(SN:{})证书更新返回结果发生异常, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
//                        } finally {
//                            if (GlobalSessionChannel.existFotaCtrl(serialNumber, OperationConstant.FOTA_CERTIFICATION_UPGRADE)) {
//                                logger.info("TBox(SN:{})开始删除证书更新在redis中的控制指令!", serialNumber);
//                                GlobalSessionChannel.removeFotaCtrl(serialNumber, OperationConstant.FOTA_CERTIFICATION_UPGRADE);
//                            }
//                        }
//                    } else {
//                        logger.warn("TBox(SN:{})在该节点没有接收证书更新请求, 返回结果由其他节点处理了!", serialNumber);
//                    }
//                } else {
//                    logger.warn("消息格式不符合, 参数个数不足3个, 不执行证书更新操作结果返回:{}", kafkaMessage.get().toString());
//                }
//            }
//        } catch (Exception e) {
//            logger.error("监听TBox证书更新上行kafka发生异常, 异常原因:{}", ThrowableUtil.getErrorInfoFromThrowable(e));
//        }
//    }
//}
