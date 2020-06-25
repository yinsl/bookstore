package com.maxus.tsp.gateway.service;

import java.util.Date;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
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
import com.maxus.tsp.gateway.common.model.DownLoadFileMo;
import com.maxus.tsp.gateway.common.model.TboxFileLoadStatus;
import com.maxus.tsp.gateway.common.model.UpLoadFileMo;
import com.maxus.tsp.gateway.mq.kafka.KafkaProducer;
import com.maxus.tsp.platform.service.model.AppJsonResult;

/**
 * 处理文件下载上传业务类
 *
 * @Author mjrni
 */
@Service
@Scope(value = "prototype")
public class FileLoadUtil extends BaseUtilProc {

    private static final Logger logger = LogManager.getLogger(FileLoadUtil.class);

    @Autowired
    private DataProcessing dataProcessing;
    
    @Autowired
    private KafkaProducer kafkaService;

    @Autowired
    private TboxService tboxService;

    @Autowired
    private RedisAPI redisAPI;

    public FileLoadUtil(RedisAPI redisAPI) {
        super(redisAPI);
        this.redisAPI = redisAPI;
    }

    // 解决对在线Tbox远控请求,回复周期内（10s）Tbox频繁登录登出造成的线程通知错误；
    private boolean tboxOnlineStatusWhenCtrl = false;

    public boolean getTboxOnlineStatusWhenCtrl() {
        return tboxOnlineStatusWhenCtrl;
    }

    /**
     * 保存本节点处理远程控制请求的实例
     */
    private static Hashtable<String, FileLoadUtil> fileLoadCtrl = new Hashtable<>();

    public static FileLoadUtil getFileLoadCtrl(String tboxsn) {
        return fileLoadCtrl.get(tboxsn);
    }

    public FileLoadUtil cloneUtil() {
        FileLoadUtil fileLoadUtil = new FileLoadUtil(redisAPI);
        fileLoadUtil.kafkaService = this.kafkaService;
        fileLoadUtil.tboxService = this.tboxService;
        return fileLoadUtil;
    }


    /**
     * 文件下载
     *
     * @param downLoadFile
     * @return
     */
    public AppJsonResult downLoadFile(DownLoadFileMo downLoadFile) {

        // 检查参数
        AppJsonResult result = checkdownLoadFileParam(downLoadFile);
        if (null != result) {
            return result;
        }
        // 检查是否在线
        result = checkOnlineTbox(downLoadFile.getSn(), "downLoadFile");
        if (null != result) {
            return result;
        }
        result = doDownLoadFile(downLoadFile);
        return result;
    }

    /**
     * 检查请求参数是否合法
     *
     * @param downLoadFile
     * @return
     */
    private AppJsonResult checkdownLoadFileParam(DownLoadFileMo downLoadFile) {
        // 检查参数
        if (null == downLoadFile) {
            logger.warn("TBox downLoadFile：下载文件参数为空！");
            return new AppJsonResult(ResultStatus.DOWNLOAD_FILR_PARAM_WRONG, "");
        }
        // 文件类型1有效
        if (downLoadFile.getFileType() != 1) {
            logger.warn("TBox(SN:{})downLoadFile：文件类型为无效值！", downLoadFile.getSn());
            return new AppJsonResult(ResultStatus.DOWNLOAD_FILR_PARAM_WRONG, "");
        }
        if (null == downLoadFile.getUrl()) {
            logger.warn("TBox(SN:{})downLoadFile：url为空！", downLoadFile.getSn());
            return new AppJsonResult(ResultStatus.DOWNLOAD_FILR_PARAM_WRONG, "");
        }
        if (null == downLoadFile.getMd5Data()) {
            logger.warn("TBox(SN:{})downLoadFile：文件md5为空！", downLoadFile.getSn());
            return new AppJsonResult(ResultStatus.DOWNLOAD_FILR_PARAM_WRONG, "");
        }
        // 首先得确认sn是合法的tbox编号
        if (!dataProcessing.isTboxValid(downLoadFile.getSn())) {
            return new AppJsonResult(ResultStatus.CAR_INFO_ERROR, "");
        }
        return null;
    }

    /**
     * 唤醒Tbox服务
     *
     * @return
     */
    private AppJsonResult checkOnlineTbox(String sn, String cmd) {
        boolean doWakeUp = false;
        try {
            String vin = tboxService.getVINForTbox(sn);
            if (StringUtils.isEmpty(vin)) {
                logger.warn("TBox(SN:{})VIN号为空或异常，vin: {}", sn, vin);
            }

            // Tbox是否在线
            doWakeUp = !dataProcessing.onlineTboxExistCheck(sn);
        } catch (Exception ex) {
            logger.error("文件操作指令发生异常失败。cmd:{}, TBox {}, 异常：{}", cmd, sn,
                    ThrowableUtil.getErrorInfoFromThrowable(ex));
            return new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, "");
        }
        fileLoadCtrl.put(sn, FileLoadUtil.this);
        if (doWakeUp) {
            dataProcessing.checkTBoxLogOutForWakeupWait(sn);
            // 将TBOX添加到唤醒队列中
            addWakeUp(sn);

            logger.info("离线TBox {}的唤醒及{}请求可以执行 ", sn, cmd);
            logger.info("开始异步发送唤醒短信并等待TBox {}登录返回处理结果。 ", sn);
            if (!dataProcessing.isSendingMessageSucceed(sn)) {
                // 唤醒失败直接通知
                logger.warn("TBox {}的{}因唤醒失败而失败 ", sn, cmd);
                redisAPI.delete(RedisConstant.WAKE_UP + "_" + sn);
                fileLoadCtrl.remove(sn);
                return new AppJsonResult(ResultStatus.RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED, "");
            } else {
                logger.info("{}流程唤醒短信发送成功并开始等待TBox {}登陆。 ", cmd, sn);
                long startTime = 0;
                try {
                    startTime = System.currentTimeMillis();
                    synchronized (FileLoadUtil.this) {
                        FileLoadUtil.this.wait(OperationConstant.WAKEUP_WAIT_TIME);
                    }
                } catch (InterruptedException e) {
                    // 等待唤醒中途如果失败，就必须清除记录，并且
                    logger.error("{}因发生异常失败。 TBox {}, 异常：{}", cmd, sn,
                            ThrowableUtil.getErrorInfoFromThrowable(e));
                    fileLoadCtrl.remove(sn);
                    return new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, "");
                } finally {
                	redisAPI.delete(RedisConstant.WAKE_UP + "_" + sn);
                }

                // 如果超时，则代表唤醒失败了，直接通知用户
                if (System.currentTimeMillis() - startTime >= OperationConstant.WAKEUP_WAIT_TIME) {
                    logger.warn("{}失败，因为 TBox {}执行唤醒后未上线。", cmd, sn);
                    fileLoadCtrl.remove(sn);
                    return new AppJsonResult(ResultStatus.RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG, "");
                } else {
                    // TBox在线
                    logger.info("TBox {}上线了，{}请求可以执行 ", sn, cmd);
                }
            }
        }
        return null;
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
     * @Title: 根据操作指令确认是否有该指令引起的唤醒操作
     * @param tboxSn
     * @return
     * @author zhuna
     * @date 2018年12月4日
     */
    public boolean existWakeUp(String tboxSn) {
        return redisAPI.hasKey(RedisConstant.WAKE_UP + "_" + tboxSn);
    }
    
    public String existTboxDownLoadFile(String sn) {
        try {
            return redisAPI.getHash(RedisConstant.TBOX_DOWNLOAD_FILE_INFO, sn);
        } catch (Exception ex) {
            logger.error("Tbox(sn: {})获取-Redis是否存在Tbox文件下载记录失败！原因：{}", sn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            return null;
        }
    }

    /*
     * 在线下发指令
     */
    private AppJsonResult doDownLoadFile(DownLoadFileMo downLoadFile) {
        String sn = downLoadFile.getSn();
        String cmd = "downLoadFile";
        try {
            // redis是否记录此次远程配置
            TboxFileLoadStatus updateInfo = new TboxFileLoadStatus();
            updateInfo.setSeqNo(downLoadFile.getSeqNo());
            updateInfo.setEventTime(downLoadFile.getCurrentTime());

            //if (GlobalSessionChannel.existTboxDownLoadFile(sn) != null) {
            if (redisAPI.hasKey(RedisConstant.COMMAND_SEND + "_" + sn)) {
                updateInfo.setResult(JSONObject.parseObject(existTboxDownLoadFile(sn),
                        TboxFileLoadStatus.class).getResult());
            }
            //GlobalSessionChannel.setTboxDownLoadFile(sn, JSONObject.toJSONString(updateInfo));
            addCommandSend(sn, JSONObject.toJSONString(updateInfo));
            logger.info("记录此次请求下载文件指令至Redis：TBoxSn:{},Value:{}", sn,
                    JSONObject.toJSONString(downLoadFile));
            // TBox上线，将其移除唤醒
            if (existWakeUp(sn)) {
            	redisAPI.delete(RedisConstant.WAKE_UP + "_" + sn);
            }
            String dateTime = DateFormatUtils.format(downLoadFile.getCurrentTime(), "yyyy-MM-dd HH:mm:ss");
            kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_DOWM_LOAD_FILE_REQ,
                    sn + "_" + JSONObject.toJSONString(downLoadFile) + "_" + dateTime, sn);
            tboxOnlineStatusWhenCtrl = true;
            return new AppJsonResult(ResultStatus.SUCCESS, "");
        } catch (Exception ex) {
            logger.error("TBox(SN:{})在线下发指令({})因发生异常而失败，异常原因:{}", sn, cmd, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
        return null;
    }

    /**
     * 上传文件
     *
     * @param upLoadFilecopy
     * @return
     */
    public AppJsonResult upLoadFile(UpLoadFileMo upLoadFile) {
        // 检查参数
        AppJsonResult result = checkupLoadFileParam(upLoadFile);
        if (null != result) {
            return result;
        }
        // 检查是否在线
        result = checkOnlineTbox(upLoadFile.getSn(), "upLoadFile");
        if (null != result) {
            return result;
        }
        result = doUpLoadFile(upLoadFile);
        return result;
    }

    private AppJsonResult checkupLoadFileParam(UpLoadFileMo upLoadFile) {
        // 检查参数
        if (null == upLoadFile) {
            logger.warn("TBox upLoadFile：上传文件指令参数为空！");
            return new AppJsonResult(ResultStatus.UPLOAD_FILE_PARAM_WARONG , "");
        }
        // 文件类型1有效
        if (upLoadFile.getFileType() != 1 && upLoadFile.getFileType() != 2) {
            logger.warn("TBox(SN:{})upLoadFile：文件类型为无效值！", upLoadFile.getSn());
            return new AppJsonResult(ResultStatus.UPLOAD_FILE_PARAM_WARONG, "");
        }
        // 首先得确认sn是合法的TBox编号
        if (!dataProcessing.isTboxValid(upLoadFile.getSn())) {
            return new AppJsonResult(ResultStatus.CAR_INFO_ERROR, "");
        }
        return null;
    }
    
    public String existTboxUpLoadFile(String sn) {
        try {
            return redisAPI.getHash(RedisConstant.TBOX_UPLOAD_FILE_INFO, sn);
        } catch (Exception ex) {
            logger.error("Tbox(sn: {})获取-Redis是否存在Tbox文件上传记录失败！原因：{}", sn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            return null;
        }
    }

    /*
     * 在线下发指令
     */
    private AppJsonResult doUpLoadFile(UpLoadFileMo upLoadFile) {
        String sn = upLoadFile.getSn();
        String cmd = "upLoadFile";
        try {
            // redis是否记录此次远程配置
            TboxFileLoadStatus updateInfo = new TboxFileLoadStatus();
            updateInfo.setSeqNo(upLoadFile.getSeqNo());
            updateInfo.setEventTime(upLoadFile.getCurrentTime());

            if (existTboxUpLoadFile(sn) != null) {
                updateInfo.setResult(JSONObject.parseObject(existTboxUpLoadFile(sn),
                        TboxFileLoadStatus.class).getResult());
            }
            //GlobalSessionChannel.setTboxUpLoadFile(sn, JSONObject.toJSONString(updateInfo));
            addCommandSend(sn, JSONObject.toJSONString(updateInfo));
            logger.info("记录此次请求上传文件指令至Redis：TBoxSn:{},Value:{}", sn,
                    JSONObject.toJSONString(upLoadFile));
            // TBox上线，将其移除唤醒
            if (existWakeUp(sn)) {
            	redisAPI.delete(RedisConstant.WAKE_UP + "_" + sn);
            }
            String dateTime = DateFormatUtils.format(upLoadFile.getCurrentTime(), "yyyy-MM-dd HH:mm:ss");
            kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_UP_LOAD_FILE_REQ,
                    sn + "_" + JSONObject.toJSONString(upLoadFile) + "_" + dateTime, sn);
            tboxOnlineStatusWhenCtrl = true;
            return new AppJsonResult(ResultStatus.SUCCESS, "");
        } catch (Exception ex) {
            logger.error("TBox(SN:{})在线下发指令({})操作因发生异常而失败，异常原因:{}", sn, cmd, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
        return null;
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
