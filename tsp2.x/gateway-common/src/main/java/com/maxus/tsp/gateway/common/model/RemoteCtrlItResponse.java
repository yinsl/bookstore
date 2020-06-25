package com.maxus.tsp.gateway.common.model;

/**
 * 网关节点获得执行结果后产生该主题。平台侦听该主题，确认请求结果
 *
 */
public class RemoteCtrlItResponse {
	// tbox序列号
	private String sn;
	// 返回码
	private String status;
	// 无
	private Object data;
	// 返回码的对应描述
	private String description;
	// 请求主题中解析得到的序列号，平台可用于确认请求时序
	private String seqNo;
	// 请求发起时间
	private long eventTime;

	public void setSn(String sn) {
		this.sn = sn;
	}
	public String getSn() {
		return sn;
	}

	public void setVin(String sn) {
		this.sn = sn;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	

	public RemoteCtrlItResponse(String sn, String status, String data, String description, String seqNo,
			long eventTime) {
		super();
		this.sn = sn;
		this.status = status;
		this.data = data;
		this.description = description;
		this.seqNo = seqNo;
		this.eventTime = eventTime;
	}

	public RemoteCtrlItResponse() {
		super();
	}

}
