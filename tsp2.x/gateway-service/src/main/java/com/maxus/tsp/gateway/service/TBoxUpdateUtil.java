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
import com.maxus.tsp.common.constant.KafkaMsgConstant;
import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.enums.ResultStatus;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.model.TboxFileLoadStatus;
import com.maxus.tsp.gateway.common.model.TboxUpdateRvmReq;
import com.maxus.tsp.gateway.mq.kafka.KafkaProducer;
import com.maxus.tsp.gateway.serviceclient.TspPlatformClient;
import com.maxus.tsp.platform.service.model.AppJsonResult;
import com.maxus.tsp.platform.service.model.vo.TboxUpdateInfoVo;

/**
 * 远程升级（含元61oms服务调用接口，及蜘蛛智行项目rvm调用接口）
 *
 * @author mjrni
 */
@Service
@Scope(value = "prototype")
public class TBoxUpdateUtil extends BaseUtilProc {

    private static final Logger logger = LogManager.getLogger(TBoxUpdateUtil.class);

    @Autowired
    private DataProcessing dataProcessing;
    
    @Autowired
    private KafkaProducer kafkaService;

    @Autowired
    private TspPlatformClient tspPlatformClient;

    @Autowired
    private RedisAPI redisAPI;

    public TBoxUpdateUtil(RedisAPI redisAPI) {
        super(redisAPI);
        this.redisAPI = redisAPI;
    }

    private static Hashtable<String, TBoxUpdateUtil> tBoxUpdateCtrl = new Hashtable<>();

    public static TBoxUpdateUtil getTBoxUpdateCtrl(String tboxsn) {
        return tBoxUpdateCtrl.get(tboxsn);
    }

    // 解决对在线Tbox远控请求,回复周期内（10s）Tbox频繁登录登出造成的线程通知错误；
    private boolean tboxOnlineStatusWhenCtrl = false;

    public boolean getTboxOnlineStatusWhenCtrl() {
        return tboxOnlineStatusWhenCtrl;
    }

    public TBoxUpdateUtil cloneUtil() {
        TBoxUpdateUtil tBoxUpdateUtil = new TBoxUpdateUtil(redisAPI);
        tBoxUpdateUtil.kafkaService = this.kafkaService;
        tBoxUpdateUtil.tspPlatformClient = this.tspPlatformClient;
        return tBoxUpdateUtil;
    }

    /**
     * 基于升级包id的远程升级（原61项目oms调用的服务接口）
     *
     * @param serialNumber
     * @param id
     * @return
     */
    public String tboxUpdate(String serialNumber, int id) {
        String ret = "";
        try {
            // 检查tbox参数是否为空
            if (StringUtils.isBlank(serialNumber)) {
                logger.warn("远程升级的TBox序列号为空.");
                return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAR_INFO_ERROR, ""));
            }
            if (id <= 0) {
                logger.warn("远程升级的升级包id不正确:{}", serialNumber);
                return JSONObject.toJSONString(
                        new AppJsonResult(ResultStatus.REMOTE_UPDATE_FAILED_FOR_WITHOUT_CORRECT_PACKAGE, ""));
            }
            ret = doTboxUpdate(serialNumber, id);
            return ret;
        } catch (Exception e) {
            logger.error("TBox({})升级发生异常:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_UPDATE_FAILED_FOR_EXCEPTION, ""));
        }
    }

    /**
     * 私有方法，升级tbox唤醒业务
     *
     * @param serialNumber
     * @param id
     * @return
     */
    private String doTboxUpdate(String serialNumber, int id) {
        // 首先得确认code是合法的tbox编号
        if (!dataProcessing.isTboxValid(serialNumber)) {
            return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAR_INFO_ERROR, ""));
        }
        // 这里得通过kafka生产一个透传指令下发
        boolean doWakeUp = !dataProcessing.onlineTboxExistCheck(serialNumber);

        tBoxUpdateCtrl.put(serialNumber, TBoxUpdateUtil.this);
        if (doWakeUp) {
            // 检查TBOX是否符合唤醒条件
            dataProcessing.checkTBoxLogOutForWakeupWait(serialNumber);

            // 将TBOX添加到唤醒队列中
            addWakeUp(serialNumber);
            logger.info("离线TBox {}的唤醒及远程配置请求可以执行 ", serialNumber);
            logger.info("开始异步发送唤醒短信并等待TBox {}登陆返回处理结果。 ", serialNumber);
            if (!dataProcessing.isSendingMessageSucceed(serialNumber)) {
                // 唤醒失败直接通知
                logger.warn("TBox {}的远程升级因唤醒失败而失败 ", serialNumber);
                redisAPI.delete(RedisConstant.WAKE_UP + "_" + serialNumber);
                tBoxUpdateCtrl.remove(serialNumber);
                return JSONObject
                        .toJSONString(new AppJsonResult(ResultStatus.RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED, ""));
            } else {
                // 执行等待
                logger.info("远程升级流程唤醒短信发送成功并开始等待TBox {}登录。 ", serialNumber);

                long startTime = 0;
                try {
                    startTime = System.currentTimeMillis();
                    synchronized (TBoxUpdateUtil.this) {
                        TBoxUpdateUtil.this.wait(OperationConstant.WAKEUP_WAIT_TIME);
                    }
                } catch (InterruptedException e) {
                    // 等待唤醒中途如果失败，就必须清除记录，并且
                    logger.error("远程升级因发生异常失败。 TBox {}, 异常：{}", serialNumber,
                            ThrowableUtil.getErrorInfoFromThrowable(e));
                    tBoxUpdateCtrl.remove(serialNumber);
                    return JSONObject
                            .toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
                } finally {
                	redisAPI.delete(RedisConstant.WAKE_UP + "_" + serialNumber);
                }

                // 如果超时，则代表唤醒失败了，直接通知用户
                if (System.currentTimeMillis() - startTime >= OperationConstant.WAKEUP_WAIT_TIME) {
                    logger.warn("远程升级失败，因为 TBox {}执行唤醒后未上线。", serialNumber);
                    tBoxUpdateCtrl.remove(serialNumber);
                    return JSONObject
                            .toJSONString(new AppJsonResult(ResultStatus.RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG, ""));
                } else {
                    // TBOX在线
                    logger.info("TBox {}上线了，远程配置请求可以执行 ", serialNumber);
                    // 继续等远程控制指令响应
                    return doUpdataTboxOnline(serialNumber, id);
                }
            }
        } else {
            logger.info("TBox({})在线，升级信息将进行发送。", serialNumber);
            // 继续等远程控制指令响应
            return doUpdataTboxOnline(serialNumber, id);
        }
    }

    // 原先61升级方式
    private String doUpdataTboxOnline(String serialNumber, int id) {
        String result = "";
        try {
            // 获取最新版本url
            TboxUpdateInfoVo updateInfo = this.tspPlatformClient.getTboxUpdateInfo(id);
            if (updateInfo == null) {
                result = JSONObject.toJSONString(
                        new AppJsonResult(ResultStatus.REMOTE_UPDATE_FAILED_FOR_EMPTY_UPDATE_INFO, ""));
            } else {
                String version = updateInfo.getUpgrade_version();
                String url = updateInfo.getUpgrade_url();
                String md5 = updateInfo.getMd5();
                if (md5.length() != OperationConstant.MD5_SIZE || url.length() < 0 || version.length() < 0) {
                    logger.warn("TBox({})远程升级信息错误. 请确认数据库中的版本号, url及 md5信息.", serialNumber);
                    result = JSONObject.toJSONString(
                            new AppJsonResult(ResultStatus.REMOTE_UPDATE_FAILED_FOR_ERROR_UPDATE_INFO, ""));
                } else {
                    this.tspPlatformClient.updateVerStatus(serialNumber, -1);
                    kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_TBOX_UPDATE,
                            serialNumber + "_" + version + "_" + url + "_" + md5, serialNumber);
                    result = JSONObject.toJSONString(new AppJsonResult(ResultStatus.SUCCESS, ""));
                }
            }
        } catch (Exception e) {
            logger.error("原61升级因发生异常失败，原因:{}", ThrowableUtil.getErrorInfoFromThrowable(e));
        }
        return result;
    }

    /**
     * RVM调用的tbox升级接口
     *
     * @param tboxUpdateRvmReq
     * @return
     */
    public AppJsonResult tboxUpdateRvm(TboxUpdateRvmReq tboxUpdateRvmReq) {
        if (tboxUpdateRvmReq == null) {
            return new AppJsonResult(ResultStatus.REMOTE_UPDATE_FAILED_FOR_ERROR_UPDATE_INFO, "");
        }
        String serialNumber = tboxUpdateRvmReq.getSn();
        String version = tboxUpdateRvmReq.getVersion();
        String url = tboxUpdateRvmReq.getUrl();
        String md5 = tboxUpdateRvmReq.getMd5();
        try {
            // 检查参数
            String checkParam = checkParam(serialNumber, version, url, md5);
            if (checkParam != null) {
                return JSONObject.parseObject(checkParam, AppJsonResult.class);
            }

            return JSONObject.parseObject(doTboxUpdateRVM(tboxUpdateRvmReq), AppJsonResult.class);

        } catch (Exception e) {
            logger.error("TBox({})升级发生异常:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            return new AppJsonResult(ResultStatus.REMOTE_UPDATE_FAILED_FOR_EXCEPTION, "");
        }
    }

    private String checkParam(String serialNumber, String version, String url, String md5) {
        // 检查tbox参数是否为空
        if (StringUtils.isBlank(serialNumber)) {
            logger.warn("远程升级的TBox序列号为空.");
            return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAR_INFO_ERROR, ""));
        }
        // 确认sn是合法的tbox编号
        if (!dataProcessing.isTboxValid(serialNumber)) {
            return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAR_INFO_ERROR, ""));
        }
        // 检查tbox参数是否为空
        if (StringUtils.isBlank(version)) {
            logger.warn("远程升级的version为空.");
            return JSONObject
                    .toJSONString(new AppJsonResult(ResultStatus.REMOTE_UPDATE_FAILED_FOR_ERROR_UPDATE_INFO, ""));
        }
        // 检查tbox参数是否为空
        if (StringUtils.isBlank(url)) {
            logger.warn("远程升级的url为空.");
            return JSONObject
                    .toJSONString(new AppJsonResult(ResultStatus.REMOTE_UPDATE_FAILED_FOR_ERROR_UPDATE_INFO, ""));
        }
        // 检查tbox参数是否为空
        if (StringUtils.isBlank(md5)) {
            logger.warn("远程升级的md5为空.");
            return JSONObject
                    .toJSONString(new AppJsonResult(ResultStatus.REMOTE_UPDATE_FAILED_FOR_ERROR_UPDATE_INFO, ""));
        }
        // 确认版本号, url及 md5信息
        if (md5.length() != OperationConstant.MD5_SIZE || url.length() < 0 || version.length() < 0) {
            logger.warn("TBox({})远程升级信息错误. 请确认版本号, url及 md5信息.", serialNumber);
            return JSONObject
                    .toJSONString(new AppJsonResult(ResultStatus.REMOTE_UPDATE_FAILED_FOR_ERROR_UPDATE_INFO, ""));
        }
        try {
            // 检查TboxSN是否合法
            if (!dataProcessing.isTboxValid(serialNumber)) {
                return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAR_INFO_ERROR, ""));
            }
        } catch (Exception ex) {
            logger.error("远程配置因发生异常失败。 TBox {}, 异常：{}", serialNumber,
                    ThrowableUtil.getErrorInfoFromThrowable(ex));
            return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
        }
        return null;
    }


    /**
     * RVM唤醒业务
     * @param tboxUpdateRvmReq
     * @return
     */
    private String doTboxUpdateRVM(TboxUpdateRvmReq tboxUpdateRvmReq) {
        String serialNumber = tboxUpdateRvmReq.getSn();
        // 首先得确认code是合法的tbox编号
        if (!dataProcessing.isTboxValid(serialNumber)) {
            return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAR_INFO_ERROR, ""));
        }
        // 这里得通过kafka生产一个透传指令下发
        boolean doWakeUp = !dataProcessing.onlineTboxExistCheck(serialNumber);

        tBoxUpdateCtrl.put(serialNumber, TBoxUpdateUtil.this);
        if (doWakeUp) {
            // 检查TBOX是否符合唤醒条件
            dataProcessing.checkTBoxLogOutForWakeupWait(serialNumber);
            // 将TBox添加到唤醒队列中
            addWakeUp(serialNumber);
            logger.info("离线TBox {}的唤醒及远程配置请求可以执行 ", serialNumber);
            logger.info("开始异步发送唤醒短信并等待TBox {}登陆返回处理结果。 ", serialNumber);
            if (!dataProcessing.isSendingMessageSucceed(serialNumber)) {
                // 唤醒失败直接通知
                logger.warn("TBox {}的远程升级因唤醒失败而失败 ", serialNumber);
                redisAPI.delete(RedisConstant.WAKE_UP + "_" + serialNumber);
                tBoxUpdateCtrl.remove(serialNumber);
                return JSONObject
                        .toJSONString(new AppJsonResult(ResultStatus.RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED, ""));
            } else {
                // 执行等待
                logger.info("远程升级流程唤醒短信发送成功并开始等待TBox {}登录。 ", serialNumber);

                long startTime = 0;
                try {
                    startTime = System.currentTimeMillis();
                    synchronized (TBoxUpdateUtil.this) {
                        TBoxUpdateUtil.this.wait(OperationConstant.WAKEUP_WAIT_TIME);
                    }
                } catch (InterruptedException e) {
                    // 等待唤醒中途如果失败，就必须清除记录，并且
                    logger.error("远程升级因发生异常失败。 TBox {}, 异常：{}", serialNumber,
                            ThrowableUtil.getErrorInfoFromThrowable(e));
                    tBoxUpdateCtrl.remove(serialNumber);
                    return JSONObject
                            .toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
                } finally {
                	redisAPI.delete(RedisConstant.WAKE_UP + "_" + serialNumber);
                }

                // 如果超时，则代表唤醒失败了，直接通知用户
                if (System.currentTimeMillis() - startTime >= OperationConstant.WAKEUP_WAIT_TIME) {
                    logger.warn("远程升级失败，因为 TBox {}执行唤醒后未上线。", serialNumber);
                    tBoxUpdateCtrl.remove(serialNumber);
                    return JSONObject
                            .toJSONString(new AppJsonResult(ResultStatus.RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG, ""));
                } else {
                    // TBOX在线
                    logger.info("TBox {}上线了，远程配置请求可以执行 ", serialNumber);
                    // 继续等远程控制指令响应
                    return doUpdataTboxOnlineRVM(tboxUpdateRvmReq);
                }
            }
        } else {
            logger.info("TBox({})在线，升级信息将进行发送。", serialNumber);
            // 继续等远程控制指令响应
            return doUpdataTboxOnlineRVM(tboxUpdateRvmReq);
        }
    }
    
    /**
     * 根据SN获取对应的远程升级值
     * @param tboxSn
     * @return
     */
    public String getRemoteUpdate(String tboxSn) {
        try {
            return redisAPI.getHash(RedisConstant.ON_REMOTE_UPDATE_OTA, tboxSn);
        } catch (Exception ex) {
            logger.error("Redis connection error,TBox({}) can't do getRemoteUpdate:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
        return null;
    }

    /**
     * RVM在线执行
     * @param tboxUpdateRvmReq
     * @return
     */
    private String doUpdataTboxOnlineRVM(TboxUpdateRvmReq tboxUpdateRvmReq) {
        logger.info("TBox({})在线，升级信息将进行发送。", tboxUpdateRvmReq.getSn());
        // 通过存储在redis中的数据判断是否下发Tbox远程升级指令
        TboxFileLoadStatus updateInfo = new TboxFileLoadStatus();
        // 更新成最新的seqnum和发起时间
        updateInfo.setSeqNo(tboxUpdateRvmReq.getSeqNo());
        updateInfo.setEventTime(tboxUpdateRvmReq.getEventTime());

        //if (GlobalSessionChannel.existRemoteUpdate(tboxUpdateRvmReq.getSn())) {
        if (redisAPI.hasKey(RedisConstant.COMMAND_SEND + "_" + tboxUpdateRvmReq.getSn())) {
            updateInfo.setResult(JSONObject.parseObject(getRemoteUpdate(tboxUpdateRvmReq.getSn()),
                    TboxFileLoadStatus.class).getResult());
        }
        //GlobalSessionChannel.addRemoteUpdate(tboxUpdateRvmReq.getSn(), JSONObject.toJSONString(updateInfo));
        addCommandSend(tboxUpdateRvmReq.getSn(), JSONObject.toJSONString(updateInfo));

        kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_TBOX_UPDATE,
                tboxUpdateRvmReq.getSn() + "_" + tboxUpdateRvmReq.getVersion() + "_" + tboxUpdateRvmReq.getUrl() + "_" + tboxUpdateRvmReq.getMd5(), tboxUpdateRvmReq.getSn());
        tboxOnlineStatusWhenCtrl = true;
        return JSONObject.toJSONString(new AppJsonResult(ResultStatus.SUCCESS, ""));
    }
    
    /**
     * @method addCommandSend
     * @description 添加一条正在下发的指令
     * @param serialNumber
     * @param value
     * @return
     * @author zhuna
     * @date 2019/2/15 15:49
     */
    public boolean addCommandSend(String serialNumber, String value) {
        try {
            Date date = new Date();
            String currentTime = DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss");
            return redisAPI.setValueWithEspireTime(RedisConstant.COMMAND_SEND + "_" + serialNumber, value + "_" + currentTime, OperationConstant.REMOTECONTROL_RESP_EXPIRED_TIME, TimeUnit.SECONDS);
        } catch (Exception ex) {
            logger.error("Redis addValue error, TBox({}) can't do addCommandSend! The error:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(ex));
            return false;
        }
    }
    
    /**
     * redis添加  正在唤醒记录
     * @param tboxsn
     * @author zhuna
     * @date 2018年12月4日
     */
    public void addWakeUp(String tboxsn) {
        // 设置75s超时,清楚redis wake up记录
        try {
            Date date = new Date();
            String currentTime = DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss");
            redisAPI.setValue(RedisConstant.WAKE_UP + "_" + tboxsn, currentTime, OperationConstant.WAKEUP_WAIT_TIME_SEC, TimeUnit.SECONDS);
        } catch (Exception ex) {
            logger.error("Redis connection error, TBox(SN:{}) can't do addWakeUp! The error:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
    }
    
}
