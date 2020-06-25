package com.maxus.tsp.gateway.common.model;

public class TakePhotoItRequest {
	/**
	 * tbox序列号
	 */
	private String sn;
	/**
	 * 摄像头编号列表
	 */
	private String cameraList;
	/**
	 * 请求序列号，用于确认请求时序，长度为22位的数字。
	 * 规则为17位时间+5位随机数，2018032216343098712345
	 */
	private String seqNo;
	/**
	 * 请求发起时间，格式为
       YYYY-MM-DD hh:mm:ss
	 */
	private long eventTime;
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	public String getCameraList() {
		return cameraList;
	}
	public void setCameraList(String cameraList) {
		this.cameraList = cameraList;
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
