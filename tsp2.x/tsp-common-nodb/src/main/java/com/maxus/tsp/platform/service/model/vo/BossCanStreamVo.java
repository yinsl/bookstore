package com.maxus.tsp.platform.service.model.vo;

import java.io.Serializable;

/**
 * @ClassName: BossCanStreamVo.java
 * @Description: 车况数据对象组，用于stream传递
 * @author 余佶
 * @version V1.0
 * @Date 2017年7月18日 下午1:42:05
 */
public class BossCanStreamVo implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ReportCan[] reportCanGroup;
	private String vin;
	private String serialNumber;
	
	public BossCanStreamVo()
	{
		super();
	}
	
	public BossCanStreamVo(ReportCan[] reportCanGroup, String vin, String serialNumber) {
		super();
		this.reportCanGroup = reportCanGroup;
		this.vin = vin;
		this.serialNumber = serialNumber;
	}
	
	public ReportCan[] getReportCanGroup() {
		return reportCanGroup;
	}
	public void setReportCanGroup(ReportCan[] reportCanGroup) {
		this.reportCanGroup = reportCanGroup;
	}
	public String getVin() {
		return vin;
	}
	public void setVin(String vin) {
		this.vin = vin;
	}
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	
	

}
