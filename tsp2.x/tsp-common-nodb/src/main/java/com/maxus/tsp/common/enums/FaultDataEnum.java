package com.maxus.tsp.common.enums;

import java.util.HashMap;
import java.util.Map;


public enum FaultDataEnum {

    ESP_FAULT("1", "稳定控制系统故障(ESP)", "2003001"),
    ABS_FAULT("2", "ABS制动防抱死系统故障", "2003002"),
    EMISSION_FAULT("3", "排放故障", "2003003"),
    ENGINE_FAULT("4", "发动机故障", "2003004"),
    SAFETY_AIR_BAG_FAULT("5", "安全气囊故障", "2003005"),
    EBD_FAULT("6", "EBD故障", "2003006"),
    EPB_FAULT("7", "EPB故障", "2003007"),
    BRAKE_FLUID_LEVEL_FAULT("8", "制动液液位信号故障", "2003008"),
    CELL_INSULATION_FAULT("9", "绝缘警告", "2003009"),
    LOW_SOC_FAULT("10", "SOC低报警", "2003010"),
    POWER_BATTERY_CHARGING_FAULT("11", "动力电池充电", "2003011"),
    TPMS_FAULT("12", "TPMS故障", "2003012"),
    POWER_BATTERY_FIRE_ALARM_FAULT("13", "动力电池火源报警", "2003013"),
    MOTOR_SPEEDING_FAULT("14", "电机超速报警", "2003014"),
    MOTOR_OVERHEATED_FAULT("15", "电机过热报警", "2003015"),
    INTERLOCK_FAULT("16", "互锁报警", "2003016"),
    POWER_SYSTEM_FAULT("17", "动力系统故障", "2003017"),
    LOW_POWER_BATTERY_TEMPERATURE_TO_CHARGE_FAULT("18", "动力电池温度过低无法充电报警", "2003018"),
    VACUUM_PUMP_FAULT("19", "真空泵故障报警", "2003019"),
    POWER_STEERING_FAULT("20", "助力转向报警", "2003020"),
    POWER_BATTERY_CUT_OFF_FAULT("21", "动力电池切断报警", "2003021"),
    POWER_BATTERY_FAULT("22", "动力电池故障", "2003022"),
    LIMITED_POWER_FAULT("23", "限功率报警", "2003023"),
    BRAKE_SYSTEM_FAULT("24", "制动系统故障", "2003024");

    private String number;
    private String value;
    private String code;

    FaultDataEnum(String number, String value, String code) {
        this.number = number;
        this.value = value;
        this.code = code;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    private static Map<String, FaultDataEnum> codeMap = new HashMap<String, FaultDataEnum>();

    static {
        for (FaultDataEnum faultDataEnum : FaultDataEnum.values()) {
            codeMap.put(faultDataEnum.getValue(), faultDataEnum);
        }
    }

    public static boolean contains(String value) {
        if (codeMap.containsKey(value)) {
            return true;
        } else {
            return false;
        }
    }

}
