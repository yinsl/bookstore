package com.maxus.tsp.platform.service.model.vo;

import com.maxus.tsp.common.util.ThrowableUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.maxus.tsp.common.enums.GBEncrptMode;
import com.maxus.tsp.common.enums.GBMessageOffset;
import com.maxus.tsp.common.enums.GBMessagePartSize;
import com.maxus.tsp.common.enums.ReplyIdentifierEnum;
import com.maxus.tsp.common.util.ByteUtil;


public class RealTimeDataOperation {
	//日志
	private static Logger logger = LogManager.getLogger(RealTimeDataOperation.class);
	//起始符
	private static final byte[] BEGIN_SIGN = {0x23, 0x23};
	//命令标识. 对应 CommandIdentifierEnum
	private byte commandIdFlag;
	//应答标识。  对应 ReplyIdentifierEnum,默认值为命令
	private byte replyIdFlag = ReplyIdentifierEnum.GB_REQUIRE.getCode();
	//唯一识别码——长度17，车辆信息使用车架号 vin,其他信息使用自定义编码
	private String uniqueID;
	//数据单元加密方式,默认不加密
	private short encryptMode = GBEncrptMode.NONE.value();
	//数据单元长度
	private byte[] realTimeDataSize = new byte[2];
	//数据单元不加密字符串
	private byte[] realTimeDateUintDecry;
	//数据单元加密字符串
	private byte[] realTimeDateUintEncry;
	//校验位
	private byte checkSum;
	//原始发送给国家平台的报文或接受到的报文
	private byte[] originalMessage;
	//如果发送给国家平台的报文是实时数据，但发生错误，需要补发，则ReSendMessage设置其修正后的报文
	private byte[] ReSendMessage;
	//解析准确性标志
	private boolean isAnalysed = false;
	public RealTimeDataOperation() {		
	}
	public boolean isAnalysed() {
		return isAnalysed;
	}

	public void setAnalysed(boolean isAnalysed) {
		this.isAnalysed = isAnalysed;
	}

	public byte getCommandIdFlag() {
		return commandIdFlag;
	}

	public void setCommandIdFlag(byte commandIdFlag) {
		this.commandIdFlag = commandIdFlag;
	}

	public byte getReplyIdFlag() {
		return replyIdFlag;
	}

	public void setReplyIdFlag(byte replyIdFlag) {
		this.replyIdFlag = replyIdFlag;
	}

	public String getUniqueID() {
		return uniqueID;
	}

	public void setUniqueID(String uniqueID) {
		this.uniqueID = uniqueID;
	}

	public short getEncryptMode() {
		return encryptMode;
	}

	public void setEncryptMode(short encryptMode) {
		this.encryptMode = encryptMode;
	}

	public byte[] getRealTimeDataSize() {
		return realTimeDataSize;
	}

	public void setRealTimeDataSize(byte[] realTimeDataSize) {
		this.realTimeDataSize = realTimeDataSize;
	}

	public byte[] getRealTimeDateUintDecry() {
		return realTimeDateUintDecry;
	}

	public void setRealTimeDateUintDecry(byte[] realTimeDateUintDecry) {
		this.realTimeDateUintDecry = realTimeDateUintDecry;
	}

	public byte[] getRealTimeDateUintEncry() {
		return realTimeDateUintEncry;
	}

	public void setRealTimeDateUintEncry(byte[] realTimeDateUintEncry) {
		this.realTimeDateUintEncry = realTimeDateUintEncry;
	}

	public byte getCheckSum() {
		return checkSum;
	}

	public void setCheckSum(byte checkSum) {
		this.checkSum = checkSum;
	}

	public byte[] getOriginalMessage() {
		return originalMessage;
	}

	public void setOriginalMessage(byte[] originalMessage) {
		this.originalMessage = originalMessage;
	}

	public byte[] getReSendMessage() {
		return ReSendMessage;
	}

	public void setReSendMessage(byte[] reSendMessage) {
		ReSendMessage = reSendMessage;
	}

	public RealTimeDataOperation(byte[] oriMessage) {
		originalMessage = oriMessage;
		analysisRecMsg();
	}
	/**
	 * 解析原始报文正确性(国标服务平台的响应报文)
	 * 返回结果成功true, 失败false
	 * @return
	 */
	public boolean analysisRecMsg() {
		isAnalysed = false;
		try {
			int originMsgSize = originalMessage.length;
			byte[] messageBegin = new byte[GBMessagePartSize.BEGIN_SIGN_SIZE.value()];
			System.arraycopy(originalMessage, GBMessageOffset.BEGIN_SIGN_OFFSET.value(), messageBegin, 0,
	        		GBMessagePartSize.BEGIN_SIGN_SIZE.value());
			if (ByteUtil.byteToHex(messageBegin).equals(ByteUtil.byteToHex(BEGIN_SIGN))
					&& originMsgSize >= GBMessagePartSize.REPLY_MSG_SIZE.value()) {
				//当起始标志正确时，才进行后面的解析
				//获取CRC校验位
				checkSum = originalMessage[originMsgSize - 1]; 
				//获取计算CRC校验的字节域
				int crcFieldSize = originMsgSize - GBMessagePartSize.BEGIN_SIGN_SIZE.value()
	                    - GBMessagePartSize.CRC_SIZE.value();
				byte[] crcField = new byte[crcFieldSize];
	            System.arraycopy(originalMessage, GBMessageOffset.CMD_ID_FLAG_OFFSET.value(), crcField,
	            		0, crcFieldSize);
	            if (ByteUtil.getCRC(crcField, crcFieldSize)==(checkSum & 0xFF)) {
	            	//CRC校验成功
					//获取命令标志
					commandIdFlag = originalMessage[GBMessageOffset.CMD_ID_FLAG_OFFSET.value()];
		            //获取应答标识
					replyIdFlag = originalMessage[GBMessageOffset.RPY_ID_FLAG_OFFSET.value()];
		            //获取vin
					byte[] carVin = new byte[GBMessagePartSize.VIN_SIZE.value()];
		            System.arraycopy(originalMessage, GBMessageOffset.VIN_OFFSET.value(), carVin, 0,
		            		GBMessagePartSize.VIN_SIZE.value());
		            uniqueID = ByteUtil.bytesToString(carVin);
		            //获取加密模式
		            encryptMode = originalMessage[GBMessageOffset.ENCRYPT_MODE_OFFSET.value()];
		            //获取数据长度
		            System.arraycopy(originalMessage, GBMessageOffset.DATAUNIT_SIZE_OFFSET.value(), realTimeDataSize, 0,
		            		GBMessagePartSize.DATAUNIT_SIZE_SIZE.value());
		            int dataSize = ByteUtil.getUnsignedInt(realTimeDataSize);
		            int totalMsgSize = GBMessagePartSize.BEGIN_SIGN_SIZE.value()
		            		+ GBMessagePartSize.CMD_ID_FLAG_SIZE.value()
		            		+ GBMessagePartSize.RPY_ID_FLAG_SIZE.value()
		            		+ GBMessagePartSize.VIN_SIZE.value()
		            		+ GBMessagePartSize.ENCRYPT_MODE_SIZE.value()
		            		+ GBMessagePartSize.DATAUNIT_SIZE_SIZE.value()
		            		+ dataSize
		            		+ GBMessagePartSize.CRC_SIZE.value();
		            if (originMsgSize == totalMsgSize) {
		            	//获取数据单元内容
		            	realTimeDateUintEncry = new byte[dataSize];
		            	System.arraycopy(originalMessage, GBMessageOffset.ENCRYPT_DATAUNIT_OFFSET.value(), realTimeDateUintEncry, 0,
		            			dataSize);
		            	isAnalysed = true;
		            }
	            }
			}
		} catch (Exception e) {
			logger.error("Analysis Message Failed:{}", ThrowableUtil.getErrorInfoFromThrowable(e));
			isAnalysed = false;
		}
		return isAnalysed;
	}

	/**
	 * 国标数据单元加密
	 * @param encryptMode
	 * @param originalBytes
	 * @param uniqueID
	 * @return
	 */
	public byte[] encryptBytes(short encryptMode, byte[] originalBytes, String uniqueID) {
		logger.info("国标数据单元加密前原始报文:{}", ByteUtil.byteToHex(originalBytes));
		byte[] encryptBytes = new byte[] {};

		switch (GBEncrptMode.getByCode(encryptMode)) {
		case AES:
			break;
		case RSA:
			break;
		case NONE:
		default:
			encryptBytes = originalBytes;
			break;
		}
		logger.info("国标数据单元加密后报文:{}", ByteUtil.byteToHex(encryptBytes));
		return encryptBytes;

	}

	/**
	 * 根据现有各部分子数据的字节内容生成数据报文
	 * 返回结果成功true, 失败false
	 * @return
	 */
	public boolean createMessageFromParts() {
		// 如果设置的子数据根据规范有误，则认为拼接失败
		boolean creatRet = false;
		try {
			if (realTimeDateUintDecry.length > 0) {
            //得到加密后字段，目前不加密，即realTimeDataUint字段。
            byte[] encryptMessage = encryptBytes(encryptMode, realTimeDateUintDecry, this.getUniqueID());
			//根据加密方式进行加密，国标加密字段的明文即国标数据单元字节
            int encryptMsgSize = encryptMessage.length;
            //设置数据单元长度字段
            realTimeDataSize = ByteUtil.short2Byte((short) encryptMsgSize);
            //计算报文总长度
            int totalMsgSize = GBMessagePartSize.BEGIN_SIGN_SIZE.value()
            		+ GBMessagePartSize.CMD_ID_FLAG_SIZE.value()
            		+ GBMessagePartSize.RPY_ID_FLAG_SIZE.value()
            		+ GBMessagePartSize.VIN_SIZE.value()
            		+ GBMessagePartSize.ENCRYPT_MODE_SIZE.value()
            		+ GBMessagePartSize.DATAUNIT_SIZE_SIZE.value()
            		+ encryptMsgSize
            		+ GBMessagePartSize.CRC_SIZE.value();
            originalMessage = new byte[totalMsgSize];
            //复制原有的header
            System.arraycopy(BEGIN_SIGN, 0, originalMessage, GBMessageOffset.BEGIN_SIGN_OFFSET.value(),
            		GBMessagePartSize.BEGIN_SIGN_SIZE.value());
            byte[] cmdFlags = new byte[GBMessagePartSize.CMD_ID_FLAG_SIZE.value()];
            byte[] rpyFlags	= new byte[GBMessagePartSize.RPY_ID_FLAG_SIZE.value()];
            cmdFlags[0] = commandIdFlag;
            rpyFlags[0] = replyIdFlag;
            //设置命令标志
            System.arraycopy(cmdFlags, 0, originalMessage, GBMessageOffset.CMD_ID_FLAG_OFFSET.value(),
            		GBMessagePartSize.CMD_ID_FLAG_SIZE.value());
            //设置应答标识
            System.arraycopy(rpyFlags, 0, originalMessage, GBMessageOffset.RPY_ID_FLAG_OFFSET.value(),
            		GBMessagePartSize.RPY_ID_FLAG_SIZE.value());
            //设置vin
            System.arraycopy(ByteUtil.stringToBytes(uniqueID), 0, originalMessage, GBMessageOffset.VIN_OFFSET.value(),
            		GBMessagePartSize.VIN_SIZE.value());
            //设置加密模式
            System.arraycopy(ByteUtil.short2Byte((short) encryptMode), 1, originalMessage,
            		GBMessageOffset.ENCRYPT_MODE_OFFSET.value(),
            		GBMessagePartSize.ENCRYPT_MODE_SIZE.value());
            //设置数据单元长度
            System.arraycopy(realTimeDataSize, 0, originalMessage, GBMessageOffset.DATAUNIT_SIZE_OFFSET.value(),
            		GBMessagePartSize.DATAUNIT_SIZE_SIZE.value());
            //复制加密段内容
            System.arraycopy(encryptMessage, 0, originalMessage, GBMessageOffset.ENCRYPT_DATAUNIT_OFFSET.value(),
            		encryptMessage.length);
            //计算CRC并进行设置
            int crcFieldSize = totalMsgSize - GBMessagePartSize.BEGIN_SIGN_SIZE.value()
                    - GBMessagePartSize.CRC_SIZE.value();
            byte[] crcField = new byte[crcFieldSize];
            System.arraycopy(originalMessage, GBMessageOffset.CMD_ID_FLAG_OFFSET.value(), crcField,
            		0, crcFieldSize);
            //报文最后一个字节设为计算的CRC值
            originalMessage[totalMsgSize - 1] = (byte) ByteUtil.getCRC(crcField, crcFieldSize);
            creatRet = true;
			}
		} catch (Exception e) {
			logger.error("Create Message Failed: {}", ThrowableUtil.getErrorInfoFromThrowable(e));
        	creatRet = false;
        }
		return creatRet;
	}
}
