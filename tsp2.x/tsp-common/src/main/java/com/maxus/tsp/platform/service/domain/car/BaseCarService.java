/**        
 * BaseCarService.java Create on 2017年7月6日
 * Copyright (c) 2017年7月6日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">赵伟阳</a>      
 * @version 1.0  
 */
package com.maxus.tsp.platform.service.domain.car;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maxus.tsp.platform.service.dao.car.BaseCarDao;
import com.maxus.tsp.platform.service.model.car.BaseCar;
import com.maxus.tsp.platform.service.model.vo.OwnerBusNichNamesVO;

/**
 * @ClassName: TboxService.java
 * @Description: 车辆基本服务
 * @author 赵伟阳
 * @version V1.0
 * @Date 2017年7月6日 下午4:11:41
 */
@Service
@Transactional
public class BaseCarService implements BaseCarServiceIF {

	@Autowired
	private BaseCarDao baseCarMapper;

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
	@Transactional(readOnly = true)
	public BaseCar selectBaseCarByVIN(String vin) {
		return baseCarMapper.selectBaseCarByVIN(vin);
	}

	/**
	 * @Title: checkCamera
	 * @Description: 根据vin和cameralist检查摄像头编号合法性
	 * @param: @param
	 *             vin cameraList
	 * @param: @return
	 * @return: integer
	 * @throws @author
	 *             余佶
	 * @Date 2017年10月10日 下午16:17:13
	 */
	@Transactional(readOnly = true)
	public int checkCamera(String vin, String cameraList) {
		String[] cameraNoList = cameraList.split(",");
		int result = 0;
		for (int i = 0; i < cameraNoList.length; i++)
			result += baseCarMapper.checkCamera(vin, cameraNoList[i]);
		return result;
	}

	/**
	 * @Title: getAllVinForTbox
	 * @Description: 获取所有tbox对应车架号信息
	 * @param: @return
	 * @return: OwnerBusNichNamesVO[]
	 * @throws @author
	 *             余佶
	 * @Date 2017年11月14日 下午16:17:13
	 */
	@Transactional(readOnly = true)
	public OwnerBusNichNamesVO[] getAllVinForTbox() {
		return baseCarMapper.getAllVinForTbox();

	}

}
