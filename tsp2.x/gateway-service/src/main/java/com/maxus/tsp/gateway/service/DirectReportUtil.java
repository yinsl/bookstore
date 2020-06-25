package com.maxus.tsp.gateway.service;

import com.maxus.tsp.common.constant.KafkaMsgConstant;
import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.enums.ResultStatus;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.DateUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.mq.kafka.KafkaProducer;
import com.maxus.tsp.gateway.serviceclient.TspPlatformClient;
import com.maxus.tsp.platform.service.model.AppJsonResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Hashtable;

/**
 * @Description 国家直连平台进行请求, 将国家平台的地址(ip, port)等信息, 通过网关发送给TBox
 * @Author mjrni
 */
@Service
@Scope(value = "prototype")
public class DirectReportUtil extends BaseUtilProc {

    private static final Logger logger = LogManager.getLogger(DirectReportUtil.class);

    @Autowired
    private DataProcessing dataProcessing;
    
    @Autowired
    private KafkaProducer kafkaService;
    //数据库相关接口
    @Autowired
    private TspPlatformClient tspPlatformClient;
    @Autowired
    private RedisAPI redisAPI;

    /**
     * TBox响应报文的处理结果
     */
    private String result;

    /**
     * port的格式
     */
    private static final String PORT_REGEX = "^[0-9]*$";

    /**
     * IP(域名)与端口号的分隔符
     */
    private static final String IP_PORT_DEVIDE_MASK = ":";

    public void setResult(String result) {
        this.result = result;
    }

    /**
     * 保存本节点处理处理国家平台直连的实例
     */
    private static Hashtable<String, DirectReportUtil> htDirectReport = new Hashtable<>();

    /**
     * 根据sn获取对应的国家直连平台处理类的实例
     *
     * @param serialNumber
     * @return
     */
    public static DirectReportUtil getDirectReportUtil(String serialNumber) {
        return htDirectReport.get(serialNumber);
    }

    public DirectReportUtil(RedisAPI redisAPI) {
        super(redisAPI);
        this.redisAPI = redisAPI;
    }

    // 解决对在线Tbox远控请求,回复周期内（10s）Tbox频繁登录登出造成的线程通知错误；
    private boolean tboxOnlineStatusWhenCtrl = false;

    public boolean getTboxOnlineStatusWhenCtrl() {
        return tboxOnlineStatusWhenCtrl;
    }

    /**
     * 创建直连控制业务的处理类
     *
     * @return
     */
    public DirectReportUtil cloneUtil() {
        DirectReportUtil directReportUti = new DirectReportUtil(redisAPI);
        directReportUti.kafkaService = this.kafkaService;
        directReportUti.tspPlatformClient = this.tspPlatformClient;
        return directReportUti;
    }

    /**
     * 开始执行直连控制
     *
     * @return
     */
    public AppJsonResult validDirectReport(String serialNumber, String value, long eventTime) {
        try {
            //进行参数校验
            if (StringUtils.isBlank(serialNumber) || !dataProcessing.isTboxValid(serialNumber)) {
                logger.warn("当前国家平台直连操作中TBox序列号为空或不合法!");
                return new AppJsonResult(ResultStatus.CAR_INFO_ERROR, "");
            }
            if (StringUtils.isBlank(value)) {
                logger.warn("TBox(SN:{})当前国家平台直连操作value为空!", serialNumber);
                return new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
            }
            return doDirectReport(serialNumber, value, eventTime);
        } catch (Exception e) {
            logger.error("TBox(SN:{})当前国家平台直连操作因发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            return new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, "");
        }
    }

    /**
     * 远控排他以及判断是否需要唤醒TBox
     *
     * @param serialNumber
     * @param value
     * @param eventTime
     * @return
     */
    private AppJsonResult doDirectReport(String serialNumber, String value, long eventTime) throws InterruptedException {
        boolean doWakeUp;
        String eventTimeStr = DateUtil.longToDateStr(eventTime);
        //尽管直连操作不需要唤醒TBox, 但是仍需要对唤醒指令进行远控排他处理, 防止该TBox在进行其他远控的唤醒操作
        if (existCommandSend(serialNumber) || existWakeUp(serialNumber)) {
            logger.warn("TBox(SN:{})国家平台直连时远控指令存在状态:{}, 当前TBox唤醒指令存在状态:{}", serialNumber, existCommandSend(serialNumber), existWakeUp(serialNumber));
            return new AppJsonResult(ResultStatus.RM_TBOX_HAVE_ACCEPT_ONE_COMMAND, "");
        } else {
            try {
                logger.info("TBox(SN:{})国家平台直连操作远控排他成功!", serialNumber);
                doWakeUp = !redisAPI.hasKey(RedisConstant.ONLINE_TBOX, serialNumber);
                if (doWakeUp) {
                    logger.warn("TBox(SN:{})不在线, 无法执行国家平台直连的操作!", serialNumber);
                    return new AppJsonResult(ResultStatus.DIRECT_REPORT_FAILED_FOR_TBOX_OFFLINE, "");
                }
                logger.info("TBox(SN:{})在线, 可以执行国家平台直连指令的操作!", serialNumber);
                //存入需要执行的国家直连操作
                htDirectReport.put(serialNumber, DirectReportUtil.this);
                //将当前控制指令存入redis
                addCommandSend(serialNumber, RedisConstant.GB_DIRECT_REPORT);
                //获取最新版url
                if (!value.equals(OperationConstant.REMOTE_CTRL_FUNCTION_CLOSED)) {
                    //判断url参数是否合法
                    int portOffset = value.lastIndexOf(IP_PORT_DEVIDE_MASK);
                    if (portOffset <= 0) {
                        logger.warn("TBox(SN:{})国家平台直连指令错误 value:{}", serialNumber, value);
                        return new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, "");
                    } else {
                        String url = value.substring(0, portOffset);
                        String port = value.substring(portOffset + 1, value.length());
                        if (!port.matches(PORT_REGEX)) {
                            logger.warn("TBox(SN:{})国家平台直连指令端口号错误:{}", serialNumber, value);
                            return new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, "");
                        } else {
                            //启动
                            kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_TBOX_DIRECT_REPORT_START,
                                    serialNumber + "_" + url + "_" + port + "_" + eventTimeStr, serialNumber);
                        }
                    }
                } else {
                    //停止
                    kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_TBOX_DIRECT_REPORT_STOP,
                            serialNumber + "_" + eventTimeStr, serialNumber);
                }
                //发送远程控制命令后, 等待10s回包
                long startTime = System.currentTimeMillis();
                synchronized (DirectReportUtil.this) {
                    DirectReportUtil.this.wait(OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME);
                }
                if (System.currentTimeMillis() - startTime >= OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME) {
                    //超过10s回复
                    logger.warn("TBox(SN:{})国家平台直连操作失败, 因为TBox没有及时回复远程控制报文, 请求时间:{}", serialNumber, eventTime);
                    return new AppJsonResult(ResultStatus.RM_TBOX_NOT_REPLY_WITHIN_LIMIT_TIME, "");
                }
                //10s内回复
                if (result == null) {
                    if (value.equals(OperationConstant.REMOTE_CTRL_FUNCTION_CLOSED)) {
                        return new AppJsonResult(ResultStatus.DIRECT_REPORT_STOP_FAILED, "");
                    } else {
                        return new AppJsonResult(ResultStatus.DIRECT_REPORT_START_FAILED_FOR_OTHER_REASON, "");
                    }
                } else if (this.result.equals(ResultStatus.SUCCESS.getCode())) {
                    return new AppJsonResult(ResultStatus.SUCCESS, "");
                } else {
                    return new AppJsonResult(ResultStatus.getResultStatus(result), "");
                }
            } catch (InterruptedException e) {
                throw e;
            } finally {
                htDirectReport.remove(serialNumber);
                removeCommandSend(serialNumber);
            }
        }
    }
}
