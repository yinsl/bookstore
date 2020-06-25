package com.maxus.tsp.gateway.common.model;

public class RemoteControlItRequest {

	/**
	 * Tbox序列号
	 */
	private String sn;
	/**
	 * 远程控制指令名称
	 */
	private String comd;
	/**
	 * 远程控制指令对应的参数
	 */
	private String value;
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
	public RemoteControlItRequest(String sn, String comd, String value, String seqNo, long eventTime) {
		super();
		this.sn = sn;
		this.comd = comd;
		this.value = value;
		this.seqNo = seqNo;
		this.eventTime = eventTime;
	}
	public RemoteControlItRequest() {
		super();
	}
}
