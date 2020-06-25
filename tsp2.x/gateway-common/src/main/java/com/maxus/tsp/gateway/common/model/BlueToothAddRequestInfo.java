package com.maxus.tsp.gateway.common.model;


/**
 * RVM蓝牙请求参数类
 * @author lzgea
 *
 */
public class BlueToothAddRequestInfo {
	//蓝牙钥匙ID，0为无效值,初始值为null
	private Integer btKeyID;
	//挑战鉴权钥匙
	private String authKey = "";
	//权限
	private Integer permissions;
	//开始日期时间
	private long startDateTime = 0;
	//结束时间 
	private long endDateTime = 0;
	
	public BlueToothAddRequestInfo(String sn, String cmd, Integer btKeyID,
			String authKey, Integer permissions, long startDateTime, long endDateTime,
			String eventTime, String seqNo) {
		super();
		this.btKeyID = btKeyID;
		this.authKey = authKey;
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
		this.permissions = permissions;
	}


	public BlueToothAddRequestInfo(){
		super();
	}
	
	
	public long getStartDateTime() {
		return startDateTime;
	}


	public void setStartDateTime(long startDateTime) {
		this.startDateTime = startDateTime;
	}


	public long getEndDateTime() {
		return endDateTime;
	}


	public void setEndDateTime(long endDateTime) {
		this.endDateTime = endDateTime;
	}

	public Integer getBtKeyID() {
		return btKeyID;
	}
	public void setBtKeyID(Integer btKeyID) {
		this.btKeyID = btKeyID;
	}
	public String getAuthKey() {
		return authKey;
	}
	public void setAuthKey(String authKey) {
		this.authKey = authKey;
	}
	public Integer getPermissions() {
		return permissions;
	}
	public void setPermissions(Integer permissions) {
		this.permissions = permissions;
	}


}
