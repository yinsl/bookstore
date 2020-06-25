/**        
 * KafkaProducerConfig.java Create on 2017年6月5日      
 * Copyright (c) 2017年6月5日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>      
 * @version 1.0  
 */
package com.maxus.tsp.gateway.mq.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.ProducerListener;

import com.maxus.tsp.gateway.config.KafkaProperties;

@EnableConfigurationProperties(KafkaProperties.class)
@Configuration
@EnableKafka
public class KafkaProducerConfig {

	@Autowired
	KafkaProperties kafkaProperties;
	
	//@Value("${kafka.broker.address}")
	//网关自身kafka地址
	private String brokerAddress;

	//@Value("${kafka.broker.itaddress}")
	//网关与rvm进行交互的kafka
	private String itbrokerAddress;

	// @Bean
	public ProducerFactory<String, String> producerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs(false));
	}

	// @Bean
	public ProducerFactory<String, String> producerFactoryit() {
		return new DefaultKafkaProducerFactory<>(producerConfigs(true));
	}

	// @Bean
	public Map<String, Object> producerConfigs(boolean isIt) {
		this.brokerAddress = kafkaProperties.getAddress();
		this.itbrokerAddress = kafkaProperties.getItaddress();
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, isIt ? itbrokerAddress : this.brokerAddress);
		props.put(ProducerConfig.RETRIES_CONFIG, 0);
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
		props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

		return props;
	}

	// @Bean
	public KafkaTemplate<String, String> kafkaTemplate() {
		KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<String, String>(producerFactory());
		ProducerListener<String, String> producerListener = new KafkaProducerListener<>();
		kafkaTemplate.setProducerListener(producerListener);
		return kafkaTemplate;
	}

	// @Bean
	public KafkaTemplate<String, String> kafkaTemplateIt() {
		KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<String, String>(producerFactoryit());
		ProducerListener<String, String> producerListener = new KafkaProducerListener<>();
		kafkaTemplate.setProducerListener(producerListener);
		return kafkaTemplate;
	}

	@Bean
	@Primary
	public KafkaProducer kafkaProducer() {
		KafkaProducer kafkaProducer = new KafkaProducer(kafkaTemplate());
		return kafkaProducer;
	}

	@Bean
	@Qualifier("kafkaItProducer")
	public KafkaItProducer kafkaItProducer() {
		KafkaItProducer kafkaItProducer = new KafkaItProducer(kafkaTemplateIt());
		return kafkaItProducer;
	}
}
