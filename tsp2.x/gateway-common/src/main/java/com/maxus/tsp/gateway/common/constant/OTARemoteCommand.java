package com.maxus.tsp.gateway.common.constant;

import java.util.HashMap;
import java.util.Map;

public enum OTARemoteCommand {
	Search((short) 0x0001, "远程寻车"),
	ControlDoor((short) 0x0002, "远程开门"),
	VehicleStart((short) 0x0003, "远程车辆启动"),
	AirConditioning((short) 0x0004, "远程空调"),
	TemperatureSetting((short) 0x0005, "远程空调温度设置"),
	SeatheatingFrontRight((short) 0x0006, "远程座椅加热（右前）"),
	SeatheatingFrontLeft((short) 0x0007, "远程座椅加热（左前"),
	SeatheatingRearRight((short) 0x0008, "远程座椅加热（右后）"),
	SeatheatingRearLeft((short) 0x0009, "远程座椅加热（左后）")/*,
	RemoteCaptrue((short) 0x8003, "远程抓拍")*/,
	LimitSpeed((short) 0x000A, "远程车辆跛行");

	private static Map<Short, OTARemoteCommand> codeMap = new HashMap<Short, OTARemoteCommand>();

	static {
		for (OTARemoteCommand item : OTARemoteCommand.values()) {
			codeMap.put(item.value(), item);
		}
	}

	private final short code;
	private final String message;

	OTARemoteCommand(short code, String message) {
		this.code = code;
		this.message = message;
	}

	public static final OTARemoteCommand getByCode(short code) {

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
