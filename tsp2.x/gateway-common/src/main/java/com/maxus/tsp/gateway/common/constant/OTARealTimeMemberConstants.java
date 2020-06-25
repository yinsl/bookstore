package com.maxus.tsp.gateway.common.constant;

public class OTARealTimeMemberConstants {

	//实时上报数据类型偏移
	public static final int REALTIMEDATA_MSG_TYPE_OFFSET = 0;
	//实时上报数据类型偏移
	public static final int REALTIMEDATA_MSG_DATETIME_OFFSET = 1;
	//实时上报数据类型偏移
	public static final int REALTIMEDATA_TYPE_AND_DATETIME_LENGTH = 8;
	//实时上报数据为实时发送
	public static final byte REALTIMEDATA_IS_REAL_TIME = 0x01;
	//实时上报数据为补发
	public static final byte REALTIMEDATA_ISNOT_REAL_TIME = 0x00;
	//实时上报数据子报文存在
	public static final byte REALTIME_SUBDATA_EXIST = 0x01;
	//实时上报数据中，有的子报文，如果有，最多只有1条
	public static final int REALTIMEDATA_FIXED_SUBMSG_MAX_SIZE = 1;
	//单条整车数据长度
	public static final int REALTIMEDATA_SINGLE_VEHICLE_SIZE = 20;
	//单条驱动电机数据长度
	public static final int REALTIMEDATA_SINGLE_MOTOR_SIZE = 12;
	//单条燃料电池数据中固定部分的长度
	public static final int REALTIMEDATA_SINGLE_FUEL_CELL_FIXPART_SIZE = 18;
	//燃料电池温度探针总数偏移位置
	public static final int REALTIMEDATA_SINGLE_FUEL_CELL_TEMP_PROBE_CNT_OFFSET = 6;
	//燃料电池温度探针总数偏移位置
	public static final int REALTIMEDATA_SINGLE_FUEL_CELL_TEMP_PROBE_CNT_SIZE = 2;
	//单条发动机数据长度
	public static final int REALTIMEDATA_SINGLE_ENGINE_SIZE = 5;
	//单条位置数据长度
	public static final int REALTIMEDATA_SINGLE_GPS_SIZE = 9;
	//单条报警数据中固定部分的长度
	public static final int REALTIMEDATA_SINGLE_ALARM_FIXPART_SIZE = 5;
	//单条报警数据中错误码分类数目
	public static final int REALTIMEDATA_SINGLE_ALARM_FAULT_TYPE_CNT = 4;
	//单条报警数据中错误码DTC CODE字节数
	public static final int REALTIMEDATA_SINGLE_ALARM_FAULT_CODE_SIZE = 4;
	//单条报警数据中错误码的开始偏移位置
	public static final int REALTIMEDATA_SINGLE_ALARM_FAULT_CNT_OFFSET = 5;
	//报警故障中的错误码计数的字节数
	public static final int REALTIMEDATA_SINGLE_ALARM_FAULT_CNT_SIZE = 1;
	//单条可充电储能电压数据子报文固定长度
	public static final int REALTIMEDATA_SINGLE_VOLTAGE_FIXPART_SIZE = 5;
	//可充电储能电池个数在子报文中所占字节数
	public static final int REALTIMEDATA_SINGLE_VOLTAGE_CELL_CNT_SIZE = 2;
	//可充电储能电池个数在子报文中所占字节数
	public static final int REALTIMEDATA_SINGLE_VOLTAGE_CELL_VOLTAGE_SIZE = 2;
	//单条可充电储能温度数据子报文固定长度
	public static final int REALTIMEDATA_SINGLE_TEMP_FIXPART_SIZE = 1;
	//可充电储能温度探针个数在子报文中所占字节数
	public static final int REALTIMEDATA_SINGLE_TEMP_TEMP_CNT_SIZE = 2;
}
