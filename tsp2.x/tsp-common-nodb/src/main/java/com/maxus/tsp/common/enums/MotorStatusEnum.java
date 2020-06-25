package com.maxus.tsp.common.enums;

public enum MotorStatusEnum {

	CONSUMING_POWER((byte)0x01,"CONSUMING_POWER"),
	GENERATING_POWER((byte)0x02,"GENERATING_POWER"),
	CLOSED((byte)0x03,"CLOSED"),
	PREPARING((byte)0x04,"PREPARING"),
	ERROR((byte)0xFE,"ERROR"),
	INVALID((byte)0xFF,"INVALID");
	
	private byte code;
	private String value;
	MotorStatusEnum(byte code, String value) {
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
