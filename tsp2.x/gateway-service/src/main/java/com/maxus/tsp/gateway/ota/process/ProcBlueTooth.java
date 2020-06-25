package com.maxus.tsp.gateway.ota.process;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OTABlueToothCtrlConstant;
import com.maxus.tsp.gateway.common.model.BlueToothCtrlRespInfo;
import com.maxus.tsp.gateway.common.ota.OTAMessage;
import com.maxus.tsp.gateway.service.KafkaService;
import com.maxus.tsp.gateway.service.TboxService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

/**
 * @ClassName: ProcBlueTooth.java
 * @Description: 解析蓝牙上行处理结果报文
 * @author: zijhm
 * @date: 2018/12/12 9:26
 * @version: 1.0
 */
public class ProcBlueTooth extends BaseOtaProc {

    public final static Logger logger = LogManager.getLogger(ProcBlueTooth.class);

    public ProcBlueTooth(KafkaService kafkaService, TboxService tboxService) {
        super(kafkaService, tboxService);
    }

    /**
     * @param requestMsg
     * @Title: checkDataAddBTKey
     * @Description: 增加蓝牙钥匙
     */
    public void checkDataAddBTKey(OTAMessage requestMsg) {
        inDataAddBTKey(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @param datagramBytes
     * @param tboxSn
     * @Title: inDataAddBTKey
     * @Description: 增加蓝牙钥匙
     */

    private void inDataAddBTKey(byte[] datagramBytes, String tboxSn) {
        logger.info("TBox(sn:{}):添加蓝牙钥匙上报的报文参数内容为:{}", tboxSn, ByteUtil.byteToHex(datagramBytes));
        BlueToothCtrlRespInfo blueToothCtrlRespInfo = new BlueToothCtrlRespInfo();
        try {
            if (datagramBytes.length != OTABlueToothCtrlConstant.ADD_DEL_KEY_TBOX_RESULT_LEN) {
                logger.warn("TBox(sn:{}):添加蓝牙钥匙上报的报文参数内容长度不对！", tboxSn);
            } else {
                Integer result = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(datagramBytes[OTABlueToothCtrlConstant.ADD_DEL_KEY_TBOX_RESULT_OFFSET]));
                Date curTime = new Date();
                String eventTime = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");
                blueToothCtrlRespInfo.setCmd(OTABlueToothCtrlConstant.COMMAND_ADD_BT_KEY);
                blueToothCtrlRespInfo.setResult(result);
                kafkaService.sndAddBtKeyResp(tboxSn + "_" + JSONObject.toJSONString(blueToothCtrlRespInfo) + "_" + eventTime, tboxSn);
            }
        } catch (Exception e) {
            logger.error("TBox(sn:{}):解析添加蓝牙钥匙上报的报文参数内容出错，原因:{}！", tboxSn,
                    ThrowableUtil.getErrorInfoFromThrowable(e));
        }
    }


    /**
     * @param requestMsg
     * @Title: checkDataDelBTKey
     * @Description: 删除蓝牙钥匙
     */
    public void checkDataDelBTKey(OTAMessage requestMsg) {
        inDataDelBTKey(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @param datagramBytes
     * @param tboxSn
     * @Title: inDataDelBTKey
     * @Description: 删除蓝牙钥匙
     */
    private void inDataDelBTKey(byte[] datagramBytes, String tboxSn) {
        logger.info("TBox(sn:{}):删除蓝牙钥匙上报的报文参数内容为:{}", tboxSn, ByteUtil.byteToHex(datagramBytes));
        BlueToothCtrlRespInfo blueToothCtrlRespInfo = new BlueToothCtrlRespInfo();
        try {
            if (datagramBytes.length != OTABlueToothCtrlConstant.ADD_DEL_KEY_TBOX_RESULT_LEN) {
                logger.warn("TBox(sn:{}):删除蓝牙钥匙上报的报文参数内容长度不对！", tboxSn);
            } else {
                blueToothCtrlRespInfo.setCmd(OTABlueToothCtrlConstant.COMMAND_DEL_BT_KEY);
                Integer result = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(datagramBytes[OTABlueToothCtrlConstant.ADD_DEL_KEY_TBOX_RESULT_OFFSET]));
                if (result > OTABlueToothCtrlConstant.ADD_DEL_KEY_TBOX_RESULT_MAX_NO) {
                    logger.warn("TBox(sn:{}):删除蓝牙钥匙上报的Result参数内容不支持，result:{}", tboxSn, result);
                }
                Date curTime = new Date();
                String eventTime = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");
                blueToothCtrlRespInfo.setResult(result);
                kafkaService.sndDelBtKeyRep(tboxSn + "_" + JSONObject.toJSONString(blueToothCtrlRespInfo) + "_" + eventTime, tboxSn);
            }
        } catch (Exception e) {
            logger.error("TBox(sn:{}):解析删除蓝牙钥匙上报的报文参数内容出错，原因:{}!", tboxSn,
                    ThrowableUtil.getErrorInfoFromThrowable(e));
        }
    }

    /**
     * @param requestMsg
     * @Title: checkDataGetBTKey
     * @Description: 获取蓝牙钥匙
     */
    public void checkDataGetBTKey(OTAMessage requestMsg) {
        inDataGetBTkey(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @param datagramBytes
     * @param tboxSn
     * @Title: inDataGetBTkey
     * @Description: 获取蓝牙钥匙
     */
    private void inDataGetBTkey(byte[] datagramBytes, String tboxSn) {
        logger.info("TBox(sn:{}):获取蓝牙钥匙上报的报文参数内容为:{}", tboxSn, ByteUtil.byteToHex(datagramBytes));
        BlueToothCtrlRespInfo blueToothCtrlRespInfo = new BlueToothCtrlRespInfo();
        try {
            // 解析过后的JsonResult
            String returnResult = "";
            // tbox返回的result
            byte[] _result = new byte[1];
            System.arraycopy(datagramBytes, 0, _result, 0, 1);
            Integer result = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_result[0]));
            blueToothCtrlRespInfo.setResult(result);
            if (result == 0) {
                byte[] _btKeyNum = new byte[1];
                System.arraycopy(datagramBytes, 1, _btKeyNum, 0, 1);
                int btKeyNum = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_btKeyNum[0]));
                blueToothCtrlRespInfo.setBtKeyNum(btKeyNum);
                int paramSize = OTABlueToothCtrlConstant.GET_BTKEY_FIX_BYTE + btKeyNum * OTABlueToothCtrlConstant.BTKEY_ID_LENGTH;
                if (paramSize == datagramBytes.length) {
                    long[] btKeyList = new long[btKeyNum];
                    for (int i = 0; i < btKeyNum; i++) {
                        byte[] _btKeyList = new byte[OTABlueToothCtrlConstant.BTKEY_ID_LENGTH];
                        System.arraycopy(datagramBytes, 2 + i * OTABlueToothCtrlConstant.BTKEY_ID_LENGTH, _btKeyList, 0, OTABlueToothCtrlConstant.BTKEY_ID_LENGTH);
                        btKeyList[i] = ByteUtil.getUnsignedLong(_btKeyList);
                    }
                    blueToothCtrlRespInfo.setBtKeyList(btKeyList);
                    //获取tbox返回的BtKeyMax
                    int btKeyMax = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(datagramBytes[2 + btKeyNum * OTABlueToothCtrlConstant.BTKEY_ID_LENGTH]));
                    blueToothCtrlRespInfo.setBtKeyMax(btKeyMax);
                } else {
                    logger.warn("TBox(sn:{}):获取蓝牙钥匙上报的报文参数长度错误", tboxSn);
                    return;
                }
            }
            blueToothCtrlRespInfo.setCmd(OTABlueToothCtrlConstant.COMMAND_GET_BT_KEY);
            Date curTime = new Date();
            String eventTime = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");
            returnResult = JSONObject.toJSONString(blueToothCtrlRespInfo);
            logger.debug("TBox(sn:{}):获取的蓝牙配置结果为:{}", tboxSn, returnResult);
            kafkaService.sndGetBtKeyRep(tboxSn + "_" + returnResult + "_" + eventTime, tboxSn);

        } catch (Exception e) {
            logger.error("TBox(sn:{}):解析获取蓝牙钥匙上报的报文参数内容出错，原因:{}！", tboxSn,
                    ThrowableUtil.getErrorInfoFromThrowable(e));
        }
    }

    /**
     * @param requestMsg
     * @Title: checkDataVerification
     * @Description: 验证蓝牙
     */
    public void checkDataVerification(OTAMessage requestMsg) {
        // 回包的待加密数据段，含CRC校验字节，初始化为0x00
        inDataVerification(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @param datagramBytes
     * @param tboxSn
     * @Title: inDataVerification
     * @Description: 验证蓝牙
     */
    private void inDataVerification(byte[] datagramBytes, String tboxSn) {

        logger.info("TBox(sn:{}):验证蓝牙钥匙上报的报文参数内容为:{}", tboxSn, ByteUtil.byteToHex(datagramBytes));
        BlueToothCtrlRespInfo blueToothCtrlRespInfo = new BlueToothCtrlRespInfo();
        try {
            if (datagramBytes.length != 1) {
                logger.warn("TBox(sn:{}):验证蓝牙钥匙上报的报文参数内容长度不对！", tboxSn);
            } else {
                blueToothCtrlRespInfo.setCmd(OTABlueToothCtrlConstant.COMMAND_SEND_VERIFICATION_CODE);
                int result = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(datagramBytes[0]));
                Date curTime = new Date();
                String eventTime = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");
                blueToothCtrlRespInfo.setResult(result);
                kafkaService.sndValidBtKeyRep(tboxSn + "_" + JSONObject.toJSONString(blueToothCtrlRespInfo) + "_" + eventTime,
                        tboxSn);
            }
        } catch (Exception e) {
            logger.error("TBox(sn:{}):解析验证蓝牙钥匙上报的报文参数内容出错，原因:{}！", tboxSn,
                    ThrowableUtil.getErrorInfoFromThrowable(e));
        }
    }

}
