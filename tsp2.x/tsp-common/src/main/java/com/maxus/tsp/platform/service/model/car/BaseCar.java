/**        
 * BaseCar.java Create on 2017年7月7日      
 * Copyright (c) 2017年7月7日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">赵伟阳</a>      
 * @version 1.0  
 */
package com.maxus.tsp.platform.service.model.car;

import java.util.Date;

/**
 * @ClassName: BaseCar.java
 * @Description: 车辆 对象
 * @author 赵伟阳
 * @version V1.0
 * @Date 2017年7月7日 下午3:01:01
 */
public class BaseCar {

	private Long id;

	private String sn;// 编号

	//private String UUID;

	private String carName;// 车辆名称

	private int state;// 状态1在用,0弃用

	private int idCarType;// 车型id

	private int idOilType;// 燃油类型

	private int idOilNo;// 油号id

	private int idCarColor;// 颜色id;

	private Date createDate;// 创建时间

	private String vin;// 车架号

	private String engineNo;// 发动机编号

	private String carNo;

	
	public BaseCar() {
		super();
	}

	public BaseCar(String vin, String engineNo) {
		super();
		this.vin = vin;
		this.engineNo = engineNo;
	}

	public String getCarNo() {
		return carNo;
	}

	public void setCarNo(String carNo) {
		this.carNo = carNo;
	}

	//private String ICCID;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

//	public String getUUID() {
//		return UUID;
//	}
//
//	public void setUUID(String uUID) {
//		UUID = uUID;
//	}

	public String getCarName() {
		return carName;
	}

	public void setCarName(String carName) {
		this.carName = carName;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getIdCarType() {
		return idCarType;
	}

	public void setIdCarType(int idCarType) {
		this.idCarType = idCarType;
	}

	public int getIdOilType() {
		return idOilType;
	}

	public void setIdOilType(int idOilType) {
		this.idOilType = idOilType;
	}

	public int getIdOilNo() {
		return idOilNo;
	}

	public void setIdOilNo(int idOilNo) {
		this.idOilNo = idOilNo;
	}

	public int getIdCarColor() {
		return idCarColor;
	}

	public void setIdCarColor(int idCarColor) {
		this.idCarColor = idCarColor;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getVin() {
		return vin;
	}

	public void setVin(String vin) {
		this.vin = vin;
	}

	public String getEngineNo() {
		return engineNo;
	}

	public void setEngineNo(String engineNo) {
		this.engineNo = engineNo;
	}

//	public String getICCID() {
//		return ICCID;
//	}
//
//	public void setICCID(String iCCID) {
//		ICCID = iCCID;
//	}

}
