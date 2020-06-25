package com.maxus.tsp.gateway.common.model;

public class BlueToothRequestInfo {

	//蓝牙请求Sn
	private String sn = "";
	//蓝牙请求指令
	private String comd = "";
	//蓝牙请求值
	private String value = "";
	//蓝牙请求序列号
	private String seqNo = "";
	//蓝牙请求发生时间
	private long eventTime;
	
	
	public BlueToothRequestInfo() {
		super();
	}
	public BlueToothRequestInfo(String sn, String comd, String value, String seqNo, long eventTime) {
		super();
		this.sn = sn;
		this.comd = comd;
		this.value = value;
		this.seqNo = seqNo;
		this.eventTime = eventTime;
	}
	
	
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
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
	public String getSeqNo() {
		return seqNo;
	}
	public void setSeqNo(String seqNo) {
		this.seqNo = seqNo;
	}
	public long getEventTime() {
		return eventTime;
	}
	public void setEventTime(long eventTime) {
		this.eventTime = eventTime;
	}
	
	
	
	
}
