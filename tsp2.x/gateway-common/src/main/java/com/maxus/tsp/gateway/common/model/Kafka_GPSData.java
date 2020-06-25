package com.maxus.tsp.gateway.common.model;

/**
 * 用于向it传递车辆位置信息的单个gps的kafka结构
 * @author uwczo
 *
 */
public class Kafka_GPSData {

	String posStatus;
	double latitude;
	double longitude;
	long collectTime;
	public String getPosStatus() {
		return posStatus;
	}
	public void setPosStatus(String posStatus) {
		this.posStatus = posStatus;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public long getCollectTime() {
		return collectTime;
	}
	public void setCollectTime(long collectTime) {
		this.collectTime = collectTime;
	}
	
}
