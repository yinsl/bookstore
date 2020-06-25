package com.maxus.tsp.gateway.common.constant;

/*
 * @Description: 国六B排放数据上报内容枚举
 * @Author: Wang Ya Jun
 * @Email: wangyajun01@saicmotor.com
 * @Date: 2019-04-02 15:20:10
 * @LastEditTime: 2019-06-10 16:34:31
 */
public enum GB6BEmission {

    SOFTWARE_VERSION("5001001", "终端软件版本号"), OBD_INFO_CNT("5001002", "OBD信息的个数"), OBD_INFO("5001003", "OBD信息"),
    DATA_STREAM_CNT("5001004", "数据流信息的个数"), DATA_STREAM_INFO("5001005", "数据流信息"),

    DIAG_PROTOCOL("5002001", "OBD诊断协议"), MIL_STATUS("5002002", "MIL状态"), DIAG_SUPPORT("5002003", "诊断支持状态"),
    DIAG_READY("5002004", "诊断就绪状态"), SOFTWARE_CAL_ID("5002005", "软件标定识别号"), CVN("5002006", "标定验证码"),
    IUPR("5002007", "IUPR值"), FAULT_CNT("5002008", "故障码总数"), FAULT_LIST("5002009", "故障码list"),

    VEHICLE_SPEED("5003001", "车速"), AIR_PRESSURE("5003002", "大气压力"), NET_TORQUE("5003003", "发动机净输出扭矩"),
    FRICTION_TORQUE("5003004", "摩擦扭矩"), ENGINE_SPEED("5003005", "发动机转速"), FUEL_FLOW_RATE("5003006", "发动机燃料流量"),
    NOX_UP("5003007", "SCR上游NOx传感器输出值"), NOX_DOWN("5003008", "SCR下游NOx传感器输出值"), REACTANT("5003009", "反应剂余量"),
    AIR_INPUT("5003010", "进气量"), TEMP_ENTRANCE("5003011", "SCR入口温度"), TEMP_EXIT("5003012", "SCR出口温度"),
    DPF("5003013", "DPF压差"), TEMP_COOLANT("5003014", "发动机冷却液温度"), FUEL_LEVEL("5003015", "油箱液位"),
    POS_STATUS("5003016", "定位信息"), LONGITUDE("5003017", "经度"), LATITUDE("5003018", "纬度"), ODO("5003019", "累计里程");

    private String code;
    private String desc;

    GB6BEmission(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
