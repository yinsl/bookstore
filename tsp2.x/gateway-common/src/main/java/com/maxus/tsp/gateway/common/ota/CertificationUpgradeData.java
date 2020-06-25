//package com.maxus.tsp.gateway.common.ota;
//
//import com.alibaba.fastjson.JSONObject;
//import com.maxus.tsp.common.util.ByteUtil;
//import com.maxus.tsp.common.util.ThrowableUtil;
//import com.maxus.tsp.gateway.common.constant.*;
//import com.maxus.tsp.gateway.common.model.fota.CertificationUpgradeReqInfo;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
///**
// * @ClassName CertificationUpgradeData
// * @Description FOTA证书更新组包类
// * @Author ssh
// * @Date 2019/2/3 9:38
// * @Version 1.0
// **/
//public class CertificationUpgradeData {
//
//    private static final Logger logger = LogManager.getLogger(CertificationUpgradeData.class);
//
//    /**
//     * @Description FOTA证书更新组包方法
//     * @Date 2019/2/3 9:44
//     * @Param [cmd]
//     * @return com.maxus.tsp.gateway.common.ota.OTAMessage
//     **/
//    public static OTAMessage getSendDataForCertificationUpgrade(String[] cmd) {
//        OTAMessage requestMsg = new OTAMessage();
//        String serialNumber = cmd[0];
//        try {
//            //创建报文头
//            System.arraycopy(OTAMessage.BeginSign, 0, requestMsg.getMsgHeader(),
//                    OTAMessageOffset.BEGIN_SIGN_OFFSET.value(), OTAMessagePartSize.BEGIN_SIGN_SIZE.value());
//            //设置SN号
//            byte[] curSN = ByteUtil.stringToBytes(serialNumber.trim());
//            if (curSN.length == 15) {
//                byte[] fullSN = new byte[16];
//                fullSN[0] = 0x20;
//                System.arraycopy(curSN,0,fullSN,1,15);
//                System.arraycopy(fullSN,0,requestMsg.getMsgHeader(),OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
//                        OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
//            } else if (curSN.length == 16) {
//                System.arraycopy(curSN, 0, requestMsg.getMsgHeader(), OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
//                        OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
//            } else {
//                return requestMsg;
//            }
//            //设置加密方式
//            if (GlobalSessionChannel.getAESKey(serialNumber) != null) {
//                requestMsg.setEncryptType(OTAEncrptMode.AES);
//            }
//            //报文序号
//            requestMsg.setSeqNum(OpCarRemoteControl.getCurrentSeqNo());
//
//            if (cmd[1] != null) {
//                CertificationUpgradeReqInfo certificationUpgradeReqInfo = JSONObject.parseObject(cmd[1], CertificationUpgradeReqInfo.class);
//                int paramSize = FOTAConstant.CERTIFICATION_UPGRADE_DOWN_PARAM_OFFSET;
//                if (certificationUpgradeReqInfo.getCmd().equals("tbox")) {
//                    requestMsg.setCommand(OTACommand.CMD_DOWN_UPD_GW_CERT.value());
//                } else if (certificationUpgradeReqInfo.getCmd().equals("avn")) {
//                    requestMsg.setCommand(OTACommand.CMD_DOWN_AVN_UPD_GW_CERT.value());
//                }
//
//                //***********  参数构造  *************
//                byte[] param = new byte[paramSize];
//                //type
//                int type = certificationUpgradeReqInfo.getType();
//                byte[] _type = ByteUtil.int2Byte(type);
//                System.arraycopy(_type,0,param,0,FOTAConstant.FOTA_PARAM_TYPE_OFFSET);
//                //size
//                int size = certificationUpgradeReqInfo.getSize();
//                byte[] _size = ByteUtil.int2Byte(size);
//                System.arraycopy(_size,0,param,1,FOTAConstant.FOTA_PARAM_SIZE_OFFSET);
//                //certification
//                String certification = certificationUpgradeReqInfo.getCertification();
//                byte[] _certification = ByteUtil.stringToBytes(certification);
//                System.arraycopy(_certification,0,param,3,FOTAConstant.FOTA_PARAM_CERTIFICATION_OFFSET);
//
//                requestMsg.setParam(param);
//                requestMsg.setParamSize(ByteUtil.short2Byte((short) paramSize));
//                logger.info("TBox(SN:{})创建证书更新下发报文:{}", serialNumber, ByteUtil.byteToHex(requestMsg.CurMessage));
//                requestMsg.createMessageFromParts();
//            } else {
//                logger.warn("TBox(SN:{})创建证书更新下发报文时，参数内容为空!",serialNumber);
//            }
//            return requestMsg;
//
//        } catch (Exception e) {
//            logger.error("TBox(SN:{})组装证书更新下发报文因发生异常而失败,异常原因:{}",serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
//            return null;
//        }
//    }
//}
