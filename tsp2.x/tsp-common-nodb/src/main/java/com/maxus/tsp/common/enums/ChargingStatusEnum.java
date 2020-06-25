package com.maxus.tsp.common.enums;

public enum ChargingStatusEnum {

	CHARGING_STOPPED((byte)0x01,"CHARGING_STOPPED"),
	CHARGING_DRIVING((byte)0x02,"CHARGING_DRIVING"),
	NO_CHARGING((byte)0x03,"NO_CHARGING"),
	CHARGING_FINISH((byte)0x04,"CHARGING_FINISH"),
	CHARGING_ERROR((byte)0xfe,"ERROR"),
	CHARGING_INVALID((byte)0xff,"INVALID");
	
	private byte code;
	private String value;
	ChargingStatusEnum(byte code, String value) {
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
