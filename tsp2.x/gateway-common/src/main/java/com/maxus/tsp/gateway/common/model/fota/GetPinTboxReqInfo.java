package com.maxus.tsp.gateway.common.model.fota;
/**
*@Title GetPinTboxReqInfo.java
*@description TBox发送的报文中的参数信息
*@time 2019年2月15日 下午1:06:28
*@author wqgzf
*@version 1.0
**/
public class GetPinTboxReqInfo {
	//请求Sn
    private String sn = "";
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
