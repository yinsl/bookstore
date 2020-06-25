package com.maxus.tsp.gateway.common.model;
/**
 * 远程控制返回值封装类
 * @author lzgea
 *
 */
public class RemoteCtrlResponseData {

	//TBOX返回错误码，没有则为空
	public String errCode = "";
	//具体参数值
	public Object[] param;
	// 获取远程车况存储结构
	public Object[] vehicleStatus;
	
	public RemoteCtrlResponseData(){
		super();
	}
	
	public RemoteCtrlResponseData(String errCode, Object[] param, Object[] vehicleStatus){
		super();
		this.errCode = errCode;
		this.param = param;
		this.vehicleStatus = vehicleStatus;
	}

	public String getErrCode() {
		return errCode;
	}

	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}

	public Object[] getParam() {
		return param;
	}

	public void setParam(Object[] param) {
		this.param = param;
	}

	public Object[] getVehicleStatus() {
		return vehicleStatus;
	}

	public void setVehicleStatus(Object[] vehicleStatus) {
		this.vehicleStatus = vehicleStatus;
	}
	
	
}
