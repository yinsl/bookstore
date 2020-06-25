package com.maxus.tsp.gateway.service;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.KafkaMsgConstant;
import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.enums.ResultStatus;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OTABlueToothCtrlConstant;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.model.BlueToothAddRequestInfo;
import com.maxus.tsp.gateway.common.model.BlueToothCtrlRespCommonInfo;
import com.maxus.tsp.gateway.common.model.BlueToothCtrlRespInfo;
import com.maxus.tsp.gateway.common.model.BlueToothRequestInfo;
import com.maxus.tsp.gateway.mq.kafka.KafkaProducer;
import com.maxus.tsp.gateway.serviceclient.TspPlatformClient;
import com.maxus.tsp.platform.service.model.AppJsonResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Hashtable;

/**
 * 处理新版蓝牙功能业务流程
 */
@Service
@Scope(value = "prototype")
public class BlueToothUtil extends BaseUtilProc {

    private static final Logger logger = LogManager.getLogger(BlueToothUtil.class);

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

    public BlueToothUtil(RedisAPI redisAPI) {
        super(redisAPI);
        this.redisAPI = redisAPI;
    }

    //用来存储TBox响应报文的处理结果
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

    //用来对TBox返回的报文进行model封装
    private BlueToothCtrlRespInfo blueToothCtrlRespInfo;

    //用来封装TBox返回的除获取蓝牙钥匙以外的结果
    private BlueToothCtrlRespCommonInfo blueToothCtrlRespCommonInfo;

    //Tbox相关接口
    @Autowired
    private TboxService tboxService;

    //请求对象
    private BlueToothRequestInfo blueToothRequestInfo = new BlueToothRequestInfo();

    public BlueToothRequestInfo getBlueToothRequestInfo() {
        return blueToothRequestInfo;
    }

    // 解决对在线Tbox远控请求,回复周期内（10s）Tbox频繁登录登出造成的线程通知错误；
    private boolean tboxOnlineStatusWhenCtrl = false;

    public boolean getTboxOnlineStatusWhenCtrl() {
        return tboxOnlineStatusWhenCtrl;
    }

    /**
     * 保存本节点处理远程控制请求的实例
     */
    private static Hashtable<String, BlueToothUtil> htBlueToothCtrl = new Hashtable<>();

    public static BlueToothUtil getBlueToothCtrl(String serialNumber) {
        return htBlueToothCtrl.get(serialNumber);
    }

    public static void removeBlueToothCtrl(String serialNumber) {
        htBlueToothCtrl.remove(serialNumber);
    }

    /**
     * 创建蓝牙控制的业务处理类
     *
     * @return
     */
    public BlueToothUtil cloneUtil() {
        BlueToothUtil blueToothUtil = new BlueToothUtil(redisAPI);
        blueToothUtil.kafkaService = this.kafkaService;
        blueToothUtil.tspPlatformClient = this.tspPlatformClient;
        blueToothUtil.tboxService = this.tboxService;
        return blueToothUtil;
    }

    public AppJsonResult validBlueToothCtrl(BlueToothRequestInfo blueToothRequestInfo) {
        this.blueToothRequestInfo = blueToothRequestInfo;
        String serialNumber = blueToothRequestInfo.getSn();
        try {
            if (StringUtils.isBlank(serialNumber)) {
                logger.warn("蓝牙控制错误, 当前蓝牙控制请求的sn为空!");
                return new AppJsonResult(ResultStatus.BLUETOOTH_CONTROL_FAILED_FOR_PARAM_ILLEGAL, "");
            }
            if (!dataProcessing.isTboxValid(serialNumber)) {
                return new AppJsonResult(ResultStatus.CAR_INFO_ERROR, "");
            }
            String cmd = blueToothRequestInfo.getComd();
            logger.info("TBox(SN:{})当前蓝牙控制的指令:{}", serialNumber, cmd);
            switch (cmd) {
                case OTABlueToothCtrlConstant.COMMAND_SEND_VERIFICATION_CODE://发送验证码
                    return verificationCode(blueToothRequestInfo);
                case OTABlueToothCtrlConstant.COMMAND_ADD_BT_KEY://添加蓝牙钥匙
                    return addBTKey(blueToothRequestInfo);
                case OTABlueToothCtrlConstant.COMMAND_DEL_BT_KEY://删除蓝牙钥匙
                    return delBTKey(blueToothRequestInfo);
                case OTABlueToothCtrlConstant.COMMAND_GET_BT_KEY://获取
                    return getBtKey(blueToothRequestInfo);
                default:
                    logger.warn("蓝牙配置请求指令错误！");
                    return new AppJsonResult(ResultStatus.BLUETOOTH_CONTROL_FAILED_FOR_COMMAND_ILLEGAL, "");
            }
        } catch (Exception e) {
            logger.error("TBox(SN:{})蓝牙控制由于发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            return new AppJsonResult(ResultStatus.BLUETOOTH_CONTROL_FAILED_FOR_EXCEPTION, "");
        }/* finally {
            htBlueToothCtrl.remove(serialNumber);
            if (existCommandSend(serialNumber)) {
                removeCommandSend(serialNumber);
            }
            if (existWakeUp(serialNumber)) {
                removeWakeUp(serialNumber);
            }
        }*/
    }

    private AppJsonResult getBtKey(BlueToothRequestInfo blueToothRequestInfo) throws Exception {
        logger.info("TBox(SN:{})获取蓝牙钥匙参数校验成功!", blueToothRequestInfo.getSn());
        return doBlueToothCtrl(blueToothRequestInfo);
    }

    private AppJsonResult delBTKey(BlueToothRequestInfo blueToothRequestInfo) throws Exception {
        if (StringUtils.isBlank(blueToothRequestInfo.getValue()) || !StringUtils.isNumeric(blueToothRequestInfo.getValue())) {
            logger.warn("TBox(SN:{}):添加蓝牙配置参数无效，BtKeyID无效或为空！", blueToothRequestInfo.getSn());
            return new AppJsonResult(ResultStatus.BLUETOOTH_CONTROL_FAILED_FOR_PARAM_ILLEGAL, "");
        }
        logger.info("TBox(SN:{})删除蓝牙钥匙参数校验成功!", blueToothRequestInfo.getSn());
        return doBlueToothCtrl(blueToothRequestInfo);
    }

    private AppJsonResult addBTKey(BlueToothRequestInfo blueToothRequestInfo) throws Exception {
        String reqValue = blueToothRequestInfo.getValue();
        String serialNumber = blueToothRequestInfo.getSn();
        BlueToothAddRequestInfo addInfo = JSONObject.parseObject(reqValue, BlueToothAddRequestInfo.class);
        if (addInfo.getBtKeyID() == null) {
            logger.warn("TBox(SN:{}):添加蓝牙配置参数无效，BtKeyID无效或为空！", serialNumber);
            return new AppJsonResult(ResultStatus.BLUETOOTH_CONTROL_FAILED_FOR_PARAM_ILLEGAL, "");
        }
        if (addInfo.getPermissions() == null) {
            logger.warn("TBox(SN:{}):添加蓝牙配置参数无效，Permissions无效或者为空！", serialNumber);
            return new AppJsonResult(ResultStatus.BLUETOOTH_CONTROL_FAILED_FOR_PARAM_ILLEGAL, "");
        }
        if (StringUtils.isBlank(addInfo.getAuthKey()) || addInfo.getAuthKey().length() != OTABlueToothCtrlConstant.AUTHKEY_LENGTH) {
            logger.warn("TBox(SN:{}):添加蓝牙配置参数无效，AuthKey不符合要求或者为空！", serialNumber);
            return new AppJsonResult(ResultStatus.BLUETOOTH_CONTROL_FAILED_FOR_PARAM_ILLEGAL, "");
        }
        if (addInfo.getStartDateTime() == OTABlueToothCtrlConstant.INVALID_TIME
                || addInfo.getEndDateTime() == OTABlueToothCtrlConstant.INVALID_TIME) {
            logger.warn("TBox(SN:{}):添加蓝牙配置参数无效，DateTime为空！", serialNumber);
            return new AppJsonResult(ResultStatus.BLUETOOTH_CONTROL_FAILED_FOR_PARAM_ILLEGAL, "");
        }
        logger.info("TBox(SN:{})添加蓝牙钥匙参数校验成功!", serialNumber);
        return doBlueToothCtrl(blueToothRequestInfo);
    }

    private AppJsonResult verificationCode(BlueToothRequestInfo blueToothRequestInfo) throws Exception {
        String verificationCodeMsg = blueToothRequestInfo.getValue();
        String serialNumber = blueToothRequestInfo.getSn();
        if (StringUtils.isBlank(verificationCodeMsg) || verificationCodeMsg.length() != OTABlueToothCtrlConstant.VERIFICATION_CODE_LENGTH) {
            logger.warn("TBox(SN:{})蓝牙配置错误：当前蓝牙请求verificationCode为空或长度错误！", serialNumber);
            return new AppJsonResult(ResultStatus.BLUETOOTH_CONTROL_FAILED_FOR_PARAM_ILLEGAL, "");
        }
        if (!verificationCodeMsg.matches("[a-fA-F0-9]+")) {
            logger.warn("TBox(SN:{})蓝牙配置错误：当前蓝牙请求verificationCode内容格式错误！", serialNumber);
            return new AppJsonResult(ResultStatus.BLUETOOTH_CONTROL_FAILED_FOR_PARAM_ILLEGAL, "");
        }
        logger.info("TBox(SN:{})发送蓝牙验证码参数校验成功!", serialNumber);
        return doBlueToothCtrl(blueToothRequestInfo);
    }

    /**
     * 同步任务处理蓝牙控制业务
     *
     * @param blueToothRequestInfo
     * @return
     */
    private AppJsonResult doBlueToothCtrl(BlueToothRequestInfo blueToothRequestInfo) throws Exception {
        boolean doWakeUp;
        String serialNumber = blueToothRequestInfo.getSn();
        Date date = new Date();
        String eventTime = DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss");
        //检查当前TBox在线状态, 确保TBox控制指令的唯一性
        if (existCommandSend(serialNumber) || existWakeUp(serialNumber)) {
            logger.warn("TBox(SN:{})蓝牙控制当前控制指令存在状态:{}, 当前唤醒指令存在状态:{}", serialNumber, existCommandSend(serialNumber), existWakeUp(serialNumber));
            return new AppJsonResult(ResultStatus.RM_TBOX_HAVE_ACCEPT_ONE_COMMAND, "");
        } else {
            logger.info("TBox(SN:{})蓝牙控制远控排他成功!", serialNumber);
            //存入需要执行的蓝牙控制的信息
            htBlueToothCtrl.put(serialNumber, BlueToothUtil.this);
            doWakeUp = !redisAPI.hasKey(RedisConstant.ONLINE_TBOX, serialNumber);
            if (doWakeUp) {
                logger.info("TBox(SN:{})不在线, 开始进行短信唤醒!", serialNumber);
                dataProcessing.checkTBoxLogOutForWakeupWait(serialNumber);
                addWakeUp(serialNumber, RedisConstant.CAR_BLUETOOTH_CTRL_REQ);
                logger.info("[蓝牙控制]TBox(SN:{})离线唤醒可以执行!", serialNumber);
                logger.info("[蓝牙控制]TBox(SN:{})开始离线唤醒, 异步发送唤醒短信等待返回结果中...", serialNumber);
                if (!dataProcessing.isSendingMessageSucceed(serialNumber)) {
                    logger.warn("[蓝牙控制]TBox(SN:{})唤醒短信发送失败!", serialNumber);
                    //移除redis中的唤醒指令
                    removeWakeUp(serialNumber);
                    //移除内存中的控制指令
                    htBlueToothCtrl.remove(serialNumber);
                    return new AppJsonResult(ResultStatus.RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED, "");
                } else {
                    logger.info("[蓝牙控制]TBox(SN:{})唤醒短信发送成功, 开始等待TBox上线!", serialNumber);
                    long startTime;
                    try {
                        startTime = System.currentTimeMillis();
                        synchronized (BlueToothUtil.this) {
                            BlueToothUtil.this.wait(OperationConstant.WAKEUP_WAIT_TIME);
                        }
                    } catch (Exception e) {
                        //等待唤醒的过程中发生异常, 需要删除控制指令
                        logger.error("TBox(SN:{})执行蓝牙控制操作因发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
                        htBlueToothCtrl.remove(serialNumber);
                        return new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, "");
                    } finally {
                        //不论唤醒成功或者失败, 都需要删除redis中的唤醒指令
                        removeWakeUp(serialNumber);
                    }

                    if (System.currentTimeMillis() - startTime >= OperationConstant.WAKEUP_WAIT_TIME) {
                        //唤醒超时未登录
                        logger.warn("TBox(SN:{})执行唤醒后未上线, 当前蓝牙控制操作失败, 请求时间:{}", serialNumber, eventTime);
                        //唤醒超时, 删除内存中的控制指令
                        htBlueToothCtrl.remove(serialNumber);
                        return new AppJsonResult(ResultStatus.RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG, "");
                    } else {
                        //唤醒后及时登录
                        logger.info("TBox(SN:{})通过唤醒上线, 开始继续执行蓝牙控制操作!", serialNumber);
                        return doWhenTBoxOnline(blueToothRequestInfo, eventTime);
                    }

                }
            } else {
                logger.info("TBox(SN:{})在线, 可以执行当前蓝牙控制指令!", serialNumber);
                return doWhenTBoxOnline(blueToothRequestInfo, eventTime);
            }
        }

    }

    /**
     * TBox在线时执行的业务流程
     *
     * @param blueToothRequestInfo
     * @param eventTime
     * @return
     */
    private AppJsonResult doWhenTBoxOnline(BlueToothRequestInfo blueToothRequestInfo, String eventTime) throws Exception {
        String serialNumber = blueToothRequestInfo.getSn();
        try {
            //用来存储TBox对请求的响应结果
            String tBoxUpResp;

            //将TBox的在线标志更改为true
            this.tboxOnlineStatusWhenCtrl = true;
            logger.info("TBox(SN:{})在线, 将本次控制信息存入redis!", serialNumber);
            addCommandSend(serialNumber, RedisConstant.CAR_BLUETOOTH_CTRL_REQ);

            //通过网关内部kafka发送一条消息, 用作内部通信
            //默认下发时间
            String downTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");

            logger.info("TBox(SN:{})当前蓝牙控制指令为:{}, 开始根据对应的comd选取对应的topic!", serialNumber, blueToothRequestInfo.getComd());
            String topic;
            switch (blueToothRequestInfo.getComd()) {
                case OTABlueToothCtrlConstant.COMMAND_SEND_VERIFICATION_CODE:
                    topic = KafkaMsgConstant.TOPIC_DOWN_BLUETOOTH_VERIFICATION_CODE;
                    break;
                case OTABlueToothCtrlConstant.COMMAND_ADD_BT_KEY:
                    topic = KafkaMsgConstant.TOPIC_DOWN_BLUETOOTH_ADD_BTKEY;
                    break;
                case OTABlueToothCtrlConstant.COMMAND_DEL_BT_KEY:
                    topic = KafkaMsgConstant.TOPIC_DOWN_BLUETOOTH_DEL_BTKEY;
                    break;
                case OTABlueToothCtrlConstant.COMMAND_GET_BT_KEY:
                    topic = KafkaMsgConstant.TOPIC_DOWN_BLUETOOTH_GET_BTKEY;
                    break;
                default:
                    logger.warn("蓝牙配置失败，因为TBox:{} 控制指令非法！", serialNumber);
                    return new AppJsonResult(ResultStatus.BLUETOOTH_CONTROL_FAILED_FOR_COMMAND_ILLEGAL, "");
            }
            //网关kafka开始下发内部通信信息
            kafkaService.sndMesForTemplate(topic, serialNumber + "_" + JSONObject.toJSONString(blueToothRequestInfo) + "_" + downTime, serialNumber);

            //发送蓝牙控制消息后, 开始等待10s的回包时间
            long startTime = System.currentTimeMillis();
            try {
                synchronized (BlueToothUtil.this) {
                    //等待10s
                    BlueToothUtil.this.wait(OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME);
                }
            } catch (Exception e) {
                logger.error("TBox(SN:{})在发送蓝牙控制信息后等待回包过程中发生异常, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
                throw e;
            }
            if (System.currentTimeMillis() - startTime >= OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME) {
                //超过10s回复结果
                logger.warn("TBox(SN:{})蓝牙控制失败, TBox没有及时回复报文, 请求下发时间:{}", serialNumber, startTime);
                return new AppJsonResult(ResultStatus.RM_TBOX_NOT_REPLY_WITHIN_LIMIT_TIME, "");
            } else {
                tBoxUpResp = htBlueToothCtrl.get(serialNumber).result;
                logger.info("TBox(SN:{})已经回复蓝牙配置的结果:{}", serialNumber, tBoxUpResp);

                String cmd = blueToothRequestInfo.getComd();
                switch (cmd) {
                    case OTABlueToothCtrlConstant.COMMAND_GET_BT_KEY://获取
                        blueToothCtrlRespInfo = JSONObject.parseObject(tBoxUpResp, BlueToothCtrlRespInfo.class);
                        return new AppJsonResult(ResultStatus.SUCCESS, blueToothCtrlRespInfo);
                    default:
                        blueToothCtrlRespCommonInfo = JSONObject.parseObject(tBoxUpResp, BlueToothCtrlRespCommonInfo.class);
                        return new AppJsonResult(ResultStatus.SUCCESS, blueToothCtrlRespCommonInfo);
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            htBlueToothCtrl.remove(serialNumber);
            if (existCommandSend(serialNumber)) {
                removeCommandSend(serialNumber);
            }
            if (existWakeUp(serialNumber)) {
                removeWakeUp(serialNumber);
            }
        }
    }
}
