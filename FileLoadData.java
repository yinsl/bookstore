package com.maxus.tsp.gateway.common.ota;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OTACommand;
import com.maxus.tsp.gateway.common.constant.OTAEncrptMode;
import com.maxus.tsp.gateway.common.constant.OTAMessageOffset;
import com.maxus.tsp.gateway.common.constant.OTAMessagePartSize;
import com.maxus.tsp.gateway.common.model.DownLoadFileMo;
import com.maxus.tsp.gateway.common.model.UpLoadFileMo;

public class FileLoadData {

	static Logger logger = LogManager.getLogger(FileLoadData.class);

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
	
	public static OTAMessage getDataForDownLoadFile(String[] cmd) {
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
			requestMsg.setSeqNum(FileLoadData.getCurrentSeqNo());
			requestMsg.setCommand(OTACommand.CMD_DOWN_DOWNLOAD_FILE.value());
			int paramSize = 42;
			if(cmd[1]!=null){
				DownLoadFileMo downLoad = JSONObject.parseObject(cmd[1], DownLoadFileMo.class);
				paramSize = paramSize + downLoad.getFileInfoSize() + downLoad.getUrl().length();
				byte[] param = new byte[paramSize];
				byte[] fileType = new byte[1];
				fileType[0] = (byte)downLoad.getFileType();
				Calendar cal = Calendar.getInstance();
				//CurrentTime
				cal.setTimeInMillis(downLoad.getCurrentTime());
				System.arraycopy(ByteUtil.CreateDateTimeBytes(cal), 0, param, 0, 7);
				// filetype
				System.arraycopy(fileType, 0, param, 7, 1);
				// fileinfosize
				System.arraycopy(ByteUtil.short2Byte((short)downLoad.getFileInfoSize()), 0, param, 8, 2);
				// file info
				if (downLoad.getFileInfoSize() != 0){
					System.arraycopy(ByteUtil.stringToBytes(downLoad.getFileInfo()), 0, param, 10, downLoad.getFileInfoSize());	
				}
				// url
				System.arraycopy(ByteUtil.stringToBytes(downLoad.getUrl()), 0, param, 10 + downLoad.getFileInfoSize(), 
						downLoad.getUrl().length());
				// md5
				System.arraycopy(ByteUtil.stringToBytes(downLoad.getMd5Data()), 0, param, 10 + downLoad.getFileInfoSize() + downLoad.getUrl().length(), 
						32);
				requestMsg.setParam(param);
			}
			// 创建下发报文
			requestMsg.setParamSize(ByteUtil.short2Byte((short) paramSize));
			requestMsg.createMessageFromParts();
			logger.info("CMD_DOWN_DOWNLOAD_FILE_DownSendData: {}" , ByteUtil.byteToHex(requestMsg.curMessage));
			return requestMsg;
		} catch (Exception ex) {
			logger.error("组装文件下载指令失败！原因：{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
		}
		return null;

	}

	public static OTAMessage getDataForUpLoadFile(String[] cmd) {
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
			requestMsg.setSeqNum(FileLoadData.getCurrentSeqNo());
			requestMsg.setCommand(OTACommand.CMD_DOWN_UPLOAD_FILE.value());
			int paramSize = 8;
			if(cmd[1]!=null){
				UpLoadFileMo upLoad = JSONObject.parseObject(cmd[1], UpLoadFileMo.class);
				paramSize = paramSize + upLoad.getLocalPath().length();
				byte[] param = new byte[paramSize];
				byte[] fileType = new byte[1];
				fileType[0] = (byte)upLoad.getFileType();
				Calendar cal = Calendar.getInstance();
				//CurrentTime
				cal.setTimeInMillis(upLoad.getCurrentTime());
				System.arraycopy(ByteUtil.CreateDateTimeBytes(cal), 0, param, 0, 7);
				// filetype
				System.arraycopy(fileType, 0, param, 7, 1);
				// filelocalpath
				System.arraycopy(ByteUtil.stringToBytes(upLoad.getLocalPath()), 0, param, 8, upLoad.getLocalPath().length());
				requestMsg.setParam(param);
			}
			// 创建下发报文
			requestMsg.setParamSize(ByteUtil.short2Byte((short) paramSize));
			requestMsg.createMessageFromParts();
			logger.info("CMD_DOWN_UPLOAD_FILE_DownSendData:{}", ByteUtil.byteToHex(requestMsg.curMessage));
			
			
			return requestMsg;
		} catch (Exception ex) {
			logger.error("组装文件上传指令失败！原因：{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
		}
		return null;
	}
}
