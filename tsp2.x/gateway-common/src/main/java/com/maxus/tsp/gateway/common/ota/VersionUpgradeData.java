package com.maxus.tsp.gateway.common.ota;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.*;
import com.maxus.tsp.gateway.common.model.fota.VersionUpgradeReqInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Calendar;

/**
 * @ClassName VersionUpgradeData
 * @Description FOTA版本升级组包类
 * @Author zijhm
 * @Date 2019/1/28 12:25
 * @Version 1.0
 **/
public class VersionUpgradeData {

    private static final Logger logger = LogManager.getLogger(VersionUpgradeData.class);

    /**
     * @Description FOTA版本升级组包方法
     * @Date 2019/1/28 12:28
     * @Param [cmd]
     * @return com.maxus.tsp.gateway.common.ota.OTAMessage
     **/
    public static OTAMessage getSendDataForVersionUpgrade(String[] cmd) {
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

            if (cmd[1] != null) {
                VersionUpgradeReqInfo versionUpgradeReqInfo = JSONObject.parseObject(cmd[1], VersionUpgradeReqInfo.class);
                int paramSize = FOTAConstant.VERSION_UPGRADE_DOWN_PARAM_OFFSET;
                requestMsg.setCommand(OTACommand.CMD_DOWN_UPGRADE_REQ.value());
                byte[] param = new byte[paramSize];
                //ID
                int id = versionUpgradeReqInfo.getId();
                byte[] _id = ByteUtil.int2Byte(id);
                System.arraycopy(_id, 0, param, 0, FOTAConstant.FOTA_PARAM_ID_OFFSET);
                //operate(后为choice)
                int operate = versionUpgradeReqInfo.getOperate();
                byte[] _operate = ByteUtil.int2Byte(operate);
                System.arraycopy(_operate, 0, param, 4, FOTAConstant.FOTA_PARAM_OPERATE_OFFSET);
                // currentDate,currentTime
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(versionUpgradeReqInfo.getEventTime());
                byte[] currentTime = ByteUtil.CreateDateTimeBytes(calendar);
                System.arraycopy(currentTime,0,param,5,FOTAConstant.FOTA_PARAM_DATA_TIME_OFFSET);

                requestMsg.setParam(param);
                requestMsg.setParamSize(ByteUtil.short2Byte((short) paramSize));
                logger.info("TBox(SN:{})创建版本升级下发报文:{}", serialNumber, ByteUtil.byteToHex(requestMsg.curMessage));
                requestMsg.createMessageFromParts();
            } else {
                logger.warn("TBox(SN:{})创建版本升级下发报文时, 参数内容为空!", serialNumber);
            }
            return requestMsg;

        } catch (Exception e) {
            logger.error("TBox(SN:{})组装版本升级下发报文因发生异常而失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            return null;
        }
    }
}
