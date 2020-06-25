package com.maxus.tsp.common.enums;

public enum VehicleEngineStatusEnum {

	STARTED((byte)0x01,"STARTED"),
	STOPPED((byte)0x02,"STOPPED"),
	OTHER((byte)0x03,"OTHER"),
	ERROR((byte)0xfe,"ERROR"),
	INVALID((byte)0xff,"INVALID");
	
	private byte code;
	private String value;
	VehicleEngineStatusEnum(byte code, String value) {
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
