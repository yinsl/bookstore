package com.maxus.tsp.platform.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.maxus.tsp.platform.service.domain.car.RemoteControlService;

/**
 * 
 * @ClassName: RemoteCtrlController.java
 * @Description: 
 * @author 任怀宇
 * @version V1.0
 * @Date 2017年7月17日 上午11:18:30
 */
@RestController
public class RemoteCtrlController {
	private final Logger logger = LogManager.getLogger(getClass());

	@Autowired
	RemoteControlService remoteCtrlService;

	@RequestMapping(value = "/app/recordRemoteCtrl", method = RequestMethod.GET)
	public int recordRemoteCtrl(String vin, String cmd, String cmdVal) {
		logger.info("Enter recordRemoteCtrl" + vin);
		return remoteCtrlService.recordRemoteCtrl(vin, cmd, cmdVal);
	}

	@RequestMapping(value = "/app/updateRemoteCtrlStatus", method = RequestMethod.GET)
	public boolean addRemoteCtrlDownStatus(int remoteCtrlID, String downDate) {
		logger.info("Enter addRemoteCtrlDownStatus");
		return remoteCtrlService.addRemoteCtrlDownStatus(remoteCtrlID, downDate);
	}
	
	@RequestMapping(value = "/app/getCountForOperingRmtCtrl", method = RequestMethod.GET)
	public int getCountForOperRmtCtrl(String vin, String limitTime) {
		logger.info("Enter getCountForOperRmtCtrl");
		return remoteCtrlService.getCountForOperRmtCtrl(vin, limitTime);
	}
}
