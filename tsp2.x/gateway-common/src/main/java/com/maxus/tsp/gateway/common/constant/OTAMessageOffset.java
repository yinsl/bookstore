/**
 * OTAMessageOffset.java Create on 2017年7月12日
 * Copyright (c) 2017年7月12日 by 上汽集团商用车技术中心
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>
 * @version 1.0
 */
package com.maxus.tsp.gateway.common.constant;

/**
 * @ClassName: OTAMessageOffset.java
 * @Description:OTA协议参数的偏移
 * @author 任怀宇
 * @version V1.0
 * @Date 2017年7月12日 下午3:07:56
 */
public enum OTAMessageOffset {

	BEGIN_SIGN_OFFSET(0), MESSAGE_SIZE_OFFSET(2), SERIAL_NUMBER_OFFSET(4), ENCRYPT_MODE_OFFSET(
			20), ENCRYPT_FIELD_OFFSET(
					21), CMD_OFFSET(21), SEQ_NO_OFFSET(23), PARAM_LENGTH_OFFSET(27), PARAM_CONTENT_OFFSET(29);
	private int code;

	OTAMessageOffset(int code) {
		this.code = code;

	}

	public int value() {
		return code;
	}
}
