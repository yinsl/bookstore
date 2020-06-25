package com.maxus.tsp.gateway.common.constant;

import java.util.HashMap;
import java.util.Map;

public enum OTARealTimeDataMember {
	Vehicle(1, "整车数据", true),
	Motor(2, "驱动电机数据", false),
	FuelCell(3, "燃料电池数据", true),
	Engine(4, "发动机数据", true),
	Pos(5, "位置数据", true),
	Extermum(6, "极值数据", true),
	Alarm(7, "报警数据", true),
	Voltage(8, "可充电储能装置电压数据", false),
	Temp(9, "可充电储能装置温度数据", false);

	private static Map<Integer, OTARealTimeDataMember> codeMap = new HashMap<>();

	static {
		for (OTARealTimeDataMember item : OTARealTimeDataMember.values()) {
			codeMap.put(item.value(), item);
		}
	}

	private final int code;
	private final String message;
	private final boolean isSingle;

	OTARealTimeDataMember(int code, String message, boolean isSingle) {
		this.code = code;
		this.message = message;
		this.isSingle = isSingle;
	}
	
	public static final OTARealTimeDataMember getByCode(int code) {

		if (codeMap.containsKey(code)) {
			return codeMap.get(code);
		}
		return null;
	}

	public int value() {
		return code;
	}

	public String getMessage() {
		return message;
	}
	
	public boolean isSingle() {
		return isSingle;
	}
}
