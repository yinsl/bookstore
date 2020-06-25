package com.maxus.tsp.common.enums;
/**
 * 国标报文数据块长度
 * @author uwczo
 *
 */
public enum GBMessagePartSize {
	//起始字节
	BEGIN_SIGN_SIZE(2), 
	//命令标志
	CMD_ID_FLAG_SIZE(1), 
	//应答标识
	RPY_ID_FLAG_SIZE(1), 
	//唯一识别码，VIN
	VIN_SIZE(17), 
	//数据单元加密模式
	ENCRYPT_MODE_SIZE(1), 
	//数据单元长度
	DATAUNIT_SIZE_SIZE(2),
	//crc
	CRC_SIZE(1),
	//国标时间长度
	DATE_TIME_SIZE(6),
	//回包报文总长度
	REPLY_MSG_SIZE(31);
	private int code;

	GBMessagePartSize(int code) {
		this.code = code;
	}

	public int value() {
		return code;
	}
}
