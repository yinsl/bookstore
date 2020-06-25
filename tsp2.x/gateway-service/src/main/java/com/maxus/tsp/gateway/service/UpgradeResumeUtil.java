package com.maxus.tsp.gateway.service;

import java.util.Date;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.KafkaMsgConstantFota;
import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.enums.ResultStatus;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.FOTAConstant;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.model.fota.UpgradeResumeReqInfo;
import com.maxus.tsp.gateway.common.model.fota.UpgradeResumeRespInfo;
import com.maxus.tsp.gateway.mq.kafka.KafkaProducer;
import com.maxus.tsp.gateway.serviceclient.TspPlatformClient;
import com.maxus.tsp.platform.service.model.AppJsonResult;

/**
*@Title UpgradeResumeUtil.java
*@description 处理FOTA继续升级业务
*@time 2019年2月11日 下午2:36:45
*@author wqgzf
*@version 1.0
**/
@Service
@Scope(value = "prototype")
public class UpgradeResumeUtil extends BaseUtilProc{
	
	@Autowired
    private DataProcessing dataProcessing;

    public UpgradeResumeUtil(RedisAPI redisAPI) {
        super(redisAPI);
        this.redisAPI = redisAPI;
    }
    
	private static final Logger logger = LogManager.getLogger(UpgradeResumeUtil.class);

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

    //Tbox相关接口
    @Autowired
    private TboxService tboxService;
    
    //请求对象
    private UpgradeResumeReqInfo upgradeResumeReqInfo = new UpgradeResumeReqInfo();

    public UpgradeResumeReqInfo getUpgradeResumeReqInfo() {
        return upgradeResumeReqInfo;
    }

    // 解决对在线Tbox远控请求,回复周期内（10s）Tbox频繁登录登出造成的线程通知错误；
    private boolean tboxOnlineStatusWhenCtrl = false;

    public boolean getTboxOnlineStatusWhenCtrl() {
        return tboxOnlineStatusWhenCtrl;
    }
    
    //保存本节点处理远程控制请求的实例
    private static Hashtable<String, UpgradeResumeUtil> htUpgradeResume = new Hashtable<>();

    //获得指定tbox的处理的远程控制的实例
    public static UpgradeResumeUtil getUpgradeResume(String tboxsn) {
        return htUpgradeResume.get(tboxsn);
    }
    
    /**
     * @Description 删除指定tbox的处理的远程控制的实例
     * @Data 2019年2月11日下午2:46:11
     * @param tboxsn
     */
    public static void removeUpgradeResume(String tboxsn){
    	htUpgradeResume.remove(tboxsn);
    }
    
    /**
     * @Description  创建版本继续升级业务处理类
     * @Data 2019年2月11日下午2:47:17
     * @return
     */
    public UpgradeResumeUtil cloneUtil() {
    	UpgradeResumeUtil upgradeResumeUtil = new UpgradeResumeUtil(redisAPI);
    	upgradeResumeUtil.kafkaService = this.kafkaService;
    	upgradeResumeUtil.tspPlatformClient = this.tspPlatformClient;
    	upgradeResumeUtil.tboxService = this.tboxService;
        return upgradeResumeUtil;
    }
    
    /**
     * @Description 开始进行继续升级操作的参数验证,验证完成后进行后续的控制操作
     * @Data 2019年2月11日下午2:55:12
     * @param upgradeResumeReqInfo
     * @return
     */
    public AppJsonResult validUpgradeResume(UpgradeResumeReqInfo upgradeResumeReqInfo) {
    	String serialNumber = upgradeResumeReqInfo.getSn();
    	if (serialNumber == null) {
    		 logger.warn("当前TBox进行继续升级操作过程中参数sn为空!");
             return new AppJsonResult(ResultStatus.SN_NULL, "");
    	}
        if (!dataProcessing.isTboxValid(serialNumber)) {
            logger.warn("TBox(SN:{})继续升级请求时sn不合法，redis与数据库中未检索到!", serialNumber);
            return new AppJsonResult(ResultStatus.SN_ERROR, "");
        }
        //校验id
        if (upgradeResumeReqInfo.getId() == null) {
            logger.warn("TBox(SN:{})继续升级请求时参数id为空!", serialNumber);
            return new AppJsonResult(ResultStatus.UPGRADE_RESUME_ID_ERROR, "");
        }
        //检验seqNo
        String seqNo = upgradeResumeReqInfo.getSeqNo();
        if (StringUtils.isBlank(seqNo)) {
            logger.warn("TBox(SN:{})执行继续升级请求时seqNo为空!", serialNumber);
            return new AppJsonResult(ResultStatus.SEQ_NO_NULL, "");
        }
        if (seqNo.length() != FOTAConstant.SEQ_NO_LENGTH || !seqNo.matches("^[0-9]*$")) {
            logger.warn("TBox(SN:{})执行继续升级请求时seqNo不符合规定!", serialNumber);
            return new AppJsonResult(ResultStatus.SEQ_NO_ERROR, "");
        }
        //校验eventTime
        if (upgradeResumeReqInfo.getEventTime() == 0) {
            logger.warn("TBox(SN:{})执行继续升级请求时eventTime为空!", serialNumber);
            return new AppJsonResult(ResultStatus.EVENT_TIME_NULL, "");
        }
		return null;
    }
    
    /**
     * 根据操作删除相应唤醒指令
     * @param tboxsn
     * @param operName
     */
    public void removeWakeUp(String tboxsn, String operName) {
        try {
            redisAPI.delete(RedisConstant.WAKE_UP + "_" + operName + "_" + tboxsn);
        } catch (Exception ex) {
            logger.error("Redis connection error,TBox({}) can't do removeWakeUp:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
    }
    
    /**
     * 添加实时位置信息
     *
     * @Title: addWakeUp
     * @Description: 根据操作指令添加相应唤醒记录
     * @param: @param
     *             tboxsn
     * @param: @param
     *             reportPos
     * @return: void
     * @throws @author
     *             余佶
     * @Date 2017年8月13日 下午3:04:49
     */
    public void addWakeUp(String tboxsn, String operName) {
        // 设置90s超时
        try {
            Date operTime = new Date();
            redisAPI.setValue(RedisConstant.WAKE_UP + "_" + operName + "_" + tboxsn, String.valueOf(operTime.getTime()),
                    OperationConstant.WAKEUP_WAIT_TIME_SEC, TimeUnit.SECONDS);
        } catch (Exception ex) {
            logger.error("Redis connection error,TBox({}) can't do addWakeUp:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
    }
    
    /**
     * @Description 开始继续升级业务
     * @Data 2019年2月11日下午2:55:50
     * @param upgradeResumeReqInfo
     * @return
     */
    public AppJsonResult doUpgradeResume(UpgradeResumeReqInfo upgradeResumeReqInfo) {
    	//判断是否需要唤醒
        boolean doWakeUp;
        String serialNumber = upgradeResumeReqInfo.getSn();
        String eventTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        // String vin = tboxService.getVINForTbox(serialNumber);
//        if (vin == null) {
//            logger.warn("TBox(SN:{})对应的vin号为空!", serialNumber);
//        } else {
//            logger.info("TBox(SN:{})对应的vin号为:{}", vin);
//        }
        //检查当前TBox状态，确保TBox控制指令唯一且TBox在线
        if (dataProcessing.isFotaCtrlExist(serialNumber,OperationConstant.FOTA_UPGRADE_RESUME)) {
        	logger.warn("TBox(SN:{})已经存在控制指令, 或者当前TBox正处于唤醒中!", serialNumber);
            return new AppJsonResult(ResultStatus.RM_TBOX_HAVE_ACCEPT_ONE_COMMAND, "");            
        } else{
        	//存入需要执行版本升级的控制指令信息
        	htUpgradeResume.put(serialNumber, UpgradeResumeUtil.this);
        	doWakeUp = !redisAPI.hasKey(RedisConstant.ONLINE_TBOX, serialNumber);
        	if (doWakeUp) {
                //TBox不在线, 需要进行唤醒
        		dataProcessing.checkTBoxLogOutForWakeupWait(serialNumber);
        		addWakeUp(serialNumber, OperationConstant.FOTA_UPGRADE_RESUME);
        		logger.info("[继续升级]TBox(SN:{})离线唤醒可以执行!", serialNumber);
                logger.info("[继续升级]TBox(SN:{})开始离线唤醒, 异步发送唤醒短信等待返回结果中...", serialNumber);
                if (!dataProcessing.isSendingMessageSucceed(serialNumber)) {
                    logger.warn("[继续升级]TBox(SN:{})短信唤醒失败!", serialNumber);
                    //移除redis中的唤醒指令
                    removeWakeUp(serialNumber, OperationConstant.FOTA_UPGRADE_RESUME);
                    //移除内存中的控制指令
                    htUpgradeResume.remove(serialNumber);
                    return new AppJsonResult(ResultStatus.RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED, "");
                } else {
                	logger.info("[继续升级]TBOx(SN:{})短信发送成功, 开始等待TBox上线...", serialNumber);
                    long startTime;
                    try {
                        startTime = System.currentTimeMillis();
                        synchronized (UpgradeResumeUtil.this) {
                            UpgradeResumeUtil.this.wait(OperationConstant.WAKEUP_WAIT_TIME);
                        }
                    } catch (Exception e) {
                        // 等待唤醒过程中发生异常, 需要删除控制指令
                        logger.error("TBox(SN:{})执行继续升级操作因发生异常而失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
                        htUpgradeResume.remove(serialNumber);
                        return new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, "");
                    } finally {
                        //不论唤醒结果, 清除redis中的唤醒指令
                        removeWakeUp(serialNumber, OperationConstant.FOTA_VERSION_UPGRADE);
                    }
                    if (System.currentTimeMillis() - startTime >= OperationConstant.WAKEUP_WAIT_TIME) {
                        logger.warn("TBox(SN:{})执行唤醒后未上线, 当前继续升级操作失败, 请求时间:{}", serialNumber, eventTime);
                        //如果唤醒超时, 删除内存中的唤醒指令
                        htUpgradeResume.remove(serialNumber);
                        return new AppJsonResult(ResultStatus.RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG, "");
                    } else {
                        logger.info("TBox(SN:{})通过唤醒上线, 开始执行继续升级指令!", serialNumber);
                        try {
                            return doWhenTBoxOnline(upgradeResumeReqInfo, eventTime);
                        } catch (Exception e) {
                            logger.error("TBox(SN:{})唤醒后处理继续升级指令因发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
                            return new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, "");
                        }
                    }
                }
        	} else {
                //TBox在线直接处理
                logger.info("TBox(SN:{})在线, 可以执行当前继续升级指令!", serialNumber);
                try {
                    return doWhenTBoxOnline(upgradeResumeReqInfo, eventTime);
                } catch (Exception e) {
                    logger.error("TBox(SN:{})在线时处理继续升级指令因发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
                    return new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, "");
                }
            }
        }
    }

    /**
     * @Title: 根据操作指令确认是否有该指令引起的唤醒操作
     * @param tboxSn
     * @return
     * @author zhuna
     * @date 2018年12月4日
     */
    public boolean existWakeUp(String tboxSn) {
        return redisAPI.hasKey(RedisConstant.WAKE_UP + "_" + tboxSn);
    }
    
    /**
     * @Description 在Redis中添加一条FOTA控制指令
     * 				通用方法
     * 				key:FOTA_REMOTE_CTRL
     * @Date 2019/1/22 19:46
     * @Param [serialNumber, operationName]
     * @return boolean
     **/
    public void addFotaCtrl(String serialNumber, String operationName) {
        try {
            redisAPI.setHash(RedisConstant.FOTA_REMOTE_CTRL_REQ, operationName + serialNumber, serialNumber);
        } catch (Exception ex) {
            logger.error("Redis连接异常, TBox(SN:{})在Redis中添加一条控制指令因异常失败:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
    }
    
    /**
     * @Title: 根据操作指令确认是否有该指令引起的唤醒操作
     * @Description: 获取实时位置信息
     * @param: @param
     *             tboxSn
     * @param: @return
     * @return: ReportPos
     * @throws @author
     *             fogmk
     * @Date 2017年7月18日 下午3:04:37
     */
    public boolean existWakeUp(String tboxSn, String operName) {
        return redisAPI.hasKey(RedisConstant.WAKE_UP + "_" + operName + "_" + tboxSn);
    }
    
	private AppJsonResult doWhenTBoxOnline(UpgradeResumeReqInfo upgradeResumeReqInfo, String eventTime) {
		String serialNumber = upgradeResumeReqInfo.getSn();
        //用来存储TBox对请求的响应结果
        String tBoxUpResp;
        
        //在线时的正常处理业务流程
        try {
        	this.tboxOnlineStatusWhenCtrl = true;
        	logger.info("TBox(SN:{})将本次继续升级的控制指令存储在Redis中!", serialNumber);
        	//确定TBox在线, 将版本继续升级指令存入redis
        	logger.info("TBox(SN:{})确定当前TBox在线, 将本次继续升级控制指令存储至Redis!", serialNumber);
        	addFotaCtrl(serialNumber, OperationConstant.FOTA_UPGRADE_RESUME);
        	//确定TBox在线, 再次清除redis中的唤醒指令
        	if (existWakeUp(serialNumber)) {
                logger.info("TBox(SN:{})当前TBox在线, 并且redis中存在唤醒指令, 清除唤醒指令(key:sn)!");
                redisAPI.delete(RedisConstant.WAKE_UP + "_" + serialNumber);
            }
            if (existWakeUp(serialNumber, OperationConstant.FOTA_UPGRADE_RESUME)) {
                logger.info("TBox(SN:{})当前TBox在线, 并且redis中存在唤醒指令, 清除唤醒指令(key:WAKE_UP+*)!", serialNumber);
                removeWakeUp(serialNumber, OperationConstant.FOTA_UPGRADE_RESUME);
            }
            //通过网关的kafka发送一条消息, 用作内部通信
            //默认下发时间
            String downTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            kafkaService.sndMesForTemplate(KafkaMsgConstantFota.TOPIC_SELF_UPGRADE_RESUME_DOWN, serialNumber + "_" + JSONObject.toJSONString(upgradeResumeReqInfo) + "_" + downTime, serialNumber);
            //发送版本升级控制指令后, 等待10s的回包时间
            long startTime = System.currentTimeMillis();
            tboxOnlineStatusWhenCtrl = true;
            synchronized (UpgradeResumeUtil.this) {
                //等待10s
                UpgradeResumeUtil.this.wait(OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME);
            }
            if (System.currentTimeMillis() - startTime >= OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME) {
                //超过10s回复结果
                logger.warn("TBox(SN:{})继续升级失败, TBox没有及时回复继续升级报文。请求下发时间:{}", serialNumber, downTime);
                return new AppJsonResult(ResultStatus.RM_TBOX_NOT_REPLY_WITHIN_LIMIT_TIME, "");
            } else {
                tBoxUpResp = htUpgradeResume.get(serialNumber).result;
                UpgradeResumeRespInfo upgradeResumeRespInfo = JSONObject.parseObject(tBoxUpResp, UpgradeResumeRespInfo.class);
                logger.info("TBox(SN:{})已经回复继续升级结果:{}", serialNumber, JSONObject.toJSONString(upgradeResumeRespInfo));
                return new AppJsonResult(ResultStatus.SUCCESS, upgradeResumeRespInfo);
            }            
        } catch (Exception e) {
        	logger.error("TBox(SN:{})继续升级操作因发生异常失败。异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            return new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, "");
        }finally {
            try {
                //移除内存以及redis中的版本查询指令
                htUpgradeResume.remove(serialNumber);

                if (existFotaCtrl(serialNumber, OperationConstant.FOTA_UPGRADE_RESUME)) {
                    removeFotaCtrl(serialNumber, OperationConstant.FOTA_UPGRADE_RESUME);
                }
            } catch (Exception e) {
                logger.error("TBox(SN:{})清除继续升级控制指令因发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            }
        }
	}
	
	/**
     * @return boolean
     * @Description 判断Redis中是否存在同一条Fota控制指令
     * 				通用方法
     * @Date 2019/1/22 18:01
     * @Param [serialNumber, operationName]
     **/
    public boolean existFotaCtrl(String serialNumber, String operationName) {
        try {
            return redisAPI.hasKey(RedisConstant.FOTA_REMOTE_CTRL_REQ, operationName + serialNumber);
        } catch (Exception ex) {
            logger.error("Redis连接异常, TBox(SN:{})判断Redis中是否存在FOTA控制指令因异常失败:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(ex));
            return false;
        }
    }
	
	/**
     * @Description 从Redis中删除一条Fota控制指令
     * 				通用方法
     * @Date 2019/1/25 16:34
     * @Param [serialNumber, operationName]
     * @return void
     **/
    public void removeFotaCtrl(String serialNumber, String operationName) {
        try {
            redisAPI.removeHash(RedisConstant.FOTA_REMOTE_CTRL_REQ, operationName + serialNumber);
        } catch (Exception ex) {
            logger.error("Redis连接异常, TBox(SN:{})删除redis中一条控制指令因异常失败:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
    }
    
}
