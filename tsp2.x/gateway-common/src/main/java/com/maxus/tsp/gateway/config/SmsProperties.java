package com.maxus.tsp.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 网关发送短信的短信网关配置（原61项目使用，融合项目后期可能就不再需要该配置）
 * @author uwczo
 *
 */
@ConfigurationProperties(prefix = "smsutil", ignoreUnknownFields = false)
public class SmsProperties {

	private String token_url;
	private String sms_url;
	private String search_user_url;
	private String register_url;
	private String appapi_url;
	private String client_id_value;
	private String client_secret_value;
	public String getToken_url() {
		return token_url;
	}
	public void setToken_url(String token_url) {
		this.token_url = token_url;
	}
	public String getSms_url() {
		return sms_url;
	}
	public void setSms_url(String sms_url) {
		this.sms_url = sms_url;
	}
	public String getSearch_user_url() {
		return search_user_url;
	}
	public void setSearch_user_url(String search_user_url) {
		this.search_user_url = search_user_url;
	}
	public String getRegister_url() {
		return register_url;
	}
	public void setRegister_url(String register_url) {
		this.register_url = register_url;
	}
	public String getAppapi_url() {
		return appapi_url;
	}
	public void setAppapi_url(String appapi_url) {
		this.appapi_url = appapi_url;
	}
	public String getClient_id_value() {
		return client_id_value;
	}
	public void setClient_id_value(String client_id_value) {
		this.client_id_value = client_id_value;
	}
	public String getClient_secret_value() {
		return client_secret_value;
	}
	public void setClient_secret_value(String client_secret_value) {
		this.client_secret_value = client_secret_value;
	}
}
