package com.maxus.tsp.gateway.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.maxus.tsp.gateway.service.MqttService;

@RestController
public class TaskController {
	
	private final Logger logger = LogManager.getLogger(getClass());
	
	@Autowired
	MqttService mqttService;
	
	@CrossOrigin
	@RequestMapping(value = "/createGroup", method = RequestMethod.GET)
	public String createTestGroup(String groupName,int groupNum) {
		logger.info("Try to create task group {}, {}",groupName,groupNum);
		return mqttService.saveTaskGroup(groupName,groupNum).toString();
	}
	
	@CrossOrigin
	@RequestMapping(value = "/addPubDetails", method = RequestMethod.GET)
	public void addPubDetails(int num,long number,String ip,long startTime,long endTime,long nanoCostAll) {
		//logger.info("Try to add publish detail {} {} {}",num,number,ip);
		mqttService.addPubDetail(num,number,ip,startTime,endTime,nanoCostAll);
	}

}
