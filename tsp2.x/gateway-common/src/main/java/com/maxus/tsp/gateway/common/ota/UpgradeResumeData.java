package com.maxus.tsp.gateway.common.ota;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.FOTAConstant;
import com.maxus.tsp.gateway.common.constant.OTACommand;
import com.maxus.tsp.gateway.common.constant.OTAEncrptMode;
import com.maxus.tsp.gateway.common.constant.OTAMessageOffset;
import com.maxus.tsp.gateway.common.constant.OTAMessagePartSize;
import com.maxus.tsp.gateway.common.model.fota.UpgradeResumeReqInfo;

/**
*@Title UpgradeResumeData.java
*@description fota继续升级的报文组装类
*@time 2019年2月12日 上午8:54:56
*@author wqgzf
*@version 1.0
**/
public class UpgradeResumeData {
	
	private static final Logger logger = LogManager.getLogger(UpgradeResumeData.class);
	
	public static OTAMessage getSendDataForUpgradeResume(String[] cmd) {
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
	        	 UpgradeResumeReqInfo upgradeResumeReqInfo = JSONObject.parseObject(cmd[1], UpgradeResumeReqInfo.class);
	        	 int paramSize = FOTAConstant.UPGRADE_RESUME_DOWN_PARAM_OFFSET;
	        	 requestMsg.setCommand(OTACommand.CMD_DOWN_UPGRADE_RESUME.value());
	        	 byte[] param = new byte[paramSize];
	             //ID
	             int id = upgradeResumeReqInfo.getId();
	             byte[] _id = ByteUtil.int2Byte(id);
	             System.arraycopy(_id, 0, param, 0, FOTAConstant.FOTA_PARAM_ID_OFFSET);
	             
	             requestMsg.setParam(param);
	             requestMsg.setParamSize(ByteUtil.short2Byte((short) paramSize));
	             logger.info("TBox(SN:{})创建继续升级下发报文:{}", serialNumber, ByteUtil.byteToHex(requestMsg.curMessage));
	             requestMsg.createMessageFromParts();
	         } else {
	                logger.warn("TBox(SN:{})创建继续升级下发报文时, 参数内容为空!", serialNumber);
	         }
	         return requestMsg;	    	 
	     } catch (Exception e) {
	    	 logger.error("TBox(SN:{})组装继续升级下发报文因发生异常而失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
	         return null;
	     }
	}
}
