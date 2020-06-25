/**        
 * GatewayApplication.java Create on 2017年6月5日      
 * Copyright (c) 2017年6月5日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>      
 * @version 1.0  
 */
package com.maxus.tsp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.redis.allredis.RedisAPI;

/**
 * 
 * @ClassName: GatewayApplication.java
 * @Description: 网关启动类
 * @author 任怀宇
 * @version V1.0
 * @Date 2017年5月31日 下午5:03:43
 */
@SpringBootApplication(exclude = { MybatisAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients
@ComponentScan(basePackages = "com.maxus.tsp.gateway,com.maxus.tsp.common.redis.allredis,com.maxus.tsp.common.util")
@RestController
@EnableAsync
public class GatewayApplication {
	
	private static final Logger logger = LogManager.getLogger(GatewayApplication.class);

	@Autowired
    RedisAPI redisAPI;
	
	public static void main(String[] args) {
		System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
		SpringApplication.run(GatewayApplication.class, args);
	}
	
	@RequestMapping("/logLevel")
	public String logLevel() {
		logger.debug("------debug----------");
		logger.info("------info-----------");
		logger.warn("------warn----------");
		logger.error("-----error-----------");
		
		return logger.getLevel().name();
	}
	
	@RequestMapping(value = "/tboxStatus", method = RequestMethod.GET)
    public String checkTboxStatus(String sn) {

        boolean online = redisAPI.hasKey(RedisConstant.ONLINE_TBOX, sn);
		logger.info("tbox(" + sn + ") is online? " + online);
		
        return online? "online" : "offline";
    }

}
