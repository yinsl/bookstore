package com.maxus.tsp.gateway.ota.process;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.KafkaMsgConstantFota;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.FOTAConstant;
import com.maxus.tsp.gateway.common.constant.OTAConstant;
import com.maxus.tsp.gateway.common.model.fota.VersionUpgradeRespInfo;
import com.maxus.tsp.gateway.common.ota.OTAMessage;
import com.maxus.tsp.gateway.service.KafkaService;
import com.maxus.tsp.gateway.service.TboxService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

/**
 * @ClassName ProcVersionUpgrade
 * @Description FOTA收到车主答复后应答报文的处理类
 * @Author zijhm
 * @Date 2019/1/28 13:18
 * @Version 1.0
 **/
public class ProcVersionUpgrade extends BaseOtaProc {

    private static Logger logger = LogManager.getLogger(ProcVersionUpgrade.class);

    public ProcVersionUpgrade(KafkaService kafkaService, TboxService tboxService) {
        super(kafkaService, tboxService);
    }

    /**
     * @Description FOTA收到版本升级后的处理方法
     * @Date 2019/1/28 13:22
     * @Param [requestMsg]
     * @return byte[]
     **/
    public byte[] checkVersionUpgrade(OTAMessage requestMsg) {
        return procVersionUpgrade(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @Description FOTA收到版本升级后的具体报文处理
     * @Date 2019/1/28 13:24
     * @Param [param, serialNumber]
     * @return byte[]
     **/
    private byte[] procVersionUpgrade(byte[] param, String serialNumber) {
        logger.debug("TBox(SN:{})收到版本升级后的答复报文解密后为:{}", serialNumber, ByteUtil.byteToHex(param));
        byte[] outData = {OTAConstant.COMMON_RESULT_SUCCESS};
        try {
            if (param.length == 5) {
                VersionUpgradeRespInfo versionUpgradeRespInfo = new VersionUpgradeRespInfo();
                //ID
                byte[] _id = new byte[FOTAConstant.FOTA_PARAM_ID_OFFSET];
                System.arraycopy(param, 0, _id, 0, FOTAConstant.FOTA_PARAM_ID_OFFSET);
                long id = ByteUtil.getUnsignedLong(_id);
                versionUpgradeRespInfo.setId(id);
                //result
                int result = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(param[FOTAConstant.FOTA_PARAM_ID_OFFSET]));
                versionUpgradeRespInfo.setResult(result);
                //dateTime
                String dateTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");

                //往网关kafka投递
                logger.debug("TBox(SN:{})收到版本升级后答复报文信息:{}", serialNumber, JSONObject.toJSONString(versionUpgradeRespInfo));
                kafkaService.sndMesForTemplate(KafkaMsgConstantFota.TOPIC_SELF_VERSION_UPGRADE_UP, serialNumber + "_" + JSONObject.toJSONString(versionUpgradeRespInfo) + "_" + dateTime, serialNumber);
            } else {
                logger.warn("TBox(SN:{})当前收到版本升级后的答复报文长度错误, 报文丢弃, 当前报文:{}", serialNumber, ByteUtil.byteToHex(param));
                outData[0] = OTAConstant.COMMON_RESULT_FAILED;
            }
        } catch (Exception e) {
            logger.error("TBox(SN:{})在解析收到版本升级后的答复报文过程中发生异常, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            outData[0] = OTAConstant.COMMON_RESULT_FAILED;
        }
        return outData;
    }
}
