/**        
 * KafkaProducer.java Create on 2017年6月5日      
 * Copyright (c) 2017年6月5日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>      
 * @version 1.0  
 */
package com.maxus.tsp.gateway.mq.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.maxus.tsp.common.constant.KafkaMsgConstant;
import com.maxus.tsp.common.util.ThrowableUtil;

//@Service
//@Qualifier("KafkaProducer")
public class KafkaProducer {

	// @Autowired
	KafkaTemplate<String, String> kafkaTemplate;

	private Logger logger = LogManager.getLogger(KafkaProducer.class);

	public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;

	}

	public Map<String, Object> sndMesForTemplate(String topic, Object value, String ifPartition, Integer partitionNum,
			String role) {
		String key = role + "-" + value.hashCode();
		String valueString = JSON.toJSONString(value);
		if (ifPartition.equals("0")) {
			// 表示使用分区
			int partitionIndex = getPartitionIndex(key, partitionNum);
			ListenableFuture<SendResult<String, String>> result = kafkaTemplate.send(topic, partitionIndex, key,
					valueString);
			Map<String, Object> res = checkProRecord(result);
			return res;
		} else {
			ListenableFuture<SendResult<String, String>> result = kafkaTemplate.send(topic, key, valueString);
			Map<String, Object> res = checkProRecord(result);
			return res;
		}
	}

	public Map<String, Object> sndMesForTemplate(String topic, Object value, String role) {
		String key = role + "-" + value.hashCode();
		String valueString = JSON.toJSONString(value);

		ListenableFuture<SendResult<String, String>> result = kafkaTemplate.send(topic, key, valueString);
		Map<String, Object> res = checkProRecord(result);
		return res;

	}

	public Map<String, Object> sndMesForTemplate(String topic, Object value, String role, boolean isNullAble) {
		String key = role + "-" + value.hashCode();
		String valueString;
		if (isNullAble) {
			valueString = JSON.toJSONString(value, SerializerFeature.WriteMapNullValue);
		} else {
			valueString = JSON.toJSONString(value);
		}

		ListenableFuture<SendResult<String, String>> result = kafkaTemplate.send(topic, key, valueString);
		Map<String, Object> res = checkProRecord(result);
		return res;

	}

	private int getPartitionIndex(String key, int partitionNum) {
		if (key == null) {
			Random random = new Random();
			return random.nextInt(partitionNum);
		} else {
			int result = Math.abs(key.hashCode()) % partitionNum;
			return result;
		}
	}

	@SuppressWarnings("rawtypes")
	private Map<String, Object> checkProRecord(ListenableFuture<SendResult<String, String>> res) {
		Map<String, Object> m = new HashMap<String, Object>();
		if (res != null) {
			try {
				SendResult r = res.get();// 检查result结果集
				/* 检查recordMetadata的offset数据，不检查producerRecord */
				Long offsetIndex = r.getRecordMetadata().offset();
				if (offsetIndex != null && offsetIndex >= 0) {
					m.put("code", KafkaMsgConstant.SUCCESS_CODE);
					m.put("message", KafkaMsgConstant.SUCCESS_MES);
					return m;
				} else {
					m.put("code", KafkaMsgConstant.KAFKA_NO_OFFSET_CODE);
					m.put("message", KafkaMsgConstant.KAFKA_NO_OFFSET_MES);
					return m;
				}
			} catch (InterruptedException e) {
				// e.printStackTrace();
				logger.error(ThrowableUtil.getErrorInfoFromThrowable(e));
				m.put("code", KafkaMsgConstant.KAFKA_SEND_ERROR_CODE);
				m.put("message", KafkaMsgConstant.KAFKA_SEND_ERROR_MES);
				return m;
			} catch (ExecutionException e) {
				// e.printStackTrace();
				logger.error(ThrowableUtil.getErrorInfoFromThrowable(e));
				m.put("code", KafkaMsgConstant.KAFKA_SEND_ERROR_CODE);
				m.put("message", KafkaMsgConstant.KAFKA_SEND_ERROR_MES);
				return m;
			}
		} else {
			m.put("code", KafkaMsgConstant.KAFKA_NO_RESULT_CODE);
			m.put("message", KafkaMsgConstant.KAFKA_NO_RESULT_MES);
			return m;
		}
	}

}
