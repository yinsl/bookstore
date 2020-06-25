package com.maxus.tsp.gateway.common.model.fota;
/**
*@Title UpgradeResumeReqInfo.java
*@description fota继续升级请求内容的封装类
*@time 2019年2月11日 下午1:52:13
*@author wqgzf
*@version 1.0
**/
public class UpgradeResumeReqInfo {
	 //请求Sn
	 private String sn;
	 //任务Id
	 private Integer id;
	 //请求序列号
	 private String seqNo;
	 //请求发生时间
	 private long eventTime;
	 
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
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
