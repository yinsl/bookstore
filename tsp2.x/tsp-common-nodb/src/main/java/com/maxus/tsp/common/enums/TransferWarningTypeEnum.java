package com.maxus.tsp.common.enums;

public enum TransferWarningTypeEnum {

	OPENING_FL_DOOR_ILLEGALLY("2001001",false,0),
	OPENING_FR_DOOR_ILLEGALLY("2001002",false,1),
	OPENING_BR_DOOR_ILLEGALLY("2001003",false,2),
	OPENING_BL_DOOR_ILLEGALLY("2001004",false,3),
	OPENING_TAIL_DOOR_ILLEGALLY("2001005",false,4),
	MOVING_VEHICLE_ILLEGALLY("2001006",false,5),
	OPENING_HOOD_ILLEGALLY("2001007",false,6),
	DISASSEMBLING_TIRES_ILLEGALLY("2001008",false,7),
	VEHICLE_FRONTSIDE_COLLISION("2001009",false,8),
	VEHICLE_BACKSIDE_COLLISION("2001010",false,9),
	VEHICLE_LEFTSIDE_COLLISION("2001011",false,10),
	VEHICLE_RIGHTSIDE_COLLISION("2001012",false,11),
	FATIGUE_DRIVING("2001013",false,12),
	CAN_DATA_INTERRUPTION("2001014",false,13),
	GETTING_GPSPOS_FAILURE("2001020",false,14),
	DRIVING_LEFTLANE_DEPARTURE("2001021",false,15),
	DRIVING_RIGHTLANE_DEPARTURE("2001022",false,16);
	
	String code;
	boolean value;
	int index;
	TransferWarningTypeEnum(String code, boolean value, int index) {
		this.code = code;
		this.value = value;
		this.index = index;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public boolean getValue() {
		return value;
	}
	public void setValue(boolean value) {
		this.value = value;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	
	
	
	
	
}
