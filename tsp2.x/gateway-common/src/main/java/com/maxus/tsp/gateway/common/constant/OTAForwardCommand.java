package com.maxus.tsp.gateway.common.constant;

import java.util.HashMap;
import java.util.Map;

public enum OTAForwardCommand {
    CMD_UP_TAKE_PHOTO((short) 0x0001, "拍照结果"),
    CMD_DOWN_TAKE_PHOTO((short) 0x8001, "拍照"),
    CMD_UP_POI((short) 0x0002, "下发结果"),
    CMD_DOWN_POI((short) 0x8002, "下发POI"),
    CMD_UP_TOKEN((short) 0x0003, "请求TOKEN"),
    CMD_DOWN_TOKEN((short) 0x8003, "下发TOKEN");

    private static Map<Short, OTAForwardCommand> codeMap = new HashMap<Short, OTAForwardCommand>();

    static {
        for (OTAForwardCommand item : OTAForwardCommand.values()) {
            codeMap.put(item.value(), item);
        }
    }

    private final short code;
    private final String message;

    public short getCode() {
        return code;
    }

    OTAForwardCommand(short code, String message) {
        this.code = code;
        this.message = message;
    }

    public static final OTAForwardCommand getByCode(short code) {

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
