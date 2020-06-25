package com.maxus.tsp.gateway.common.model;

public class TboxUpdateRvmReq {
	// 请求SN号
	private String sn;
	// tbox版本号
	private String version;
	// 升级url
	private String url;
	// 升级的MD5值
	private String md5;
	private String seqNo;
	// 请求时间
	private long eventTime;
	
	
	public TboxUpdateRvmReq(){
		
	}
	
	public TboxUpdateRvmReq(String sn, String version, String url, String md5, String seqNo, long eventTime) {
		super();
		this.sn = sn;
		this.version = version;
		this.url = url;
		this.md5 = md5;
		this.seqNo = seqNo;
		this.eventTime = eventTime;
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

	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	public synchronized TboxUpdateRvmReq generatCopyBean() {
		TboxUpdateRvmReq bean = new TboxUpdateRvmReq();
		bean.setEventTime(this.eventTime);
		bean.setMd5(this.md5);
		bean.setSeqNo(this.seqNo);
		bean.setSn(this.sn);
		bean.setUrl(this.url);
		bean.setVersion(this.version);
		return bean;
	}
}
