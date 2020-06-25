package com.maxus.tsp.platform.service.model.vo;

import java.util.Date;

/**
 * @ClassName: TboxUpdateInfoVo.java
 * @Description: 网关前端使用TBox更新相关属性的简化TboxUpdateInfoVo
 * @author 余佶
 * @version V1.0
 * @Date 2017年10月16日 上午10:41:41
 */
public class TboxUpdateInfoVo {
	//id
	private Integer id;
	//升级包版本
	private String upgrade_version;
	//升级包地址
	private String upgrade_url;
	//升级时间
	private Date upgrade_time;
	//更新描述
	private String upgrade_note;
	//md5
	private String md5;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getUpgrade_version() {
		return upgrade_version;
	}
	public void setUpgrade_version(String upgrade_version) {
		this.upgrade_version = upgrade_version;
	}
	public String getUpgrade_url() {
		return upgrade_url;
	}
	public void setUpgrade_url(String upgrade_url) {
		this.upgrade_url = upgrade_url;
	}
	public Date getUpgrade_time() {
		return upgrade_time;
	}
	public void setUpgrade_time(Date upgrade_time) {
		this.upgrade_time = upgrade_time;
	}
	public String getUpgrade_note() {
		return upgrade_note;
	}
	public void setUpgrade_note(String upgrade_note) {
		this.upgrade_note = upgrade_note;
	}
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	
	
	
}
