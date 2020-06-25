package com.maxus.tsp.gateway.mq.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import com.maxus.tsp.gateway.config.KafkaProperties;


@Configuration
@EnableKafka
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaItConsumerConfig {

	//private static final Logger logger = LogManager.getLogger(KafkaItConsumerConfig.class);
	
	@Autowired
	KafkaProperties kafkaProperties;
	
	//@Value("${kafka.broker.itaddress}")
	private String brokerItAddress;
	@Value("${kafkaconsumer.groupid.ittest:rvmKafka}")
	private String itBrokerGroupId;

	@Bean("KafkaItContainer")
	KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerItFactory());
		//factory.setConcurrency(3);
		factory.getContainerProperties().setPollTimeout(3000);
		return factory;
	}
	
	public ConsumerFactory<String, String> consumerItFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerItConfigs());
	}

	
	public Map<String, Object> consumerItConfigs() {
		Map<String, Object> propsMap = new HashMap<>();
		this.brokerItAddress = kafkaProperties.getItaddress();
		propsMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.brokerItAddress);
		propsMap.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, kafkaProperties.getAutoCommitIntervalMs());
		propsMap.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
		propsMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		propsMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
	    propsMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        //设定同一个group id，当网关多节点水平扩展时，只需要一个节点侦听到kafka请求
		propsMap.put(ConsumerConfig.GROUP_ID_CONFIG, itBrokerGroupId);
		propsMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		return propsMap;
	}

}
