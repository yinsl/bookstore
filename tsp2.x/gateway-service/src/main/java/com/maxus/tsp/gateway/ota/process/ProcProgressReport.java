package com.maxus.tsp.gateway.ota.process;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.KafkaMsgConstantFota;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OTAConstant;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.model.fota.ProgressItRespInfo;
import com.maxus.tsp.gateway.common.model.fota.ProgressTBoxReqInfo;
import com.maxus.tsp.gateway.common.ota.OTAMessage;
import com.maxus.tsp.gateway.service.KafkaService;
import com.maxus.tsp.gateway.service.TboxService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Hashtable;

/**
 * @ClassName ProcDownloadProgress
 * @Description 进度上报报文的处理类
 * @Author zijhm
 * @Date 2019/2/15 15:48
 * @Version 1.0
 **/
public class ProcProgressReport extends BaseOtaProc {

    //日志
    private static Logger logger = LogManager.getLogger(ProcProgressReport.class);

    //rvm返回的进度上报处理的结果
    private ProgressItRespInfo itResult;

    public void setItResult(ProgressItRespInfo itResult) {
        this.itResult = itResult;
    }

    //存储当前请求对象
    private static Hashtable<String, ProcProgressReport> getProgressReport = new Hashtable<>();

    //获取存储的请求对象
    public static ProcProgressReport getProcProgressReport(String serialNumber) {
        return getProgressReport.get(serialNumber);
    }

    public ProcProgressReport(KafkaService kafkaService, TboxService tboxService) {
        super(kafkaService, tboxService);
    }

    /**
     * @return byte[]
     * @Description Fota上报进度报文的处理方法
     * @Date 2019/2/15 15:52
     * @Param [requestMsg]
     **/
    public byte[] checkProgressReport(OTAMessage requestMsg, String cmd) {
        return procProgressReport(requestMsg.getParam(), requestMsg.getSerialNumber(), cmd);
    }

    /**
     * @return byte[]
     * @Description 处理进度上报报文
     * @Date 2019/2/15 15:55
     * @Param [param, serialNumber]
     **/
    private byte[] procProgressReport(byte[] param, String serialNumber, String cmd) {
        logger.info("TBox(SN:{})当前进度上报的CMD为:{}, 进度上报报文解密后参数部分为:{}", serialNumber, cmd, ByteUtil.byteToHex(param));
        boolean result = false;
        byte[] outData = null;
        try {
            //JsonLength
            byte[] _jsonLength = new byte[2];
            System.arraycopy(param, 4, _jsonLength, 0, 2);
            int jsonLength = ByteUtil.getUnsignedInt(_jsonLength);

            //判断报文长度是否正确
            if (param.length == 4 + jsonLength + 2) {
                ProgressTBoxReqInfo progressTBoxReqInfo = new ProgressTBoxReqInfo();
                progressTBoxReqInfo.setSn(serialNumber);
                progressTBoxReqInfo.setCmd(cmd);
                progressTBoxReqInfo.setDataSize(jsonLength);
                progressTBoxReqInfo.setSeqNo();
                progressTBoxReqInfo.setEventTime(System.currentTimeMillis());

                //ID
                byte[] _id = new byte[4];
                System.arraycopy(param, 0, _id, 0, 4);
                long id = ByteUtil.getUnsignedLong(_id);
                progressTBoxReqInfo.setId(id);

                //progress/result
                if (jsonLength != 0) {
                    byte[] _data = new byte[jsonLength];
                    System.arraycopy(param, 6, _data, 0, jsonLength);
                    progressTBoxReqInfo.setData(ByteUtil.byteToHex(_data));
                } else {
                    logger.warn("TBox(SN:{})当前CMD:{}上报进度json长度为0!", serialNumber, cmd);
                    progressTBoxReqInfo.setData("");
                }

                logger.debug("TBox(SN:{})解析CMD:{}进度上报报文结果为:{}", serialNumber, cmd, JSONObject.toJSONString(progressTBoxReqInfo));
                result = kafkaService.transferItData(KafkaMsgConstantFota.TOPIC_IT_PROGRESS_REPORT, progressTBoxReqInfo, serialNumber);
            } else {
                logger.warn("TBox(SN:{})CMD:{}进度上报报文长度错误, 报文丢弃!", serialNumber, cmd);
            }
        } catch (Exception e) {
            logger.error("TBox(SN:{})解析CMD:{}因发生异常失败, 异常原因:{}", serialNumber, cmd, ThrowableUtil.getErrorInfoFromThrowable(e));
        }
        try {
            if (result) {
                //往IT Kafka发OperationConstant.ONLINE_COMMAND_WAIT_TIME送请求成功
                getProgressReport.put(serialNumber, ProcProgressReport.this);
                long startTime = System.currentTimeMillis();
                logger.info("TBox(SN:{})的CMD:{}进度上报信息已经投递给it, 等待it响应!", serialNumber, cmd);
                //等待it回复当前进度上报处理结果, 10s内下发结果给TBox, 10s外超时
                synchronized (ProcProgressReport.this) {
                    ProcProgressReport.this.wait(OperationConstant.ONLINE_COMMAND_WAIT_TIME);
                }
                //如果等待超时, 直接结束, 不给TBox回复报文
                long endTime = System.currentTimeMillis();
                if (endTime - startTime >= OperationConstant.ONLINE_COMMAND_WAIT_TIME) {
                    logger.warn("TBox(SN:{})当前CMD:{}进度上报信息投递给it, 超时未回复!", serialNumber, cmd);
                } else {
                    logger.info("TBox(SN:{})当前CMD:{}进度上报IT成功回复, 开始进行组包并回复TBox!", serialNumber, cmd);
                    outData = progressReportParamMessage(cmd);
                }
            }
        } catch (Exception e) {
            logger.error("TBox(SN:{})当前的CMD:{}进度上报回复过程因发生异常失败, 异常原因:{}", serialNumber, cmd, ThrowableUtil.getErrorInfoFromThrowable(e));
        }finally {
            getProgressReport.remove(serialNumber);
        }
        return outData;
    }

    /**
     * @Description 进度上报回复的组包方法
     * @Date 2019/2/18 15:28
     * @Param [cmd]
     * @return byte[]
     **/
    private byte[] progressReportParamMessage(String cmd) {
        byte[] param = null;
        String command = itResult.getCmd();
        String serialNumber = itResult.getSn();
        if (!command.equals(cmd)) {
            logger.warn("TBox(SN:{})当前回复指令为:{}, 目标指令为:{}", serialNumber, command, cmd);
        } else {
            int result = itResult.getResult();
            switch (result) {
                case 0:
                    param = new byte[]{OTAConstant.COMMON_RESULT_SUCCESS};
                    break;
                case 1:
                    param = new byte[]{OTAConstant.COMMON_RESULT_FAILED};
                    break;
                default:
                    logger.warn("TBox(SN:{})当前IT回复结果为无效值:{}!", serialNumber, result);
                    break;
            }
        }
        return param;
    }
}
