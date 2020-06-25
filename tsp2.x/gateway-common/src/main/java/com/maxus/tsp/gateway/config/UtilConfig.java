package com.maxus.tsp.gateway.config;

import com.maxus.tsp.common.jpush.JPushAPI;
import com.maxus.tsp.common.util.ChinaMobileSMSUtil;
import com.maxus.tsp.common.util.SMSUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = { JpushProperties.class, SmsProperties.class ,ChinaMobileProperties.class})
public class UtilConfig {
	private static final Logger LOGGER = LogManager.getLogger(UtilConfig.class);
	


	//极光推送配置
	@Autowired
	private JpushProperties jpushProperties;

 	//SMSUtil配置
	@Autowired
	private SmsProperties smsProperties;
	
	//ChinaMobileSMSUtil配置
	@Autowired
	private ChinaMobileProperties chinaMobileProperties;

	
	@Bean
	int setJpushConfiguration() {
		//初始化极光推送配置（TODO:原61项目使用，融合项目后期可能就不再需要该配置）
		String appKey = jpushProperties.getAppKey();
		String masterSecret = jpushProperties.getMasterSecret();
		String appKeyEntertainment = jpushProperties.getAppKeyEntertainment();
		String masterSecretEntertainment = jpushProperties.getMasterSecretEntertainment();
		if (appKey != null
				&& masterSecret != null
				&& appKeyEntertainment != null
				&& masterSecretEntertainment != null) {
			LOGGER.info("Set jpush client configuration according to bootstrap.properties");
			JPushAPI.setJPushClient(appKey, masterSecret, appKeyEntertainment, masterSecretEntertainment);
		}

		return 0;
	}
	
	@Bean
	int setSMSUtilConfiguration() {
		// set SUMUtil configuration according to configuration from
		// application.yml
		// TODO:原61项目使用，融合项目后期可能就不再需要该配置
		String smsUtilTokenUrl = smsProperties.getToken_url();
		String smsUtilSmsUrl = smsProperties.getSms_url();
		String smsUtilSearchUserUrl = smsProperties.getSearch_user_url();
		String smsUtilRegisterUrl = smsProperties.getRegister_url();
		String smsUtilAppapiUrl = smsProperties.getAppapi_url();
		String smsUtilClientIDValue = smsProperties.getClient_id_value();
		String smsUtilClientSecretValue = smsProperties.getClient_secret_value();
		if (smsUtilTokenUrl != null && smsUtilSmsUrl != null && smsUtilSearchUserUrl != null
				&& smsUtilRegisterUrl != null && smsUtilAppapiUrl != null && smsUtilClientIDValue != null
				&& smsUtilClientSecretValue != null) {
			LOGGER.info("Set SMSUtil configuration according to bootstrap.properties");
			SMSUtil.setSMSUtil(smsUtilClientIDValue, smsUtilClientSecretValue, smsUtilTokenUrl, smsUtilSmsUrl,
					smsUtilSearchUserUrl, smsUtilAppapiUrl, smsUtilRegisterUrl);
		}
		return 0;
	}
	
	@Bean
	int setChinaMobileProperties() {
		//初始化移动物联网短信网关配置信息
		String sms_url = chinaMobileProperties.getSendsms_url();
		String userName =chinaMobileProperties.getUsername();
		String passWord = chinaMobileProperties.getPassword();
		if(sms_url!=null&&userName!=null&&passWord!=null){
			LOGGER.info("Set chinaMobileUtil configuration according to bootstrap.properties");
			ChinaMobileSMSUtil.setChinaMobileProperties(sms_url, userName, passWord);
		}
		return 0;
	}
}
