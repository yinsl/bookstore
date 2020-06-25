package com.maxus.tsp.platform.service.model.vo;

import java.io.Serializable;
import java.util.Date;

import com.maxus.tsp.common.util.ThrowableUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.maxus.tsp.common.constant.AlarmTypeConstant;

public class ReportAlarm implements Serializable, Cloneable{
	
	private static Logger logger = LogManager.getLogger(ReportAlarm.class);
	private static final long serialVersionUID = 488866069431877556L;
	// 产生报警的tbox序列号
	private String sn;
	// 车架号vin
	private String vin;
	// Longitude
	private int longitude;
	// latitude
	private int latitude;
	// gps创建时间
	private Date createTime;
	// 报警编码
	private AlarmTypeConstant alarmCode;
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	public String getVin() {
		return vin;
	}
	public void setVin(String vin) {
		this.vin = vin;
	}
	public int getLongitude() {
		return longitude;
	}
	public void setLongitude(int longitude) {
		this.longitude = longitude;
	}
	public int getLatitude() {
		return latitude;
	}
	public void setLatitude(int latitude) {
		this.latitude = latitude;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public AlarmTypeConstant getAlarmCode() {
		return alarmCode;
	}
	public void setAlarmCode(AlarmTypeConstant alarmCode) {
		this.alarmCode = alarmCode;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	//复制警告信息对象，由于用户信息和用户车辆信息为固定数据，故只需浅克隆
	public static ReportAlarm copyValueOf(ReportAlarm inputAlarmInfo)
	{
		try {
				return (ReportAlarm)inputAlarmInfo.clone();
		} catch (CloneNotSupportedException ex) {
			logger.error("ReportAlarm clone failed.原因:{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
			return null;
		} 
	}
	
}
