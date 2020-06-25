/**        
 * ConfigServerApplication.java Create on 2017年6月5日      
 * Copyright (c) 2017年6月5日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>      
 * @version 1.0  
 */
package com.maxus.tsp.config.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@EnableAutoConfiguration
@EnableDiscoveryClient
@EnableConfigServer
@RestController
public class ConfigServerApplication {
	
	private static final Logger logger = LogManager.getLogger(ConfigServerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
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
