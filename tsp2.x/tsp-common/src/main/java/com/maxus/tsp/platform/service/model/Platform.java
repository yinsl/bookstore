package com.maxus.tsp.platform.service.model;

import java.util.Date;

public class Platform {

	private long id;
	private String ip;
	private int port;
	private String username;
	private String password;
	private long typeId;
	private String status;
	private Date lastLoginTime;
	private Date lastLogoutTime;
	private String connectIp;
	private int connectPort;
	private String remark;
	private Date createTime;
	private String createdBy;
	private Date updateTime;
	private String updatedBy;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public long getTypeId() {
		return typeId;
	}

	public void setTypeId(long typeId) {
		this.typeId = typeId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public Date getLastLogoutTime() {
		return lastLogoutTime;
	}

	public void setLastLogoutTime(Date lastLogoutTime) {
		this.lastLogoutTime = lastLogoutTime;
	}

	public String getConnectIp() {
		return connectIp;
	}

	public void setConnectIp(String connectIp) {
		this.connectIp = connectIp;
	}

	public int getConnectPort() {
		return connectPort;
	}

	public void setConnectPort(int connectPort) {
		this.connectPort = connectPort;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getUpdateBy() {
		return updatedBy;
	}

	public void setUpdateBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Platform(long id, String ip, int port, String username, String password, long typeId, String status,
			Date lastLoginTime, Date lastLogoutTime, String connectIp, int connectPort, String remark, Date createTime,
			String createdBy, Date updateTime, String updatedBy) {
		super();
		this.id = id;
		this.ip = ip;
		this.port = port;
		this.username = username;
		this.password = password;
		this.typeId = typeId;
		this.status = status;
		this.lastLoginTime = lastLoginTime;
		this.lastLogoutTime = lastLogoutTime;
		this.connectIp = connectIp;
		this.connectPort = connectPort;
		this.remark = remark;
		this.createTime = createTime;
		this.createdBy = createdBy;
		this.updateTime = updateTime;
		this.updatedBy = updatedBy;
	}

	public Platform() {
		super();
	}

	
}
