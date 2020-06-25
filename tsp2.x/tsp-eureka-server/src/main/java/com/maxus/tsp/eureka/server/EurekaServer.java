/**        
 * EurekaServer.java Create on 2017年6月5日      
 * Copyright (c) 2017年6月5日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>      
 * @version 1.0  
 */
package com.maxus.tsp.eureka.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @ClassName:     EurekaServer.java   
 * @Description:    
 * @author         任怀宇  
 * @version        V1.0     
 * @Date           2017年6月14日 上午9:38:18
 */
@SpringBootApplication
@EnableEurekaServer
@RestController
public class EurekaServer {
	
	private static final Logger logger = LogManager.getLogger(EurekaServer.class);
	
	public static void main(String[] args) {
        SpringApplication.run(EurekaServer.class, args);
    }
	
	@RequestMapping("/logLevel")
	public String logLevel() {
		logger.debug("------debug----------");
		logger.info("------info-----------");
		logger.warn("------warn----------");
		logger.error("-----error-----------");
		
		return logger.getLevel().name();
	}
	
}
