/**
 * KafkaProducer.java Create on 2017年6月5日
 * Copyright (c) 2017年6月5日 by 上汽集团商用车技术中心
 *
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>
 * @version 1.0
 */
package com.maxus.tsp.gateway.mq.kafka;

import org.springframework.kafka.core.KafkaTemplate;

public class KafkaItProducer extends KafkaProducer {

    /**
     * @param kafkaTemplate
     */
    public KafkaItProducer(KafkaTemplate<String, String> kafkaTemplate) {
        super(kafkaTemplate);

    }

}
