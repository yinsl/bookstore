package com.maxus.tsp.gateway.service;


import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.KafkaMsgConstant;
import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.enums.ResultStatus;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.model.RemoteCtrlResponseData;
import com.maxus.tsp.gateway.mq.kafka.KafkaProducer;
import com.maxus.tsp.platform.service.model.AppJsonResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

/**
 * 获取整车状态 业务处理类
 *
 * @Author mjrni
 */
@Service
@Scope(value = "prototype")
public class GetVehicleStatusUtil extends BaseUtilProc {

    private static final Logger logger = LogManager.getLogger(GetVehicleStatusUtil.class);

    @Autowired
    private DataProcessing dataProcessing;
    
    @Autowired
    private KafkaProducer kafkaService;
    //数据库相关接口
//    @Autowired
//    private TspPlatformClient tspPlatformClient;
    @Autowired
    private RedisAPI redisAPI;


    public GetVehicleStatusUtil(RedisAPI redisAPI) {
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

    // 解决对在线Tbox远控请求,回复周期内（10s）Tbox频繁登录登出造成的线程通知错误；
    private boolean tboxOnlineStatusWhenCtrl = false;

    public boolean getTboxOnlineStatusWhenCtrl() {
        return tboxOnlineStatusWhenCtrl;
    }

    public static Hashtable<String, GetVehicleStatusUtil> htGetVehicleStatusUtil = new Hashtable<>();

    public static GetVehicleStatusUtil getGetVehicleStatusUtil(String serialNumber) {
        return htGetVehicleStatusUtil.get(serialNumber);
    }

    /**
     * 创建执行获取整车状态的业务类实例
     *
     * @return
     */
    public GetVehicleStatusUtil cloneUtil() {
        GetVehicleStatusUtil getVehicleStatusUtil = new GetVehicleStatusUtil(redisAPI);
        getVehicleStatusUtil.kafkaService = this.kafkaService;
        return getVehicleStatusUtil;
    }

    /**
     * 开始执行获取整车状态请求, 首先进行参数校验, 通过参数校验后开始进行业务处理
     *
     * @return
     */
    public AppJsonResult validGetVehicleStatusCtrl(String serialNumber, String value, long eventTime) {
        try {
            if (StringUtils.isBlank(value) || StringUtils.isBlank(serialNumber)) {
                logger.warn("获取车况失败, SN或value为空!sn:{},value:{}", serialNumber, value);
                return new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
            }
            String[] requestID = StringUtils.split(value, ",");
            for (String id : requestID) {
                if (org.apache.commons.lang.StringUtils.isBlank(id)) {
                    logger.warn("TBox(SN:{}):当前获取车况请求ID有误；id:{} ", serialNumber, value);
                    return new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, "");
                }
            }
            return doGetVehicleStatusCtrl(serialNumber, value, eventTime);
        } catch (Exception e) {
            logger.error("TBox(SN:{})当前获取车况因发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            return new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, "");
        }
    }

    /**
     * 参数校验成功后, 开始进行业务处理
     *
     * @param serialNumber
     * @param value
     * @param eventTime
     * @return
     */
    private AppJsonResult doGetVehicleStatusCtrl(String serialNumber, String value, long eventTime) throws Exception {
        boolean doWakeUp;
        //开始进行远程排他
        if (existCommandSend(serialNumber) || existWakeUp(serialNumber)) {
            logger.warn("TBox(SN:{})获取车况当前控制指令存在状态:{}, 当前唤醒指令存在状态:{}", serialNumber, existCommandSend(serialNumber), existWakeUp(serialNumber));
            return new AppJsonResult(ResultStatus.RM_TBOX_HAVE_ACCEPT_ONE_COMMAND, "");
        }
        logger.info("TBox(SN:{})远控排他成功, 开始进行远程控制!", serialNumber);
        //存入需要执行的远程获取车况的的信息
        htGetVehicleStatusUtil.put(serialNumber, GetVehicleStatusUtil.this);
        doWakeUp = !redisAPI.hasKey(RedisConstant.ONLINE_TBOX, serialNumber);
        if (doWakeUp) {
            logger.info("TBox(SN:{})不在线, 开始进行短信唤醒!", serialNumber);
            dataProcessing.checkTBoxLogOutForWakeupWait(serialNumber);
            addWakeUp(serialNumber, RedisConstant.GET_VEHICLE_STATUS_REQ);
            logger.info("[获取车况]TBox(SN:{})离线唤醒可以执行!", serialNumber);
            logger.info("[获取车况]TBox(SN:{})开始离线唤醒, 异步发送唤醒短信等待返回结果中...", serialNumber);
            if (!dataProcessing.isSendingMessageSucceed(serialNumber)) {
                logger.warn("[获取车况]TBox(SN:{})唤醒短信发送失败!", serialNumber);
                //移除redis中的唤醒指令
                removeWakeUp(serialNumber);
                //移除内存中的控制指令
                htGetVehicleStatusUtil.remove(serialNumber);
                return new AppJsonResult(ResultStatus.RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED, "");
            }
            logger.info("[获取车况]TBox(SN:{})唤醒短信发送成功, 开始等待TBox上线!", serialNumber);
            long startTime = System.currentTimeMillis();
            synchronized (GetVehicleStatusUtil.this) {
                GetVehicleStatusUtil.this.wait(OperationConstant.WAKEUP_WAIT_TIME);
            }
            removeWakeUp(serialNumber);
            if (System.currentTimeMillis() - startTime >= OperationConstant.WAKEUP_WAIT_TIME) {
                //唤醒超时未登录
                logger.warn("TBox(SN:{})执行唤醒后未上线, 当前获取车况操作失败, 请求时间:{}", serialNumber, eventTime);
                htGetVehicleStatusUtil.remove(serialNumber);
                return new AppJsonResult(ResultStatus.RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG, "");
            }
            logger.info("TBox(SN:{})通过唤醒上线, 开始继续执行获取车况操作!", serialNumber);
            return doWhenTBoxOnline(serialNumber, value, eventTime);
        } else {
            logger.info("TBox(SN:{})在线, 可以执行当前获取车况指令!", serialNumber);
            return doWhenTBoxOnline(serialNumber, value, eventTime);
        }
    }

    /**
     * TBox在线时执行获取车况指令
     *
     * @param serialNumber
     * @param value
     * @param eventTime
     * @return
     */
    private AppJsonResult doWhenTBoxOnline(String serialNumber, String value, long eventTime) throws Exception {
        try {
            //把控制指令存至redis
            addCommandSend(serialNumber, RedisConstant.GET_VEHICLE_STATUS_REQ);
            String dateTime = DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd HH:mm:ss");
            kafkaService.sndMesForTemplate(KafkaMsgConstant.GET_VEHICLE_STATUS_DOWN_CTRL, serialNumber + "_" + value + "_" + dateTime, serialNumber);
            // 默认的下发时间
            String downDate = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            // 发送远程控制命令后，等待回包10s
            long startTime = 0;
            startTime = System.currentTimeMillis();
            tboxOnlineStatusWhenCtrl = true;
            synchronized (GetVehicleStatusUtil.this) {
                GetVehicleStatusUtil.this.wait(OperationConstant.REMOTE_CONFIG_RESPONSE_WAIT_TIME);
            }
            if (System.currentTimeMillis() - startTime >= OperationConstant.REMOTE_CONFIG_RESPONSE_WAIT_TIME) {
                //超过10s回复结果
                logger.warn("TBox(SN:{})获取车况失败, 因为TBox没有及时回复报文, 请求时间:{}", serialNumber, downDate);
                return new AppJsonResult(ResultStatus.RM_TBOX_NOT_REPLY_WITHIN_LIMIT_TIME, "");
            } else {
                String[] result = org.apache.commons.lang.StringUtils.split(data, "@");
                if(result[0].equals("0")){
                    return new AppJsonResult(ResultStatus.SUCCESS, JSONObject.parseObject(result[2], RemoteCtrlResponseData.class));
                } else {
                    return new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, result[2]);
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            htGetVehicleStatusUtil.remove(serialNumber);
            removeCommandSend(serialNumber);
            removeWakeUp(serialNumber);
        }
    }
}
