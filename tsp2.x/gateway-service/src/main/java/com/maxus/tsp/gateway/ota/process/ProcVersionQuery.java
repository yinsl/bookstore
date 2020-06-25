package com.maxus.tsp.gateway.ota.process;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.KafkaMsgConstantFota;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OTAConstant;
import com.maxus.tsp.gateway.common.model.BaseFotaCtrlItResp;
import com.maxus.tsp.gateway.common.ota.OTAMessage;
import com.maxus.tsp.gateway.service.KafkaService;
import com.maxus.tsp.gateway.service.TboxService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

/**
 * @ClassName ProcVersionQuery
 * @Description FOTA版本查询报文处理类
 * @Author zijhm
 * @Date 2019/1/23 14:02
 * @Version 1.0
 **/
public class ProcVersionQuery extends BaseOtaProc{

    private static Logger logger = LogManager.getLogger(ProcVersionQuery.class);

    public ProcVersionQuery(KafkaService kafkaService, TboxService tboxService) {
        super(kafkaService, tboxService);
    }

    /**
     * @Description 收到版本查询通知的答复报文的处理
     * @Date 2019/1/23 14:16
     * @Param [requestMsg]
     * @return byte[]
     **/
    public byte[] checkVersionQuery(OTAMessage requestMsg) {
        return procVersionQuery(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @Description 解析收到版本查询通知的答复报文
     * @Date 2019/1/23 14:28
     * @Param [param, serialNumber]
     * @return byte[]
     **/
    private byte[] procVersionQuery(byte[] param, String serialNumber) {
        logger.debug("TBox(SN:{})收到版本查询通知后的答复报文解密后为:{}", serialNumber, ByteUtil.byteToHex(param));
        byte[] outData = {OTAConstant.COMMON_RESULT_SUCCESS};
        try {
            if (param.length == 1) {
                BaseFotaCtrlItResp baseFotaCtrlItResp = new BaseFotaCtrlItResp();
                baseFotaCtrlItResp.setSn(serialNumber);
                //result
                int result = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(param[0]));
                baseFotaCtrlItResp.setData(result);
                //dateTime
                String dateTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");

                //往网关kafka投递
                kafkaService.sndMesForTemplate(KafkaMsgConstantFota.TOPIC_SELF_VERSION_QUERY_UP, serialNumber + "_" + JSONObject.toJSONString(baseFotaCtrlItResp) + "_" + dateTime, serialNumber);
            } else {
                logger.warn("TBox(SN:{})收到版本查询通知后的答复报文长度错误, 报文丢弃!", serialNumber);
                outData[0] = OTAConstant.COMMON_RESULT_FAILED;
            }
        } catch (Exception e) {
            logger.error("TBox(SN:{})在解析收到版本查询通知后的答复报文过程很重发生异常, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            outData[0] = OTAConstant.COMMON_RESULT_FAILED;
        }
        return outData;
    }
}
