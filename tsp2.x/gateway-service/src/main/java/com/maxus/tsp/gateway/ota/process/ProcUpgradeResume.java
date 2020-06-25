package com.maxus.tsp.gateway.ota.process;

import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.KafkaMsgConstantFota;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.FOTAConstant;
import com.maxus.tsp.gateway.common.constant.OTAConstant;
import com.maxus.tsp.gateway.common.model.fota.UpgradeResumeRespInfo;
import com.maxus.tsp.gateway.common.ota.OTAMessage;
import com.maxus.tsp.gateway.service.KafkaService;
import com.maxus.tsp.gateway.service.TboxService;

/**
*@Title ProcUpgradeResume.java
*@description fota继续升级的报文解析类
*@time 2019年2月12日 上午9:59:02
*@author wqgzf
*@version 1.0
**/
public class ProcUpgradeResume extends BaseOtaProc {
	 private static Logger logger = LogManager.getLogger(ProcUpgradeResume.class);

	 public ProcUpgradeResume(KafkaService kafkaService, TboxService tboxService) {
	    super(kafkaService, tboxService);
	 }
	 
	 public byte[] checkUpgradeResume(OTAMessage requestMsg) {
	        return procUpgradeResume(requestMsg.getParam(), requestMsg.getSerialNumber());
	    }

	private byte[] procUpgradeResume(byte[] param, String serialNumber) {
        logger.debug("TBox(SN:{})收到继续升级后的答复报文解密后为:{}", serialNumber, ByteUtil.byteToHex(param));
        byte[] outData = {OTAConstant.COMMON_RESULT_SUCCESS};
        try {
            if (param.length == 5) {
                UpgradeResumeRespInfo upgradeResumeRespInfo = new UpgradeResumeRespInfo();
                //ID
                byte[] _id = new byte[FOTAConstant.FOTA_PARAM_ID_OFFSET];
                System.arraycopy(param, 0, _id, 0, FOTAConstant.FOTA_PARAM_ID_OFFSET);
                long id = ByteUtil.getUnsignedLong(_id);
                upgradeResumeRespInfo.setId(id);
                //result
                int result = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(param[FOTAConstant.FOTA_PARAM_ID_OFFSET]));
                upgradeResumeRespInfo.setResult(result);
                //dateTime
                String dateTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");

                //往网关kafka投递
                logger.debug("TBox(SN:{})收到继续升级后答复报文信息:{}", serialNumber, JSONObject.toJSONString(upgradeResumeRespInfo));
                kafkaService.sndMesForTemplate(KafkaMsgConstantFota.TOPIC_SELF_UPGRADE_RESUME_UP, serialNumber + "_" + JSONObject.toJSONString(upgradeResumeRespInfo) + "_" + dateTime, serialNumber);
            } else {
                logger.warn("TBox(SN:{})当前收到继续升级后的答复报文长度错误, 报文丢弃, 当前报文:{}", serialNumber, ByteUtil.byteToHex(param));
                outData[0] = OTAConstant.COMMON_RESULT_FAILED;
            }
        } catch (Exception e) {
            logger.error("TBox(SN:{})在解析收到继续升级后的答复报文过程中发生异常, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            outData[0] = OTAConstant.COMMON_RESULT_FAILED;
        }
        return outData;
    }
}
