package com.maxus.tsp.gateway.common.model;

public class HomeCtrlResultMo {

	private int cmd;
	private int result;	
	
	


	public int getCmd() {
		return cmd;
	}


	public void setCmd(int cmd) {
		this.cmd = cmd;
	}


	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	
	public HomeCtrlResultMo() {
		super();
	}
	public HomeCtrlResultMo(int cmd, int result){
		super();
		this.cmd = cmd;
		this.result = result;
	}
}
