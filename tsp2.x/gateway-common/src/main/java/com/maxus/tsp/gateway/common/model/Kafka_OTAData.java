package com.maxus.tsp.gateway.common.model;

import com.maxus.tsp.platform.service.model.vo.CodeValue;

public class Kafka_OTAData {

	String command;
	String sn;
	String vin;
//	String originalMessage;
	long collectTime;
	long gatewayTimeIn;
	long gatewayTimeOut;
	CodeValue[] items;
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	public String getVin() {
		return vin;
	}
	public void setVin(String vin) {
		this.vin = vin;
	}
/*	public String getOriginalMessage() {
		return originalMessage;
	}
	public void setOriginalMessage(String originalMessage) {
		this.originalMessage = originalMessage;
	}*/
	public long getCollectTime() {
		return collectTime;
	}
	public void setCollectTime(long collectTime) {
		this.collectTime = collectTime;
	}
	public CodeValue[] getItems() {
		return items;
	}
	public void setItems(CodeValue[] items) {
		this.items = items;
	}

	public long getGatewayTimeIn() {
		return gatewayTimeIn;
	}

	public void setGatewayTimeIn(long gatewayTimeIn) {
		this.gatewayTimeIn = gatewayTimeIn;
	}

	public long getGatewayTimeOut() {
		return gatewayTimeOut;
	}

	public void setGatewayTimeOut(long gatewayTimeOut) {
		this.gatewayTimeOut = gatewayTimeOut;
	}
}
