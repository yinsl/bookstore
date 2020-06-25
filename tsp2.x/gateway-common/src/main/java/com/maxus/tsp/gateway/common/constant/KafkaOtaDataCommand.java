package com.maxus.tsp.gateway.common.constant;

/**
 * @ClassName KafkaOtaDataCommand
 * @Description Kafka topic:ota_data command
 * @Author zijhm
 * @Date 2019/1/14 14:19
 * @Version 1.0
 **/
public class KafkaOtaDataCommand {
    // 登陆传递
    public static final String KAFKA_PARAM_LOGIN = "LOGIN";
    // 登出传递
    public static final String KAFKA_PARAM_LOGOUT = "LOGOUT";
    // 国标登录传递
    public static final String KAFKA_PARAM_GBLOGIN = "GB_LOGIN";
    // 国标登出传递
    public static final String KAFKA_PARAM_GBLOGOUT = "GB_LOGOUT";
    // 新能源实时数据传递
    public static final String KAFKA_PARAM_REALTIME_DATA = "REALTIME_DATA";
    // 新能源实时数据补发传递
    public static final String KAFKA_PARAM_RE_REALTIME_DATA = "RE_REALTIME_DATA";
    // 车身故障报警
    public static final String KAFKA_PARAM_WARNING = "WARNING";
    // 车况上报
    public static final String KAFKA_PARAM_STATUS = "STATUS";
    // 仪表故障
    public static final String KAFKA_PARAM_FAULT = "FAULT";
    // 国六车辆国标登入
    public static final String KAFKA_PARAM_GB_LOGIN_EMSN = "GB_LOGIN_EMSN";
    // 国六车辆国标登出
    public static final String KAFKA_PARAM_GB_LOGINOUT_EMSN = "GB_LOGOUT_EMSN";
    // 国六实时数据上报
    public static final String KAFKA_PARAM_EMISSION_DATA = "EMISSION_DATA";
    // 国六补发实时数据上报
    public static final String KAFKA_PARAM_RE_EMISSION_DATA = "RE_EMISSION_DATA";
    // 危险行为预警
    public static final String KAFKA_PARAM_EARLY_WARNING = "EARLY_WARNING";
}
