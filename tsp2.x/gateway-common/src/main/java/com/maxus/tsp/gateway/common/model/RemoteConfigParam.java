package com.maxus.tsp.gateway.common.model;

/*
 * 获取远程配置类中Dataparam封装类
 */
public class RemoteConfigParam {

	private int configID;
	private String value;
	public int getConfigID() {
		return configID;
	}
	public void setConfigID(int configID) {
		this.configID = configID;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

}
