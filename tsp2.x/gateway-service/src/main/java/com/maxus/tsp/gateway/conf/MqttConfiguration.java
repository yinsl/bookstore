package com.maxus.tsp.gateway.conf;

import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.internal.security.SSLSocketFactoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.integration.mqtt.support.MqttMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.gateway.server.OTAResolveServerHandler;
import com.maxus.tsp.gateway.service.MqttService;

@Configuration
@EnableConfigurationProperties(MqttProperties.class)
public class MqttConfiguration {
	
	private static Logger  logger = LoggerFactory.getLogger(MqttConfiguration.class);

	@Autowired
	private MqttProperties mqttProperties;
	
	@Autowired
	private MqttService mqttService;
	
	@Autowired
	private OTAResolveServerHandler oTAResolveServerHandler;
	
	// ---------------------------------订阅--------------------------------------
	@Bean
	public MessageChannel mqttInputChannel() {
		return new DirectChannel();
	}
	
	@Bean
	public MqttMessageConverter mqttMessageConverter() {
		DefaultPahoMessageConverter converter = new DefaultPahoMessageConverter(1, false, "utf-8");
		converter.setPayloadAsBytes(true);
		return converter;
	}

	@Bean
	public MessageProducer inbound() throws Exception {
		
		MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
				getClientId(mqttProperties.getClientIdRecive()), clientFactory(),mqttProperties.getReciveTopics());
		//adapter.addTopic("$queue/test");
		adapter.setCompletionTimeout(mqttProperties.getCompletionTimeout());
		adapter.setConverter(mqttMessageConverter());
		adapter.setQos(1);
		adapter.setOutputChannel(mqttInputChannel());
		return adapter;
	}

	@Bean
	@ServiceActivator(inputChannel = "mqttInputChannel")
	public MessageHandler handler() {

		return new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message){
				
				mqttService.execute(()->{
					
						MessageHeaders headers = message.getHeaders();
						StringBuffer headerStr = new StringBuffer();
						headers.forEach((x, y) -> headerStr.append("[" + x + ":" + headers.get(y) + "] "));

						String topic = (String) headers.get("mqtt_receivedTopic") != null
								? (String) headers.get("mqtt_receivedTopic")
								: (String) headers.get("mqtt_topic");
						byte[] payload = (byte[]) message.getPayload();
						try {
						/*
						 * logger.info(
						 * "收到MQTT报文: header:: {}	\n报文信息: Payload's class type [{}] Message class type [{}]\nTopic: [{}],message payload length:[{}], message payload: [{}]"
						 * , headerStr.toString(), message.getPayload().getClass().getCanonicalName(),
						 * message.getClass().getCanonicalName(), topic, payload.length,
						 * ByteUtil.byteToHex(payload));
						 */
						if (topic.contains("TOPIC_SUB")) {
							String cmd = topic.substring(topic.lastIndexOf("/") + 1);

							switch (cmd) {
							case "stressRequest":
								mqttService.taskStart();
								// 空载压测topic 无需业务处理
								break;
							case "normal":
								mqttService.taskStart();
								oTAResolveServerHandler.resolve(message);
								// 正常业务处理
								break;
							case "addGroup":
								JSONObject group = JSON.parseObject(new String(payload));
								//logger.info("To add group: {}",group);
								mqttService.saveTaskGroup(group.getString("groupName"), group.getIntValue("groupNum"));
								break;
							case "addDetail":
								JSONObject detail = JSON.parseObject(new String(payload));
								//logger.info("To add detail: {}",detail);
								mqttService.addPubDetail(detail.getInteger("num"), detail.getLongValue("number"),
										detail.getString("ip"), detail.getLong("startTime"), detail.getLong("endTime"),
										detail.getLong("nanoCostAll"));
								break;
							case "cleanStatus":
								mqttService.cleanCountBean();
								break;
							}
						}else if ("willTopic".equals(topic)) {
							String msg = new String(payload);
							logger.info("\n<===============================\n"+msg +"\n===============================>");
							mqttService.addErrorMsg(msg);
						}else {
							mqttService.taskStart();
							oTAResolveServerHandler.resolve(message);
						}
					}catch (Exception e) {
						logger.error("解析mqtt报文异常",e);
						mqttService.addErrorMsg(e.getMessage());
					} finally {
						mqttService.taskEnd(topic);
					}
				});
			}
		};
	}

	@Bean
	public MqttPahoClientFactory clientFactory() throws Exception {
		DefaultMqttPahoClientFactory clientFactory = new DefaultMqttPahoClientFactory();

		MqttConnectOptions options = new MqttConnectOptions();
		options.setServerURIs(mqttProperties.getHosts());
		// 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
		options.setCleanSession(mqttProperties.isCleanSession());
		// 设置连接的用户名
		options.setUserName(mqttProperties.getUsername());
		// 设置连接的密码
		options.setPassword(mqttProperties.getPassword().toCharArray());
		// 设置超时时间 单位为秒
		options.setConnectionTimeout(mqttProperties.getConnectionTimeout());
		// 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
		options.setKeepAliveInterval(mqttProperties.getKeepalive());
		options.setMaxInflight(mqttProperties.getMaxInflight());
		options.setWill("willTopic", ("Subscribe offline."+mqttService.getHostIP()).getBytes(), 2, false);

		Properties sslProperties = new Properties();
		sslProperties.setProperty(SSLSocketFactoryFactory.SSLPROTOCOL, mqttProperties.getProtocol());
		sslProperties.setProperty(SSLSocketFactoryFactory.TRUSTSTORE, mqttProperties.getTrustStore());
		sslProperties.setProperty(SSLSocketFactoryFactory.TRUSTSTOREPWD, mqttProperties.getTrustStorePassword());
		sslProperties.setProperty(SSLSocketFactoryFactory.TRUSTSTORETYPE, mqttProperties.getTrustStoreType());
		sslProperties.setProperty(SSLSocketFactoryFactory.KEYSTORE, mqttProperties.getKeyStore());
		sslProperties.setProperty(SSLSocketFactoryFactory.KEYSTOREPWD, mqttProperties.getKeyStorePassword());
		sslProperties.setProperty(SSLSocketFactoryFactory.KEYSTORETYPE, mqttProperties.getKeyStoreType());
		options.setSSLProperties(sslProperties);
		
		clientFactory.setConnectionOptions(options);
		
		return clientFactory;
	}
	
	//-----------------------------------发布---------------------------------

	@Bean
	@ServiceActivator(inputChannel = "mqttOutboundChannel")
	public MessageHandler mqttOutbound() throws Exception {
		MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(
				getClientId(mqttProperties.getClientIdSend()), clientFactory());
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
		void sendToMqtt(byte[] payload, @Header(MqttHeaders.TOPIC) String topic, @Header(MqttHeaders.QOS) int qos);
	}
	
	private String getClientId(String clientId) {
		return clientId+"_"+ getHostIP()+ "_" + System.currentTimeMillis();
	}
	
	private String getHostIP() {
		return mqttService.getHostIP();
	}

}
