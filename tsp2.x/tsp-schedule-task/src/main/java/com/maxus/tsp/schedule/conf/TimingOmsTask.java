package com.maxus.tsp.schedule.conf;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.maxus.tsp.schedule.service.client.OmsServiceClient;

/**
 * @ClassName TimingOmsTask.java
 * @Description 
 * @author zhuna
 * @date 2017年12月20日
 */
@Component
public class TimingOmsTask {
	
	private static final Logger logger = LogManager.getLogger(TimingOmsTask.class);
	
	@Autowired
	private OmsServiceClient omsServiceClient;
	
	//OMS用户车辆信息统计定时任务
	@Scheduled(cron = "0 0 1 * * ?")
	//@Scheduled(cron = "0 0/1 * * * ?")
	public void userCarStatistics() {
		logger.info("======userCartask=======running===" + System.currentTimeMillis() + "=======" + new Date());
		omsServiceClient.userCarStatTimingTask();
	}
}
