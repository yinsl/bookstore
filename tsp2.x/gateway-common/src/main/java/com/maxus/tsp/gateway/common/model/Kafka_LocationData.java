package com.maxus.tsp.gateway.common.model;

import java.util.List;

/**
 * 用于向it传递车辆位置信息的kafka结构
 * @author uwczo
 *
 */
public class Kafka_LocationData {
 String sn;
 String originalMessage;
 List<Kafka_GPSData> items;

public String getSn() {
	return sn;
}
public void setSn(String sn) {
	this.sn = sn;
}
public String getOriginalMessage() {
	return originalMessage;
}
public void setOriginalMessage(String originalMessage) {
	this.originalMessage = originalMessage;
}
public List<Kafka_GPSData> getItems() {
	return items;
}
public void setItems(List<Kafka_GPSData> items) {
	this.items = items;
}
 
}
