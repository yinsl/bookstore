package com.maxus.tsp.gateway.common.model;
/**
 * 用于Tbox文件服务相关上行状态（远程升级、文件上传、文件下载）
 * @author lzgea
 *
 */
public class TboxFileLoadStatus {

	//rvm请求序列号
	private String seqNo;
	// tbox升级状态
	private String result;
	// 请求发起是时间
	private long eventTime;
	
	public long getEventTime() {
		return eventTime;
	}
	public void setEventTime(long eventTime) {
		this.eventTime = eventTime;
	}
	public String getSeqNo() {
		return seqNo;
	}
	public void setSeqNo(String seqNo) {
		this.seqNo = seqNo;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	
	
	
}
