package com.maxus.tsp.gateway.common.constant;

public enum OTADrtRepMessagePartSize {
	PORT_SIZE(2),DATE_TIME_SIZE(7);
	private int code;

	OTADrtRepMessagePartSize(int code) {
		this.code = code;
	}

	public int value() {
		return code;
	}
}
