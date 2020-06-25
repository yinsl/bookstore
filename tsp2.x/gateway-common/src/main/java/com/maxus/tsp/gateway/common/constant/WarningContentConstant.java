package com.maxus.tsp.gateway.common.constant;

import java.util.HashMap;
import java.util.Map;
/**
 * 报警消息内容枚举
 * @author uwczo
 *
 */
public enum WarningContentConstant {

	DRIVER_DOOR_OPEN((short) 0x0001, "驾驶座车门打开，请立即检视确保车辆周围无异常发生。若不幸被盗，您可立即定位车辆后并及时联系警方进行协寻。"),
	CO_DRIVER_DOOR_OPEN((short) 0x0002, "副驾驶座车门打开，请立即检视确保车辆周围无异常发生。若不幸被盗，您可立即定位车辆后并及时联系警方进行协寻。"),
	RIGHT_REAR_DOOR_OPEN((short) 0x0003, "右后座车门打开，请立即检视确保车辆周围无异常发生。若不幸被盗，您可立即定位车辆后并及时联系警方进行协寻。"),
	LEFT_REAR_DOOR_OPEN((short) 0x0004, "左后座车门打开，请立即检视确保车辆周围无异常发生。若不幸被盗，您可立即定位车辆后并及时联系警方进行协寻。"),
	TAILGATE_OPEN((short) 0x0005, "尾门打开，请立即检视确保车辆周围无异常发生。若不幸被盗，您可立即定位车辆后并及时联系警方进行协寻。"),
	INVALID_MOVEMENT((short) 0x0006, "车辆非法移动，请立即检视确保车辆周围无异常发生。若不幸被盗，您可立即定位车辆后并及时联系警方进行协寻。"),
	OPENING_HOOD_ILLEGALLY((short) 0x0007, "引擎盖非法打开，请立即检视确保车辆周围无异常发生。若不幸被盗，您可立即定位车辆后并及时联系警方进行协寻。"),
	DISASSEMBLING_TIRES_ILLEGALLY((short) 0x0008, "轮胎非法拆卸，请立即检视确保车辆周围无异常发生。若不幸被盗，您可立即定位车辆后并及时联系警方进行协寻。"),
	VEHICLE_FRONTSIDE_COLLISION((short) 0x0009, "前部撞击，请立即查看！"),
	VEHICLE_BACKSIDE_COLLISION((short) 0x000A, "后部撞击，请立即查看！"),
	VEHICLE_LEFTSIDE_COLLISION((short) 0x000B, "左侧撞击，请立即查看！"),
	VEHICLE_RIGHTSIDE_COLLISION((short) 0x000C, "右侧撞击，请立即查看！"),
	FATIGUE_DRIVING((short) 0x000D, "右侧撞击，请立即查看！"),
	CAN_DATA_INTERRUPTION((short) 0x000E, "CAN数据中断，请立即查看！"),
	ESP_MULFUNCTION((short) 0x000F, "有稳定控制系统故障，请立即查看！"),
	ABS_MULFUNCTION((short) 0x0010, "有ABS制动防抱死系统故障，请立即查看！"),
	EMISSION_MULFUNCTION((short) 0x0011, "有排放故障，请立即查看！"),
	ENGINE_MULFUNCTION((short) 0x0012, "有发动机故障，请立即查看！"),
	AIRBAG_MULFUNCTION((short) 0x0013, "有安全气囊故障，请立即查看！"),
	GETTING_GPSPOS_FAILURE((short) 0x0014, "GPS未定位，请立即查看！"),
	DRIVING_LEFTLANE_DEPARTURE((short) 0x0015, "车道向左偏离，请立即查看！"),
	DRIVING_RIGHTLANE_DEPARTURE((short) 0x0016, "车道向右偏离，请立即查看！"),
	EBD_MULFUNCTION((short) 0x0017, "有EBD故障，请立即查看！"),;

	private static Map<Short, WarningContentConstant> codeMap = new HashMap<Short, WarningContentConstant>();

	static {
		for (WarningContentConstant item : WarningContentConstant.values()) {
			codeMap.put(item.value(), item);
		}
	}

	private final short code;
	private final String message;

	WarningContentConstant(short code, String message) {
		this.code = code;
		this.message = message;
	}

	public static final WarningContentConstant getByCode(short code) {

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
