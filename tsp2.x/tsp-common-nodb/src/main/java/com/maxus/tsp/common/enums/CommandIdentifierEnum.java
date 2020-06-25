package com.maxus.tsp.common.enums;

public enum CommandIdentifierEnum {
	VEHICLE_LOGIN_IN_UP((byte)0x01,"车辆登入"),
	REALTIME_DATA_REPORT_UP((byte)0x02,"实时信息上报"),
	RESEND_DATA_REPORT_UP((byte)0x03,"补发信息上报"),
	VEHICLE_LOGIN_OUT_UP((byte)0x04,"车辆登出"),
	PLATFORM_LOGIN_IN_UP((byte)0x05,"平台登入"),
	PLATFORM_LOGIN_OUT_UP((byte)0x06,"平台登出");
	
	private byte code;
	private String value;
	CommandIdentifierEnum(byte code, String value) {
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
