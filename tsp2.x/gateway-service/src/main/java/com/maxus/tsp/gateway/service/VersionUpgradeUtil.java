package com.maxus.tsp.gateway.service;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.KafkaMsgConstantFota;
import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.enums.ResultStatus;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.FOTAConstant;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.model.fota.VersionUpgradeReqInfo;
import com.maxus.tsp.gateway.common.model.fota.VersionUpgradeRespInfo;
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
 * @ClassName VersionUpgradeUtil
 * @Description 处理FOTA版本升级业务
 * @Author zijhm
 * @Date 2019/1/24 17:02
 * @Version 1.0
 **/
@Service
@Scope(value = "prototype")
public class VersionUpgradeUtil extends BaseUtilProc {

    private static final Logger logger = LogManager.getLogger(VersionUpgradeUtil.class);

    //Kafka相关接口
    @Autowired
    private KafkaProducer kafkaService;
    //数据库相关接口
    @Autowired
    private TspPlatformClient tspPlatformClient;

    @Autowired
    private RedisAPI redisAPI;
    
    @Autowired
    private DataProcessing dataProcessing;

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

    //Tbox相关接口
    @Autowired
    private TboxService tboxService;

    public VersionUpgradeUtil(RedisAPI redisAPI) {
        super(redisAPI);
        this.redisAPI = redisAPI;
    }

    //请求对象
    private VersionUpgradeReqInfo versionUpgradeReqInfo = new VersionUpgradeReqInfo();

    public VersionUpgradeReqInfo getVersionUpgradeReqInfo() {
        return versionUpgradeReqInfo;
    }

    // 解决对在线Tbox远控请求,回复周期内（10s）Tbox频繁登录登出造成的线程通知错误；
    private boolean tboxOnlineStatusWhenCtrl = false;

    public boolean getTboxOnlineStatusWhenCtrl() {
        return tboxOnlineStatusWhenCtrl;
    }

    /**
     * 保存本节点处理远程控制请求的实例
     */
    private static Hashtable<String, VersionUpgradeUtil> htVersionUpgrade = new Hashtable<>();

    /**
     * 获得指定tbox的处理的远程控制的实例
     *
     * @return the htRemoteCtrl
     */
    public static VersionUpgradeUtil getVersionUpgrade(String tboxsn) {
        return htVersionUpgrade.get(tboxsn);
    }

    /**
     * 删除指定tbox的处理的远程控制的实例
     * @param tboxsn
     * @author zhuna
     * @date 2018年11月29日
     */
    public static void removeVersionUpgrade(String tboxsn){
        htVersionUpgrade.remove(tboxsn);
    }

    /**
     * @Description 创建版本升级业务处理类
     * @Date 2019/1/24 17:30
     * @Param []
     * @return com.maxus.tsp.gateway.service.VersionUpgradeUtil
     **/
    public VersionUpgradeUtil cloneUtil() {
        VersionUpgradeUtil versionUpgradeUtil = new VersionUpgradeUtil(redisAPI);
        versionUpgradeUtil.kafkaService = this.kafkaService;
        versionUpgradeUtil.tspPlatformClient = this.tspPlatformClient;
        versionUpgradeUtil.tboxService = this.tboxService;
        return versionUpgradeUtil;
    }

    /**
     * @Description 版本升级参数校验
     * @Date 2019/1/24 18:32
     * @Param [versionUpgradeReqInfo]
     * @return com.maxus.tsp.platform.service.model.AppJsonResult
     **/
    public AppJsonResult validVersionUpgrade(VersionUpgradeReqInfo versionUpgradeReqInfo) {
        String serialNumber = versionUpgradeReqInfo.getSn();
        if (serialNumber == null) {
            logger.warn("当前TBox进行版本升级操作过程中参数sn为空!");
            return new AppJsonResult(ResultStatus.PARAM_NULL, "");
        }
        if (serialNumber.length() != FOTAConstant.SERIAL_NUMBER_LENGTH) {
            logger.warn("TBox(SN:{})版本升级时sn长度错误!", serialNumber);
            return new AppJsonResult(ResultStatus.PARAM_ERROR, "");
        }
        //检验sn是否合法
        if (!dataProcessing.isTboxValid(serialNumber)) {
            logger.warn("TBox(SN:{})版本升级请求时sn不合法，redis与数据库中未检索到!", serialNumber);
            return new AppJsonResult(ResultStatus.CAR_INFO_ERROR, "");
        }
        //校验id
        if (versionUpgradeReqInfo.getId() == null) {
            logger.warn("TBox(SN:{})版本升级请求时参数id为空!", serialNumber);
            return new AppJsonResult(ResultStatus.PARAM_NULL, "");
        }
        //校验operate
        if (versionUpgradeReqInfo.getOperate() == null) {
            logger.warn("TBox(SN:{})版本升级请求时参数operate为空!", serialNumber);
            return new AppJsonResult(ResultStatus.PARAM_NULL, "");
        } else if (versionUpgradeReqInfo.getOperate() < 0 || versionUpgradeReqInfo.getOperate() > 2) {
            logger.warn("TBox(SN:{})版本升级请求时参数operate不合法!", serialNumber);
            return new AppJsonResult(ResultStatus.PARAM_ERROR, "");
        }
        //检验seqNo
        String seqNo = versionUpgradeReqInfo.getSeqNo();
        if (StringUtils.isBlank(seqNo)) {
            logger.warn("TBox(SN:{})执行版本升级请求时seqNo为空!", serialNumber);
            return new AppJsonResult(ResultStatus.PARAM_NULL, "");
        }
        if (seqNo.length() != FOTAConstant.SEQ_NO_LENGTH || !seqNo.matches("^[0-9]*$")) {
            logger.warn("TBox(SN:{})执行版本升级请求时seqNo不符合规定!", serialNumber);
            return new AppJsonResult(ResultStatus.PARAM_ERROR, "");
        }
        //校验eventTime
        if (versionUpgradeReqInfo.getEventTime() == 0) {
            logger.warn("TBox(SN:{})执行版本升级请求时eventTime为空!", serialNumber);
            return new AppJsonResult(ResultStatus.PARAM_NULL, "");
        }
        return null;
    }

    /**
     * @Description 开始执行版本升级业务
     * @Date 2019/1/24 18:33
     * @Param [versionUpgradeReqInfo]
     * @return com.maxus.tsp.platform.service.model.AppJsonResult
     **/
    public AppJsonResult doVersionUpgrade(VersionUpgradeReqInfo versionUpgradeReqInfo) {
        //判断是否需要唤醒
        boolean doWakeUp;
        String serialNumber = versionUpgradeReqInfo.getSn();
        String eventTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        // String vin = tboxService.getVINForTbox(serialNumber);
//        if (vin == null) {
//            logger.warn("TBox(SN:{})对应的vin号为空!", serialNumber);
//        } else {
//            logger.info("TBox(SN:{})对应的vin号为:{}", vin);
//        }
        //检查当前TBox状态，确保TBox控制指令唯一且TBox在线
        if (dataProcessing.isFotaCtrlExist(serialNumber, OperationConstant.FOTA_VERSION_UPGRADE)) {
            //TBox存在控制指令或正处于唤醒中
            logger.warn("TBox(SN:{})已经存在控制指令, 或者当前TBox正处于唤醒中!", serialNumber);
            return new AppJsonResult(ResultStatus.RM_TBOX_HAVE_ACCEPT_ONE_COMMAND, "");
        } else {
            //存入需要执行版本升级的控制指令信息
            htVersionUpgrade.put(serialNumber, VersionUpgradeUtil.this);
            doWakeUp = !redisAPI.hasKey(RedisConstant.ONLINE_TBOX, serialNumber);
            if (doWakeUp) {
                //TBox不在线, 需要进行唤醒
                dataProcessing.checkTBoxLogOutForWakeupWait(serialNumber);
                addWakeUp(serialNumber, RedisConstant.FOTA_VERSION_UPGRADE_REQ);
                logger.info("[版本升级]TBox(SN:{})离线唤醒可以执行!", serialNumber);
                logger.info("[版本升级]TBox(SN:{})开始离线唤醒, 异步发送唤醒短信等待返回结果中...", serialNumber);
                if (!dataProcessing.isSendingMessageSucceed(serialNumber)) {
                    logger.warn("[版本升级]TBox(SN:{})短信唤醒失败!", serialNumber);
                    //移除redis中的唤醒指令
                    removeWakeUp(serialNumber);
                    //移除内存中的控制指令
                    htVersionUpgrade.remove(serialNumber);
                    return new AppJsonResult(ResultStatus.RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED, "");
                } else {
                    logger.info("[版本升级]TBOx(SN:{})短信发送成功, 开始等待TBox上线...", serialNumber);
                    long startTime;
                    try {
                        startTime = System.currentTimeMillis();
                        synchronized (VersionUpgradeUtil.this) {
                            VersionUpgradeUtil.this.wait(OperationConstant.WAKEUP_WAIT_TIME);
                        }
                    } catch (Exception e) {
                        // 等待唤醒过程中发生异常, 需要删除控制指令
                        logger.error("TBox(SN:{})执行版本升级操作因发生异常而失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
                        htVersionUpgrade.remove(serialNumber);
                        return new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, "");
                    } finally {
                        //不论唤醒结果, 清除redis中的唤醒指令
                        removeWakeUp(serialNumber);
                    }
                    //判断唤醒是否超时, 如果唤醒超时, 直接通知用户
                    if (System.currentTimeMillis() - startTime >= OperationConstant.WAKEUP_WAIT_TIME) {
                        logger.warn("TBox(SN:{})执行唤醒后未上线, 当前版本升级操作失败, 请求时间:{}", serialNumber, eventTime);
                        //如果唤醒超时, 删除内存中的唤醒指令
                        htVersionUpgrade.remove(serialNumber);
                        return new AppJsonResult(ResultStatus.RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG, "");
                    } else {
                        logger.info("TBox(SN:{})通过唤醒上线, 开始继续执行版本升级指令!", serialNumber);
                        try {
                            return doWhenTBoxOnline(versionUpgradeReqInfo, eventTime);
                        } catch (Exception e) {
                            logger.error("TBox(SN:{})唤醒后处理版本升级指令因发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
                            return new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, "");
                        }
                    }
                }
            } else {
                //TBox在线直接处理
                logger.info("TBox(SN:{})在线, 可以执行当前版本升级指令!", serialNumber);
                try {
                    return doWhenTBoxOnline(versionUpgradeReqInfo, eventTime);
                } catch (Exception e) {
                    logger.error("TBox(SN:{})在线时处理版本升级指令因发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
                    return new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, "");
                }
            }
        }
    }

    /**
     * @Description TBox在线时处理控制版本升级控制指令
     * @Date 2019/1/28 10:24
     * @Param [versionUpgradeReqInfo, eventTime]
     * @return com.maxus.tsp.platform.service.model.AppJsonResult
     **/
    private AppJsonResult doWhenTBoxOnline(VersionUpgradeReqInfo versionUpgradeReqInfo, String eventTime) {
        String serialNumber = versionUpgradeReqInfo.getSn();
        //用来存储TBox对请求的响应结果
        String tBoxUpResp;

        //在线时的正常处理业务流程
        try {
            this.tboxOnlineStatusWhenCtrl = true;
            logger.info("TBox(SN:{})将本次版本升级的控制指令存储在Redis中!", serialNumber);
            //确定TBox在线, 将版本查询指令存入redis
            logger.info("TBox(SN:{})确定当前TBox在线, 将本次版本控制指令存储至Redis!", serialNumber);
            addCommandSend(serialNumber, RedisConstant.FOTA_VERSION_UPGRADE_REQ);
            //确定TBox在线, 再次清除redis中的唤醒指令
            if (existWakeUp(serialNumber)) {
                logger.info("TBox(SN:{})当前TBox在线, 并且redis中存在唤醒指令, 清除唤醒指令(key:sn)!");
                removeWakeUp(serialNumber);
            }
            //通过网关的kafka发送一条消息, 用作内部通信
            //默认下发时间
            String downTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            kafkaService.sndMesForTemplate(KafkaMsgConstantFota.TOPIC_SELF_VERSION_UPGRADE_DOWN, serialNumber + "_" + JSONObject.toJSONString(versionUpgradeReqInfo) + "_" + downTime, serialNumber);
            //发送版本升级控制指令后, 等待10s的回包时间
            long startTime = System.currentTimeMillis();
            tboxOnlineStatusWhenCtrl = true;
            synchronized (VersionUpgradeUtil.this) {
                //等待10s
                VersionUpgradeUtil.this.wait(OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME);
            }
            if (System.currentTimeMillis() - startTime >= OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME) {
                //超过10s回复结果
                logger.warn("TBox(SN:{})版本升级失败, TBox没有及时回复版本升级报文。请求下发时间:{}", serialNumber, downTime);
                return new AppJsonResult(ResultStatus.RM_TBOX_NOT_REPLY_WITHIN_LIMIT_TIME, "");
            } else {
                tBoxUpResp = htVersionUpgrade.get(serialNumber).result;
                VersionUpgradeRespInfo versionUpgradeRespInfo = JSONObject.parseObject(tBoxUpResp, VersionUpgradeRespInfo.class);
                logger.info("TBox(SN:{})已经回复版本升级结果:{}", serialNumber, JSONObject.toJSONString(versionUpgradeRespInfo));
                return new AppJsonResult(ResultStatus.SUCCESS, versionUpgradeRespInfo);
            }
        } catch (Exception e) {
            logger.error("TBox(SN:{})版本升级操作因发生异常失败。异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            return new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, "");
        }finally {
            try {
                //移除内存以及redis中的版本查询指令
                htVersionUpgrade.remove(serialNumber);

                if (existCommandSend(serialNumber)) {
                    removeCommandSend(serialNumber);
                }
            } catch (Exception e) {
                logger.error("TBox(SN:{})清除版本升级控制指令因发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            }
        }
    }
}
