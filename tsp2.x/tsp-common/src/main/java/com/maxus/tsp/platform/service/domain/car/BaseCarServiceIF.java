/**        
 * BaseCarServiceIF.java Create on 2017年7月6日
 * Copyright (c) 2017年7月6日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">赵伟阳</a>      
 * @version 1.0  
 */
package com.maxus.tsp.platform.service.domain.car;


import com.maxus.tsp.platform.service.model.car.BaseCar;

/**
 * @ClassName: BaseCarServiceIF
 * @Description: 车辆基本服务
 * @author 赵伟阳
 * @version V1.0
 * @Date 2017年7月6日 下午4:12:25
 */
public interface BaseCarServiceIF {

	/**
	 * @Title: selectBaseCarByVIN
	 * @Description: 根据发动机和车架号来查询车辆
	 * @param: @param
	 *             engineNo
	 * @param: @param
	 *             vin
	 * @param: @return
	 * @return: Long
	 * @throws @author
	 *             赵伟阳
	 * @Date 2017年7月24日 下午1:17:13
	 */
	BaseCar selectBaseCarByVIN(String vin);

}
