package com.maxus.tsp.gateway.common.constant;

import java.util.HashMap;
import java.util.Map;

/**
*@Title GetVehicleStatusWindowSkylightEnum.java
*@description 车窗和天窗开合度解析结果
*@time 2019年3月4日 下午3:23:16
*@author wqgzf
*@version 1.0
**/
public enum GetVehicleStatusWindowSkylightEnum {	
	GET_VEHICLE_STATUS_OFF(0,"Off/Stop"),
	GET_VEHICLE_STATUS_VALID(50,"有效"),
	GET_VEHICLE_STATUS_FULL(100,"Full Open"),
	GET_VEHICLE_STATUS_SKYLIGHT_FULL(10,"Full Tilt"),
	GET_VEHICLE_STATUS_INVALID(255,"无效");	
	
private static Map<Integer, GetVehicleStatusWindowSkylightEnum> codeValue = new HashMap<>();
	
	static{
		for(GetVehicleStatusWindowSkylightEnum item: GetVehicleStatusWindowSkylightEnum.values()){
			codeValue.put(item.getId(), item);
		}
	}
	
	private int id;
	private String type;
	GetVehicleStatusWindowSkylightEnum(int id, String type){
		this.id = id;
		this.type = type;
	}
	
	public final static GetVehicleStatusWindowSkylightEnum getValue(int id){
		if(0 < id && 100 >id) {
			id = 50;
			if(codeValue.containsKey(id)){
			return codeValue.get(id);
		}
	   }
		return null;
	}
	
	public final static GetVehicleStatusWindowSkylightEnum getValue(int key,int id){
		if(3 == key && 100 == id) {
		return codeValue.get(10);
		} else {
			return GetVehicleStatusWindowSkylightEnum.getValue(id);
		}		
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

