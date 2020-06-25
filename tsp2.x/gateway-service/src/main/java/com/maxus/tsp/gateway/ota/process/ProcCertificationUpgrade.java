package com.maxus.tsp.gateway.ota.process;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.KafkaMsgConstantFota;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OTAConstant;
import com.maxus.tsp.gateway.common.model.fota.CertificationUpgradeRespInfo;
import com.maxus.tsp.gateway.common.ota.OTAMessage;
import com.maxus.tsp.gateway.service.KafkaService;
import com.maxus.tsp.gateway.service.TboxService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

/**
 * @ClassName ProcCertificationUpgrade
 * @Description FOTA证书更新报文处理类
 * @Author ssh
 * @Date 2019/2/12 10:57
 * @Version 1.0
 **/
public class ProcCertificationUpgrade extends BaseOtaProc {
    private static Logger logger = LogManager.getLogger(ProcCertificationUpgrade.class);

    public ProcCertificationUpgrade (KafkaService kafkaService, TboxService tboxService) {
        super(kafkaService, tboxService);
    }
    /**
     * @Description FOTA收到证书更新后的处理方法
     * @Date 2019/2/12 11:10
     * @Param [requestMsg]
     * @return byte[]
     **/
    public byte[] checkCertificationUpgrade(OTAMessage requestMsg) {
        return procCertificationUpgrade(requestMsg.getParam(), requestMsg.getSerialNumber());
    }


    /**
     * @Description FOTA收到证书更新后的具体报文处理
     * @Date 2019/2/12 11:14
     * @Param [param, serialNumber]
     * @return byte[]
     **/
    private byte[] procCertificationUpgrade(byte[] param, String serialNumber) {
        logger.debug("TBox(SN:{})收到证书更新后的答复报文解密后为:{}", serialNumber, ByteUtil.byteToHex(param));
        byte[] outData = {OTAConstant.COMMON_RESULT_SUCCESS};
        try {
            if (param.length == 1) {
                CertificationUpgradeRespInfo certificationUpgradeRespInfo = new CertificationUpgradeRespInfo();

                //result
                int result = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(param[0]));
                certificationUpgradeRespInfo.setData(result);

                //dateTime
                String dateTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");

                //往网关kafka投递
                logger.debug("TBox(SN:{})收到证书更新后答复报文信息:{}", serialNumber, JSONObject.toJSONString(certificationUpgradeRespInfo));
                kafkaService.sndMesForTemplate(KafkaMsgConstantFota.TOPIC_SELF_CERTIFICATION_UPGRADE_UP, serialNumber + "_" + JSONObject.toJSONString(certificationUpgradeRespInfo) + "_" + dateTime, serialNumber);
            } else {
                logger.warn("TBox(SN:{})当前收到证书更新后的答复报文长度错误, 报文丢弃, 当前报文:{}", serialNumber, ByteUtil.byteToHex(param));
                outData[0] = OTAConstant.COMMON_RESULT_FAILED;
            }
        } catch (Exception e) {
            logger.error("TBox(SN:{})在解析收到证书更新后的答复报文过程中发生异常, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            outData[0] = OTAConstant.COMMON_RESULT_FAILED;
        }
        return outData;
    }
}
