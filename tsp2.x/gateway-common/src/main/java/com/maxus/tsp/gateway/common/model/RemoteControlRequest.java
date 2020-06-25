package com.maxus.tsp.gateway.common.model;

public class RemoteControlRequest {
	//车架号
	private String vin;
	//tbox序列号
	private String serialNumber;
	//远程控制命令
	private String comd;
	//远程控制命令参数
	private String value;
	public String getVin() {
		return vin;
	}
	public void setVin(String vin) {
		this.vin = vin;
	}
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	public String getComd() {
		return comd;
	}
	public void setComd(String comd) {
		this.comd = comd;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

}
