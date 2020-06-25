package com.maxus.tsp.platform.service.model.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 解析it redis的对象类，反序列化时，JSON对象中的未知字段忽略
 * 
 * @author uwczo
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItRedisInfo {

	// Redis KEY,tbox序列号，不需要写入Redis value
	@JsonIgnore
	private String sn;
	// 车架号
	private String vin;
	// SIM卡号
	private String iccid;
	// uuid
	private String uuid;

	private String loginIp;

	private String loginPort;
	// 激活状态
	private String deviceLockStatus;
	// 上传的topic
	private String upLoaderTopic;
	// 可充电储能子系统数
	private int batteryNum;
	// 可充电储能系统编码列表
	private Object batteryCode;
	// 摄像头
	private List<Integer> cameraNumList;
	// 短信服务运行商代号
	private String mno;
	// SIM卡号
	private String simNumber;
	// token
	private String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public List<Integer> getCameraNumList() {
		return cameraNumList;
	}

	public void setCameraNumList(List<Integer> cameraNumList) {
		this.cameraNumList = cameraNumList;
	}

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

	public String getIccid() {
		return iccid;
	}

	public void setIccid(String iccid) {
		this.iccid = iccid;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getLoginIp() {
		return loginIp;
	}

	public void setLoginIp(String loginIp) {
		this.loginIp = loginIp;
	}

	public String getLoginPort() {
		return loginPort;
	}

	public void setLoginPort(String loginPort) {
		this.loginPort = loginPort;
	}

	public String getDeviceLockStatus() {
		return deviceLockStatus;
	}

	public void setDeviceLockStatus(String deviceLockStatus) {
		this.deviceLockStatus = deviceLockStatus;
	}

	public String getUpLoaderTopic() {
		return upLoaderTopic;
	}

	public void setUpLoaderTopic(String upLoaderTopic) {
		this.upLoaderTopic = upLoaderTopic;
	}

	public int getBatteryNum() {
		return batteryNum;
	}

	public void setBatteryNum(int batteryNum) {
		this.batteryNum = batteryNum;
	}

	public Object getBatteryCode() {
		return batteryCode;
	}

	public void setBatteryCode(Object batteryCode) {
		this.batteryCode = batteryCode;
	}

	public String getMno() {
		return mno;
	}

	public void setMno(String mno) {
		this.mno = mno;
	}

	public String getSimNumber() {
		return simNumber;
	}

	public void setSimNumber(String number) {
		this.simNumber = number;
	}

	public ItRedisInfo(String sn, String vin, String iccid, String uuid, String deviceLockStatus, String upLoaderTopic,
			int batteryNum, Object batteryCode, String mno, String number) {
		super();
		this.sn = sn;
		this.vin = vin;
		this.iccid = iccid;
		this.uuid = uuid;
		this.deviceLockStatus = deviceLockStatus;
		this.upLoaderTopic = upLoaderTopic;
		this.batteryNum = batteryNum;
		this.batteryCode = batteryCode;
		this.mno = mno;
		this.simNumber = number;
	}

	public ItRedisInfo() {
		super();
	}

}
