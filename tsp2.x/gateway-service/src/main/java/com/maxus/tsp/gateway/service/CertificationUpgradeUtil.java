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
import com.maxus.tsp.gateway.common.model.fota.CertificationUpgradeReqInfo;
import com.maxus.tsp.gateway.common.model.fota.CertificationUpgradeRespInfo;
import com.maxus.tsp.gateway.mq.kafka.KafkaProducer;
import com.maxus.tsp.gateway.serviceclient.TspPlatformClient;
import com.maxus.tsp.platform.service.model.AppJsonResult;

/**
 * @ClassName CertificationUpgradeUtil
 * @Description 处理FOTA证书更新业务
 * @Author ssh
 * @Date 2019/1/31 16:58
 * @Version 1.0
 **/
@Service
@Scope(value = "prototype")
public class CertificationUpgradeUtil extends BaseUtilProc {
    private static final Logger logger = LogManager.getLogger(CertificationUpgradeUtil.class);

    @Autowired
    private DataProcessing dataProcessing;
    
    //kafka相关接口
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

    public CertificationUpgradeUtil(RedisAPI redisAPI) {
        super(redisAPI);
        this.redisAPI = redisAPI;
    }

    //请求对象
    private CertificationUpgradeReqInfo certificationUpgradeReqInfo = new CertificationUpgradeReqInfo();

    public CertificationUpgradeReqInfo getCertificationUpgradeReqInfo() {
        return certificationUpgradeReqInfo;
    }

    //解决对在线Tbox远控请求，回复周期内(10s)Tbox频繁登陆登出造成的线程通知错误
    private boolean tboxOnlineStatusWhenCtrl = false;

    public boolean getTboxOnlineStatusWhenCtrl() {
        return tboxOnlineStatusWhenCtrl;
    }

    /**
     * 保存本节点处理远程控制请求的实例
     */
    private static Hashtable<String, CertificationUpgradeUtil> htCertificationUpgrade = new Hashtable<>();

    /**
     * 获得指定Tbox处理远程控制请求的实例
     */

    public static CertificationUpgradeUtil getCertificationUpgrade(String tboxsn) {
        return htCertificationUpgrade.get(tboxsn);
    }

    /**
     * 删除指定tbox的处理的远程控制的实例
     *
     * @param tboxsn
     * @author zhuna
     * @date 2018年11月29日
     */
    public static void removeCertificationUpgrade(String tboxsn) {
        htCertificationUpgrade.remove(tboxsn);
    }

    /**
     * @return com.maxus.tsp.gateway.service.CertificationUpgradeUtil
     * @Description 创建证书更新业务处理类
     * @Date 2019/2/1 10:27
     * @Param []
     **/
    public CertificationUpgradeUtil cloneUtil() {
        CertificationUpgradeUtil certificationUpgradeUtil = new CertificationUpgradeUtil(redisAPI);
        certificationUpgradeUtil.kafkaService = this.kafkaService;
        certificationUpgradeUtil.tspPlatformClient = this.tspPlatformClient;
        certificationUpgradeUtil.tboxService = this.tboxService;
        return certificationUpgradeUtil;
    }

    /**
     * @return com.maxus.tsp.platform.service.model.AppJsonResult
     * @Description 证书更新参数校验
     * @Date 2019/2/1 10:27
     * @Param [certificationUpgradeReqInfo]
     **/
    public AppJsonResult validCertificationUpgrade(CertificationUpgradeReqInfo certificationUpgradeReqInfo) {
        String serialNumber = certificationUpgradeReqInfo.getSn();
        if (serialNumber == null) {
            logger.warn("当前TBox进行证书更新操作过程中参数Sn为空！");
            return new AppJsonResult(ResultStatus.SN_NULL, "");
        }
        if (serialNumber.length() != FOTAConstant.SERIAL_NUMBER_LENGTH) {
            logger.warn("TBox(SN:{})证书更新请求时sn号不正确，请检查!", serialNumber);
            return new AppJsonResult(ResultStatus.SN_ERROR, "");
        }

        //校验cmd
        if (certificationUpgradeReqInfo.getCmd() == null) {
            logger.warn("TBox(SN:{})证书更新请求时参数cmd为空！", serialNumber);
            return new AppJsonResult(ResultStatus.CERTIFICATE_CMD_NULL, "");
        } else if (!certificationUpgradeReqInfo.getCmd().equals("tbox") && !certificationUpgradeReqInfo.getCmd().equals("avn")) {
            logger.warn("TBox(SN:{})请求证书更新指令cmd无效, 不为avn或tbox!！", serialNumber);
            return new AppJsonResult(ResultStatus.CERTIFICATE_CMD_ERROR, "");
        }

        //校验type
        if (certificationUpgradeReqInfo.getType() == null) {
            logger.warn("TBox(SN:{})证书更新请求时参数type为空！", serialNumber);
            return new AppJsonResult(ResultStatus.CERTIFICATE_TYPE_NULL, "");
        } else if (certificationUpgradeReqInfo.getType() != 0 && certificationUpgradeReqInfo.getType() != 1 && certificationUpgradeReqInfo.getType() != 2) {
            logger.warn("TBox(SN:{})请求证书更新证书类型type为无效值!", serialNumber);
            return new AppJsonResult(ResultStatus.CERTIFICATE_TYPE_ERROR, "");
        }

        //校验size
        if (2*certificationUpgradeReqInfo.getSize() != certificationUpgradeReqInfo.getCertification().length()) {
            logger.warn("TBox(SN:{}请求证书更新后台证书url长度与size不符!");
            return new AppJsonResult(ResultStatus.CERTIFICATE_SIZE_ERROR,"");
        }

        //校验certification
        if (certificationUpgradeReqInfo.getCertification() == null) {
            logger.warn("TBox(SN:{})证书更新请求时参数certification为空！", serialNumber);
            return new AppJsonResult(ResultStatus.CERTIFICATE_URL_NULL, "");
        }
        if (!certificationUpgradeReqInfo.getCertification().matches("^[0-9A-Fa-f]+$")) {
            logger.warn("TBox(SN:{})请求证书更新后台证书url格式不是16进制报文!",serialNumber);
            return new AppJsonResult(ResultStatus.CERTIFICATE_URL_ERROR,"");
        }

        //检验seqNo
        String seqNo = certificationUpgradeReqInfo.getSeqNo();
        if (StringUtils.isBlank(seqNo)) {
            logger.warn("TBox(SN:{})证书更新请求时seqNo为空！", seqNo);
            return new AppJsonResult(ResultStatus.SEQ_NO_NULL, "");
        }
        if (seqNo.length() != FOTAConstant.SEQ_NO_LENGTH || !seqNo.matches("^[0-9]*$")) {
            logger.warn("TBox(SN:{})执行证书更新请求时seqNo长度不为22或内容不全为数字!", serialNumber);
            return new AppJsonResult(ResultStatus.SEQ_NO_ERROR, "");
        }

        //校验eventTime
        if (certificationUpgradeReqInfo.getEventTime() == 0) {
            logger.warn("TBox(SN:{})执行证书更新请求时eventTime为空!", serialNumber);
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
    	redisAPI.delete(RedisConstant.WAKE_UP + "_" + operName + "_" + tboxsn);
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
     * @return com.maxus.tsp.platform.service.model.AppJsonResult
     * @Description 开始执行证书更新业务
     * @Date 2019/2/1 14:11
     * @Param [certificationUpgradeReqInfo]
     **/
    public AppJsonResult doCertificationUpgrade(CertificationUpgradeReqInfo certificationUpgradeReqInfo) {
        //判断是否需要唤醒
        boolean doWakeUp;
        String serialNumber = certificationUpgradeReqInfo.getSn();
        String eventTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        // String vin = tboxService.getVINForTbox(serialNumber);
//        if (vin == null) {
//            logger.warn("TBox(SN:{})对应的vin号为空!", serialNumber);
//        } else {
//            logger.info("TBox(SN:{})对应的vin号为:{}", serialNumber, vin);
//        }

        //检查当前TBox状态，确保TBox控制指令唯一且TBox在线
        if (dataProcessing.isFotaCtrlExist(serialNumber, OperationConstant.FOTA_CERTIFICATION_UPGRADE)) {
            //TBox存在控制指令或正处于唤醒中
            logger.warn("TBox(SN:{})已经存在控制指令, 或者当前TBox正处于唤醒中!", serialNumber);
            return new AppJsonResult(ResultStatus.RM_TBOX_HAVE_ACCEPT_ONE_COMMAND, "");
        } else {
            //存入需要执行证书更新的控制指令信息
            htCertificationUpgrade.put(serialNumber, CertificationUpgradeUtil.this);
            doWakeUp = !redisAPI.hasKey(RedisConstant.ONLINE_TBOX, serialNumber);
            if (doWakeUp) {
                //TBox不在线，需要进行唤醒
                dataProcessing.checkTBoxLogOutForWakeupWait(serialNumber);
                addWakeUp(serialNumber, OperationConstant.FOTA_CERTIFICATION_UPGRADE);
                logger.info("[证书更新]TBox(SN:{})离线唤醒可以执行!", serialNumber);
                logger.info("[证书更新]TBox(SN:{})开始离线唤醒, 异步发送唤醒短信等待返回结果中...", serialNumber);
                if (!dataProcessing.isSendingMessageSucceed(serialNumber)) {
                    logger.warn("[证书更新]TBox(SN:{})短信唤醒失败!", serialNumber);
                    //移除redis中的唤醒指令
                    removeWakeUp(serialNumber, OperationConstant.FOTA_CERTIFICATION_UPGRADE);
                    //移除内存中的控制指令
                    htCertificationUpgrade.remove(serialNumber);
                    return new AppJsonResult(ResultStatus.RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED, "");
                } else {
                    logger.info("[证书更新]TBOx(SN:{})短信发送成功, 开始等待TBox上线...", serialNumber);
                    long startTime;
                    try {
                        startTime = System.currentTimeMillis();
                        synchronized (CertificationUpgradeUtil.this) {
                            CertificationUpgradeUtil.this.wait(OperationConstant.WAKEUP_WAIT_TIME);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        //等待唤醒过程中发生异常，需要删除控制指令
                        logger.error("TBox(SN:{})执行证书更新操作因发生异常而失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
                        htCertificationUpgrade.remove(serialNumber);
                        return new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, "");
                    } finally {
                        //不论唤醒结果，清除redis中的唤醒指令
                        removeWakeUp(serialNumber, OperationConstant.FOTA_CERTIFICATION_UPGRADE);
                    }
                    //判断唤醒是否超时，如果唤醒超时，直接通知用户
                    if (System.currentTimeMillis() - startTime >= OperationConstant.WAKEUP_WAIT_TIME) {
                        logger.warn("TBox(SN:{})执行唤醒后未上线, 当前证书更新操作失败, 请求时间:{}", serialNumber, eventTime);
                        //如果唤醒超时，删除内存中的唤醒指令
                        htCertificationUpgrade.remove(serialNumber);
                        return new AppJsonResult(ResultStatus.RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG, "");
                    } else {
                        logger.info("TBox(SN:{})通过唤醒上线, 开始继续执行证书更新指令!", serialNumber);
                        try {
                            return doWhenTBoxOnline(certificationUpgradeReqInfo, eventTime);
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error("TBox(SN:{})唤醒后处理证书更新指令因发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
                            return new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, "");
                        }
                    }
                }
            } else {
                //TBox在线直接处理
                logger.info("TBox(SN:{})在线, 可以执行当前证书更新指令!", serialNumber);
                try {
                    return doWhenTBoxOnline(certificationUpgradeReqInfo, eventTime);
                } catch (Exception e) {
                    logger.error("TBox(SN:{})在线时处理版本升级指令因发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
                    return new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, "");
                }
            }
        }
    }

    /**
     * @Description TBox在线时处理控制证书更新控制指令
     * @Date 2019/2/2 8:50
     * @Param [certificationUpgradeReqInfo, eventTime]
     * @return com.maxus.tsp.platform.service.model.AppJsonResult
     **/
    private AppJsonResult doWhenTBoxOnline(CertificationUpgradeReqInfo certificationUpgradeReqInfo, String eventTime) {
        String serialNumber = certificationUpgradeReqInfo.getSn();
        //用来存储TBox对请求的响应结果
        String tBoxUpResp;

        //在线时的正常业务处理
        try {
            this.tboxOnlineStatusWhenCtrl = true;
                logger.info("TBox(SN:{})将本次证书更新的控制指令存储在Redis中！", serialNumber);
                //确定TBox在线，将证书更新指令存入redis
                logger.info("TBox(SN:{})确定当前TBox在线,将本次证书更新指令存储至Redis!", serialNumber);
                addCommandSend(serialNumber,RedisConstant.FOTA_CERTIFICATION_UPGRADE_REQ);
                //确定TBox在线，再次清除redis中的唤醒指令
                if (existWakeUp(serialNumber)) {
                    logger.info("TBox(SN:{})当前TBox在线,并且redis中存在唤醒指令，清除唤醒指令(key:sn)!");
                    redisAPI.delete(RedisConstant.WAKE_UP + "_" + serialNumber);
                    removeWakeUp(serialNumber);
                }
                //通过网关的kafka发送一条消息，用作内部通信
                //默认下发时间
                String downTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
                kafkaService.sndMesForTemplate(KafkaMsgConstantFota.TOPIC_SELF_CERTIFICATION_UPGRADE_DOWN, serialNumber + "_" + JSONObject.toJSONString(certificationUpgradeReqInfo) + "_" + downTime, serialNumber);
                //发送证书更新控制指令后,等待10s的回包时间
                long startTime = System.currentTimeMillis();
                tboxOnlineStatusWhenCtrl = true;
                synchronized (CertificationUpgradeUtil.this) {
                    //等待10s
                    CertificationUpgradeUtil.this.wait(OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME);
                }
                if (System.currentTimeMillis() - startTime >= OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME) {
                    //超过10s回复结果
                    logger.warn("TBox(SN:{})证书更新时报,TBox没有及时回复证书更新报文。请求下发时间:{}", serialNumber, downTime);
                    return new AppJsonResult(ResultStatus.RM_TBOX_NOT_REPLY_WITHIN_LIMIT_TIME, "");
                } else {
                    tBoxUpResp = htCertificationUpgrade.get(serialNumber).result;
                    CertificationUpgradeRespInfo certificationUpgradeRespInfo = JSONObject.parseObject(tBoxUpResp, CertificationUpgradeRespInfo.class);
                    logger.info("TBox(SN:{})已经回复证书更新结果:{}", serialNumber, JSONObject.toJSONString(certificationUpgradeRespInfo));
                    return new AppJsonResult(ResultStatus.SUCCESS, certificationUpgradeRespInfo.getData());
                }
        } catch (Exception e) {
            logger.error("异常原因:{}",serialNumber,ThrowableUtil.getErrorInfoFromThrowable(e));
            return new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION,"");
        }finally{
            try {
                htCertificationUpgrade.remove(serialNumber);
                    if (existFotaCtrl(serialNumber,OperationConstant.FOTA_CERTIFICATION_UPGRADE)) {
                        removeFotaCtrl(serialNumber,OperationConstant.FOTA_CERTIFICATION_UPGRADE);
                    }
            } catch (Exception e) {
                logger.error("TBox(SN:{})清除证书更新控制指令因发生异常失败，异常原因:{}",serialNumber,ThrowableUtil.getErrorInfoFromThrowable(e));
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
