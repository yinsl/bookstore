package com.maxus.tsp.common.enums;

public enum CarcanSystemPowerModeEnum {

	OFF("0","OFF"),
	ACC("1","Accessory"),
	RUN("2","Run"),
	CRANK("3","Crank_Request");
	
	private String code;
	private String value;
	CarcanSystemPowerModeEnum(String code, String value) {
	    this.code = code;
	    this.value = value;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
}
