package com.maxus.tsp.gateway.common.model;

/**
 * @ClassName HomeControlItRequest.java
 * @Description 
 * @author zhuna
 * @date 2018年11月8日
 */
public class HomeCtrlItRequest extends BaseRmtCtrlItReq{

	/**
	 * 参数长度（报文字节长度）
	 */
	private Integer paramSize;

	/**
	 * 房车家居远程控制指令对应的参数
	 */
	private String param;
	public Integer getParamSize() {
		return paramSize;
	}
	public void setParamSize(Integer paramSize) {
		this.paramSize = paramSize;
	}
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}

	public HomeCtrlItRequest(String sn, int paramSize, String param, String seqNo, long eventTime) {
		super();
		this.setSn(sn);
		this.paramSize = paramSize;
		this.param = param;
		this.setSeqNo(seqNo);
		this.setEventTime(eventTime);
	}
	public HomeCtrlItRequest() {
		super();
	}
}
