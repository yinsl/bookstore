/**
 * ChangeNickNameController.java Create on 2017年7月6日
 * Copyright (c) 2017年7月6日 by 上汽集团商用车技术中心 
 * @author <a href="renhuaiyu@saicmotor.com">赵伟阳</a>
 * @version 1.0  
 */
package com.maxus.tsp.platform.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.platform.service.domain.car.BaseCarService;
import com.maxus.tsp.platform.service.model.car.BaseCar;
import com.maxus.tsp.platform.service.model.vo.OwnerBusNichNamesVO;

/**
 * @ClassName: ChangeNickNameController.java
 * @Description: 修改用户的基本信息的控制器
 * @author zhuna
 * @version V1.0
 * @Date 2018年12月13日 下午3:37:26
 */

@RestController
@RequestMapping(value = { "/tsp/appapi", "/api" })
public class BaseCarController {

	private static final Logger LOGGER = LogManager.getLogger(BaseCarController.class);

	@Autowired
	private BaseCarService baseCarService;

	@RequestMapping(value = "selectBaseCarByVIN", method = { RequestMethod.GET, RequestMethod.POST })
	public BaseCar selectBaseCarByVIN(String vin) {
		LOGGER.debug("Enter selectBaseCarByVIN:" + vin);
		try {
			BaseCar result = baseCarService.selectBaseCarByVIN(vin);
			return result;
		} catch (Exception e) {
			LOGGER.error(ThrowableUtil.getErrorInfoFromThrowable(e));
			return null;
		}
	}

	@RequestMapping(value = "checkCamera", method = { RequestMethod.GET, RequestMethod.POST })
	public int checkCamera(@RequestParam(value = "vin") String vin,
			@RequestParam(value = "cameraList") String cameraList) {
		LOGGER.debug("Enter checkCamera:" + vin);
		try {
			int result = baseCarService.checkCamera(vin, cameraList);
			return result;
		} catch (Exception e) {
			LOGGER.error(ThrowableUtil.getErrorInfoFromThrowable(e));
			return 0;
		}
	}

	@RequestMapping(value = "getAllVinForTbox", method = { RequestMethod.GET, RequestMethod.POST })
	public OwnerBusNichNamesVO[] getAllVinForTbox() {
		LOGGER.debug("Enter getAllVinForTbox:");
		try {
			OwnerBusNichNamesVO[] result = baseCarService.getAllVinForTbox();
			return result;
		} catch (Exception e) {
			LOGGER.error(ThrowableUtil.getErrorInfoFromThrowable(e));
			return null;
		}
	}
}
