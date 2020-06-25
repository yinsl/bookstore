package com.maxus.tsp.common.enums;

public enum ReplyIdentifierEnum {
	GB_SUCCESS((byte)0x01,"接收到的信息正确"),
	GB_FAIL((byte)0x02,"设置未成功"),
	GB_VIN_DUP_ERR((byte)0x03,"VIN重复错误"),
	GB_REQUIRE((byte)0xFE,"命令");

	private byte code;
	private String value;
	ReplyIdentifierEnum(byte code, String value) {
	    this.code = code;
	    this.value = value;
	}

	public byte getCode() {
		return code;
	}

	public void setCode(byte code) {
		this.code = code;
	}

	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
}
