package com.maxus.tsp.gateway.common.model;

public class RemoteCtrlRespInfo {

	//远程请求回复对应的指令
	private String comd;
	//远程请求对应的值
	private String value;
	//此次远程控制执行结果
	private String result;
	//请求时间
	private String reqTime;
	//数据
	private String data;
	
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
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
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getReqTime() {
		return reqTime;
	}
	public void setReqTime(String reqTime) {
		this.reqTime = reqTime;
	}
	
}
