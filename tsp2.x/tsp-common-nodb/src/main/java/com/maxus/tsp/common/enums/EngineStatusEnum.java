package com.maxus.tsp.common.enums;

public enum EngineStatusEnum {

	ON((byte)0x01,"ON"),
	OFF((byte)0x02,"OFF"),
	ERROR((byte)0xFE,"ERROR"),
	INVALID((byte)0xFF,"INVALID");
	
	private byte code;
	private String value;
	EngineStatusEnum(byte code, String value) {
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
