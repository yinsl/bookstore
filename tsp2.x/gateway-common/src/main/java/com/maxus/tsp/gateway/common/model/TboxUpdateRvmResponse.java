package com.maxus.tsp.gateway.common.model;
/**
 * 返回给RVM tbox远程升级返回结果类
 * @author lzgea
 *
 */
public class TboxUpdateRvmResponse {

	//sn T-box序列号
	private String sn;
	//status返回码，表示结果含义见“status值对应含义表“描述
	private String status;
	//description返回码的对应描述
	private String description;
	//data TBOX返回结果
	private Object data;
	//seqNo请求主题中解析得到的序列号，平台可用于确认请求时序。
	private String seqNo;
	//eventTime请求发起时间，格式为时间戳
	private long eventTime;
	
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
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
