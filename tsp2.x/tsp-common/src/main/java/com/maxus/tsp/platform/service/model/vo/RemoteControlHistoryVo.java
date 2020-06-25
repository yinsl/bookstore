package com.maxus.tsp.platform.service.model.vo;

import java.util.Date;

public class RemoteControlHistoryVo {

	//序列ID
	private int id;
	//车架号
	private String vin;
	//命令
	private String cmd;
	//参数值
	private String cmdVal;
	//下发状态
	private String downState;
	//下发时间
	private Date downDate;
	//创建时间
	private Date createDate;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getVin() {
		return vin;
	}
	public void setVin(String vin) {
		this.vin = vin;
	}
	public String getCmd() {
		return cmd;
	}
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	public String getCmdVal() {
		return cmdVal;
	}
	public void setCmdVal(String cmdVal) {
		this.cmdVal = cmdVal;
	}
	public String getDownState() {
		return downState;
	}
	public void setDownState(String downState) {
		this.downState = downState;
	}
	public Date getDownDate() {
		return downDate;
	}
	public void setDownDate(Date downDate) {
		this.downDate = downDate;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
}
