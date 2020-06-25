package com.maxus.tsp.gateway.common.model;
/**
 * @Title EarlyWarningInfo
 * @Description 预警信息
 * @author 汪亚军
 * @date 2018年11月20日
 */
public class EarlyWarningInfo {
	//sn
	private String sn;
	//vin
	private String vin;
	//command
	private String command;
	//warningType
	private int warningType;
	//startTime
	private long startTime;
	//endTime
	private long endTime;
	//speed
	private long speed;
	//posStatus
	private String posStatus;
	//latitude
	private long latitude;
	//longitude
	private long longitude;
	private long gatewayTimeIn;
	private long gatewayTimeOut;
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	public String getVin() {
		return vin;
	}
	public void setVin(String vin) {
		this.vin = vin;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public int getWarningType() {
		return warningType;
	}
	public void setWarningType(int warningType) {
		this.warningType = warningType;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public long getSpeed() {
		return speed;
	}
	public void setSpeed(long speed) {
		this.speed = speed;
	}
	public String getPosStatus() {
		return posStatus;
	}
	public void setPosStatus(String posStatus) {
		this.posStatus = posStatus;
	}
	public long getLatitude() {
		return latitude;
	}
	public void setLatitude(long latitude) {
		this.latitude = latitude;
	}
	public long getLongitude() {
		return longitude;
	}
	public void setLongitude(long longitude) {
		this.longitude = longitude;
	}

	public long getGatewayTimeIn() {
		return gatewayTimeIn;
	}

	public void setGatewayTimeIn(long gatewayTimeIn) {
		this.gatewayTimeIn = gatewayTimeIn;
	}

	public long getGatewayTimeOut() {
		return gatewayTimeOut;
	}

	public void setGatewayTimeOut(long gatewayTimeOut) {
		this.gatewayTimeOut = gatewayTimeOut;
	}
}
