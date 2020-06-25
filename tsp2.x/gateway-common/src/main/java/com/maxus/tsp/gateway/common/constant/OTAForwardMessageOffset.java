package com.maxus.tsp.gateway.common.constant;

public enum OTAForwardMessageOffset {
	COMMAND_OFFSET(0), PARAM_SIZE_OFFSET(2), PARAM_OFFSET(4),
	SHOOT_ID_OFFSET(4), CAMERA_NUM_DOWN_OFFSET(20), CAMERA_LIST_OFFSET(21),
	CAMERA_NUM_UP_OFFSET(4), SHOOT_RESULT_OFFSET(5), LONGITUDE_OFFSET(4), LATITUDE_OFFSET(8), ADDRESS_OFFSET(12);
	private int code;

	OTAForwardMessageOffset(int code) {
		this.code = code;

	}

	public int value() {
		return code;
	}
}
