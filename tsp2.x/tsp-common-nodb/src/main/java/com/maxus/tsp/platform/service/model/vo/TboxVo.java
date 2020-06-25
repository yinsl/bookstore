/**        
 * TboxVo.java Create on 2017年7月13日      
 * Copyright (c) 2017年7月13日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">赵伟阳</a>      
 * @version 1.0  
 */
package com.maxus.tsp.platform.service.model.vo;

/**
 * @ClassName: TboxVo.java
 * @Description: 网关前端使用TBox相关属性的简化Tboxinfo
 * @author 赵伟阳
 * @version V1.0
 * @Date 2017年7月13日 下午1:41:41
 */
public class TboxVo {
	// 主键
	private Long id;
	// ICCID，Tbox中SIM卡唯一标示
	private String iccid;
	// TboxSn，Tbox唯一标示
	private String sn;
	// sim卡号
	private String simno;
	// tbox uuid'
	private String uuid;
	
	// TBOX状态，0：初始化；1：锁定
	private Integer status;
	
	private String pkey;
	
	private String major;
	
	private String minor;

	public String getMajor() {
		return major;
	}

	public void setMajor(String major) {
		this.major = major;
	}

	public String getMinor() {
		return minor;
	}

	public void setMinor(String minor) {
		this.minor = minor;
	}

	public String getPkey() {
		return pkey;
	}

	public void setPkey(String pkey) {
		this.pkey = pkey;
	}

	/**
	 * @return the iccid
	 */
	public String getIccid() {
		return iccid;
	}

	/**
	 * @param iccid
	 *            the iccid to set
	 */
	public void setIccid(String iccid) {
		this.iccid = iccid;
	}

	/**
	 * @return the sn
	 */
	public String getSn() {
		return sn;
	}

	/**
	 * @param sn
	 *            the sn to set
	 */
	public void setSn(String sn) {
		this.sn = sn;
	}

	/**
	 * @return the simno
	 */
	public String getSimno() {
		return simno;
	}

	/**
	 * @param simno
	 *            the simno to set
	 */
	public void setSimno(String simno) {
		this.simno = simno;
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid
	 *            the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}



	/**
	 * @return the status
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}



}
