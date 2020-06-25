package com.maxus.tsp.common.enums;

public enum DCDCStatusEnum {

	NORMAL((byte)0x01,"NORMAL"),
	OFF((byte)0x02,"OFF"),
	ERROR((byte)0xFE,"ERROR"),
	INVALID((byte)0xff,"INVALID");
	
	private byte code;
	private String value;
	DCDCStatusEnum(byte code, String value) {
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
