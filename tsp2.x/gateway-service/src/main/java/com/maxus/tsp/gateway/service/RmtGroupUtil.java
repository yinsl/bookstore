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

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.KafkaMsgConstant;
import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.enums.ResultStatus;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.model.RmtGroupRequestInfo;
import com.maxus.tsp.gateway.common.model.RmtGroupRespInfo;
import com.maxus.tsp.gateway.mq.kafka.KafkaProducer;
import com.maxus.tsp.gateway.serviceclient.TspPlatformClient;
import com.maxus.tsp.platform.service.model.AppJsonResult;

/**
 * @ClassName RmtGroupUtil
 * @Description 组合远控业务处理类
 * @Author zijhm
 * @Date 2019/2/1 8:59
 * @Version 1.0
 **/
@Service
@Scope(value = "prototype")
public class RmtGroupUtil extends BaseUtilProc{

    private static final Logger logger = LogManager.getLogger(RmtGroupUtil.class);

    @Autowired
    private DataProcessing dataProcessing;
    
    //Kafka相关接口
    @Autowired
    private KafkaProducer kafkaService;
    //数据库相关接口
    @Autowired
    private TspPlatformClient tspPlatformClient;
    //Tbox相关接口
    @Autowired
    private TboxService tboxService;

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

    public RmtGroupUtil(RedisAPI redisAPI) {
        super(redisAPI);
        this.redisAPI = redisAPI;
    }

    //用来封装TBox返回的数据进行封装
    private RmtGroupRespInfo rmtGroupRespInfo;

    //组合远控请求参数的对象
    private RmtGroupRequestInfo rmtGroupRequestInfo = new RmtGroupRequestInfo();

    public RmtGroupRequestInfo getRmtGroupRequestInfo() {
        return rmtGroupRequestInfo;
    }

    // 解决对在线Tbox远控请求,回复周期内（10s）Tbox频繁登录登出造成的线程通知错误；
    private boolean tboxOnlineStatusWhenCtrl = false;

    public boolean getTboxOnlineStatusWhenCtrl() {
        return tboxOnlineStatusWhenCtrl;
    }

    //保存本节点处理远程控制请求的实例
    private static Hashtable<String, RmtGroupUtil> htRmtGroup = new Hashtable<>();

    //获得指定TBox的处理的组合远控的实例
    public static RmtGroupUtil getRmtGroupUtil(String serialNumber) {
        return htRmtGroup.get(serialNumber);
    }

    //删除指定TBox处理组合远程控制的实例
    public static void  removeRmtGroup(String serialNumber) {
        htRmtGroup.remove(serialNumber);
    }

    /**
     * @Description 实例化组合远控的业务处理类
     * @Date 2019/2/1 9:22
     * @Param []
     * @return com.maxus.tsp.gateway.service.RmtGroupUtil
     **/
    public RmtGroupUtil cloneUtil() {
        RmtGroupUtil rmtGroupUtil = new RmtGroupUtil(redisAPI);
        rmtGroupUtil.kafkaService = this.kafkaService;
        rmtGroupUtil.tspPlatformClient = this.tspPlatformClient;
        rmtGroupUtil.tboxService = this.tboxService;
        return rmtGroupUtil;
    }

    /**
     * @return com.maxus.tsp.platform.service.model.AppJsonResult
     * @Description 组合远控参数校验, 校验无误后执行组合远控
     * @Date 2019/2/1 10:35
     * @Param []
     **/
    public AppJsonResult validRmtGroup(RmtGroupRequestInfo rmtGroupRequestInfo) {
        String serialNumber = rmtGroupRequestInfo.getSn();
        if (StringUtils.isBlank(serialNumber)) {
            logger.warn("TBox当前组合远控参数sn为空!");
            return new AppJsonResult(ResultStatus.PARAM_NULL, "");
        }
        if (!dataProcessing.isTboxValid(serialNumber)) {
            logger.warn("TBox(SN:{})当前组合远控请求参数sn不可用!", serialNumber);
            return new AppJsonResult(ResultStatus.CAR_INFO_ERROR, "");
        }
        String otaType = rmtGroupRequestInfo.getOtaType();
        if (StringUtils.isBlank(otaType)) {
            logger.warn("TBox(SN:{})当前组合远控请求参数otaType为空!", serialNumber);
            return new AppJsonResult(ResultStatus.PARAM_NULL, "");
        }
        if (!"REMOTECTRL".equals(otaType)&&!"REMOTECTRL_EXT".equals(otaType)) {
            logger.warn("TBox(SN:{})当前组合控制请求参数otaType错误!", serialNumber);
            return new AppJsonResult(ResultStatus.PARAM_ERROR, "");
        }
        if("REMOTECTRL".equals(otaType)) {
        	String comd = rmtGroupRequestInfo.getComd();
        	if (StringUtils.isBlank(comd)) {
        		logger.warn("TBox(SN:{})当前组合控制请求参数comd为空!", serialNumber);
        		return new AppJsonResult(ResultStatus.PARAM_NULL, "");
        	}
	        if (comd.length() < 2 || comd.length() > 4 || !comd.matches("[a-fA-F0-9]+")) {
	            logger.warn("TBox(SN:{})当前组合请求参数comd错误!", serialNumber);
	            return new AppJsonResult(ResultStatus.PARAM_ERROR, "");
	        }
	        String value = rmtGroupRequestInfo.getValue();
	        if (StringUtils.isBlank(value)) {
	            logger.warn("TBox(SN:{})当前组合远控请求参数value为空!", serialNumber);
	            return new AppJsonResult(ResultStatus.PARAM_NULL, "");
	        }
            if (value.length() < 2 || value.length() > 4 || !value.matches("[a-fA-F0-9]+") || value.length() % 2 != 0) {
                logger.warn("TBox(SN:{})当前组合远控请求参数value错误!", serialNumber);
                return new AppJsonResult(ResultStatus.PARAM_ERROR, "");
            }
	        String temperature = rmtGroupRequestInfo.getTemperature();
	        if (!StringUtils.isBlank(temperature)) {
	            if (temperature.length() != 2 || !temperature.matches("[a-fA-F0-9]+")) {
	                logger.warn("TBox(SN:{})当前组合远控请求参数temperature错误!", serialNumber);
	                return new AppJsonResult(ResultStatus.PARAM_ERROR, "");
	            }
	        }
        } else if ("REMOTECTRL_EXT".equals(otaType)) {
        	int parameSize = rmtGroupRequestInfo.getParamSize();
        	if (parameSize == 0 ) {
        		logger.warn("TBox(SN:{})当前组合控制请求参数parameSize为空!", serialNumber);
        		return new AppJsonResult(ResultStatus.PARAM_NULL, "");
        	} else {
	        	String param = rmtGroupRequestInfo.getParam();
		        if (StringUtils.isBlank(param)) {
		            logger.warn("TBox(SN:{})当前组合远控请求参数param为空!", serialNumber);
		            return new AppJsonResult(ResultStatus.PARAM_NULL, "");
		        }
		        if (param.length()%2!=0||!param.matches("[a-fA-F0-9]+")||param.length()!=parameSize*2) {
		            logger.warn("TBox(SN:{})当前组合远控请求参数param错误!", serialNumber);
		            return new AppJsonResult(ResultStatus.PARAM_ERROR, "");
		        }
        	}
        }
        String seqNo = rmtGroupRequestInfo.getSeqNo();
        if (StringUtils.isBlank(seqNo)) {
            logger.warn("TBox(SN:{})当前组合远控请求seqNo为空!", serialNumber);
            return new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
        }
        if (rmtGroupRequestInfo.getEventTime() == 0) {
            logger.warn("TBox(SN:{})当前组合远控请求参数eventTime为0!", serialNumber);
            return new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
        }
        return doRmtGroup(rmtGroupRequestInfo);
    }

    /**
     * @Description 参数校验通过后执行组合远控
     * @Date 2019/2/1 14:07
     * @Param [rmtGroupRequestInfo]
     * @return com.maxus.tsp.platform.service.model.AppJsonResult
     **/
    private AppJsonResult doRmtGroup(RmtGroupRequestInfo rmtGroupRequestInfo) {
        //标记TBox是否需要唤醒
        boolean doWakeUp;
        String serialNumber = rmtGroupRequestInfo.getSn();
        String eventTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        //检查当前TBox的状态, 确保当前TBox控制指令唯一且TBox在线
        if (dataProcessing.isRmtGroupCtrlExist(serialNumber)) {
            //TBox存在控制指令或正处于唤醒中
            logger.warn("TBox(SN:{})已经存在控制指令, 或者当前TBox正处于唤醒中!", serialNumber);
            return new AppJsonResult(ResultStatus.RM_TBOX_HAVE_ACCEPT_ONE_COMMAND, "");
        } else {
            //将当前组合远控的控制信息存入内存
            htRmtGroup.put(serialNumber, RmtGroupUtil.this);
            
            //TODO:
            redisAPI.setValue(RedisConstant.CAR_HOME_CTRL_REQ, rmtGroupRequestInfo.toString());
            doWakeUp = !redisAPI.hasKey(RedisConstant.ONLINE_TBOX, serialNumber);
            if (doWakeUp) {
                //TBox不在线, 需要进行唤醒
                dataProcessing.checkTBoxLogOutForWakeupWait(serialNumber);
                //在redis中存入当前TBox的唤醒指令
                addWakeUp(serialNumber, RedisConstant.RM_GROUP_CTRL);
                logger.info("[组合远控]TBox(SN:{})离线唤醒可以执行!", serialNumber);
                logger.info("[组合远控]TBox(SN:{})开始离线唤醒, 异步发送唤醒短信等待返回结果中...", serialNumber);
                if (!dataProcessing.isSendingMessageSucceed(serialNumber)) {
                    logger.warn("[组合远控]TBox(SN:{})短信唤醒失败!", serialNumber);
                    //移除redis中的唤醒指令
                    removeWakeUp(serialNumber);
                    //移除内存中的控制指令
                    htRmtGroup.remove(serialNumber);
                    return new AppJsonResult(ResultStatus.RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED, "");
                } else {
                    logger.info("[组合远控]TBox(SN:{})唤醒短信发送成功!", serialNumber);
                    long startTime = System.currentTimeMillis();
                    try {
                        synchronized (RmtGroupUtil.this) {
                            RmtGroupUtil.this.wait(OperationConstant.WAKEUP_WAIT_TIME);
                        }
                    } catch (Exception e) {
                        //等待唤醒的过程中发生异常, 需要删除内存中的控制指令
                        logger.error("TBox(SN:{})执行组合远控操作, 等待唤醒过程中发生异常而失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
                        htRmtGroup.remove(serialNumber);
                    } finally {
                        //不论唤醒成功还是失败, 清除redis中的唤醒指令
                        removeWakeUp(serialNumber);
                    }
                    //判断唤醒是否超时
                    if (System.currentTimeMillis() - startTime >= OperationConstant.WAKEUP_WAIT_TIME) {
                        logger.warn("TBox(SN:{})执行唤醒后未上线, 当前组合远控请求执行失败, 请求时间:{}", serialNumber, eventTime);
                        //唤醒超时, 删除内存中的控制指令
                        htRmtGroup.remove(serialNumber);
                        return new AppJsonResult(ResultStatus.RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG, "");
                    } else {
                        logger.info("TBox(SN:{})通过唤醒上线, 开始执行组合远控指令!", serialNumber);
                        try {
                            return doWhenTBoxOnline(rmtGroupRequestInfo);
                        } catch (Exception e) {
                            logger.error("TBox(SN:{})唤醒上线后执行组合远控因发生异常而失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
                            return new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, "");
                        }
                    }
                }
            } else {
                //TBox在线, 直接处理当前组合远控的请求
                logger.info("TBox(SN:{})在线, 可以执行当前组合远控的请求!", serialNumber);
                try {
                    return doWhenTBoxOnline(rmtGroupRequestInfo);
                } catch (Exception e) {
                    logger.error("TBox(SN:{})在线直接执行组合远控因发生异常而失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
                    return new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, "");
                }
            }
        }
    }

    /**
     * @Description TBox在线或唤醒后执行当前的远程控制指令
     * @Date 2019/2/4 16:02
     * @Param [rmtGroupRequestInfo]
     * @return com.maxus.tsp.platform.service.model.AppJsonResult
     **/
    private AppJsonResult doWhenTBoxOnline(RmtGroupRequestInfo rmtGroupRequestInfo) {
        String serialNumber = rmtGroupRequestInfo.getSn();
        //用来存储TBox请求的响应结果
        String tBoxUpResp;
        //在线时的正常处理业务流程
        try {
            this.tboxOnlineStatusWhenCtrl = true;
            logger.info("TBox(SN:{})确定当前TBox在线, 将本次控制指令存储在Redis中!", serialNumber);
            addCommandSend(serialNumber, RedisConstant.RM_GROUP_CTRL);
            //当前TBox在线, 判断redis中是否存在唤醒指令
            if (existWakeUp(serialNumber)) {
                logger.info("TBox(SN:{})在线并且redis中存在唤醒指令, 清除唤醒指令!", serialNumber);
                removeWakeUp(serialNumber);
            }
            //通过网关kafka发送一条消息, 用作内部通信
            //默认下发时间
            String downTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            if(rmtGroupRequestInfo.getOtaType().equals("REMOTECTRL")) {
                addRmtGroupTopic(serialNumber, OperationConstant.RM_GROUP_CTRL, KafkaMsgConstant.TOPIC_SELF_RM_GROUP_DOWN);
                kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_SELF_RM_GROUP_DOWN, serialNumber + "_" + JSONObject.toJSONString(rmtGroupRequestInfo) + "_" + downTime, serialNumber);
            } else if(rmtGroupRequestInfo.getOtaType().equals("REMOTECTRL_EXT")){
                addRmtGroupTopic(serialNumber, OperationConstant.RM_GROUP_CTRL, KafkaMsgConstant.TOPIC_SELF_RM_EXT_DOWN);
                kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_SELF_RM_EXT_DOWN, serialNumber + "__" + JSONObject.toJSONString(rmtGroupRequestInfo) + "__" + downTime, serialNumber);
            }
            //发送组合远控指令后, 等待10s的回包时间
            long startTime = System.currentTimeMillis();
            tboxOnlineStatusWhenCtrl = true;
            synchronized (RmtGroupUtil.this) {
                //等待10s
                RmtGroupUtil.this.wait(OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME);
            }
            if (System.currentTimeMillis() - startTime >= OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME) {
                //超过10s回复结果
                logger.warn("TBox(SN:{})组合远控失败, TBox没有及时回复组合远控报文。请求下发时间:{}", serialNumber, downTime);
                removeRmtGroupTopic(serialNumber, OperationConstant.RM_GROUP_CTRL);
                return new AppJsonResult(ResultStatus.RM_TBOX_NOT_REPLY_WITHIN_LIMIT_TIME, "");
            } else {
                //TBox及时回复报文
                tBoxUpResp = htRmtGroup.get(serialNumber).result;
                rmtGroupRespInfo = JSONObject.parseObject(tBoxUpResp, RmtGroupRespInfo.class);
                logger.info("TBox(SN:{})已经回复远程组合控制结果:{}", serialNumber, tBoxUpResp);
                return new AppJsonResult(ResultStatus.SUCCESS, rmtGroupRespInfo);
            }
        } catch (Exception e) {
            logger.error("TBox(SN:{})组合远控操作因发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            return new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, "");
        }finally {
            try {
                htRmtGroup.remove(serialNumber);
                removeRmtGroupTopic(serialNumber, OperationConstant.RM_GROUP_CTRL);
                if (existCommandSend(serialNumber)) {
                    removeCommandSend(serialNumber);
                }
            } catch (Exception e) {
                logger.error("TBox(SN:{})清除组合远控信息因发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            }
        }
    }
    
    /**
     * @return void
     * @Description 将远程组合控制的网关下发topic存入到redis
     * @Date 2019/1/30 8:39
     * @Param []
     **/
    public void addRmtGroupTopic(String serialNumber, String operationName, String topic) {
        try {
            boolean result = redisAPI.setValue(operationName + serialNumber, topic);
            logger.debug("TBox(SN:{})添加组合远控topic到redis, Key:{}, Value:{}, 添加结果:{}", serialNumber, operationName + serialNumber, topic, result);
        } catch (Exception e) {
            logger.error("TBox(SN:{})保存远程组合控制topic到redis中发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
        }
    }
    
    /**
     * @return void
     * @Description 删除redis中的远程组合控制topic
     * @Date 2019/1/30 8:39
     * @Param []
     **/
    public void removeRmtGroupTopic(String serialNumber, String operationName) {
        try {
            boolean result = redisAPI.delete(operationName + serialNumber);
            logger.debug("TBox(SN:{})删除redis中对应的组合远控的topic, Key:{}, result:{}", serialNumber, operationName + serialNumber, result);
        } catch (Exception e) {
            logger.error("TBox(SN:{})删除Redis中远程组合控制topic发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
        }
    }
    
}
