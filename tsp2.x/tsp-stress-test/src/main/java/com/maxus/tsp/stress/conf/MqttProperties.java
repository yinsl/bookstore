package com.maxus.tsp.stress.conf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "com.mqtt")
public class MqttProperties {
	
	private static Logger logger = LogManager.getLogger(MqttProperties.class);

	private boolean async;

	private boolean cleanSession;

	private String clientIdRecive;

	private String clientIdSend;

	private int completionTimeout;

	private int connectionTimeout;

	private String defaultTopic;

	private String[] hosts;

	private int keepalive;

	private String keyPass;

	private String keyStore;

	private String keyStorePassword;

	private String keyStoreType;
	
	private int maxInflight;

	private String password;

	private String protocol;

	private String[] reciveTopics;

	private String[] sendTopics;

	private String trustStore;

	private String trustStorePassword;

	private String trustStoreType;

	private String username;
	
	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public boolean isCleanSession() {
		return cleanSession;
	}

	public void setCleanSession(boolean cleanSession) {
		this.cleanSession = cleanSession;
	}

	public String getClientIdRecive() {
		return clientIdRecive;
	}

	public void setClientIdRecive(String clientIdRecive) {
		this.clientIdRecive = clientIdRecive;
	}

	public String getClientIdSend() {
		return clientIdSend;
	}

	public void setClientIdSend(String clientIdSend) {
		this.clientIdSend = clientIdSend;
	}

	public int getCompletionTimeout() {
		return completionTimeout;
	}

	public void setCompletionTimeout(int completionTimeout) {
		this.completionTimeout = completionTimeout;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public String getDefaultTopic() {
		return defaultTopic;
	}

	public void setDefaultTopic(String defaultTopic) {
		this.defaultTopic = defaultTopic;
	}

	public String[] getHosts() {
		return hosts;
	}

	public void setHosts(String[] hosts) {
		this.hosts = hosts;
	}

	public int getKeepalive() {
		return keepalive;
	}

	public void setKeepalive(int keepalive) {
		this.keepalive = keepalive;
	}

	public String getKeyPass() {
		return keyPass;
	}

	public void setKeyPass(String keyPass) {
		this.keyPass = keyPass;
	}

	public String getKeyStore() {
		return keyStore;
	}

	public void setKeyStore(String keyStore) {
		this.keyStore = keyStore;
	}

	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	public String getKeyStoreType() {
		return keyStoreType;
	}

	public void setKeyStoreType(String keyStoreType) {
		this.keyStoreType = keyStoreType;
	}

	public int getMaxInflight() {
		return maxInflight;
	}

	public void setMaxInflight(int maxInflight) {
		this.maxInflight = maxInflight;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String[] getReciveTopics() {
		return reciveTopics;
	}

	public void setReciveTopics(String[] reciveTopics) {
		this.reciveTopics = reciveTopics;
	}

	public String[] getSendTopics() {
		return sendTopics;
	}

	public void setSendTopics(String[] sendTopics) {
		this.sendTopics = sendTopics;
	}

	public String getTrustStore() {
		return trustStore;
	}

	public void setTrustStore(String trustStore) {
		this.trustStore = trustStore;
	}

	public String getTrustStorePassword() {
		return trustStorePassword;
	}

	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}

	public String getTrustStoreType() {
		return trustStoreType;
	}

	public void setTrustStoreType(String trustStoreType) {
		this.trustStoreType = trustStoreType;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
