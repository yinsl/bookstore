package com.maxus.tsp.schedule.conf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.maxus.tsp.common.jpush.JPushAPI;
import com.maxus.tsp.common.util.SMSUtil;

@Configuration
public class UtilConfig {
	private static final Logger LOGGER = LogManager.getLogger(UtilConfig.class);

	//JPush
	@Value("${JpushClient.appKey}")
	private String appKey;

	@Value("${JpushClient.masterSecret}")
	private String masterSecret;

	@Value("${JpushClient.appKeyEntertainment}")
	private String appKeyEntertainment;

	@Value("${JpushClient.masterSecretEntertainment}")
	private String masterSecretEntertainment;

	//SMSUtil7
	//SMSUtil
	@Value("${SMSUtil.token_url}")
	private String smsUtilTokenUrl;

	@Value("${SMSUtil.sms_url}")
	private String smsUtilSmsUrl;

	@Value("${SMSUtil.search_user_url}")
	private String smsUtilSearchUserUrl;

	@Value("${SMSUtil.register_url}")
	private String smsUtilRegisterUrl;

	@Value("${SMSUtil.appapi_url}")
	private String smsUtilAppapiUrl;

	@Value("${SMSUtil.client_id_value}")
	private String smsUtilClientIDValue;

	@Value("${SMSUtil.client_secret_value}")
	private String smsUtilClientSecretValue;
	
	@Bean
	int setJpushConfiguration() {
		//set jpush client configuration according to configuration from application.yml
		if (appKey != null && masterSecret != null && appKeyEntertainment != null && masterSecretEntertainment != null) {
			LOGGER.info("Set jpush client configuration according to application.yml");
			JPushAPI.setJPushClient(appKey, masterSecret, appKeyEntertainment, masterSecretEntertainment);
		}

		return 0;
	}
	
	@Bean
	int setSMSUtilConfiguration() {
		//set SUMUtil configuration according to configuration from application.yml
		if (smsUtilTokenUrl!=null && smsUtilSmsUrl!=null && smsUtilSearchUserUrl!=null && smsUtilRegisterUrl!=null &&
				smsUtilAppapiUrl!=null && smsUtilClientIDValue!=null && smsUtilClientSecretValue!=null) {
			LOGGER.info("Set SMSUtil configuration according to application.yml");
			SMSUtil.setSMSUtil(smsUtilClientIDValue, smsUtilClientSecretValue, smsUtilTokenUrl, smsUtilSmsUrl, 
					smsUtilSearchUserUrl, smsUtilAppapiUrl, smsUtilRegisterUrl);
		}
		return 0;
	}
}
