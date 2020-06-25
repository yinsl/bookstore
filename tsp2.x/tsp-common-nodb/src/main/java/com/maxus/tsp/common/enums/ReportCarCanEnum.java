package com.maxus.tsp.common.enums;

public enum ReportCarCanEnum {
	
//	CARACN_AVERG_FUEl_CONSUMPTION("2002001",""),
//	CARACN_AVERG_FUEl_CONSUMPTION("2002002",""),
//	CARACN_AVERG_FUEl_CONSUMPTION("2002003",""),
//	CARACN_AVERG_FUEl_CONSUMPTION("2002004",""),
//	CARACN_AVERG_FUEl_CONSUMPTION("2002005",""),
//	CARACN_AVERG_FUEl_CONSUMPTION("2002006",""),
//	CARACN_AVERG_FUEl_CONSUMPTION("2002007",""),
//	CARACN_AVERG_FUEl_CONSUMPTION("2002008",""),
//	CARACN_AVERG_FUEl_CONSUMPTION("2002009",""),
//	CARACN_AVERG_FUEl_CONSUMPTION("2002010",""),
//	CARACN_AVERG_FUEl_CONSUMPTION("2002010",""),
//	CARACN_AVERG_FUEl_CONSUMPTION("2002011",""),
//	CARACN_AVERG_FUEl_CONSUMPTION("2002012",""),
//	CARACN_AVERG_FUEl_CONSUMPTION("2002013",""),
//	CARACN_AVERG_FUEl_CONSUMPTION("2002001",""),
//	CARACN_AVERG_FUEl_CONSUMPTION("2002001",""),
//	CARACN_AVERG_FUEl_CONSUMPTION("2002001",""),
//	CARACN_AVERG_FUEl_CONSUMPTION("2002001",""),
//	CARACN_AVERG_FUEl_CONSUMPTION("2002001",""),
//	CARACN_AVERG_FUEl_CONSUMPTION("2002001",""),
//	CARACN_AVERG_FUEl_CONSUMPTION("2002001",""),
//	CARACN_AVERG_FUEl_CONSUMPTION("2002001",""),
//	
	//跛行相应枚举值
	SPEEDLIMIT_ON("1","ON"),
	SPEEDLIMIT_OFF("0","OFF"),
	SPEEDLIMIT_INVALID("-255","INVALID"),
	
	CRANK_SHAFT_SPEED("1004002","曲轴转速"),
	FUEL_CONSUME_PERCENT("1004003","发动机燃料消耗率");
	
	//跛行相应枚举值
	
	
	private String code;
	private String value;
	ReportCarCanEnum(String code, String value) {
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
