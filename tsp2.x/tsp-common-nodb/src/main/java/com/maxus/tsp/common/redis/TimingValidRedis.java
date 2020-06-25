package com.maxus.tsp.common.redis;

import com.maxus.tsp.common.redis.allredis.RedisAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.maxus.tsp.common.constant.RedisConstant;

/**
 * @ClassName TimingValidRedis.java
 * @Description
 * @author 任怀宇
 * @date 2017年12月20日
 */
@Component
public class TimingValidRedis {

	// private static final Logger logger =
	// LogManager.getLogger(TimingValidRedis.class);

	@Autowired
	private RedisAPI redisAPI;

	// OMS用户车辆信息统计定时任务
	@Scheduled(fixedDelay = RedisConstant.FIVE_MINUTE_TIME)
	public void validateRedis() {
		// 原来不可用，看看是否可以变成可用了
		redisAPI.validateRedis();

	}
}
