package com.maxus.tsp.common.enums;

import java.util.HashMap;
import java.util.Map;


public enum GBEncrptMode {
	NONE((short) 0x01, "无加密"),
	RSA((short) 0x02, "RSA加密"),
	AES((short) 0x03, "AES128加密"),
	ABNORMAL((short) 0xFE, "异常加密"),
	INVALID((short) 0xFF, "无效加密");

	private static Map<Short, GBEncrptMode> codeMap = new HashMap<Short, GBEncrptMode>();

	static {
		for (GBEncrptMode item : GBEncrptMode.values()) {
			codeMap.put(item.value(), item);
		}
	}

	private final short code;
	private final String message;

	GBEncrptMode(short code, String message) {
		this.code = code;
		this.message = message;
	}

	public static final GBEncrptMode getByCode(short code) {

		if (codeMap.containsKey(code)) {
			return codeMap.get(code);
		}
		return null;
	}

	public short value() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}
