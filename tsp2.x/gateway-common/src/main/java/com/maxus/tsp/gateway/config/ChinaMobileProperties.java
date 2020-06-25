package com.maxus.tsp.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 移动物联网短信接口配置
 * @author uwczo
 *
 */
@ConfigurationProperties(prefix = "chinamobilesmsutil",ignoreUnknownFields = false)
public class ChinaMobileProperties {
	//移动短信网关（第三方）接口地址
	private String sendsms_url;
	//用户名
	private String username;
	//密码
	private String password;
	public String getSendsms_url() {
		return sendsms_url;
	}
	public void setSendsms_url(String sendsms_url) {
		this.sendsms_url = sendsms_url;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
}
