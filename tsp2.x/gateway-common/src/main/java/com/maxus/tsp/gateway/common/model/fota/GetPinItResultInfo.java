package com.maxus.tsp.gateway.common.model.fota;
/**
*@Title GetPinItResultInfo.java
*@description IT回复的参数信息
*@time 2019年2月15日 下午1:55:15
*@author wqgzf
*@version 1.0
**/
public class GetPinItResultInfo {
	//请求Sn
    private String sn = "";
    //Pin码大小
    private int pinSize;
    //pin码信息
    private String pin = "";
    //请求序列号
    private String seqNo = "";
    //请求发生时间
    private long eventTime;
    
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	
	public int getPinSize() {
		return pinSize;
	}
	public void setPinSize(int pinSize) {
		this.pinSize = pinSize;
	}
	
	public String getPin() {
		return pin;
	}
	public void setPin(String pin) {
		this.pin = pin;
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
