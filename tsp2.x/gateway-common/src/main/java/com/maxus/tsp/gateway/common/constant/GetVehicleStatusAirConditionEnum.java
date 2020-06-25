package com.maxus.tsp.gateway.common.constant;

import java.util.HashMap;
import java.util.Map;

public enum GetVehicleStatusAirConditionEnum {
	//当前车况的有效字段ID
	GET_VEHICLE_STATUS_ID(1,"有效值"),
	//	空调状态。
	//	4: Full Hot
	//	3: Full Cold
	//	2: Auto
	//	1: Blower only
	//	0: Off/Stop
	//	其它值：无效
	AIR_CONDITION_FULL_HOT(4,"Full Hot"),
	AIR_CONDITION_FULL_COLD(3,"Full Cold"),
	AIR_CONDITION_AUTO(2,"Auto"),
	AIR_CONDITION_BLOWER_ONLY(1,"Blower only"),
	AIR_CONDITION_OFF_STOP(0,"Off/Stop"),
	AIR_CONDITION_INVALID(255,"invalid");
	
	private static Map<Integer, GetVehicleStatusAirConditionEnum> codeValue = new HashMap<>();
	
	static{
		for(GetVehicleStatusAirConditionEnum item: GetVehicleStatusAirConditionEnum.values()){
			codeValue.put(item.getId(), item);
		}
	}
	
	private int id;
	private String type;
	GetVehicleStatusAirConditionEnum(int id, String type){
		this.id = id;
		this.type = type;
	}
	
	public final static GetVehicleStatusAirConditionEnum getValue(int id){
		if(codeValue.containsKey(id)){
			return codeValue.get(id);
		}
		return null;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
