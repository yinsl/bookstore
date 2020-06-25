package com.maxus.tsp.gateway.service;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.KafkaMsgConstantFota;
import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.enums.ResultStatus;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.FOTAConstant;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.model.BaseFotaCtrlItResp;
import com.maxus.tsp.gateway.common.model.BaseRmtCtrlItReq;
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
 * @ClassName VersionQueryUtil
 * @Description 处理FOTA版本查询业务流程
 * @Author zijhm
 * @Date 2019/1/22 14:12
 * @Version 1.0
 **/
@Service
@Scope(value = "prototype")
public class VersionQueryUtil extends BaseUtilProc {

	@Autowired
    private DataProcessing dataProcessing;
	
    private static final Logger logger = LogManager.getLogger(VersionQueryUtil.class);

    //Kafka相关接口
    @Autowired
    private KafkaProducer kafkaService;
    //数据库相关接口
    @Autowired
    private TspPlatformClient tspPlatformClient;

    @Autowired
    private RedisAPI redisAPI;

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
    private BaseFotaCtrlItResp baseFotaCtrlItResp;

    //Tbox相关接口
    @Autowired
    private TboxService tboxService;

    public VersionQueryUtil(RedisAPI redisAPI) {
        super(redisAPI);
        this.redisAPI = redisAPI;
    }

    //请求对象
    private BaseRmtCtrlItReq versionQueryRequestInfo = new BaseRmtCtrlItReq();

    public BaseRmtCtrlItReq getVersionQueryRequestInfo() {
        return versionQueryRequestInfo;
    }

    // 解决对在线Tbox远控请求,回复周期内（10s）Tbox频繁登录登出造成的线程通知错误；
    private boolean tboxOnlineStatusWhenCtrl = false;

    public boolean getTboxOnlineStatusWhenCtrl() {
        return tboxOnlineStatusWhenCtrl;
    }

    /**
     * 保存本节点处理远程控制请求的实例
     */
    private static Hashtable<String, VersionQueryUtil> htVersionQuery = new Hashtable<>();

    /**
     * 获得指定tbox的处理的远程控制的实例
     *
     * @return the htRemoteCtrl
     */
    public static VersionQueryUtil getVersionQuery(String tboxsn) {
        return htVersionQuery.get(tboxsn);
    }

    /**
     * 删除指定tbox的处理的远程控制的实例
     * @param tboxsn
     * @author zhuna
     * @date 2018年11月29日
     */
    public static void removeVersionQuery(String tboxsn){
        htVersionQuery.remove(tboxsn);
    }

    /**
     * @Description 创建版本查询的业务处理类
     * @Date 2019/1/22 15:09
     * @Param [versionQueryRequestInfo]
     * @return com.maxus.tsp.gateway.service.HomeCtrlUtil
     **/
    public VersionQueryUtil cloneUtil() {
        VersionQueryUtil versionQueryUtil = new VersionQueryUtil(redisAPI);
        versionQueryUtil.kafkaService = this.kafkaService;
        versionQueryUtil.tspPlatformClient = this.tspPlatformClient;
        versionQueryUtil.tboxService = this.tboxService;
        return versionQueryUtil;
    }

    /**
     * @Description 参数校验
     * @Date 2019/1/22 15:28
     * @Param [versionQueryRequestInfo]
     * @return com.maxus.tsp.platform.service.model.AppJsonResult
     **/
    public AppJsonResult validVersionQuery(BaseRmtCtrlItReq versionQueryRequestInfo) {
        //检验sn是否为有效值
        String serialNumber = versionQueryRequestInfo.getSn();
        if (StringUtils.isBlank(serialNumber)) {
            logger.warn("当前TBox版本查询参数sn为空!");
            return new AppJsonResult(ResultStatus.PARAM_NULL, "");
        }
        if (serialNumber.length() != FOTAConstant.SERIAL_NUMBER_LENGTH) {
            logger.warn("TBox(SN:{})版本查询时sn长度错误!", serialNumber);
            return new AppJsonResult(ResultStatus.PARAM_ERROR, "");
        }
        //检验sn是否合法
        if (!dataProcessing.isTboxValid(serialNumber)) {
            logger.warn("TBox(SN:{})版本查询请求时sn不合法，redis与数据库中未检索到!", serialNumber);
            return new AppJsonResult(ResultStatus.CAR_INFO_ERROR, "");
        }
        //检验seqNo
        String seqNo = versionQueryRequestInfo.getSeqNo();
        if (StringUtils.isBlank(seqNo)) {
            logger.warn("TBox(SN:{})执行版本查询请求时seqNo为空!", serialNumber);
            return new AppJsonResult(ResultStatus.PARAM_NULL, "");
        }
        if (seqNo.length() != FOTAConstant.SEQ_NO_LENGTH || !seqNo.matches("^[0-9]*$")) {
            logger.warn("TBox(SN:{})执行版本查询请求时seqNo不符合规定!", serialNumber);
            return new AppJsonResult(ResultStatus.PARAM_ERROR, "");
        }
        //检验eventTime
        long eventTime = versionQueryRequestInfo.getEventTime();
        if (eventTime == 0) {
            logger.warn("TBox(SN:{})执行版本查询请求时eventTime为空!", serialNumber);
            return new AppJsonResult(ResultStatus.PARAM_NULL, "");
        }
        return null;
    }

    /**
     * @Description 同步任务处理FOTA版本查询业务
     * @Date 2019/1/22 18:16
     * @Param [versionQueryRequestInfo]
     * @return com.maxus.tsp.platform.service.model.AppJsonResult
     **/
    public AppJsonResult doVersionQuery(BaseRmtCtrlItReq versionQueryRequestInfo) {
        // 重置本次版本查询指令的结果状态
//        boolean existRequest;
        boolean doWakeUp;
        String serialNumber = versionQueryRequestInfo.getSn();
        Date curTime = new Date();
        String eventTime = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");
        // String vin = tboxService.getVINForTbox(serialNumber);
//        if (vin == null) {
//            logger.warn("TBox(SN:{})对应的vin号为空!", serialNumber);
//        } else {
//            logger.info("TBox(SN:{})对应vin号为:{}", serialNumber, vin);
//        }
        //检查当前TBox的状态，确保TBox控制指令唯一且在线
        if (existCommandSend(serialNumber) || existWakeUp(serialNumber)) {
            //TBox存在控制指令或者正处于唤醒中
            logger.warn("TBox(SN:{})已经存在控制指令, 或当前TBox正处于唤醒中!", serialNumber);
            return new AppJsonResult(ResultStatus.RM_TBOX_HAVE_ACCEPT_ONE_COMMAND, "");
        } else {
            //存入需要执行的版本查询的指令的信息
            htVersionQuery.put(serialNumber, VersionQueryUtil.this);
            doWakeUp = !redisAPI.hasKey(RedisConstant.ONLINE_TBOX, serialNumber);
            if (doWakeUp) {
                //TBox不在线, 进行唤醒
                dataProcessing.checkTBoxLogOutForWakeupWait(serialNumber);
                addWakeUp(serialNumber, RedisConstant.FOTA_VERSION_QUERY_REQ);
                logger.info("[版本查询]TBox(SN:{})离线唤醒可以执行!", serialNumber);
                logger.info("[版本查询]TBox(SN:{})开始离线唤醒, 异步发送唤醒短信等待返回结果中...", serialNumber);
                if (!dataProcessing.isSendingMessageSucceed(serialNumber)) {
                    logger.warn("[版本查询]TBox(SN:{})短信唤醒失败!", serialNumber);
                    //移除redis中的唤醒指令
                    removeWakeUp(serialNumber);
                    //移除内存中的控制指令
                    htVersionQuery.remove(serialNumber);
                    return new AppJsonResult(ResultStatus.RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED, "");
                } else {
                    logger.info("[版本查询]TBox(SN:{})短信发送成功, 开始等待TBox上线...", serialNumber);
                    long startTime;
                    try {
                        startTime = System.currentTimeMillis();
                        synchronized (VersionQueryUtil.this) {
                            VersionQueryUtil.this.wait(OperationConstant.WAKEUP_WAIT_TIME);
                        }
                    } catch (Exception e) {
                        //等待唤醒过程中发生异常，需要删除控制指令
                        logger.error("TBox(SN:{})执行版本查询操作因异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
                        htVersionQuery.remove(serialNumber);
                        return new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, null);
                    } finally {
                        //不论唤醒结果如何，清除redis中的唤醒指令
                        removeWakeUp(serialNumber);
                    }
                    //如果唤醒超时，直接通知用户
                    if (System.currentTimeMillis() - startTime >= OperationConstant.WAKEUP_WAIT_TIME) {
                        logger.warn("TBox(SN:{})执行唤醒后未上线, 当前版本查询操作失败, 请求时间:{}", serialNumber, eventTime);
                        //如果唤醒超时, 删除内存中的唤醒指令
                        htVersionQuery.remove(serialNumber);
                        return new AppJsonResult(ResultStatus.RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG, null);
                    } else {
                        logger.info("TBox(SN:{})通过唤醒上线, 开始继续执行版本查询指令!", serialNumber);
                        try {
                            return doWhenTBoxOnline(versionQueryRequestInfo, eventTime);
                        } catch (Exception e) {
                            logger.error("TBox(SN:{})唤醒后处理版本查询指令因发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
                            return new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, null);
                        }
                    }
                }
            } else {
                logger.info("TBox(SN:{})在线, 可以执行当前版本查询指令!", serialNumber);
                try {
                    return doWhenTBoxOnline(versionQueryRequestInfo, eventTime);
                } catch (Exception e) {
                    logger.error("TBox(SN:{})在线时处理版本查询指令因发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
                    return new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, null);
                }
            }
        }
    }

    /**
     * @return com.maxus.tsp.platform.service.model.AppJsonResult
     * @Description TBox在线时处理版本查询业务
     * @Date 2019/1/22 19:37
     * @Param [versionQueryRequestInfo]
     **/
    private AppJsonResult doWhenTBoxOnline(BaseRmtCtrlItReq versionQueryRequestInfo, String eventTime) {
        String serialNumber = versionQueryRequestInfo.getSn();
        //用来存储TBox对请求的响应结果
        String tBoxUpResp;

        //在线时的正常处理业务流程
        try {
            this.tboxOnlineStatusWhenCtrl = true;
            logger.info("TBox(SN:{})将本次版本查询的控制指令存储在Redis中!", serialNumber);
            //确定Tbox在线, 将版本查询指令存入redis
            logger.info("TBox(SN:{})确定当前TBox在线, 将本次版本控制指令存入redis中!", serialNumber);
            addCommandSend(serialNumber, RedisConstant.FOTA_VERSION_QUERY_REQ);
            //确定TBox在线, 再次清除redis中的唤醒指令
            if (existWakeUp(serialNumber)) {
                logger.info("TBox(SN:{})当前TBox在线, 并且redis中存在唤醒指令, 清除唤醒指令(key:sn)!", serialNumber);
                removeWakeUp(serialNumber);
            }
            //通过网关的kafka发送一条消息，做内部通信
            //默认下发时间
            String downTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            kafkaService.sndMesForTemplate(KafkaMsgConstantFota.TOPIC_SELF_VERSION_QUERY_DOWN, serialNumber + "_" + JSONObject.toJSONString(versionQueryRequestInfo )+ "_" + downTime, serialNumber);

            //发送版本查询控制指令后, 等待10s的回包时间
            long startTime = System.currentTimeMillis();
            tboxOnlineStatusWhenCtrl = true;
            synchronized (VersionQueryUtil.this) {
                //等待10s
                VersionQueryUtil.this.wait(OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME);
            }
            if (System.currentTimeMillis() - startTime >= OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME) {
                //超过10s回复结果
                logger.warn("TBox(SN:{})版本查询失败, TBox没有及时回复版本查询报文。请求下发时间:{}", serialNumber, downTime);
                return new AppJsonResult(ResultStatus.RM_TBOX_NOT_REPLY_WITHIN_LIMIT_TIME, null);
            } else {
                tBoxUpResp = htVersionQuery.get(serialNumber).result;
                baseFotaCtrlItResp = JSONObject.parseObject(tBoxUpResp, BaseFotaCtrlItResp.class);
                logger.info("TBox(SN:{})已经回复版本查询结果:{}", serialNumber, baseFotaCtrlItResp.getData());
                return new AppJsonResult(ResultStatus.SUCCESS, baseFotaCtrlItResp.getData());
            }
        } catch (Exception e) {
            logger.error("TBox(SN:{})版本查询操作因发生异常失败。异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            return new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, null);
        }finally {
            try {
                //移除内存以及redis中的版本查询指令
                htVersionQuery.remove(serialNumber);
                if (existCommandSend(serialNumber)) {
                    removeCommandSend(serialNumber);
                }
            } catch (Exception e) {
                logger.error("TBox(SN:{})清除版本查询控制指令因发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            }
        }
    }
}
