package com.maxus.tsp.gateway.common.ota;

import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OTABlueToothCtrlConstant;
import com.maxus.tsp.gateway.common.constant.OTACommand;
import com.maxus.tsp.gateway.common.constant.OTAEncrptMode;
import com.maxus.tsp.gateway.common.constant.OTAMessageOffset;
import com.maxus.tsp.gateway.common.constant.OTAMessagePartSize;
import com.maxus.tsp.gateway.common.model.BlueToothAddRequestInfo;
import com.maxus.tsp.gateway.common.model.BlueToothRequestInfo;

/**
 * 蓝牙报文组包方法
 *
 * @author 顾浩炜
 */
@Component
public class BlueToothData {

    static Logger logger = LogManager.getLogger(BlueToothData.class);

    @Autowired
    private RedisAPI redisAPI;

    public OTAMessage getSendDataForBlueTooth(String[] cmd) {
        OTAMessage requestMsg = new OTAMessage();
        String tboxSN = cmd[0];
        try {
            // 创建报文头
            System.arraycopy(OTAMessage.BeginSign, 0, requestMsg.getMsgHeader(),
                    OTAMessageOffset.BEGIN_SIGN_OFFSET.value(), OTAMessagePartSize.BEGIN_SIGN_SIZE.value());
            // 设置SN号
            byte[] curSN = ByteUtil.stringToBytes(tboxSN.trim());
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
            if (redisAPI.getHash(RedisConstant.SECURITY_KEY, tboxSN) != null) {
                requestMsg.setEncryptType(OTAEncrptMode.AES);
            }
            // 报文序号
            requestMsg.setSeqNum(OpCarRemoteControl.getCurrentSeqNo());
            int paramSize;
            if (cmd[1] != null) {
                BlueToothRequestInfo requestInfo = JSONObject.parseObject(cmd[1], BlueToothRequestInfo.class);
                Calendar cal = Calendar.getInstance();
                switch (requestInfo.getComd()) {
                    case OTABlueToothCtrlConstant.COMMAND_SEND_VERIFICATION_CODE:
                        requestMsg.setCommand(OTACommand.CMD_DOWN_VERIFICATION_CODE.value());
                        paramSize = 11;
                        byte[] paramVerify = new byte[paramSize];
                        cal.setTimeInMillis(System.currentTimeMillis());
                        //currentTime
                        System.arraycopy(ByteUtil.CreateDateTimeBytes(cal), 0, paramVerify, 0, 7);
                        //code
                        System.arraycopy(ByteUtil.hexStringToBytes(requestInfo.getValue()), 0, paramVerify, 7, 4);
                        requestMsg.setParam(paramVerify);
                        break;
                    case OTABlueToothCtrlConstant.COMMAND_ADD_BT_KEY:
                        requestMsg.setCommand(OTACommand.CMD_DOWN_ADD_BT_KEY.value());
                        paramSize = 42;
                        BlueToothAddRequestInfo addInfo = JSONObject.parseObject(requestInfo.getValue(), BlueToothAddRequestInfo.class);
                        byte[] param = new byte[paramSize];
                        //currentTime
                        long currentTimeMillis = System.currentTimeMillis();
                        cal.setTimeInMillis(currentTimeMillis);
                        System.arraycopy(ByteUtil.CreateDateTimeBytes(cal), 0, param, 0, 7);
                        //BtKeyID
                        System.arraycopy(ByteUtil.int2Byte(addInfo.getBtKeyID()), 0, param, 7, 4);
                        //authkey
                        System.arraycopy(ByteUtil.stringToBytes(addInfo.getAuthKey()), 0, param, 11, 16);
                        //startDateTime
                        cal.setTimeInMillis(addInfo.getStartDateTime());
                        System.arraycopy(ByteUtil.CreateDateTimeBytes(cal), 0, param, 27, 7);
                        //endTime
                        cal.setTimeInMillis(addInfo.getEndDateTime());
                        System.arraycopy(ByteUtil.CreateDateTimeBytes(cal), 0, param, 34, 7);
                        //Permissions
                        byte[] permission = new byte[1];
                        int permissions = addInfo.getPermissions();
                        permission[0] = (byte) permissions;
                        System.arraycopy(permission, 0, param, 41, 1);
                        requestMsg.setParam(param);
                        break;
                    case OTABlueToothCtrlConstant.COMMAND_DEL_BT_KEY:
                        requestMsg.setCommand(OTACommand.CMD_DOWN_DEL_BT_KEY.value());
                        paramSize = 11;
                        byte[] paramDelBtKey = new byte[paramSize];
                        cal.setTimeInMillis(System.currentTimeMillis());
                        //currentTime
                        System.arraycopy(ByteUtil.CreateDateTimeBytes(cal), 0, paramDelBtKey, 0, 7);
                        //btKeyId
                        System.arraycopy(ByteUtil.int2Byte(Integer.parseUnsignedInt(requestInfo.getValue())), 0, paramDelBtKey, 7, 4);
                        requestMsg.setParam(paramDelBtKey);
                        break;
                    case OTABlueToothCtrlConstant.COMMAND_GET_BT_KEY:
                        requestMsg.setCommand(OTACommand.CMD_DOWN_GET_BT_KEY.value());
                        paramSize = 7;
                        //currentTime
                        cal.setTimeInMillis(System.currentTimeMillis());
                        requestMsg.setParam(ByteUtil.CreateDateTimeBytes(cal));
                        break;
                    default:
                        return null;
                }
                // 创建下发报文
                requestMsg.setParamSize(ByteUtil.short2Byte((short) paramSize));
                requestMsg.createMessageFromParts();
                logger.debug("TBox(SN:{})创建下发报文:{}", tboxSN, ByteUtil.byteToHex(requestMsg.curMessage));
            }
            return requestMsg;
        } catch (Exception e) {
            logger.error("TBox(SN:{})组装蓝牙配置报文失败，原因：{}", tboxSN, ThrowableUtil.getErrorInfoFromThrowable(e));
            return null;
        }
    }
}
