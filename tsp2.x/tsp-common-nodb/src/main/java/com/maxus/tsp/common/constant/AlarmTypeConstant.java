package com.maxus.tsp.common.constant;


public enum AlarmTypeConstant {
	ENGINE_MULFUNCTION("01", "发动机故障", "engine"),
	EBD_MULFUNCTION("02", "EBD故障", "EBD"),
	ABS_MULFUNCTION("03", "ABS故障", "ABS"),
	ABNORMAL_ENTRY("04", "非法进入", "illegal_entry")/*,
	INVALID_MOVEMENT("05", "非法移动", "illegal_move"),
	OPENING_HOOD("06", "引擎盖非法打开", "open hood"),
	DISASSEMBLING_TIRES("07", "轮胎非法拆卸", "disassembling tires"),
	VEHICLE_COLLISION("08", "车辆被碰撞", "vehicle collision"),
	FATIGUE_DRIVING("09", "疲劳驾驶", "fatigue driving"),
	CAN_INTERRUPT("10", "CAN数据中断", "can interrupt"),
	DTC_CODE_UPDATE("11", "车辆故障状态有更新", "dtc code update"),
	GETTING_GPSPOS_FAILURE("12", "GPS未定位报警", "get gps failure"),
	DRIVING_LANE_DEPARTURE("13", "车道偏离", "driving lane departure")*/;

	private String code;
	public String getCode() {
		return code;
	}
	//返回报警码
	public void setCode(String code) {
		this.code = code;
	}
	//返回报警类型
	private String description;
	public String getDescription() {
		return description;
	}
	
	//返回报警类型 EN
	private String descriptionEN;
	
	public String getDescriptionEN() {
		return descriptionEN;
	}
	public void setDescriptionEN(String descriptionEN) {
		this.descriptionEN = descriptionEN;
	}
	//返回短信提示信息
	public String getSmsDescription() {
		return String.format("%s情况",description);
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	AlarmTypeConstant(String code, String des, String desEN) {
		setCode(code);
		setDescription(des);
		setDescriptionEN(desEN);
	}
	//根据报警码获得报警类型
	public static AlarmTypeConstant getResultStatus(String code) {
		for (AlarmTypeConstant r : AlarmTypeConstant.values()) {
			if (r.getCode().equals(code)) {
				return r;
			}
		}
		return null;
	}
}
