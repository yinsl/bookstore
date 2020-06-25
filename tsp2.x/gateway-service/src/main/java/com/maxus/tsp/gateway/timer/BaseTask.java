package com.maxus.tsp.gateway.timer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.gateway.common.model.BaseRmtCtrlItReq;
import com.maxus.tsp.gateway.service.KafkaService;

public abstract class BaseTask implements Runnable {

	protected final Logger logger = LogManager.getLogger(this.getClass());

	protected static ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(10);

	protected BaseRmtCtrlItReq rmtRequest;
	
	protected RedisAPI redisAPI;

	/**
	 * 给IT发送操作超时结果
	 */
	protected KafkaService kafkaService;

	// 是否需要取消本定时器
	protected boolean cancelFlag = false;

}
