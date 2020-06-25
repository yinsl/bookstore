package com.maxus.tsp.gateway.ota.process;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.KafkaMsgConstantFota;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.model.fota.AgreeUpgradeReqInfo;
import com.maxus.tsp.gateway.common.model.fota.AgreeUpgradeRespInfo;
import com.maxus.tsp.gateway.common.ota.OTAMessage;
import com.maxus.tsp.gateway.service.KafkaService;
import com.maxus.tsp.gateway.service.TboxService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Hashtable;

/**
 * @ClassName ProcAgreeUpgrade
 * @Description 车主AVN端是否同意升级报文的处理类
 * @Author ssh
 * @Date 2019/2/20 16:24
 * @Version 1.0
 **/
public class ProcAgreeUpgrade extends BaseOtaProc {
    //日志
    private static Logger logger = LogManager.getLogger(ProcAgreeUpgrade.class);

    //rvm返回的车主AVN端是否同意升级的结果
    private AgreeUpgradeRespInfo agreeUpgradeRespInfo;

    public void setAgreeUpgradeRespInfo(AgreeUpgradeRespInfo agreeUpgradeRespInfo) {
        this.agreeUpgradeRespInfo = agreeUpgradeRespInfo;
    }

    //存储当前请求对象
    private static Hashtable<String, ProcAgreeUpgrade> getAgreeUpgrade = new Hashtable<>();

    //获取存储的请求对象

    public static ProcAgreeUpgrade getProcAgreeUpgrade(String serialNumber) {
        return getAgreeUpgrade.get(serialNumber);
    }

    public ProcAgreeUpgrade(KafkaService kafkaService, TboxService tboxService) {
        super(kafkaService, tboxService);
    }

    /**
     * @return byte[]
     * @Description Fota车主AVN端是否同意升级报文的处理方法
     * @Date 2019/2/20 16:49
     * @Param [requestMsg]
     **/
    public byte[] checkAgreeUpgrade(OTAMessage requestMsg) {
        return procAgreeUpgrade(requestMsg.getParam(), requestMsg.getSerialNumber());
    }


    /**
     * @return byte[]
     * @description 处理车主AVN端是否同意升级报文
     * @Date 2019/2/20 17:00
     */
    private byte[] procAgreeUpgrade(byte[] param, String serialNumber) {
        logger.info("TBox(SN:{})当前车主AVN端是否同意升级的报文解密后参数部分为:{},", serialNumber, ByteUtil.byteToHex(param));
        boolean result = false;
        byte[] outData = null;

        try {
            if (param.length == 5) {
                AgreeUpgradeReqInfo agreeUpgradeReqInfo = new AgreeUpgradeReqInfo();

                //ID
                byte[] _id = new byte[4];
                System.arraycopy(param,0,_id,0,4);
                long id = ByteUtil.byte2Int(_id);
                agreeUpgradeReqInfo.setId(id);
                //operate
//                byte[] _operate = new byte[1];
//                System.arraycopy(param,4,_operate,0,1);
                int operate = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(param[4]));
                if (operate<0||operate>2) {
                    logger.warn("TBox(SN:{})车主AVN端是否同意升级车主选择结果为无效值:{}！",serialNumber,operate);
                }
//                Integer operate = ByteUtil.byte2Int(_operate);
                agreeUpgradeReqInfo.setData(operate);

                agreeUpgradeReqInfo.setSn(serialNumber);
                agreeUpgradeReqInfo.setSeqNo();
                agreeUpgradeReqInfo.setEventTime(System.currentTimeMillis());
                logger.debug("TBox(SN:{})车主AVN端是否同意升级报文结果为:{}", serialNumber, JSONObject.toJSONString(agreeUpgradeReqInfo));
                result = kafkaService.transferItData(KafkaMsgConstantFota.TOPIC_IT_AGREE_UPGRADE, agreeUpgradeReqInfo, serialNumber);
            } else {
                logger.warn("TBox(SN:{})车主AVN端是否同意升级报文长度错误, 报文丢弃!",serialNumber);
            }
        } catch (Exception e) {
            logger.error("TBox(SN:{})因发生异常失败,异常原因:{}",serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
        }
        try {
            if (result) {
                //往IT Kafka发送OperationConstant.ONLINE_COMMAND_WAIT_TIME请求成功
                getAgreeUpgrade.put(serialNumber,ProcAgreeUpgrade.this);
                long startTime = System.currentTimeMillis();
                logger.info("TBox(SN:{})的车主AVN端是否同意升级信息已经投递给it,等待it响应!",serialNumber);
                //等待it回复当前车主AVN端是否同意升级处理结果,10s内下发结果给TBox,10s外超时
                synchronized (ProcAgreeUpgrade.this) {
                    ProcAgreeUpgrade.this.wait(OperationConstant.ONLINE_COMMAND_WAIT_TIME);
                }
                //如果等待超时，直接结束，不给TBox回复报文
                long endTime = System.currentTimeMillis();
                if (endTime - startTime >= OperationConstant.ONLINE_COMMAND_WAIT_TIME) {
                    logger.warn("TBox(SN:{})当前车主AVN端是否同意升级信息投递给it,超时未回复!", serialNumber);
                } else {
                    logger.info("TBox(SN:{})当前车主AVN端是否同意升级IT成功回复,开始进行组包并回复TBox!",serialNumber);
                    outData = agreeUpgradeParamMessage();
                }
            }
        } catch (Exception e) {
            logger.error("TBox(SN:{})当前的车主AVN端是否同意升级回复过程因发生异常失败,异常原因:{}",serialNumber,ThrowableUtil.getErrorInfoFromThrowable(e));
        }
        return outData;
    }

    /**
     * @param
     * @return byte[]
     * @Description 车主AVN端是否同意升级回复的组包报文
     * @Date 2019/2/21 10:41
     */
    private byte[] agreeUpgradeParamMessage() {
        String serialNumber = agreeUpgradeRespInfo.getSn();
        byte[] param = new byte[5];
        Long id = agreeUpgradeRespInfo.getId();
        if (id != null) {
            byte[] _id = ByteUtil.int2Byte(id.intValue());
            System.arraycopy(_id, 0, param, 0, 4);
            Integer result = agreeUpgradeRespInfo.getResult();
            if (result != null) {
                if (result < 0 || result > 1) {
                    logger.warn("TBox(SN:{})当前IT回复结果为无效值:{}", serialNumber, result);
                }
//                byte[] _result = ByteUtil.int2Byte(result);
                byte[] _result = new byte[1];
                if  (result > 255){
                    logger.warn("TBox(SN:{})车主AVN端IT回复结果超限",serialNumber);
                    return null;
                } else {
                    _result[0] = result.byteValue();
                    System.arraycopy(_result, 0, param, 4, 1);
                    logger.info("TBox(SN:{})车主AVN端IT回复组包下发内容：{}", serialNumber, ByteUtil.byteToHex(param));
                    return param;
                }
            } else {
                logger.warn("TBox(SN:{})当前IT回复消息缺失处理结果Result",serialNumber);
                return null;
            }
        } else {
            logger.warn("TBox(SN:{})当前IT回复消息缺失升级任务ID",serialNumber);
            return null;
        }
    }
}
