package com.maxus.tsp.gateway.service;

import java.util.Date;
import java.util.Hashtable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.maxus.tsp.common.constant.KafkaMsgConstant;
import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.enums.ResultStatus;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OTARemoteCommand;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.model.RemoteCtrlResponseData;
import com.maxus.tsp.gateway.common.ota.RemoteCtrlInfo;
import com.maxus.tsp.gateway.mq.kafka.KafkaProducer;
import com.maxus.tsp.gateway.serviceclient.TspPlatformClient;
import com.maxus.tsp.platform.service.model.AppJsonResult;

@Service
@Scope(value = "prototype")
public class RemoteCtrlUtil extends BaseUtilProc {

	@Autowired
    private DataProcessing dataProcessing;
	
    @Autowired
    private KafkaProducer kafkaService;

    // 数据库服务
    @Autowired
    private TboxService tboxService;
    // kafka服务,用于与it进行交互

    @Autowired
    private RedisAPI redisAPI;

    private final Logger logger = LogManager.getLogger(getClass());
    @Autowired
    private TspPlatformClient tspPlatformClient;

    /**
     * 用于记录当前远程控制请求是否是需要唤醒阶段
     */
    private boolean tboxOnlineStatusWhenCtrl = false;

    public boolean getTboxOnlineStatusWhenCtrl() {
        return tboxOnlineStatusWhenCtrl;
    }

    /**
     * 获得指定tbox的处理的远程控制的实例
     *
     * @return the htRemoteCtrl
     */
    public static RemoteCtrlUtil getHtRemoteCtrl(String tboxsn) {
        return htRemoteCtrl.get(tboxsn);
    }

    /**
     * 获得指定tbox的处理的透传控制的实例
     *
     * @return the htRemoteCtrl
     */
    public static RemoteCtrlUtil getHtForwardCtrl(String tboxsn) {
        return htForwardCtrl.get(tboxsn);
    }

    /**
     * 保存本节点处理远程控制请求的实例
     */
    private static Hashtable<String, RemoteCtrlUtil> htRemoteCtrl = new Hashtable<>();

    /**
     * 保存本节点处理透传拍照请求的实例
     */
    private static Hashtable<String, RemoteCtrlUtil> htForwardCtrl = new Hashtable<>();

    public void setResult(String result) {
        this.result = result;
    }

    private String result = "";

    public RemoteCtrlUtil(RedisAPI redisAPI) {
        super(redisAPI);
    }

    // @Override
    public RemoteCtrlUtil cloneUtil() {
        RemoteCtrlUtil remoteCtrlUtil = new RemoteCtrlUtil(redisAPI);
        remoteCtrlUtil.kafkaService = this.kafkaService;
        remoteCtrlUtil.tspPlatformClient = this.tspPlatformClient;
        remoteCtrlUtil.tboxService = this.tboxService;
        // remoteCtrlUtil.
        return remoteCtrlUtil;
    }

    /**
     * 检验远控参数是否合法
     * 
     * @param serialNumber
     * @param comd
     * @param value
     * @param vin
     * @return
     */
    public AppJsonResult validRemoteCtrl(String serialNumber, String comd, String value, String vin) {
        // 检查serialNumber参数是否为空
        if (StringUtils.isBlank(comd)) {
            logger.warn("远程控制comd参数为空。 vin:{}, serialNumber:{}", vin, serialNumber);
            return new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
        }
        try {
            OTARemoteCommand.valueOf(comd);
        } catch (IllegalArgumentException e) {
            logger.error("远程控制指令错误 comd:{}, TBox:{}。", comd, serialNumber);
            return new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, "");
        }
        // 检查serialNumber参数是否为空
        if (StringUtils.isBlank(value)) {
            logger.warn("远程控制value参数为空。 vin:{}, serialNumber:{}", vin, serialNumber);
            return new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
        }
        // 不需要调用数据库
        logger.debug("当前远程控制请求是 TBox:{}, comd:{}, value:{}", serialNumber, comd, value);
        if (!comd.equals("TemperatureSetting") && !(value.equals(OperationConstant.REMOTE_CTRL_FUNCTION_OPEN)
                || value.equals(OperationConstant.REMOTE_CTRL_FUNCTION_CLOSED))) {
            logger.warn("远程控制指令错误 comd:{}, TBox:{}。", comd, serialNumber);
            return new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, "");
        } else if (comd.equals("TemperatureSetting") && (!StringUtils.isNumeric(value)
                || !(Integer.parseInt(value) <= 255 && Integer.parseInt(value) >= 0))) {
            logger.warn("远程控制指令错误 comd:{}, TBox:{}。", comd, serialNumber);
            return new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, "");
        }
        return null;
    }

    // 非异步的远程控制流程
    public AppJsonResult doRemoteCtrl(String cmValue, String serialNumber, OTARemoteCommand commd) {
        // 重置本次远程指令的结果状态
        AppJsonResult rcResult;
        boolean existWakeUp = false;
        boolean existTBoxCommand = false;
        boolean existRequest = false;
        boolean doWakeUp = false;
        Date curTime = new Date();
        String eventTime = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");
        String vin = tboxService.getVINForTbox(serialNumber);
        // 首先得确认code是合法的tbox编号
        try {
            if (!dataProcessing.isTboxValid(serialNumber)) {
                return new AppJsonResult(ResultStatus.CAR_INFO_ERROR, "");
            }
            // 查询唤醒清单中是否已经存在正在唤醒的请求
            // existRequest = DataProcessing.isRequestRmtExist(serialNumber, vin);
            existWakeUp = existWakeUp(serialNumber);
            existTBoxCommand = existCommandSend(serialNumber);
            logger.info("TBox({})的Redis会话唤醒状态 ：{},指令存在状态：{}", serialNumber, existWakeUp, existTBoxCommand);
            existRequest = existWakeUp || existTBoxCommand;
            /* !redisAPI.hasKey(RedisConstant.ONLINE_TBOX, serialNumber); */
        } catch (Exception e) {
            logger.error("远程控制因发生异常失败。TBox(SN:{}), 异常:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            return new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, "");
        }
        // Tbox已经存在相应的远程控制指令
        if (existRequest) {
            // 提示用户不能进行操作
            logger.warn("TBox(SN:{})正在唤醒或执行其他操控，不接受新的请求。", serialNumber);
            return new AppJsonResult(ResultStatus.RM_TBOX_HAVE_ACCEPT_ONE_COMMAND, "");
        } else {
            // 远程控制指令记录于数据库
            int recordID = 0;
            // 存入需要远程控制信息
            htRemoteCtrl.put(serialNumber, RemoteCtrlUtil.this);
            doWakeUp = !dataProcessing.onlineTboxExistCheck(serialNumber);
            logger.info("TBox(SN:{})当前是否需要唤醒状态:{}", serialNumber, doWakeUp);
            if (doWakeUp) {
                dataProcessing.checkTBoxLogOutForWakeupWait(serialNumber);
                // GlobalSessionChannel.addWakeUp(serialNumber,
                // OperationConstant.REMOTE_CONTROL);
                addWakeUp(serialNumber, commd.name());
                logger.info("离线TBox(SN:{})的唤醒及远程控制请求可以执行 ", serialNumber);
                try {
                    recordID = this.tspPlatformClient.recordRemoteCtrl(vin, commd.getMessage(), cmValue);
                } catch (Exception e) {
                    logger.error("远程控制因发生异常失败。TBox(SN:{}), 异常:{}", serialNumber,
                            ThrowableUtil.getErrorInfoFromThrowable(e));
                    removeWakeUp(serialNumber);
                    htRemoteCtrl.remove(serialNumber);
                    return new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, "");
                }
                if (!dataProcessing.isSendingMessageSucceed(serialNumber)) {
                    // 唤醒失败，直接调用回复
                    logger.warn("TBox(SN:{})的远程控制因唤醒失败而失败 ", serialNumber);
                    // 清除wakeup记录
                    removeWakeUp(serialNumber);
                    htRemoteCtrl.remove(serialNumber);
                    return new AppJsonResult(ResultStatus.RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED, "");
                } else {
                    logger.info("远程控制流程唤醒短信发送成功并开始等待TBox(SN:{})登陆。 ", serialNumber);
                    // 等tbox唤醒后在继续，最多等一分钟
                    long startTime = 0;
                    try {
                        startTime = System.currentTimeMillis();
                        synchronized (RemoteCtrlUtil.this) {
                            RemoteCtrlUtil.this.wait(OperationConstant.WAKEUP_WAIT_TIME);
                        }
                    } catch (InterruptedException e) {
                        // 等待唤醒中途如果失败，就必须清除记录，并且
                        logger.error("远程控制因发生异常失败。TBox(SN:{}), 异常:{}", serialNumber,
                                ThrowableUtil.getErrorInfoFromThrowable(e));
                        htRemoteCtrl.remove(serialNumber);
                        return new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, "");
                    } finally {
                        // GlobalSessionChannel.removeWakeUp(serialNumber,
                        // OperationConstant.REMOTE_CONTROL);
                        // 清除在Redis中的唤醒记录
                        removeWakeUp(serialNumber);
                    }

                    // 如果超时，则代表唤醒失败了，直接通知用户
                    if (System.currentTimeMillis() - startTime >= OperationConstant.WAKEUP_WAIT_TIME) {
                        // 清除wakeup记录
                        logger.warn("远程控制失败，因为TBox(SN:{})执行唤醒后未上线。请求时间:{}", serialNumber, eventTime);
                        htRemoteCtrl.remove(serialNumber);
                        return new AppJsonResult(ResultStatus.RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG, "");
                    }
                }
            } else {
                logger.info("在线TBox(SN:{})的远程控制请求可以执行 ", serialNumber);
                try {
                    recordID = this.tspPlatformClient.recordRemoteCtrl(vin, commd.getMessage(), cmValue);
                } catch (Exception e) {
                    logger.error("远程控制因发生异常失败。TBox(SN:{}), 异常:{}", serialNumber,
                            ThrowableUtil.getErrorInfoFromThrowable(e));
                    htRemoteCtrl.remove(serialNumber);
                    return new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, "");
                }
            }
            // 继续等远程控制指令响应
            rcResult = doRemoteCtrlWhenTboxOnline(recordID, cmValue, serialNumber, commd, eventTime);
            return rcResult;
        }
    }
    
    /**
     * @Title: getRtDownSendTime
     * @Description: 获取tbox远程控制下发时间
     * @param: @param
     *             tboxsn
     * @return: void
     * @throws @author
     *             余佶
     * @Date 2017年8月12日 下午1:52:37
     */
    public String getRtDownSendTime(String tboxsn) {
        try {
            return redisAPI.getHash(RedisConstant.CAR_REMOTE_CTRL_DOWN, tboxsn);
        } catch (Exception ex) {
            logger.error("Redis connection error,TBox({}) can't do getRtDownSendTime:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            return null;
        }
    }

    // 在线情况下执行远程控制
    public AppJsonResult doRemoteCtrlWhenTboxOnline(int recordID, String cmValue, String serialNumber,
            OTARemoteCommand commd, String eventTime) {
        AppJsonResult rcResult;
        try {
            RemoteCtrlInfo ctrlInfo = new RemoteCtrlInfo();
            ctrlInfo.remoteCmd = commd;
            this.tboxOnlineStatusWhenCtrl = true;
            // 等响应报文上来时，设置命令执行状态
            // GlobalSessionChannel.addRemoteCtrl(serialNumber, ctrlInfo);
            addCommandSend(serialNumber, ctrlInfo.remoteCmd.toString());
            // tbox上线，将其移除唤醒，进一步发送远程控制
            if (existWakeUp(serialNumber)) {
                removeWakeUp(serialNumber);
            }
            /*
             * if (GlobalSessionChannel.existWakeUp(serialNumber,
             * OperationConstant.REMOTE_CONTROL)) {
             * GlobalSessionChannel.removeWakeUp(serialNumber,
             * OperationConstant.REMOTE_CONTROL); }
             */
            // 这里得通过kafka生产一个远程控制指令下发
            kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_REMOTECTRL,
                    serialNumber + "_" + commd.value() + "_" + cmValue + "_" + eventTime, serialNumber);
            Date curTime = new Date();
            // 默认的下发时间
            String downDate = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");
            // 发送远程控制命令后，等待回包10s
            long startTime = 0;
            startTime = System.currentTimeMillis();
            synchronized (RemoteCtrlUtil.this) {
                RemoteCtrlUtil.this.wait(OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME);
            }
            if (System.currentTimeMillis() - startTime >= OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME) {
                // 超过10s回复结果
                logger.warn("远程控制失败，因为TBox(SN:{})没有及时回复远程控制报文。请求时间:{}", serialNumber, eventTime);
                // 更新远程控制下发时间
                downDate = getRtDownSendTime(serialNumber);
                this.tspPlatformClient.addRemoteCtrlDownStatus(recordID, downDate);
                rcResult = new AppJsonResult(ResultStatus.RM_TBOX_NOT_REPLY_WITHIN_LIMIT_TIME, "");
            } else {
                downDate = getRtDownSendTime(serialNumber);
                this.tspPlatformClient.addRemoteCtrlDownStatus(recordID, downDate);
                String[] stateValue = htRemoteCtrl.get(serialNumber).result.split("_");
                logger.info("TBox(SN:{}):已经回复远程控制结果:{}", serialNumber, stateValue[0]);
                RemoteCtrlResponseData tboxResultData = new RemoteCtrlResponseData();
                tboxResultData.setErrCode(stateValue[1]);
                if (!ResultStatus.SUCCESS.equals(ResultStatus.getResultStatus(stateValue[0]))) {
                    rcResult = new AppJsonResult(ResultStatus.getResultStatus(stateValue[0]), tboxResultData);
                } else {
                    rcResult = new AppJsonResult(ResultStatus.getResultStatus(stateValue[0]), "");
                }
            }
        } catch (Exception e) {
            logger.error("远程控制因发生异常失败。TBox(SN:{}), 异常:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            rcResult = new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, "");
        } finally {
            // 可以获取到本次远程指令的处理结果了，该结果由RemoteCtrlListener的listenRes方法来设置
            htRemoteCtrl.remove(serialNumber);
            removeRtDownSendTime(serialNumber);
            // 消除Redis中下发的指令记录
            removeCommandSend(serialNumber);
            /*
             * if (GlobalSessionChannel.existTboxRemoteCtrl(serialNumber)) {
             * GlobalSessionChannel.removeRemoteCtrl(serialNumber); }
             */
        }
        return rcResult;
    }
    
    /**
     * @Title: removeRtDownSendTime
     * @Description: 清除tbox远程控制下发时间
     * @param: @param
     *             tboxsn
     * @return: void
     * @throws @author
     *             余佶
     * @Date 2017年8月12日 下午1:52:37
     */
    public void removeRtDownSendTime(String tboxsn) {
        try {
            redisAPI.removeHash(RedisConstant.CAR_REMOTE_CTRL_DOWN, tboxsn);
        } catch (Exception ex) {
            logger.error("Redis connection error, TBox({}) can't do removeRtDownSendTime:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
    }
    
}
