package com.maxus.tsp.common.util.conf;

import java.net.MalformedURLException;

import javax.xml.soap.SOAPException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.maxus.tsp.common.util.LianTongSMSUtil;
import com.sun.xml.wss.XWSSecurityException;

@Configuration
@EnableConfigurationProperties(LiantongSMSProperties.class)
public class LianTongSMSConfig {
	
	@Autowired
	private LiantongSMSProperties liantongSMSProperties;
	
	@Bean
	public LianTongSMSUtil lianTongSMSUtil() throws MalformedURLException, SOAPException, XWSSecurityException {
		LianTongSMSUtil lianTongSMSUtil = new LianTongSMSUtil();
		lianTongSMSUtil.setLiantongSMSProperties(liantongSMSProperties);
		return lianTongSMSUtil;
	}

}
