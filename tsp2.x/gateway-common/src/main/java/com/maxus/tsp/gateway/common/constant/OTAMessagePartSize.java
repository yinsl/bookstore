/**
 * OTAMessagePartSize.java Create on 2017年7月12日
 * Copyright (c) 2017年7月12日 by 上汽集团商用车技术中心
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>
 * @version 1.0
 */
package com.maxus.tsp.gateway.common.constant;

/**
 * @ClassName: OTAMessagePartSize.java
 * @Description:
 * @author 任怀宇
 * @version V1.0
 * @Date 2017年7月12日 下午3:08:20
 */
public enum OTAMessagePartSize {
	OTA_HEADER_SIZE(21), BEGIN_SIGN_SIZE(2), MESSAGE_SIZE_SIZE(2), SERIAL_NUMBER_SIZE(16), ENCYPT_MODE_SIZE(
			1), CMD_SIZE(2), SEQ_NO_SIZE(4), PARAM_LENGTH_SIZE(2), CRC_SIZE(1);
	private int code;

	OTAMessagePartSize(int code) {
		this.code = code;
	}

	public int value() {
		return code;
	}
}
