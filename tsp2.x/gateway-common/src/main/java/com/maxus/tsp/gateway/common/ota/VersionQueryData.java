package com.maxus.tsp.gateway.common.ota;

import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OTACommand;
import com.maxus.tsp.gateway.common.constant.OTAEncrptMode;
import com.maxus.tsp.gateway.common.constant.OTAMessageOffset;
import com.maxus.tsp.gateway.common.constant.OTAMessagePartSize;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;

/**
 * @ClassName VersionQueryData
 * @Description FOTA版本查询组包类
 * @Author zijhm
 * @Date 2019/1/23 10:10
 * @Version 1.0
 **/
public class VersionQueryData {

    private static Logger logger = LogManager.getLogger(VersionQueryData.class);

    public static OTAMessage getSendDataForVersionQuery(String[] cmd) {
        OTAMessage requestMsg = new OTAMessage();
        String serialNumber = cmd[0];
        try {
            // 创建报文头
            System.arraycopy(OTAMessage.BeginSign, 0, requestMsg.getMsgHeader(),
                    OTAMessageOffset.BEGIN_SIGN_OFFSET.value(), OTAMessagePartSize.BEGIN_SIGN_SIZE.value());
            // 设置SN号
            byte[] curSN = ByteUtil.stringToBytes(serialNumber.trim());
            if (curSN.length == 15) {
                byte[] fullSN = new byte[16];
                fullSN[0] = 0x20;
                System.arraycopy(curSN, 0, fullSN, 1, 15);
                System.arraycopy(fullSN, 0, requestMsg.getMsgHeader(), OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
                        OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
            } else if (curSN.length == 16) {
                System.arraycopy(curSN, 0, requestMsg.getMsgHeader(), OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
                        OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
            } else {
                return requestMsg;
            }
            // 设置加密方式
            if (GlobalSessionChannel.getAESKey(serialNumber) != null) {
                requestMsg.setEncryptType(OTAEncrptMode.AES);
            }
            // 报文序号
            requestMsg.setSeqNum(OpCarRemoteControl.getCurrentSeqNo());
            //下发报文没有item 长度为0
            int paramSize = 0;
            requestMsg.setCommand(OTACommand.CMD_DOWN_OTA_QUERY_NOTIFY.value());
            byte[] param = new byte[paramSize];
            requestMsg.setParam(param);
            requestMsg.setParamSize(ByteUtil.short2Byte((short) paramSize));
            logger.info("TBox(SN:{})创建版本查询下发报文:{}", serialNumber, ByteUtil.byteToHex(requestMsg.curMessage));
            requestMsg.createMessageFromParts();
            return requestMsg;
        } catch (UnsupportedEncodingException e) {
            logger.error("TBox(SN:{})组装版本查询下发报文因发生异常而失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            return null;
        }
    }
}
