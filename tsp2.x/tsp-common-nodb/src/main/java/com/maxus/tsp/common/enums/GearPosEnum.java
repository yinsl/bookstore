package com.maxus.tsp.common.enums;

public enum GearPosEnum {

	NEUTRAL_GEAR((byte)0x00,"NEUTRAL_GEAR"),
	ONE_GEAR((byte)0x01,"ONE_GEAR"),
	TWO_GEAR((byte)0x02,"TWO_GEAR"),
	THREE_GEAR((byte)0x03,"THREE_GEAR"),
	FOUR_GEAR((byte)0x04,"FOUR_GEAR"),
	FIVE_GEAR((byte)0x05,"FIVE_GEAR"),
	SIX_GEAR((byte)0x06,"SIX_GEAR"),
	REVERSE_GEAR((byte)0x0D,"REVERSE_GEAR"),
	AUTODRIVE_GEAR((byte)0x0E,"AUTODRIVE_GEAR"),
	STOP_GEAR((byte)0x0F,"STOP_GEAR"),
//	ERROR((byte)0xFE,"ERROR"),//超过约定值，不超过十六进制长度，统一转发ERROR
//	INVALID((byte)0xFF,"INVALID"),
	HAS_DRIVE_FORCE((byte)0x20,"hasDriveForce"),
	HAS_DRIVE_BRAKE((byte)0x10,"hasBrakingForce");
	
	private byte code;
	private String value;
	GearPosEnum(byte code, String value) {
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
