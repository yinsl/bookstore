package com.maxus.tsp.gateway.common.constant;

import java.util.HashMap;
import java.util.Map;
/**
 * @ClassName:     OTAEncrptMode.java
 * @Description:   加密方式
 * @author         任怀宇
 * @version        V1.0
 * @Date           2017年7月20日 上午10:42:01
 */
public enum OTAEncrptMode {
	RSA((short) 0x01, "RSA双层加密"), AES((short) 0x0002, "AES加密"), RC4((short) 0x03,
			"RC4加密"), PKI((short) 0x04, "PKI/CA加密"), NONE((short) 0x00, "无加密");

	private static Map<Short, OTAEncrptMode> codeMap = new HashMap<>();

	static {
		for (OTAEncrptMode item : OTAEncrptMode.values()) {
			codeMap.put(item.value(), item);
		}
	}

	private final short code;
	private final String message;

	OTAEncrptMode(short code, String message) {
		this.code = code;
		this.message = message;
	}

	public static final OTAEncrptMode getByCode(short code) {

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
