package com.maxus.tsp.common.enums;

public enum RunningModelEnum {

	EV((byte)0x01,"EV"),
	PHEV((byte)0x02,"PHEV"),
	FV((byte)0x03,"FV"),
	ERROR((byte)0xFE,"ERROR"),
	INVALID((byte)0xFF,"INVALID");
	
	private byte code;
	private String value;
	RunningModelEnum(byte code, String value) {
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
