package com.maxus.tsp.common.enums;
/**
 * 国标报文数据块偏移
 * @author uwczo
 *
 */
public enum GBMessageOffset {
	//起始字节
	BEGIN_SIGN_OFFSET(0), 
	//命令标志
	CMD_ID_FLAG_OFFSET(2), 
	//应答标识
	RPY_ID_FLAG_OFFSET(3), 
	//唯一识别码，VIN
	VIN_OFFSET(4), 
	//数据单元加密模式
	ENCRYPT_MODE_OFFSET(21), 
	//数据单元长度
	DATAUNIT_SIZE_OFFSET(22),
	//数据单元
	ENCRYPT_DATAUNIT_OFFSET(24);
	private int code;

	GBMessageOffset(int code) {
		this.code = code;

	}

	public int value() {
		return code;
	}
}
