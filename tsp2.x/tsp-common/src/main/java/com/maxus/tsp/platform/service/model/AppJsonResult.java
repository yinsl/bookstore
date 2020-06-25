package com.maxus.tsp.platform.service.model;

import com.maxus.tsp.common.enums.ResultStatus;

/**
 * 
 * @ClassName: AppJsonResult.java
 * @Description: 手机app接口返回的类
 * @author 张涛
 * @version V1.0
 * @Date 2017年8月30日 上午10:50:46
 */
public class AppJsonResult {

	private String status;
	private String description;
	private Object data;

	public AppJsonResult() {
		super();
	}

	public AppJsonResult(ResultStatus status, Object data) {
		super();
		this.status = status.getCode();
		this.description = status.getDescription();
		this.data = data;
	}
	
	public AppJsonResult(String status, Object data) {
		super();
		this.status = status;
		this.data = data;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

}
