package com.maxus.tsp.stress.conf;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.internal.security.SSLSocketFactoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.Header;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.stress.service.MqttService;


@Configuration
@EnableConfigurationProperties(MqttProperties.class)
public class MqttConfiguration {

	private static Logger logger = LogManager.getLogger(MqttConfiguration.class);

	@Autowired
	private MqttProperties mqttProperties;
	@Autowired
	MqttService mqttService;
	
	@Bean
	public MqttConnectOptions getMqttConnectOptions() {
		MqttConnectOptions options = new MqttConnectOptions();

		Properties sslClientProps = new Properties();
		sslClientProps.setProperty(SSLSocketFactoryFactory.SSLPROTOCOL, mqttProperties.getProtocol());
		sslClientProps.setProperty(SSLSocketFactoryFactory.KEYSTORE, mqttProperties.getKeyStore());
		sslClientProps.setProperty(SSLSocketFactoryFactory.KEYSTOREPWD, mqttProperties.getKeyStorePassword());
		sslClientProps.setProperty(SSLSocketFactoryFactory.KEYSTORETYPE, mqttProperties.getKeyStoreType());
		sslClientProps.setProperty(SSLSocketFactoryFactory.TRUSTSTORE, mqttProperties.getTrustStore());
		sslClientProps.setProperty(SSLSocketFactoryFactory.TRUSTSTOREPWD, mqttProperties.getTrustStorePassword());
		sslClientProps.setProperty(SSLSocketFactoryFactory.TRUSTSTORETYPE, mqttProperties.getTrustStoreType());
		options.setSSLProperties(sslClientProps);
		options.setServerURIs(mqttProperties.getHosts());
		options.setCleanSession(mqttProperties.isCleanSession());
		options.setUserName(mqttProperties.getUsername());
		options.setPassword(mqttProperties.getPassword().toCharArray());
		options.setConnectionTimeout(mqttProperties.getConnectionTimeout());
		options.setMaxInflight(mqttProperties.getMaxInflight());
		options.setKeepAliveInterval(mqttProperties.getKeepalive());
		options.setWill("willTopic", ("Publish offline."+mqttService.getHostIP()).getBytes(), 2, false);

		return options;
	}

	@Bean
	public MqttPahoClientFactory clientFactory() throws Exception {
		DefaultMqttPahoClientFactory clientFactory = new DefaultMqttPahoClientFactory();
		clientFactory.setConnectionOptions(getMqttConnectOptions());
		return clientFactory;
	}

	// ---------------------------------订阅--------------------------------------
	@Bean
	public MessageChannel mqttInputChannel() {
		return new DirectChannel();
	}

	@Bean
	public MessageProducer inbound() throws Exception {
		MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
				getClientId(mqttProperties.getClientIdRecive()), clientFactory(), mqttProperties.getReciveTopics());
		adapter.setCompletionTimeout(mqttProperties.getCompletionTimeout());
		adapter.setConverter(new DefaultPahoMessageConverter());
		adapter.setQos(1);
		adapter.setOutputChannel(mqttInputChannel());
		return adapter;
	}

	@Bean
	@ServiceActivator(inputChannel = "mqttInputChannel")
	public MessageHandler handler() {
		return new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				MessageHeaders headers = message.getHeaders();
				StringBuffer headerStr = new StringBuffer();
				headers.forEach((x, y) -> headerStr.append("[" + x + " : " + headers.get(y) + "] "));

				String topic = (String) headers.get("mqtt_receivedTopic");
				String payload = (String) message.getPayload();
				/*
				 * logger.info(
				 * "收到 topic [{}] 信息 , payload :{} Payload's class type [{}] Message class type [{}]"
				 * ,
				 * topic,message.getPayload(),message.getPayload().getClass().getCanonicalName()
				 * , message.getClass().getCanonicalName());
				 */
				if (topic.contains("TOPIC_PUB")) {

					String cmd = topic.substring(topic.lastIndexOf("/") + 1);
					switch (cmd) {
					case "stressStart":
						JSONObject params = JSON.parseObject((String) message.getPayload());
						mqttService.stressTest(params.getString("topic"), params.getString("msg"),
								params.getInteger("start"), params.getInteger("end"), params.getInteger("corePoolSize"),
								params.getInteger("maxPoolSize"), params.getBoolean("isClearStatus"),
								Boolean.FALSE.equals(params.getBoolean("isShare")));
						break;
					}

				} else if ("willTopic".equals(topic)) {
					String msg = new String(payload);
					logger.info("\n<===============================\n" + msg + "\n===============================>");
					mqttService.addErrorMsg(msg);
				} else {
					mqttService.countBean.received.incrementAndGet();
				}
			}
		};
	}

	// ----------------------发布------------------------------------
	@Bean
	@ServiceActivator(inputChannel = "mqttOutboundChannel")
	public MessageHandler mqttOutbound() throws Exception {

		MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(getClientId(mqttProperties.getClientIdSend()), clientFactory());
		messageHandler.setAsync(mqttProperties.isAsync());

		messageHandler.setDefaultTopic(mqttProperties.getDefaultTopic());
		return messageHandler;
	}

	@Bean
	public MessageChannel mqttOutboundChannel() {
		return new DirectChannel();
	}

	@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
	public interface MyGateway {
		void sendToMqtt(byte[] payload, @Header(name = MqttHeaders.TOPIC) String topic, @Header(MqttHeaders.QOS) int qos);
	}

	private String getClientId(String clientId) {
		return clientId+"_"+ mqttService.getHostIP() +"_"+ System.currentTimeMillis();

	}

}
