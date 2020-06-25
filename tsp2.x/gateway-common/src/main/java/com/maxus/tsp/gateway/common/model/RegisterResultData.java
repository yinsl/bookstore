package com.maxus.tsp.gateway.common.model;

/**
 * 平台处理register_data topic上的设备激活状态，处理后写入处理状态，网关监听，获得平台的处理状态
 *
 */
public class RegisterResultData {
	//T-box序列号
	private String sn;
	//操作成功/失败
	private String status;

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
}
