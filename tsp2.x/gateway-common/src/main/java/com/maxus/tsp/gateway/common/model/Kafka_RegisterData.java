package com.maxus.tsp.gateway.common.model;

/**
 * 用于向it传递tbox注册信息的kafka结构
 * @author uwczo
 *
 */
public class Kafka_RegisterData {

	String sn;
	String vin;

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
}
