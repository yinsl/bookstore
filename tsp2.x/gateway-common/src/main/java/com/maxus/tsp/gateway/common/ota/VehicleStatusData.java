package com.maxus.tsp.gateway.common.ota;

import java.util.concurrent.atomic.AtomicInteger;

import com.maxus.tsp.common.util.ThrowableUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.gateway.common.constant.OTACommand;
import com.maxus.tsp.gateway.common.constant.OTAEncrptMode;
import com.maxus.tsp.gateway.common.constant.OTAMessageOffset;
import com.maxus.tsp.gateway.common.constant.OTAMessagePartSize;

/**
 * 获取车况报文组装方式
 * 
 * @author lzgea
 *
 */
public class VehicleStatusData {
	static Logger logger = LogManager.getLogger(VehicleStatusData.class);
	private static AtomicInteger _serianlNo = new AtomicInteger(0);

	public static OTAMessage getGetVehicleStatus(String[] cmd) {
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
			if (GlobalSessionChannel.getAESKey(tboxSN) != null) {
				requestMsg.setEncryptType(OTAEncrptMode.AES);
			}
			// 报文序号
			requestMsg.setSeqNum(OpCarRemoteControl.getCurrentSeqNo());
			int paramSize = 0;
			if (cmd[1] != null) {
				String[] reqId = StringUtils.split(cmd[1], ",");
				paramSize = 2 + reqId.length * 2;
				byte[] param = new byte[paramSize];
				System.arraycopy(ByteUtil.short2Byte((short)reqId.length), 0, param, 0, 2);
				int i=0;
				for(String id :reqId){
					System.arraycopy(ByteUtil.short2Byte((short)Integer.parseUnsignedInt(id)), 0, param, 2+2*i, 2);
					i++;
				}
				requestMsg.setParam(param);

			}
			requestMsg.setParamSize(ByteUtil.short2Byte((short)paramSize));
			requestMsg.setCommand(OTACommand.CMD_DOWN_GET_VEHICLE_STATUS.value());
			requestMsg.createMessageFromParts();
			logger.info("DownSendData:{} ", ByteUtil.byteToHex(requestMsg.curMessage));
			return requestMsg;
		} catch (Exception e) {
			logger.error("组装远程车况报文失败，原因：{}", ThrowableUtil.getErrorInfoFromThrowable(e));
			return null;
		}
	}
}
