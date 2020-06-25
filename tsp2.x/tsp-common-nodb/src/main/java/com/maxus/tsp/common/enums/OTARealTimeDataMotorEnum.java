package com.maxus.tsp.common.enums;

public enum OTARealTimeDataMotorEnum {

	MOTOR_ID("1002001","驱动电机序号"),
	MOTOR_STATUS("1002002","驱动电机状态"),
	MOTOR_CONTROLLER_TEMP("1002003","驱动电机控制器温度"),
	RPM("1002004","驱动电机转速"),
	MOTOR_TORQUE("1002005","驱动电机转矩"),
	MOTOR_TEMP("1002006","驱动电机温度"),
	CONTROLLER_IN_VOLT("1002007","电机控制器输入电压"),
	CONTROLLER_DC_BUSCURRENT("1002008","电机控制器直流母线电流");

	private String code;
	private String value;
	private OTARealTimeDataMotorEnum (String code, String value) {
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
