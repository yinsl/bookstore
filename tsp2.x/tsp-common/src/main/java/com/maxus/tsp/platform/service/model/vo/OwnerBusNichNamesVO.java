/**        
 * OwnerBusNichNamesVO.java Create on 2017年7月12日      
 * Copyright (c) 2017年7月12日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">赵伟阳</a>      
 * @version 1.0  
 */
package com.maxus.tsp.platform.service.model.vo;

/**
 * @ClassName: OwnerBusNichNamesVO.java
 * @Description: 
 * @author 赵伟阳
 * @version V1.0
 * @Date 2017年7月12日 下午1:32:26
 */
public class OwnerBusNichNamesVO {

	private String sn;// 车辆编号
	private String carName;// 车辆昵称
	private String carType;// 车辆类型
	private String vin; // 车架号

	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

	public String getCarName() {
		return carName;
	}

	public void setCarName(String carName) {
		this.carName = carName;
	}

	public String getCarType() {
		return carType;
	}

	public void setCarType(String carType) {
		this.carType = carType;
	}

	public String getVin() {
		return vin;
	}

	public void setVin(String vin) {
		this.vin = vin;
	}

}
