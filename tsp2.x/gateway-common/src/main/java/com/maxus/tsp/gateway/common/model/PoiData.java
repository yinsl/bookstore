package com.maxus.tsp.gateway.common.model;

public class PoiData {

	private Integer longitude;
	private Integer latitude;
	private String address;
	private Integer gPSType;
	private Integer posType;
	
	public PoiData() {
		super();
	}
	
	public PoiData(Integer longitude, Integer latitude, String address, Integer gPSType,Integer posType){
		this.longitude = longitude;
		this.latitude = latitude;
		this.address = address;	
		this.gPSType = gPSType;
		this.posType = posType;
	}

	public Integer getLongitude() {
		return longitude;
	}
	public void setLongitude(Integer longitude) {
		this.longitude = longitude;
	}
	public Integer getLatitude() {
		return latitude;
	}
	public void setLatitude(Integer latitude) {
		this.latitude = latitude;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}

	public Integer getgPSType() {
		return gPSType;
	}

	public void setgPSType(Integer gPSType) {
		this.gPSType = gPSType;
	}

	public Integer getPosType() {
		return posType;
	}

	public void setPosType(Integer posType) {
		this.posType = posType;
	}
	
	
	
}
