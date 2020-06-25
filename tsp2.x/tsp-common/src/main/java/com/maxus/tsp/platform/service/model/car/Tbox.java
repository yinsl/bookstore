/**        
 * Tbox.java Create on 2017年7月13日      
 * Copyright (c) 2017年7月13日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">赵伟阳</a>      
 * @version 1.0  
 */
package com.maxus.tsp.platform.service.model.car;

import java.util.Date;

/**
 * @ClassName: BaseCan.java
 * @Description: 
 * @author 赵伟阳
 * @version V1.0
 * @Date 2017年7月13日 下午1:41:41
 */
public class Tbox {
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
	// 该tbox的公钥
	private String pkey;
	// TBOX状态，0：初始化；1：锁定
	private Integer status;
	// tsp公钥版本更新状态
	private Integer pkeyVersionStatus;
	// TBox软件版本升级状态，0：远程升级成功\n1：正在下载\n2：下载完成\n3：升级失败，原因为升级包下载失败\n4：升级失败，原因为升级包校验失败\n5：升级失败，原因为其它\n其它值：无效
	private Integer swVersionStatus;

	private Date creatDate;

	private Date updateDate;

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
	 * @return the pkey
	 */
	public String getPkey() {
		return pkey;
	}

	/**
	 * @param pkey
	 *            the pkey to set
	 */
	public void setPkey(String pkey) {
		this.pkey = pkey;
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

	/**
	 * @return the pkeyVersionStatus
	 */
	public Integer getPkeyVersionStatus() {
		return pkeyVersionStatus;
	}

	/**
	 * @param pkeyVersionStatus
	 *            the pkeyVersionStatus to set
	 */
	public void setPkeyVersionStatus(Integer pkeyVersionStatus) {
		this.pkeyVersionStatus = pkeyVersionStatus;
	}

	/**
	 * @return the swVersionStatus
	 */
	public Integer getSwVersionStatus() {
		return swVersionStatus;
	}

	/**
	 * @param swVersionStatus
	 *            the swVersionStatus to set
	 */
	public void setSwVersionStatus(Integer swVersionStatus) {
		this.swVersionStatus = swVersionStatus;
	}

	/**
	 * @return the updatedate
	 */
	public Date getUpdateDate() {
		return updateDate;
	}

	/**
	 * @param updatedate
	 *            the updatedate to set
	 */
	public void setUpdateDate(Date updatedate) {
		this.updateDate = updatedate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreatDate() {
		return creatDate;
	}

	public void setCreatDate(Date creatDate) {
		this.creatDate = creatDate;
	}

}
