package com.maxus.tsp.gateway.common.constant;

public enum OTAForwardMessagePartSize {
	COMMAND_SIZE(2), PARAM_SIZE_SIZE(2), SHOOT_ID_SIZE(16), CAMERA_NUM_SIZE(1), CAMERA_RESULT_SIZE(9), LONGITUDE_SIZE(
			4), LATITUDE_SIZE(4), DATE_TIME_SIZE(7),GPSTYPE_SIZE(1),POSTYPE_SIZE(1);
	private int code;

	OTAForwardMessagePartSize(int code) {
		this.code = code;
	}

	public int value() {
		return code;
	}
}
