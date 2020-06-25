/**        
 * KafkaProducerListener.java Create on 2017年6月5日      
 * Copyright (c) 2017年6月5日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>      
 * @version 1.0  
 */
package com.maxus.tsp.gateway.mq.kafka;


import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.support.ProducerListener;

public class KafkaProducerListener<T,K> implements ProducerListener<String, String> {
	private static final Logger LOG = LogManager.getLogger(KafkaProducerListener.class);

	@Override
	public void onError(String topic, Integer partition, String key, String value, Exception exception) {
		LOG.error("==========kafka 发送数据错误（日志开始）==========");
		LOG.error("----------kafka 发送数据错误 topic:{}", topic);
		LOG.error("----------kafka 发送数据错误 partition:{}", partition);
		LOG.error("----------kafka 发送数据错误 key:{}", key);
		LOG.error("----------kafka 发送数据错误  value:{}", value);
		LOG.error("----------kafka 发送数据错误 Exception:{}", exception);
		LOG.error("~~~~~~~~~~kafka发送数据错误（日志结束）~~~~~~~~~~");
	}

	@Override
	public void onSuccess(String topic, Integer partition, String key, String value, RecordMetadata recordMetadata) {
		LOG.info("==========kafka发送数据成功（日志开始）==========");
		LOG.info("----------kafka 发送数据成功 topic:{}", topic);
		LOG.info("----------kafka 发送数据成功 partition:{}", partition);
		LOG.info("----------kafka 发送数据成功 key:{}", key);
		LOG.info("----------kafka 发送数据成功 value:{}", value);
		LOG.info("----------kafka 发送数据成功 RecordMetadata:{}", recordMetadata);
		LOG.info("~~~~~~~~~~kafka发送数据成功（日志结束）~~~~~~~~~~");

	}

}
