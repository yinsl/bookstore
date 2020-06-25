package com.maxus.tsp.common.enums;

public enum CarcanPowerBattStatusEnum {

	PARKCHARGE("1","ParkCharge"),
	DRIVINGCHARGE("2","DrivingCharge"),
	CHARGECOMPLETION("3","Uncharge"),
	UNCHARGE("4","ChargeCompletion");
	
	private String code;
	private String value;
	CarcanPowerBattStatusEnum(String code, String value) {
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
