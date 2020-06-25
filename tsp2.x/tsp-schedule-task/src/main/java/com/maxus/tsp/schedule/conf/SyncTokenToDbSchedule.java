package com.maxus.tsp.schedule.conf;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.maxus.tsp.common.constant.ScheduleConstant;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.schedule.service.client.DaTong2TspClient;
/**
 * 每间隔5分钟同步redis里的token数据到Mysql
 * @author csfxh
 *
 */
@Component
public class SyncTokenToDbSchedule {

	private static final Logger LOGGER = LogManager.getLogger(TimingOmsTask.class);
	@Autowired
	private DaTong2TspClient datong2tspClient;
	
	@Scheduled(fixedDelay = ScheduleConstant.TIMER_SYNCHRONIZE_REDIS_DATA_TO_MYSQL)
	public void SynchronizeRedisTokenToDb(){
		LOGGER.info("SyncTokenToDbSchedule running " + System.currentTimeMillis());
		try{
			// 将数据保存到数据库
			datong2tspClient.SnychronizeRdsDataToDb();
		}catch (Exception e) {
			LOGGER.error("redis back up token is failure" + ThrowableUtil.getErrorInfoFromThrowable(e));
		}
	}
	@Scheduled(fixedDelay = ScheduleConstant.TIMER_SYNCHRONIZE_REDIS_DATA_TO_MYSQL)
	public void SynchronizeDbToRedis(){
		LOGGER.info("SynchronizeDbToRedis running " + System.currentTimeMillis());
		try{
			// 将数据库数据同步redis
		datong2tspClient.SynchronizeDbToRedis();
		}catch (Exception e) {
			LOGGER.error("redis back up ownerName is failure" + ThrowableUtil.getErrorInfoFromThrowable(e));
		}		
		
	}
	
}
