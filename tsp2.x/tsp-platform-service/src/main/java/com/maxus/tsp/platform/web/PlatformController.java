package com.maxus.tsp.platform.web;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.maxus.tsp.platform.service.domain.PlatformServiceImpl;
import com.maxus.tsp.platform.service.model.Platform;

/**
 * 
 * @ClassName: PlatformController.java
 * @author 胡宗明
 * @version V1.0
 * @Date 2018年4月17日 下午14：47
 */
@RestController
public class PlatformController {

	private static final Logger logger = LogManager.getLogger(PlatformController.class);
	@Autowired
	private PlatformServiceImpl platformServiceImpl;

	@RequestMapping(value = "/getPlatformList", method = RequestMethod.GET)
	public List<Platform> getPlatformList() {
		try {
			return platformServiceImpl.platformAll();
		} catch (Exception e) {
			logger.info("mysql connection error, can't do getPlatformList:" + e);
		}
		return null;
	}
}
