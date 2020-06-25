package com.maxus.tsp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.maxus.tsp.common.util.ThrowableUtil;


@SpringBootApplication(exclude = {MybatisAutoConfiguration.class,DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@EnableAsync
@ComponentScan(basePackages = "com.maxus.tsp.stress,com.maxus.tsp.common.redis.allredis")
public class StressTestApplication {
	
	private static Logger logger = LogManager.getLogger(StressTestApplication.class);

	public static void main(String[] args) {
		try {
		//异步写日志
		System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");

		SpringApplication.run(StressTestApplication.class, args);
		}catch(Exception e) {
			logger.error(ThrowableUtil.getErrorInfoFromThrowable(e));
		}
	}

}
