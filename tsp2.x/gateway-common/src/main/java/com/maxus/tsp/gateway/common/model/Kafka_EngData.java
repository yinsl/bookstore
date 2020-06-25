package com.maxus.tsp.gateway.common.model;

public class Kafka_EngData {
	private String sn;
	private String vin;
	// 数据采集时间
	private long collectTime;
	// 压缩状态位 0表示不压缩；1表示gzip压缩
	private int compressStatus;
	// 工程数据内容
	private String items;

	
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	public String getVin() {
		return vin;
	}
	public void setVin(String vin) {
		this.vin = vin;
	}
	public long getCollectTime() {
		return collectTime;
	}
	public void setCollectTime(long collectTime) {
		this.collectTime = collectTime;
	}
	public int getCompressStatus() {
		return compressStatus;
	}
	public void setCompressStatus(int compressStatus) {
		this.compressStatus = compressStatus;
	}
	public String getItems() {
		return items;
	}
	public void setItems(String items) {
		this.items = items;
	}
}
