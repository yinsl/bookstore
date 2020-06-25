package com.maxus.tsp.gateway.service;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.KafkaMsgConstant;
import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.enums.ResultStatus;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.model.PoiData;
import com.maxus.tsp.gateway.common.model.PoiRespInfo;
import com.maxus.tsp.gateway.mq.kafka.KafkaProducer;
import com.maxus.tsp.gateway.serviceclient.TspPlatformClient;
import com.maxus.tsp.platform.service.model.AppJsonResult;
import com.maxus.tsp.platform.service.model.vo.ItRedisInfo;

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
 * 处理poi透传指令的业务处理类
 *
 * @Author mjrni
 */
@Service
@Scope(value = "prototype")
public class EntertainmentPOIUtil extends BaseUtilProc {

    private static final Logger logger = LogManager.getLogger(EntertainmentPOIUtil.class);

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
//    //Tbox相关接口
//    @Autowired
//    private TboxService tboxService;

    //用来存储TBox返回的结果
    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    //解决对在线Tbox远控请求,回复周期内（10s）Tbox频繁登录登出造成的线程通知错误；
    private boolean tboxOnlineStatusWhenCtrl = false;

    public boolean getTboxOnlineStatusWhenCtrl() {
        return tboxOnlineStatusWhenCtrl;
    }

    //保存本节点处理透传poi请求的实例
    private static Hashtable<String, EntertainmentPOIUtil> htForwardUtil = new Hashtable<>();

    //根据sn号获取当前节点的处理透传请求的实例
    public static EntertainmentPOIUtil getHtForwardUtil(String serialNumber) {
        return htForwardUtil.get(serialNumber);
    }

    public EntertainmentPOIUtil(RedisAPI redisAPI) {
        super(redisAPI);
        this.redisAPI = redisAPI;
    }

    /**
     * 创建处理透传的实例克隆对象
     *
     * @return
     */
    public EntertainmentPOIUtil cloneUtil() {
        EntertainmentPOIUtil entertainmentPOIUtil = new EntertainmentPOIUtil(redisAPI);
        entertainmentPOIUtil.kafkaService = this.kafkaService;
        entertainmentPOIUtil.tspPlatformClient = this.tspPlatformClient;
        return entertainmentPOIUtil;
    }

    /**
     * 对请求POI参数进行校验, 校验通过以后, 开始进行相关控制
     *
     * @param serialNumber
     * @param longitude
     * @param latitude
     * @param address
     * @return
     */
    public AppJsonResult validEntertainmentPOI(String serialNumber, int gpsType, int posType, Integer longitude, Integer latitude, String address) {
        try {
            if (StringUtils.isBlank(serialNumber)) {
                logger.warn("当前POI透传请求参数sn为空!");
                return new AppJsonResult(ResultStatus.CAR_INFO_ERROR, "");
            }
            ItRedisInfo itTboxInfo = dataProcessing.getITTboxInfo(serialNumber);
            if (itTboxInfo == null) {
                logger.warn("TBox(SN:{})当前POI透传请求参数sn在redis中不存在!", serialNumber);
                return new AppJsonResult(ResultStatus.CAR_INFO_ERROR, "");
            }
            String vin = itTboxInfo.getVin();
            if (vin == null) {
                logger.warn("TBox(SN:{})对应vin号为空!", serialNumber);
                vin = "";
            }
            if (longitude == null) {
                logger.warn("TBox(SN:{})当前POI透传请求参数longitude为空!", serialNumber);
                return new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
            }
            if (latitude == null) {
                logger.warn("TBox(SN:{})当前POI透传请求参数latitude为空!", serialNumber);
                return new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
            }
            if (StringUtils.isBlank(address)) {
                logger.warn("TBox(SN:{})当前POI透传请求参数address为空!", serialNumber);
                return new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
            }
            logger.info("TBox(SN:{})本次POI透传请求参数为vin:{}, longitude:{}, latitude:{}, address:{}", serialNumber, vin, longitude, latitude, address);
            //参数校验成功开始进行业务处理
            return doPoiConfig(serialNumber, vin, longitude, latitude, address, gpsType, posType);
        } catch (Exception e) {
            logger.error("TBox(SN:{})执行POI透传请求时发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            return new AppJsonResult(ResultStatus.FAIL, "");
        }
    }

    /**
     * 参数校验成功后, 开始进行POI透传指令的业务处理
     *
     * @param serialNumber
     * @param vin
     * @param longitude
     * @param latitude
     * @param address
     * @param gpsType
     * @param posType
     * @return
     */
    private AppJsonResult doPoiConfig(String serialNumber, String vin, Integer longitude, Integer latitude, String address, int gpsType, int posType) throws Exception {
        boolean doWakeUp;
        String eventTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        //远控排他
        //尽管sendPOI不需要进行TBox唤醒操作, 但是仍然需要对该sn进行唤醒的排他操作, 防止该sn处于其他远程控制指令的唤醒操作中
        if (existCommandSend(serialNumber) || existWakeUp(serialNumber)) {
            logger.warn("TBox(SN:{})对应远程控制指令状态:{}, 唤醒指令存在状态:{}", serialNumber, existCommandSend(serialNumber), existWakeUp(serialNumber));
            return new AppJsonResult(ResultStatus.RM_TBOX_HAVE_ACCEPT_ONE_COMMAND, "");
        }
        logger.info("TBox(SN:{})当前POI透传请求指令远控排他成功!", serialNumber);
        //存入当前需要执行的远程控制对象
        htForwardUtil.put(serialNumber, EntertainmentPOIUtil.this);
        doWakeUp = !redisAPI.hasKey(RedisConstant.ONLINE_TBOX, serialNumber);
        if (doWakeUp) {
            logger.warn("TBox(SN:{})不在线, 不执行当前POI透传指令!", serialNumber);
            return new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_TBOX_OFFLINE, "");
        }
        logger.info("TBox(SN:{})在线, 开始执行当前POI透传指令!", serialNumber);
        return doPoiConfigWhenTBoxOnline(serialNumber, vin, longitude, latitude, gpsType, posType, address, eventTime, OperationConstant.TAKE_PHOTO_RESPONSE_WAIT_TIME_FOR_ONLINE);
    }

    /**
     * TBox在线时执行POI透传请求
     *
     * @param serialNumber
     * @param vin
     * @param longitude
     * @param latitude
     * @param gpsType
     * @param posType
     * @param address
     * @param eventTime
     * @param waitRespTime
     * @return
     */
    private AppJsonResult doPoiConfigWhenTBoxOnline(String serialNumber, String vin, Integer longitude, Integer latitude, int gpsType, int posType, String address, String eventTime, long waitRespTime) throws Exception {
        try {
            String operateName = OperationConstant.FORWARD_POI;
            //把控制指令存入redis
            addCommandSend(serialNumber, operateName);
            PoiData poiData = new PoiData(longitude, latitude, address, gpsType, posType);
            kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_FORWARD_DOWN, serialNumber + "_" + OperationConstant.FORWARD_POI + "_" + JSONObject.toJSONString(poiData) + "_" + eventTime, serialNumber);
            //发送远程控制命令后, 等待10s的回包时间
            long startTime = System.currentTimeMillis();
            tboxOnlineStatusWhenCtrl = true;
            synchronized (EntertainmentPOIUtil.this) {
                EntertainmentPOIUtil.this.wait(waitRespTime);
            }
            if (System.currentTimeMillis() - startTime >= waitRespTime) {
                //超过10s回复结果
                logger.warn("TBox(SN:{})没有在有限时间内返回结果!", serialNumber);
                return new AppJsonResult(ResultStatus.RM_TBOX_NOT_REPLY_WITHIN_LIMIT_TIME, "");
            }
            //获取TBox返回的结果
            String[] kafkaReturn = htForwardUtil.get(serialNumber).result.split("_");
            // 获取tbox返回结果
            PoiRespInfo stateValue = JSONObject.parseObject(kafkaReturn[0], PoiRespInfo.class);
            if (stateValue.getData().equals(0)) {
                logger.info("TBox {}已经返回poi结果, 结果为成功。vin：{}，请求时间：{}", serialNumber, vin, eventTime);
                return new AppJsonResult(ResultStatus.SUCCESS, "");
            } else {
                logger.warn("TBox {}已经返回poi结果, 结果为失败。vin：{}，请求时间：{}", serialNumber, vin, eventTime);
                return new AppJsonResult(ResultStatus.FAIL, "");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            removeCommandSend(serialNumber);
            htForwardUtil.remove(serialNumber);
        }
    }
}
