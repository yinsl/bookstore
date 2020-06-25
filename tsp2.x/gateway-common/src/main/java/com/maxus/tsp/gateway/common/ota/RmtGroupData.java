package com.maxus.tsp.gateway.common.ota;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OTACommand;
import com.maxus.tsp.gateway.common.constant.OTAEncrptMode;
import com.maxus.tsp.gateway.common.constant.OTAMessageOffset;
import com.maxus.tsp.gateway.common.constant.OTAMessagePartSize;
import com.maxus.tsp.gateway.common.model.RmtGroupRequestInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Calendar;

/**
 * @ClassName RmtGroupData
 * @Description 组合远控组装报文类
 * @Author zijhm
 * @Date 2019/2/14 10:09
 * @Version 1.0
 **/
public class RmtGroupData {

    private static final Logger logger = LogManager.getLogger(RmtGroupData.class);

    /**
     * @Description 组合远控组包方法
     * @Date 2019/2/14 10:12
     * @Param [cmd]
     * @return com.maxus.tsp.gateway.common.ota.OTAMessage
     **/
    public static OTAMessage getSendDataForRmtGroup(String[] cmd) {
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
                // requestMsg.setEncryptType(OTAEncrptMode.NONE);
            }
            // 报文序号
            requestMsg.setSeqNum(OpCarRemoteControl.getCurrentSeqNo());
            int paramSize = 0;
            if (cmd[1] != null) {
                RmtGroupRequestInfo rmtGroupRequestInfo = JSONObject.parseObject(cmd[1], RmtGroupRequestInfo.class);
                if ("REMOTECTRL".equals(rmtGroupRequestInfo.getOtaType())) {
                    // 控制指令
                    requestMsg.setCommand(OTACommand.CMD_DOWN_REMOTECTRL.value());
                    // 定义param
                    paramSize = 12;
                    byte[] param = new byte[paramSize];
                    // Command
                    String comd = rmtGroupRequestInfo.getComd();
                    byte[] _comd = ByteUtil.hexStringToBytes(comd);
                    if (comd.length() == 2) {
                        System.arraycopy(_comd, 0, param, 1, 1);
                    } else {
                        System.arraycopy(_comd, 0, param, 0, 2);
                    }
                    // Temperature
                    String temperature = rmtGroupRequestInfo.getTemperature();
                    if (!StringUtils.isBlank(temperature)) {
                        byte[] _temperature = ByteUtil.hexStringToBytes(temperature);
                        System.arraycopy(_temperature, 0, param, 2, 1);
                    } else {
                        param[2] = (byte) 0x00;
                    }
                    // Flag
                    String value = rmtGroupRequestInfo.getValue();
                    byte[] _value = ByteUtil.hexStringToBytes(value);
                    if (value.length() == 2) {
                        System.arraycopy(_value, 0, param, 4, 1);
                    } else {
                        System.arraycopy(_value, 0, param, 3, 2);
                    }
                    // currentTime
                    byte[] currentTime = ByteUtil.CreateDateTimeBytes(Calendar.getInstance());
                    System.arraycopy(currentTime, 0, param, 5, 7);
                    requestMsg.setParam(param);
                    logger.info("TBox(SN:{})组建组合远控REMOTECTRL下发报文参数部分为:{}", serialNumber, ByteUtil.byteToHex(param));
                } else if ("REMOTECTRL_EXT".equals(rmtGroupRequestInfo.getOtaType())) {
                    // 控制指令
                    requestMsg.setCommand(OTACommand.CMD_DOWN_REMOTECTRL_EXT.value());
                    int extparamSize = rmtGroupRequestInfo.getParamSize();
                    paramSize = 9 + extparamSize;
                    byte[] param = new byte[paramSize];
                    // 放入paramSize
                    byte[] _extparamSize = new byte[2];
                    _extparamSize = ByteUtil.short2Byte((short) extparamSize);
                    System.arraycopy(_extparamSize, 0, param, 0, 2);
                    // 放入param
                    String extparam = rmtGroupRequestInfo.getParam();
                    byte[] _extparam = ByteUtil.hexStringToBytes(extparam);
                    System.arraycopy(_extparam, 0, param, 2, extparamSize);
                    // 放入currentTime
                    byte[] currentTime = ByteUtil.CreateDateTimeBytes(Calendar.getInstance());
                    System.arraycopy(currentTime, 0, param, paramSize - 7, 7);
                    requestMsg.setParam(param);
                    logger.info("TBox(SN:{})组建远控REMOTECTRL_EXT报文成功，组建报文的指令参数为:{}", serialNumber, ByteUtil.byteToHex(param));
                }
            } else {
                logger.warn("TBox(SN:{})组装远程组合控制下发报文时, 控制请求内容为空!", serialNumber);
            }
            requestMsg.setParamSize(ByteUtil.short2Byte((short) paramSize));
            requestMsg.createMessageFromParts();
            logger.info("TBox(SN:{})组建远程组合控制下发报文成功:{}", serialNumber, ByteUtil.byteToHex(requestMsg.curMessage));
            return requestMsg;
        } catch (Exception e) {
            logger.error("TBox(SN:{})组合远控组装下发报文因发生异常失败, 异常原因:{}", serialNumber,
                    ThrowableUtil.getErrorInfoFromThrowable(e));
            return null;
        }
    }
}
