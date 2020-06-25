package com.maxus.tsp.gateway.common.ota;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.gateway.common.constant.OTACommand;
import com.maxus.tsp.gateway.common.constant.OTAEncrptMode;
import com.maxus.tsp.gateway.common.constant.OTAMessageOffset;
import com.maxus.tsp.gateway.common.constant.OTAMessagePartSize;

/**
 * 远程配置指令构造
 * @author 顾浩炜
 *
 */
public class RemoteConfigData {
	static Logger logger = LogManager.getLogger(RemoteConfigData.class);

	private static AtomicInteger _serianlNo = new AtomicInteger(0);

	// 获取当前的报文的seq no
	public static byte[] getCurrentSeqNo() {
		byte[] curSeqNo;
		// 线程安全加1
		curSeqNo = ByteUtil.int2Byte(_serianlNo.incrementAndGet());
		if (_serianlNo.incrementAndGet() == Integer.MAX_VALUE - 1) {
			_serianlNo = new AtomicInteger(0);
		}
		return curSeqNo;
	}

	// 构造远程配置报文
	public static OTAMessage getSendDataForRmtConfig(String[] cmd) {
		String tboxSN = cmd[0];
		String idCode = cmd[1];
		String value = cmd[2];
		String dateTime = cmd[3];
		OTAMessage requestMsg = new OTAMessage();
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
			requestMsg.setSeqNum(getCurrentSeqNo());

			byte[] remoteConfigParam;
			short paramSize;
			int valueOffset;
			// 判断是不是获取远配指令
			if (idCode.equals("0")) {
				// 设置CMD字
				requestMsg.setCommand(OTACommand.CMD_DOWN_GET_CONFIG.value());
				remoteConfigParam = new byte[7];
				// dateTime
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date dateTime1 = sdf.parse(dateTime);
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dateTime1);
				System.arraycopy(ByteUtil.CreateDateTimeBytes(calendar), 0, remoteConfigParam, 0, 7);
			} else {
				// 设置CMD字
				requestMsg.setCommand(OTACommand.CMD_DOWN_REMOTE_CONFIG.value());
				// 字段ID为3时调整长度
				if ((short) Integer.parseInt(idCode) == 3) {
					remoteConfigParam = new byte[15];
					paramSize = 6;
					valueOffset = 4;
					System.arraycopy(ByteUtil.int2Byte(Integer.parseInt(value)), 0, remoteConfigParam, 4, valueOffset);
				} else {
					remoteConfigParam = new byte[13];
					paramSize = 4;
					valueOffset = 2;
					System.arraycopy(ByteUtil.short2Byte((short) Integer.parseInt(value)), 0, remoteConfigParam, 4,
							valueOffset);
				}

				// ParamSize
				System.arraycopy(ByteUtil.short2Byte(paramSize), 0, remoteConfigParam, 0, 2);
				// Param
				System.arraycopy(ByteUtil.short2Byte((short) Integer.parseInt(idCode)), 0, remoteConfigParam, 2, 2);
				// System.arraycopy(ByteUtil.short2Byte((short)Integer.parseInt(value)),
				// 0, remoteConfigParam, 4, valueOffset);
				// dateTime
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date dateTime1 = sdf.parse(dateTime);
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dateTime1);
				System.arraycopy(ByteUtil.CreateDateTimeBytes(calendar), 0, remoteConfigParam, 4 + valueOffset, 7);
			}

			// 设置参数长度
			requestMsg.setParamSize(ByteUtil.short2Byte((short) remoteConfigParam.length));
			requestMsg.setParam(remoteConfigParam);
			// 创建下发报文
			requestMsg.createMessageFromParts();
			logger.info("DownSendData: " + ByteUtil.byteToHex(requestMsg.curMessage));
		} catch (Exception e) {
			logger.error("远程控制指令构造发生异常 " + "Tbox=" + tboxSN + "comd=" + OTACommand.CMD_DOWN_REMOTE_CONFIG + "异常：", e);
		}
		return requestMsg;

	}
}
