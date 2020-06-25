package com.maxus.tsp.schedule.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @ClassName OmsServiceClient.java
 * @Description 
 * @author zhuna
 * @date 2017年12月20日
 */
@FeignClient("tsp-oms-service")
public interface OmsServiceClient {
	
	@RequestMapping(value = "/omsapi/userCarStatTimingTask", method = {RequestMethod.GET})
	public void userCarStatTimingTask();
	
}
