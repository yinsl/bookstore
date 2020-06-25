/*
package com.maxus.tsp.gateway.mq.kafka;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.KafkaMsgConstant;
import com.maxus.tsp.gateway.common.model.Kafka_BigData;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.Optional;


*/
/**
 * 需要传递it的kafka信息内部侦听 用于网关内部数据调整和再传递给it
 * 
 * @author uwczo
 *
 *//*

public class TransferCtrlInnerListener {
	private Logger logger = LogManager.getLogger(TransferCtrlInnerListener.class);
	
	*/
/**
	 * 测试双消费者，模拟接收it的kafka消费，仅测试时使用
	 * @param record
	 *//*

	@KafkaListener(topics = {KafkaMsgConstant.TOPIC_REGISTER_DATA, KafkaMsgConstant.TOPIC_OTA_DATA,
			KafkaMsgConstant.TOPIC_LOCATON_DATA, KafkaMsgConstant.TOPIC_IT_REMOTECTRL_RESPONSE,KafkaMsgConstant.TOPIC_IT_TAKEPHOTO_RESPONSE,KafkaMsgConstant.TOPIC_IT_REMOTEUPDATE_RESPONSE,KafkaMsgConstant.TOPIC_IT_DOWNLOAD_FILE_REQUEST,KafkaMsgConstant.TOPIC_IT_UPLOAD_FILE_REQUEST}, containerFactory = "KafkaContainer")
	public void listenOta(ConsumerRecord<?, ?> record) {
		Optional<?> kafkaMessage = Optional.ofNullable(record.value());
		if (kafkaMessage.isPresent()) {
			Object message = kafkaMessage.get();
			logger.info("=====网关模拟it kafka收到了："+message.toString());
		}
	}
	
	// 解析大数据投递的items
	@KafkaListener(topics=KafkaMsgConstant.TOPIC_BIG_DATA, containerFactory="KafkaContainer")
	public void recieveMsg(ConsumerRecord<?, ?> c) {
		Kafka_BigData data = JSONObject.parseObject(c.value().toString(), Kafka_BigData.class);
		byte[] v = data.getItems();
		for (byte b : v) {
			System.out.println(b);
		}
	}
	
	// 模拟it监听网关房车家居远控回复结果
	@KafkaListener(topics=KafkaMsgConstant.TOPIC_IT_HOME_CTRL_RESPONSE, containerFactory="KafkaContainer")
	public void recieveHomeCtrlMsg(ConsumerRecord<?, ?> record) {
		Optional<?> kafkaMessage = Optional.ofNullable(record.value());
		if (kafkaMessage.isPresent()) {
			Object message = kafkaMessage.get();
			logger.info("=====网关模拟it kafka收到了房车家居远控回复："+message.toString());
		}
	}

}
*/
