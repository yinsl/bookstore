package com.maxus.tsp.common.util.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "liantong.sms")
public class LiantongSMSProperties {
	
	private String namespaceUrl;
	
	private String prefix;
	
	private String soapAction;
	
	private String smsUrl;
	
	private String licenseKey;
	
	private String username;

	private String password;
	
	public String getNamespaceUrl() {
		return namespaceUrl;
	}

	public void setNamespaceUrl(String namespaceUrl) {
		this.namespaceUrl = namespaceUrl;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSoapAction() {
		return soapAction;
	}

	public void setSoapAction(String soapAction) {
		this.soapAction = soapAction;
	}

	public String getSmsUrl() {
		return smsUrl;
	}

	public void setSmsUrl(String smsUrl) {
		this.smsUrl = smsUrl;
	}

	public String getLicenseKey() {
		return licenseKey;
	}

	public void setLicenseKey(String licenseKey) {
		this.licenseKey = licenseKey;
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
