package com.maxus.tsp.gateway.common.constant;

/**
 * 报警类型枚举
 * @author uwczo
 *
 */
public enum WarningTypeConstant {
	ENGINE_MULFUNCTION("01", "发动机故障"),
	EBD_MULFUNCTION("02", "EBD故障"),
	ABS_MULFUNCTION("03", "ABS故障"),
	ABNORMAL_ENTRY("04", "异常进入"),
	INVALID_MOVEMENT("05", "非法移动"),
	OPENING_HOOD("06", "引擎盖非法打开"),
	DISASSEMBLING_TIRES("07", "轮胎非法拆卸"),
	VEHICLE_COLLISION("08", "车辆被碰撞"),
	FATIGUE_DRIVING("09", "疲劳驾驶"),
	CAN_INTERRUPT("10", "CAN数据中断"),
	DTC_CODE_UPDATE("11", "车辆故障状态有更新"),
	GETTING_GPSPOS_FAILURE("12", "GPS未定位报警"),
	DRIVING_LANE_DEPARTURE("13", "车道偏离");

	private String code;
	public String getCode() {
		return code;
	}
	//返回报警码
	public void setCode(String code) {
		this.code = code;
	}
	//返回报警类型
	public String getDescription() {
		return description;
	}
	//返回短信提示信息
	public String getSmsDescription() {
		return String.format("%s情况",description);
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	private String description;
	
	WarningTypeConstant(String code, String des) {
		setCode(code);
		setDescription(des);
	}
}
