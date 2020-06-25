package com.maxus.tsp.gateway.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 网关使用的kafka配置
 * @author uwczo
 *
 */
@ConfigurationProperties(prefix = KafkaProperties.DS, ignoreUnknownFields = false)
public class KafkaProperties {
	public final static String DS="kafka.broker";
	//网关自身使用的kafka地址
	private String address;
	//网关与RVM交互的kafka地址
	private String itaddress;

	public int getAutoCommitIntervalMs() {
		return autoCommitIntervalMs;
	}

	public void setAutoCommitIntervalMs(int autoCommitIntervalMs) {
		this.autoCommitIntervalMs = autoCommitIntervalMs;
	}

	//自动提交的间隔时间
	private int autoCommitIntervalMs=500;
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getItaddress() {
		return itaddress;
	}
	public void setItaddress(String itadress) {
		this.itaddress = itadress;
	}

	
}
