/**        
 * PlatformApplication.java Create on 2017年6月13日      
 * Copyright (c) 2017年6月13日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>      
 * @version 1.0  
 */
package com.maxus.tsp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @ClassName: PlatformApplication.java
 * @Description: 
 * @author 任怀宇
 * @version V1.0
 * @Date 2017年6月13日 下午4:49:13
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@ComponentScan(basePackages = "com.maxus.tsp.platform")
@MapperScan("com.maxus.tsp.platform.service.dao")
public class PlatformApplication {

	/**
	 * @Title: main
	 * @Description: 
	 * @param: @param
	 *             args
	 * @return: void
	 * @throws @author
	 *             fogmk
	 * @Date 2017年6月13日 下午4:49:13
	 */
	public static void main(String[] args) {

		System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");

		SpringApplication.run(PlatformApplication.class, args);
	}

}
