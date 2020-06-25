package com.maxus.tsp.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 读取网关的极光推送配置（原61项目使用，融合项目后期可能就不再需要该配置）
 * @author uwczo
 *
 */
@ConfigurationProperties(prefix = "jpushclient", ignoreUnknownFields = false)
public class JpushProperties {
	//app的极光配置key
	private String appKey;
	//app的极光配置masterSecret
	private String masterSecret;
	//娱乐主机极光配置key
	private String appKeyEntertainment;
	//娱乐主机极光配置masterSecret
	private String masterSecretEntertainment;

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getMasterSecret() {
		return masterSecret;
	}

	public void setMasterSecret(String masterSecret) {
		this.masterSecret = masterSecret;
	}

	public String getAppKeyEntertainment() {
		return appKeyEntertainment;
	}

	public void setAppKeyEntertainment(String appKeyEntertainment) {
		this.appKeyEntertainment = appKeyEntertainment;
	}

	public String getMasterSecretEntertainment() {
		return masterSecretEntertainment;
	}

	public void setMasterSecretEntertainment(String masterSecretEntertainment) {
		this.masterSecretEntertainment = masterSecretEntertainment;
	}
	
}
