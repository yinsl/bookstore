package com.maxus.tsp.gateway.mq.kafka;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.KafkaMsgConstant;
import com.maxus.tsp.common.enums.ResultStatus;
import com.maxus.tsp.common.util.DateUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OTARemoteCommand;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.model.*;
import com.maxus.tsp.gateway.controller.*;
import com.maxus.tsp.gateway.ota.TspServiceProc;
import com.maxus.tsp.gateway.service.*;
import com.maxus.tsp.platform.service.model.AppJsonResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.Optional;

/**
 * 需要传递it的kafka信息外部侦听 用于侦听it的请求
 *
 * @author uwczo
 */
// @Scope(value = "prototype")
public class TransferCtrlOutterListener extends BaseListener {
    // 远程控制调用类
    @Autowired
    private RemoteCtrlUtil tboxRmtCtrl;


    //	// 调用远程配置类
//	// @Autowired
//	private TboxRemoteConfigCtrl tboxRmtConfigCtrl;
    @Autowired
    private KafkaService kafkaService;

//	@Autowired
//	private GatewayClient gatewayClient;

//	@Autowired
//	private TboxRemoteConfigCtrl tboxRemoteConfigCtrl;

    @Autowired
    private RemoteConfigUtil remoteConfigUtil;

//	@Autowired
//	private TBoxDirectReport directReportRVM;

    @Autowired
    private DirectReportUtil directReportUtil;

    //	@Autowired
//	private EntertainmentPOICtrl entertainmentPOICtrl;
    @Autowired
    private EntertainmentPOIUtil entertainmentPOIUtil;

//	@Autowired
//	private TboxGetVehicleStatusCtrl tboxGetVehicleStatus;

    @Autowired
    private GetVehicleStatusUtil getVehicleStatusUtil;


    @Autowired
    private TBoxRemoteCtrl tBoxRemoteCtrl;

//	@Autowired
//	private TBoxUpdateCtrl tBoxUpdateCtrl;

    @Autowired
    private TBoxUpdateUtil tBoxUpdateUtil;


    // 日志
    private final Logger logger = LogManager.getLogger(getClass());


    /**
     * it kafka 获取远程控制请求，执行本地测试，并获得结果，调整封装，返回it
     *
     * @param record
     */
    @KafkaListener(topics = KafkaMsgConstant.TOPIC_IT_REMOTECTRL_REQUEST, containerFactory = "KafkaItContainer")
    public void listenRmtCtrlRequest(ConsumerRecord<?, ?> record) {
        logger.debug("kafka日志测试-远程控制");
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            // 获取远程控制请求内容
            try {
                RemoteControlItRequest rmtRequest = JSONObject.parseObject((String) message,
                        RemoteControlItRequest.class);
                if (rmtRequest != null) {
                    logger.info("Tbox(sn:　{}):该远控消息生产时间：{} ", rmtRequest.getSn(), record.timestamp());
                    asynDoRemoteCtrl(rmtRequest);
                } else {
                    logger.warn("rvm当前远程控制请求kafka数据为空");
                }
            } catch (Exception ex) {
                logger.error("当前处理远程控制请求发生异常, 原因：{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
            }
        }
    }

    // 异步处理远程控制，避免操作时间过久让kafka请求等待
    public void asynDoRemoteCtrl(RemoteControlItRequest rmtRequest) {
        TspServiceProc.getThreadPool().submit(() -> {
            try {
                // 打印远程控制请求详情
                logger.info("rvm远程控制请求信息如下：{}", JSONObject.toJSONString(rmtRequest));
                // 需要判断请求参数中的时间是否已经过期，过期则不执行以下操作
                AppJsonResult appJsonResult = new AppJsonResult();
                if (DateUtil.timeDifference(rmtRequest.getEventTime())) {
                    if (rmtRequest.getSn().startsWith(" s")) {
                        // 模拟测试时使用的tbox
                        logger.info("rvm的TBox({})远程控制请求将模拟执行: {}", rmtRequest.getSn(),
                                rmtRequest.getComd());
                        // 调整远程控制返回
                        RemoteCtrlItResponse remoteCtrlItResponse = new RemoteCtrlItResponse();
                        remoteCtrlItResponse.setSn(rmtRequest.getSn());
                        remoteCtrlItResponse.setStatus(ResultStatus.SUCCESS.getCode());
                        // data为错误码
                        remoteCtrlItResponse.setData(null);
                        remoteCtrlItResponse.setDescription(ResultStatus.SUCCESS.getDescription());
                        remoteCtrlItResponse.setSeqNo(rmtRequest.getSeqNo());
                        remoteCtrlItResponse.setEventTime(rmtRequest.getEventTime());
                        kafkaService.transferRmtResponse(remoteCtrlItResponse);
                    } else {
                        // 判断是否是远程配置请求
                        // CmdRemoteStallConfig 熄火上传位置时间配置
                        // CmdRemoteStallSetting 熄火上传位置设置
                        // CmdRemoteBigDataConfig 大数据上传时间配置
                        // CmdRemoteBigDataSetting 大数据上传设置
                        // GetConfig 获取配置信息
                        String comd = rmtRequest.getComd();
                        if (("CmdRemoteStallConfig").equals(comd) || ("CmdRemoteBigDataConfig").equals(comd)
                                || ("CmdRemoteStallSetting").equals(comd)
                                || ("CmdRemoteBigDataSetting").equals(comd) || ("GetConfig").equals(comd)
                                || ("CmdRemoteEngDataConfig").equals(comd)
                                || ("CmdRemoteEngDataSetting").equals(comd)) {
                            logger.info("rvm的TBox({})远程控制请求(远程配置)开始执行", rmtRequest.getSn());
//								appJsonResult = gatewayClient.remoteConfigRVM(comd, rmtRequest.getValue(),
//										rmtRequest.getSn(), rmtRequest.getEventTime(), rmtRequest.getSeqNo(), null);
//								appJsonResult = tboxRemoteConfigCtrl.remoteConfigRVM(comd, rmtRequest.getValue(),
//										rmtRequest.getSn(), rmtRequest.getEventTime(), rmtRequest.getSeqNo(), null);
                            RemoteConfigUtil remoteConfigUtil = this.remoteConfigUtil.cloneUtil();
                            appJsonResult = remoteConfigUtil.doRemoteConfig(comd, rmtRequest.getValue(), rmtRequest.getSn(), rmtRequest.getEventTime(), rmtRequest.getSeqNo(), null);
                        } else if (("DirectReport").equals(comd)) {
                            // 国家平台直连操作请求
//								appJsonResult = gatewayClient.directReportRVM(rmtRequest.getSn(), rmtRequest.getValue(),
//										rmtRequest.getEventTime());
//							appJsonResult = directReportRVM.directReportRVM(rmtRequest.getSn(), rmtRequest.getValue(),
//									rmtRequest.getEventTime());
                            DirectReportUtil directReportUtil = this.directReportUtil.cloneUtil();
                            appJsonResult = directReportUtil.validDirectReport(rmtRequest.getSn(), rmtRequest.getValue(), rmtRequest.getEventTime());
                        } else if (("SendPOI").equals(comd)) {
                            String[] poiParam = rmtRequest.getValue().split(",");
                            //远程SendPOI value值：坐标系(0:大地坐标；1:火星坐标；2:百度坐标),经度,纬度,地址
                            if (poiParam.length != 4) {
                                appJsonResult = new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
                            } else {
                                // 检查posType
                                int gpsType = Integer.parseInt(poiParam[0]);
                                int posType = 0;
                                // 第0位：0表示北纬；1表示南纬
                                // 第1位：0表示东经；1表示西经
                                // 经度(正：东经，负：西经)
                                int longitude = Integer.parseInt(poiParam[1]);
                                // 纬度(正：北纬，负：南纬)
                                int latitude = Integer.parseInt(poiParam[2]);
                                if (longitude > 0 && latitude > 0) {
                                    posType = 0;
                                } else if (longitude > 0 && latitude < 0) {
                                    posType = 1;
                                } else if (longitude < 0 && latitude < 0) {
                                    posType = 3;
                                } else if (longitude < 0 && latitude > 0) {
                                    posType = 2;
                                }

//									appJsonResult = gatewayClient.poiConfigRVM(rmtRequest.getSn(),gpsType,posType,
//											longitude,latitude, poiParam[3]);
//								appJsonResult = entertainmentPOICtrl.poiConfigRVM(rmtRequest.getSn(), gpsType, posType,
//										longitude, latitude, poiParam[3]);
                                EntertainmentPOIUtil entertainmentPOIUtil = this.entertainmentPOIUtil.cloneUtil();
                                appJsonResult = entertainmentPOIUtil.validEntertainmentPOI(rmtRequest.getSn(), gpsType, posType, longitude, latitude, poiParam[3]);
                            }
                        } else if (("GetVehicleStatus").equals(comd)) {
                            logger.info("rvm的TBox({})远程控制获取车况请求开始执行", rmtRequest.getSn());
//								appJsonResult = gatewayClient.tboxGetVehicleStatus(rmtRequest.getSn(), rmtRequest.getValue(),
//										rmtRequest.getEventTime());
//							appJsonResult = tboxGetVehicleStatus.tboxGetVehicleStatus(rmtRequest.getSn(), rmtRequest.getValue(),
//									rmtRequest.getEventTime());
                            GetVehicleStatusUtil getVehicleStatusUtil = this.getVehicleStatusUtil.cloneUtil();
                            appJsonResult = getVehicleStatusUtil.validGetVehicleStatusCtrl(rmtRequest.getSn(), rmtRequest.getValue(), rmtRequest.getEventTime());
                        } else {
                            // 获得远程控制结果
                            logger.info("rvm的TBox({})远程控制请求开始执行", rmtRequest.getSn());
                            RemoteCtrlUtil rmtCtrl = tboxRmtCtrl.cloneUtil();
                            appJsonResult = rmtCtrl.validRemoteCtrl(rmtRequest.getSn(), rmtRequest.getComd(), rmtRequest.getValue(), null);
                            if (appJsonResult == null) {
                                appJsonResult = rmtCtrl.doRemoteCtrl(rmtRequest.getValue(), rmtRequest.getSn(), OTARemoteCommand.valueOf(rmtRequest.getComd()));
//									appJsonResult = gatewayClient.remoteControlRVM(rmtRequest.getSn(), rmtRequest.getComd(), rmtRequest.getValue());
                            }

                        }
                        if (null != appJsonResult && null != appJsonResult.getStatus()) {
                            appJsonResult = getReturnAppJsonResultRVM(JSONObject.toJSONString(appJsonResult));
                        }
                        // 调整远程控制返回
                        RemoteCtrlItResponse remoteCtrlItResponse = new RemoteCtrlItResponse();
                        remoteCtrlItResponse.setSn(rmtRequest.getSn());
                        remoteCtrlItResponse.setStatus(appJsonResult.getStatus());
                        // data为错误码
                        remoteCtrlItResponse.setData(appJsonResult.getData());
                        remoteCtrlItResponse.setDescription(appJsonResult.getDescription());
                        remoteCtrlItResponse.setSeqNo(rmtRequest.getSeqNo());
                        remoteCtrlItResponse.setEventTime(rmtRequest.getEventTime());
                        kafkaService.transferRmtResponse(remoteCtrlItResponse);
                    }
                } else {
                    logger.warn("rvm的远程控制请求消息超时，不执行");
                    appJsonResult = new AppJsonResult(ResultStatus.OP_UNDO_FOR_REQUEST_TIME_EXPIRED, null);
                    RemoteCtrlItResponse remoteCtrlItResponse = new RemoteCtrlItResponse();
                    remoteCtrlItResponse.setSn(rmtRequest.getSn());
                    remoteCtrlItResponse.setStatus(appJsonResult.getStatus());
                    remoteCtrlItResponse.setData(appJsonResult.getData());
                    remoteCtrlItResponse.setDescription(appJsonResult.getDescription());
                    remoteCtrlItResponse.setSeqNo(rmtRequest.getSeqNo());
                    remoteCtrlItResponse.setEventTime(rmtRequest.getEventTime());
                    kafkaService.transferRmtResponse(remoteCtrlItResponse);
                }
            } catch (Exception ex) {
                logger.error("当前处理远程控制请求发生异常:{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
            }
        });
    }

    /**
     * 获取注册结果数据
     *
     * @param record
     */
    @KafkaListener(topics = KafkaMsgConstant.TOPIC_REGISTER_RESULT_DATA, containerFactory = "KafkaItContainer")
    public void listenRegisterResult(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            // 获取注册结果数据
            try {
                logger.info("接收结果：" + (String) message);
                RegisterResultData registerRt = JSONObject.parseObject(message.toString(), RegisterResultData.class);
                if (registerRt != null) {
                    String curRtTbox = registerRt.getSn();
                    String curRtStatus = registerRt.getStatus();
                    if ((!StringUtils.isBlank(curRtTbox)) && (!StringUtils.isBlank(curRtStatus))) {
                        logger.info("Tbox({})注册结果：{}", curRtTbox, curRtStatus);
                        // 确认当前节点的上是否有等待响应的注册验证线程
                        if (curRtStatus.equals(OperationConstant.REGISTER_SUCCESS)) {
                            kafkaService.notifyReturnRegisterRtToTbox(curRtTbox);
                        }
                    } else {
                        logger.warn("当前注册结果kafka数据有问题：Tbox({})注册结果 {}", curRtTbox, curRtStatus);
                    }
                } else {
                    logger.warn("当前注册结果kafka数据为空");
                }
            } catch (Exception ex) {
                logger.error("当前处理注册结果发生异常:{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
            }
        }

    }


    /**
     * 获取远程拍照请求，执行，并获得结果，调整封装，返回it
     *
     * @param record
     */
    @KafkaListener(topics = KafkaMsgConstant.TOPIC_IT_TAKEPHOTO_REQUEST, containerFactory = "KafkaItContainer")
    public void listenTakePhotoRequest(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            // 获取拍照请求内容
            try {
                TakePhotoItRequest rmtRequest = JSONObject.parseObject((String) message, TakePhotoItRequest.class);
                if (rmtRequest != null) {
                    asynDoTakePhoto(rmtRequest);
                } else {
                    logger.warn("rvm当前远程控制请求kafka数据为空");
                }
            } catch (Exception ex) {
                logger.error("当前处理远程控制请求发生异常,原因：{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
            }
        }
    }

    // 异步处理拍照请求，避免操作时间过久让kafka请求等待
    public void asynDoTakePhoto(TakePhotoItRequest takePhotoRequest) {
        TspServiceProc.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 打印远程拍照请求详情
                    logger.info("rvm远程拍照请求信息如下：{}", JSONObject.toJSONString(takePhotoRequest));
                    AppJsonResult appJsonResult;
                    // 需要判断请求参数中的时间是否已经过期，过期则不执行以下操作
                    if (DateUtil.timeDifference(takePhotoRequest.getEventTime())) {
                        // 获得拍照结果
//						appJsonResult = gatewayClient.takePhotoForRVM(takePhotoRequest.getSn(),
//								takePhotoRequest.getCameraList());
                        appJsonResult = tBoxRemoteCtrl.takePhotoForRVM(takePhotoRequest.getSn(),
                                takePhotoRequest.getCameraList());
                        // 调整appJsonResult结果改为远程控制返回结果RemoteCtrlItResponse
                        RemoteCtrlItResponse remoteCtrlItResponse = new RemoteCtrlItResponse();
                        remoteCtrlItResponse.setSn(takePhotoRequest.getSn());
                        remoteCtrlItResponse.setStatus(appJsonResult.getStatus());
                        remoteCtrlItResponse.setData(appJsonResult.getData());
                        remoteCtrlItResponse.setDescription(appJsonResult.getDescription());
                        remoteCtrlItResponse.setSeqNo(takePhotoRequest.getSeqNo());
                        remoteCtrlItResponse.setEventTime(takePhotoRequest.getEventTime());
                        kafkaService.transferTakePhotoResponse(remoteCtrlItResponse);
                    } else {
                        logger.warn("rvm的远程拍照请求消息超时，不执行");
                        appJsonResult = new AppJsonResult(ResultStatus.OP_UNDO_FOR_REQUEST_TIME_EXPIRED, null);
                        RemoteCtrlItResponse remoteCtrlItResponse = new RemoteCtrlItResponse();
                        remoteCtrlItResponse.setSn(takePhotoRequest.getSn());
                        remoteCtrlItResponse.setStatus(appJsonResult.getStatus());
                        remoteCtrlItResponse.setData(appJsonResult.getData());
                        remoteCtrlItResponse.setDescription(appJsonResult.getDescription());
                        remoteCtrlItResponse.setSeqNo(takePhotoRequest.getSeqNo());
                        remoteCtrlItResponse.setEventTime(takePhotoRequest.getEventTime());
                        kafkaService.transferTakePhotoResponse(remoteCtrlItResponse);
                    }
                } catch (Exception ex) {
                    logger.error("当前处理远程控制请求发生异常!原因：{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
                }
            }
        });
    }

    /**
     * 监听it远程升级指令；
     *
     * @param record
     */
    @KafkaListener(topics = KafkaMsgConstant.TOPIC_IT_REMOTEUPDATE_REQUEST, containerFactory = "KafkaItContainer")
    public void listenRemoteUpdateRequest(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            try {
                TboxUpdateRvmReq tboxUpdateRvmReq = JSONObject.parseObject((String) message, TboxUpdateRvmReq.class);
                if (tboxUpdateRvmReq != null) {
                    logger.info("Tbox(sn:　{}):该远程升级消息生产时间：{} ", tboxUpdateRvmReq.getSn(), record.timestamp());
                    if (tboxUpdateRvmReq.getSn().length() != 0) {
                        String[] sn = tboxUpdateRvmReq.getSn().split(",");
                        for (String serialNumber : sn) {
                            tboxUpdateRvmReq.setSn(serialNumber);
                            TboxUpdateRvmReq req = new TboxUpdateRvmReq(serialNumber, tboxUpdateRvmReq.getVersion(),
                                    tboxUpdateRvmReq.getUrl(), tboxUpdateRvmReq.getMd5(), tboxUpdateRvmReq.getSeqNo(),
                                    tboxUpdateRvmReq.getEventTime());
                            asynDoRemoteUpdate(req);
                        }
                    } else {
                        logger.warn("rvm当前远程控制(远程升级)请求sn数据为空");
                    }
                } else {
                    logger.warn("rvm当前远程控制(远程升级)请求kafka数据为空");
                }
            } catch (Exception ex) {
                logger.error("当前处理远程控制请求(远程升级)发生异常,原因：{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
            }
        }
    }

    public void asynDoRemoteUpdate(TboxUpdateRvmReq req) {
        TspServiceProc.getThreadPool().execute(() -> {

            TboxUpdateRvmReq tboxUpdateRvmReq = req.generatCopyBean();
            try {
                // FOTA
                logger.info("rvm远程升级信息如下：{}", JSONObject.toJSONString(tboxUpdateRvmReq));
                AppJsonResult appJsonResult = new AppJsonResult();
                if (DateUtil.timeDifference(tboxUpdateRvmReq.getEventTime())) {
                    // 获得拍照结果
//						appJsonResult = gatewayClient.tboxRemoteUpdateRVM(tboxUpdateRvmReq);
                    TBoxUpdateUtil tBoxUpdateUtil = this.tBoxUpdateUtil.cloneUtil();
                    appJsonResult = tBoxUpdateUtil.tboxUpdateRvm(tboxUpdateRvmReq);
                } else {
                    logger.warn("rvm的远程升级请求消息超时，不执行");
                    appJsonResult = new AppJsonResult(ResultStatus.OP_UNDO_FOR_REQUEST_TIME_EXPIRED, null);
                }
                TboxUpdateRvmResponse tboxUpdateRvmResponse = new TboxUpdateRvmResponse();
                tboxUpdateRvmResponse.setSn(tboxUpdateRvmReq.getSn());
                tboxUpdateRvmResponse.setSeqNo(tboxUpdateRvmReq.getSeqNo());
                tboxUpdateRvmResponse.setStatus(appJsonResult.getStatus());
                tboxUpdateRvmResponse.setDescription(appJsonResult.getDescription());
                tboxUpdateRvmResponse.setEventTime(tboxUpdateRvmReq.getEventTime());
                kafkaService.transferTboxFOTAResponse(tboxUpdateRvmResponse);
            } catch (Exception ex) {
                logger.error("当前Tbox远程升级请求发生异常!原因：{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
            }

        });
    }


}
