package com.maxus.tsp.gateway.common.model.fota;
/**
*@Title UpgradeResumeRespInfo.java
*@description fota继续升级的返回内容封装类
*@time 2019年2月12日 上午8:34:39
*@author wqgzf
*@version 1.0
**/
public class UpgradeResumeRespInfo {
	private long id;
	private int result;
    
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
}
