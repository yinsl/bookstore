package com.maxus.tsp.common.enums;

public enum AlertLevelEnum {

	LEVEL_0((byte)0x00,"LEVEL_0"),
	LEVEL_1((byte)0x01,"LEVEL_1"),
	LEVEL_2((byte)0x02,"LEVEL_2"),
	LEVEL_3((byte)0x03,"LEVEL_3"),
	ERROR((byte)0xFE,"ERROR"),
	INVALID((byte)0xff,"INVALID");
	
	private byte code;
	private String value;
	AlertLevelEnum(byte code, String value) {
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
