package com.maxus.tsp;

import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @ClassName: ScheduleApplication.java
 * @Description: 定时任务工程
 * @author 余佶
 * @version V1.0
 * @Date 2017年6月13日 下午4:49:13
 */
@SpringBootApplication(exclude = {MybatisAutoConfiguration.class,DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@ComponentScan(basePackages = "com.maxus.tsp.schedule,com.maxus.tsp.common.redis")
public class ScheduleApplication {

	/**
	 * @Title: main
	 * @Description: 定时任务主程序
	 * @param: @param
	 *             args
	 * @return: void
	 * @throws @author
	 *             fogmk
	 * @Date 2017年6月13日 下午4:49:13
	 */
	public static void main(String[] args) {
		
		//异步写日志
		System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");

		SpringApplication.run(ScheduleApplication.class, args);
	}

}
