package com.maxus.tsp.gateway.service;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.KafkaMsgConstant;
import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.enums.ResultStatus;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.constant.RmtConfigEnum;
import com.maxus.tsp.gateway.common.model.RemoteCtrlRespInfo;
import com.maxus.tsp.gateway.common.model.RemoteCtrlResponseData;
import com.maxus.tsp.gateway.mq.kafka.KafkaProducer;
import com.maxus.tsp.gateway.serviceclient.TspPlatformClient;
import com.maxus.tsp.platform.service.model.AppJsonResult;
import com.maxus.tsp.platform.service.model.vo.BaseCar;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

/**
 * 处理TBox远程配置业务
 *
 * @Author mjrni
 */
@Service
@Scope(value = "prototype")
public class RemoteConfigUtil extends BaseUtilProc {

    private static final Logger logger = LogManager.getLogger(RemoteConfigUtil.class);

    @Autowired
    private DataProcessing dataProcessing;
    
    //Kafka相关接口
    @Autowired
    private KafkaProducer kafkaService;
    //数据库相关接口
    @Autowired
    private TspPlatformClient tspPlatformClient;
    @Autowired
    private RedisAPI redisAPI;
    //Tbox相关接口
    @Autowired
    private TboxService tboxService;

    public RemoteConfigUtil(RedisAPI redisAPI) {
        super(redisAPI);
        this.redisAPI = redisAPI;
    }

    //TBox响应报文的处理结果
    private String result;
    private String data;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    // 远程配置响应信息
    private RemoteCtrlRespInfo remoteCtrlRespInfo;

    // 解决对在线Tbox远控请求,回复周期内（10s）Tbox频繁登录登出造成的线程通知错误；
    private boolean tboxOnlineStatusWhenCtrl = false;

    public boolean getTboxOnlineStatusWhenCtrl() {
        return tboxOnlineStatusWhenCtrl;
    }

    /**
     * 保存本节点处理远程配置请求的实例
     */
    private static Hashtable<String, RemoteConfigUtil> htRemoteConfig = new Hashtable<>();

    /**
     * 获取当前节点处理控制请求的实例
     *
     * @param serialNumber
     * @return
     */
    public static RemoteConfigUtil getRemoteConfigUtil(String serialNumber) {
        return htRemoteConfig.get(serialNumber);
    }

    /**
     * 删除当前节点处理控制请求的实例
     *
     * @param serialNumber
     */
    public static void removeRemoteConfigUtil(String serialNumber) {
        htRemoteConfig.remove(serialNumber);
    }

    /**
     * 创建远程配置的业务处理类
     *
     * @return
     */
    public RemoteConfigUtil cloneUtil() {
        RemoteConfigUtil remoteConfigUtil = new RemoteConfigUtil(redisAPI);
        remoteConfigUtil.kafkaService = this.kafkaService;
        remoteConfigUtil.tspPlatformClient = this.tspPlatformClient;
        remoteConfigUtil.tboxService = this.tboxService;
        return remoteConfigUtil;
    }

    public AppJsonResult doRemoteConfig(String reqCmd, String reqValue, String serialNumber, long eventTime, String seqNo, String vin) {
        logger.debug("开始执行远程配置!");
        remoteCtrlRespInfo = new RemoteCtrlRespInfo();
        remoteCtrlRespInfo.setComd(reqCmd);
        remoteCtrlRespInfo.setValue(reqValue);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        remoteCtrlRespInfo.setReqTime(format.format(new Date(eventTime)));

        String reqSerialNumber = serialNumber;

        //参数校验
        try {
            //改装车
            if (StringUtils.isBlank(vin)) {
                //如果vin为空, 在检查sn是否为空
                if (StringUtils.isBlank(serialNumber)) {
                    logger.warn("当前远程配置请求, vin与sn都为空!");
                    return new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
                }
            } else {
                //根据vin获取sn
                BaseCar baseCar = tspPlatformClient.selectBaseCarByVIN(vin);
                if (baseCar == null) {
                    logger.warn("当前远程配置请求vin号在数据库中不存在! vin:{}", vin);
                    return new AppJsonResult(ResultStatus.VIN_ERROR, "");
                }
                reqSerialNumber = baseCar.getSn();
            }
            if (StringUtils.isBlank(reqSerialNumber)) {
                logger.warn("当前远程配置请求错误, serialNumber为空!");
                return new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
            }
            if (StringUtils.isBlank(reqCmd)) {
                logger.warn("TBox(SN:{})当前远程配置参数错误, reqCmd为空!", reqSerialNumber);
                return new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
            }
            if (StringUtils.isBlank(reqValue)) {
                logger.warn("TBox(SN:{})当前远程配置参数错误, reqValue为空!", reqSerialNumber);
                return new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
            }
            long reqNumber = Long.parseUnsignedLong(reqValue);
            if (reqNumber > OperationConstant.REMOTE_CONFIG_REQ_MAX_VALUE) {
                logger.warn("TBox(SN:{})当前远程配置参数错误, value非法!", reqSerialNumber);
                return new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, "");
            }
            if (eventTime == 0) {
                logger.warn("TBox(SN:{})当前远程配置参数错误, eventTime为0!", reqSerialNumber);
                return new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
            }
            if (StringUtils.isBlank(seqNo)) {
                logger.warn("TBox(SN:{})当前远程配置请求参数错误, seqNo为空!", reqSerialNumber);
                return new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
            }
            // cmd:1.熄火后上传周期[0,2880]；2.大数据上传频率；3.tbox开关；4.工程数据0.请求获取远程配置
            int valueNum = Integer.parseUnsignedInt(reqValue);
            int idCode;
            switch (reqCmd) {
                case "CmdRemoteStallConfig":
                    if (valueNum < 0 || valueNum > 2880) {
                        logger.warn("TBox(SN:{})当前远程配置cmd:{}参数value:{}超出允许范围!", reqSerialNumber, reqCmd, valueNum);
                        return new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
                    }
                    idCode = Integer.parseInt(RmtConfigEnum.RMT_CONFIG_TYPE_STALL.getCode());
                    return doRemoteConfigCtrl(reqSerialNumber, reqValue, eventTime, seqNo, idCode, reqCmd);
                case "CmdRemoteBigDataConfig":
                    if (valueNum <= 0 || valueNum > 60) {
                        logger.warn("TBox(SN:{})当前远程配置cmd:{}参数value:{}超出允许范围!", reqSerialNumber, reqCmd, valueNum);
                        return new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
                    }
                    if (reqValue.equals(RmtConfigEnum.RMT_CONFIG_INVALID.getCode())) {
                        logger.warn("TBox(SN:{})当前远程配置cmd:{}参数value:{}为无效值0!", reqSerialNumber, reqCmd, reqValue);
                        return new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, "");
                    }
                    idCode = Integer.parseInt(RmtConfigEnum.RMT_CONFIG_TYPE_BIGDATA.getCode());
                    return doRemoteConfigCtrl(reqSerialNumber, reqValue, eventTime, seqNo, idCode, reqCmd);
                case "CmdRemoteStallSetting":
                    idCode = Integer.valueOf(RmtConfigEnum.RMT_CONFIG_TYPE_STALLSETTING_SWITCH.getCode());
                    if (reqValue.equals(RmtConfigEnum.RMT_CONFIG_SWITCH_OFF.getCode())) {
                        reqValue = RmtConfigEnum.RMT_CONFIG_STALLSETTING_OFF.getCode();
                    } else if (reqValue.equals(RmtConfigEnum.RMT_CONFIG_SWITCH_ON.getCode())) {
                        reqValue = RmtConfigEnum.RMT_CONFIG_STALLSETTING_ON.getCode();
                    } else {
                        return new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, "");
                    }
                    return doRemoteConfigCtrl(reqSerialNumber, reqValue, eventTime, seqNo, idCode, reqCmd);
                case "CmdRemoteBigDataSetting":
                    idCode = Integer.valueOf(RmtConfigEnum.RMT_CONFIG_TYPE_BIGDATA_SWITCH.getCode());
                    if (reqValue.equals(RmtConfigEnum.RMT_CONFIG_SWITCH_OFF.getCode())) {
                        reqValue = RmtConfigEnum.RMT_CONFIG_BIGDATASETTING_OFF.getCode();

                    } else if (reqValue.equals(RmtConfigEnum.RMT_CONFIG_SWITCH_ON.getCode())) {
                        reqValue = RmtConfigEnum.RMT_CONFIG_BIGDATASETTING_ON.getCode();
                    } else {
                        return new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, "");
                    }
                    return doRemoteConfigCtrl(reqSerialNumber, reqValue, eventTime, seqNo, idCode, reqCmd);
                case "GetConfig":
                    idCode = Integer.valueOf(RmtConfigEnum.RMT_CONFIG_TYPE_GETCONFIG.getCode());
                    if (!reqValue.equals(RmtConfigEnum.RMT_CONFIG_SWITCH_ON.getCode())) {
                        return new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, "");
                    }
                    return doRemoteConfigCtrl(reqSerialNumber, reqValue, eventTime, seqNo, idCode, reqCmd);
                case "CmdRemoteEngDataConfig":
                    if (valueNum <= 0 || valueNum > 60) {
                        logger.warn("TBox(SN:{})远程配置cmd:{}参数value:{}超出允许范围!", reqSerialNumber, reqCmd, valueNum);
                        return new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
                    }
                    if (reqValue.equals(RmtConfigEnum.RMT_CONFIG_INVALID.getCode())) {
                        logger.warn("TBox(SN:{})远程配置cmd:{}参数reqValue:{}为无效值0。", reqSerialNumber, reqCmd, reqValue);
                        return new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, "");
                    }
                    idCode = Integer.valueOf(RmtConfigEnum.RMT_CONFIG_TYPE_ENGDATA.getCode());
                    return doRemoteConfigCtrl(reqSerialNumber, reqValue, eventTime, seqNo, idCode, reqCmd);
                case "CmdRemoteEngDataSetting":
                    idCode = Integer.valueOf(RmtConfigEnum.RMT_CONFIG_TYPE_ENGDATA_SWITCH.getCode());
                    if (reqValue.equals(RmtConfigEnum.RMT_CONFIG_SWITCH_OFF.getCode())) {
                        reqValue = RmtConfigEnum.RMT_CONFIG_ENGDATASETTING_OFF.getCode();

                    } else if (reqValue.equals(RmtConfigEnum.RMT_CONFIG_SWITCH_ON.getCode())) {
                        reqValue = RmtConfigEnum.RMT_CONFIG_ENGDATASETTING_ON.getCode();
                    } else {
                        return new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, "");
                    }
                    return doRemoteConfigCtrl(reqSerialNumber, reqValue, eventTime, seqNo, idCode, reqCmd);
                default:
                    logger.warn("TBox(SN:{})当前远程配置指令错误, cmd:{}", reqSerialNumber, reqCmd);
                    return new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, "");
            }
        } catch (Exception e) {
            logger.error("TBox(SN:{})当前远程配置因发生异常失败, 异常原因:{}", reqSerialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            return new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, "");
        }
    }

    /**
     * 参数校验完成后, 开始进行远程配置
     *
     * @param reqSerialNumber
     * @param reqValue
     * @param eventTime
     * @param seqNo
     * @param idCode
     * @param reqCmd
     * @return
     */
    private AppJsonResult doRemoteConfigCtrl(String reqSerialNumber, String reqValue, long eventTime, String seqNo, int idCode, String reqCmd) throws Exception {
            //首先确认sn的合法性
            if (!dataProcessing.isTboxValid(reqSerialNumber)) {
                logger.warn("TBox(SN:{})当前远程配置请求错误, sn不合法!", reqSerialNumber);
                return new AppJsonResult(ResultStatus.CAR_INFO_ERROR, "");
            }

            //检查TBox是否在线是否需要唤醒
            boolean doWakeUp;
            //检查当前TBox在线状态, 确保TBox控制指令的唯一性
            if (existCommandSend(reqSerialNumber) || existWakeUp(reqSerialNumber)) {
                logger.warn("TBox(SN:{})远程控制指令存在状态:{}, 唤醒指令存在状态:{}", reqSerialNumber, existCommandSend(reqSerialNumber), existWakeUp(reqSerialNumber));
                return new AppJsonResult(ResultStatus.RM_TBOX_HAVE_ACCEPT_ONE_COMMAND, "");
            } else {
                logger.info("TBox(SN:{})当前远程配置远控排他成功!", reqSerialNumber);
                //存入需要执行的远程配置的信息
                htRemoteConfig.put(reqSerialNumber, RemoteConfigUtil.this);
                doWakeUp = !redisAPI.hasKey(RedisConstant.ONLINE_TBOX, reqSerialNumber);
                if (doWakeUp) {
                    logger.info("TBox(SN:{})不在线, 开始进行短信唤醒!", reqSerialNumber);
                    dataProcessing.checkTBoxLogOutForWakeupWait(reqSerialNumber);
                    addWakeUp(reqSerialNumber, RedisConstant.RM_CONFIG_CTRL);
                    logger.info("[远程配置]TBox(SN:{})离线唤醒可以执行!", reqSerialNumber);
                    logger.info("[远程配置]TBox(SN:{})开始离线唤醒, 异步发送唤醒短信等待返回结果中...", reqSerialNumber);
                    if (!dataProcessing.isSendingMessageSucceed(reqSerialNumber)) {
                        logger.warn("[远程配置]TBox(SN:{})唤醒短信发送失败!", reqSerialNumber);
                        //移除redis中的唤醒指令
                        removeWakeUp(reqSerialNumber);
                        //移除内存中的控制指令
                        htRemoteConfig.remove(reqSerialNumber);
                        return new AppJsonResult(ResultStatus.RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED, "");
                    } else {
                        logger.info("[远程配置]TBox(SN:{})唤醒短信发送成功, 开始等待TBox上线!", reqSerialNumber);
                        long startTime;
                        try {
                            startTime = System.currentTimeMillis();
                            synchronized (RemoteConfigUtil.this) {
                                RemoteConfigUtil.this.wait(OperationConstant.WAKEUP_WAIT_TIME);
                            }
                        } catch (Exception e) {
                            //等待唤醒的过程中发生异常, 需要删除控制指令
                            logger.error("TBox(SN:{})执行远程配置操作因发生异常失败, 异常原因:{}", reqSerialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
                            htRemoteConfig.remove(reqSerialNumber);
                            return new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, "");
                        } finally {
                            removeWakeUp(reqSerialNumber);
                        }

                        if (System.currentTimeMillis() - startTime >= OperationConstant.WAKEUP_WAIT_TIME) {
                            //唤醒超时未登录
                            logger.warn("TBox(SN:{})执行唤醒后未上线, 当前远程配置操作失败, 请求时间:{}", reqSerialNumber, eventTime);
                            //唤醒超时, 删除内存中的控制指令
                            htRemoteConfig.remove(reqSerialNumber);
                            return new AppJsonResult(ResultStatus.RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG, "");
                        } else {
                            //唤醒后及时登录
                            logger.info("TBox(SN:{})通过唤醒上线, 开始继续执行远程配置操作!", reqSerialNumber);
                            return doWhenTBoxOnline(reqSerialNumber, reqValue, eventTime, seqNo, idCode, reqCmd);
                        }
                    }
                } else {
                    //TBox在线
                    logger.info("TBox(SN:{})在线, 可以执行当前远程配置操作!",reqSerialNumber);
                    return doWhenTBoxOnline(reqSerialNumber, reqValue, eventTime, seqNo, idCode, reqCmd);
                }
            }
    }

    /**
     * TBox在线时进行的控制内容
     * @param reqSerialNumber
     * @param reqValue
     * @param eventTime
     * @param seqNo
     * @param idCode
     * @param reqCmd
     * @return
     */
    private AppJsonResult doWhenTBoxOnline(String reqSerialNumber, String reqValue, long eventTime, String seqNo, int idCode, String reqCmd) throws Exception {
        try {
            //将TBox的在线标志更改为true
            this.tboxOnlineStatusWhenCtrl = true;
            logger.info("TBox(SN:{})在线, 将本次控制信息存入redis!", reqSerialNumber);
            addCommandSend(reqSerialNumber, RedisConstant.RM_CONFIG_CTRL);

            //通过网关内部kafka发送一条消息, 用作内部通信
            //默认下发时间
            String downTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_DOWN_REMOTE_CONFIG,
                    reqSerialNumber + "_" + idCode + "_" + reqValue + "_" + downTime, reqSerialNumber);

            //发送远程配置命令后, 等待回包10s
            long startTime= System.currentTimeMillis();
            try {
                synchronized (RemoteConfigUtil.this) {
                    RemoteConfigUtil.this.wait(OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME);
                }
            } catch (Exception e) {
                logger.error("TBox(SN:{})在发送远程配置信息后等待回包过程中发生异常, 异常原因:{}", reqSerialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
                throw e;
            }
            if (System.currentTimeMillis() - startTime >= OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME) {
                //超过10s回复结果
                logger.warn("TBox(SN:{})远程配置失败, TBox没有及时回复报文, 请求下发时间:{}", reqSerialNumber, startTime);
                return new AppJsonResult(ResultStatus.RM_TBOX_NOT_REPLY_WITHIN_LIMIT_TIME, "");
            } else {
                String stateValue = htRemoteConfig.get(reqSerialNumber).result;
                String returnData = htRemoteConfig.get(reqSerialNumber).data;
                RemoteCtrlResponseData remoteCtrlResponseData = JSONObject.parseObject(returnData, RemoteCtrlResponseData.class);
                logger.info("TBox(SN:{})已经回复当前远程配置的结果:{}", reqSerialNumber, stateValue);
                if (stateValue.equals("0")) {
                    return new AppJsonResult(ResultStatus.SUCCESS, remoteCtrlResponseData);
                } else {
                    return new AppJsonResult(ResultStatus.RM_REMOTE_CONTROL_CANNOT_DONE_FOR_OTHER_REASONS, remoteCtrlResponseData);
                }
            }
        } catch (Exception e) {
            throw e;
        }finally {
            htRemoteConfig.remove(reqSerialNumber);
            if (existCommandSend(reqSerialNumber)) {
                removeCommandSend(reqSerialNumber);
            }
            if (existWakeUp(reqSerialNumber)) {
                removeWakeUp(reqSerialNumber);
            }
        }
    }
}
