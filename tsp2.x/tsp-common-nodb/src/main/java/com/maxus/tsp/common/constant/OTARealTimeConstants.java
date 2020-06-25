package com.maxus.tsp.common.constant;

public class OTARealTimeConstants {

	//invalid code
	public static final byte INVALID_BYTE_VALUE = (byte)0xFF;
	public static final int INVALID_BYTE_STRING = 255;
	public static final int INVALID_BYTE_ERROR = 254;
	public static final int INVALID_ZERO_VALUE = 0;
	public static final int INVALID_2BYTE_VALUE = 65535;
	public static final long INVALID_UINT16_VALUE = 65535L;
	public static final long INVALID_UINT16_ERROR = 65534L;
	public static final long INVALID_UINT16_STRING = INVALID_UINT16_VALUE;
	public static final long INVALID_UINT32_VALUE = 4294967295L;
	public static final long INVALID_UINT32_ERROR = 4294967294L;
	public static final long INVALID_UINT32_STRING = INVALID_UINT32_VALUE;
	public static final String INVALID_JSNINFO= "";
	
	public static final int UINT8_OFFSET = 1;
	public static final int UINT16_OFFSET = 2;
	public static final int UINT32_OFFSET = 4;
	//dispatch_data code

	//battery info
	public static final String BATTERY_NUM = "1000004";
	public static final String BATTERY_CODE = "1000005";
	
	//VEHICLE
	public static final String VEHICLE_STATUS = "1001001";
	public static final byte VEHICLE_STATUS_STARTED = (byte)0x01;
	public static final byte VEHICLE_STATUS_STOPPED = (byte)0x02;
	public static final byte VEHICLE_STATUS_OTHER = (byte)0x03;
	public static final byte VEHICLE_STATUS_INVALID = (byte)0xFF;
	
	public static final String CHARGE_STATUS="1001002";
	public static final byte CHARGE_STATUS_CHARGING_STOPPED = (byte)0x01;
	public static final byte CHARGE_STATUS_CHARGING_DRIVING = (byte)0x02;
	public static final byte CHARGE_STATUS_NO_CHARGING = (byte)0x03;
	public static final byte CHARGE_STATUS_CHARGING_FINISH = (byte)0x04;
	public static final byte CHARGE_STATUS_CHARGING_INVALID = (byte)0xFF;
	
	public static final String POWER_TYPE="1001003";
	public static final byte RUNNING_MODEL_EV = (byte)0x01;
	public static final byte RUNNING_MODEL_PHEV = (byte)0x02;
	public static final byte RUNNING_MODEL_FV = (byte)0x03;
//	public static final byte RUNNING_MODEL_INVALID = (byte)0xFF;
	
	public static final String VEHICLE_SPEED="1001004";
	public static final String ODO="1001005";
	public static final String TOTAL_CURRENT="1001007";
	public static final String TOTAL_VOLTAGE="1001006";
	public static final String SOC="1001008";
	public static final String DCDC_STATUS="1001009";
	public static final byte DCDC_STATUS_NORMAL = (byte)0x01;
	public static final byte DCDC_STATUS_OFF = (byte)0x02;
	public static final byte DCDC_STATUS_INVALID = (byte)0xFF;
	
	public static final String GEAR_POS="1001010";

	
	public static final String INSULATE_RESIST="1001011";
	public static final String ACC_PEDAL="1001012";
	public static final String BRAKE_PEDAL="1001013";
	
	//MOTOR
	public static final String VOLTAGE_DATA_LIST = "1000001";
	public static final String TEMP_DATA_LIST = "1000002";
	
	//MOTOR
	public static final String MOTOR_ID = "1002001";
	public static final String MOTOR_STATUS = "1002002";
	public static final byte MOTOR_STATUS_CONSUMING_POWER =(byte)0x01;
	public static final byte MOTOR_STATUS_GENERATING_POWER =(byte)0x02;
	public static final byte MOTOR_STATUS_CLOSED =(byte)0x03;
	public static final byte MOTOR_STATUS_PREPARING =(byte)0x04;
	
	public static final String MOTOR_CONTROLLER_TEMP = "1002003";
	public static final String RPM = "1002004";
	public static final String MOTOR_TORQUE = "1002005";
	public static final String MOTOR_TEMP = "1002006";
	public static final String CONTROLLER_IN_VOLT = "1002007";
	public static final String CONTROLLER_DC_BUSCURRENT = "1002008";
	
	//RealTimeDataFuelCell
	public static final String FUEL_CELL_VOLT = "1003001";
	public static final String FUEL_CELL_CURRENT = "1003002";
	public static final String FUEL_CONSUMEPERCENT = "1003003";
	public static final String TEMP_PROBE_CNT = "1003004";
	public static final String PROB_TEMP_LIST = "1003005";
	public static final String HYDROGEN_MAX_TEMP = "1003006";
	public static final String HYDROGEN_MAX_TEMP_PROBE = "1003007";
	public static final String HYDROGEN_MAX_CONCENTRATE = "1003008";
	public static final String HYDROGEN_MAX_CONCENTRATEID = "1003009";
	public static final String HYDROGEN_MAX_PRESSURE = "1003010";
	public static final String HYDROGEN_MAX_PRESSUREID = "1003011";
	public static final String HVDCDC = "1003012";
	public static final byte RUNNING = (byte)0x01;
	public static final byte DISCONNECTED = (byte)0x02;
	public static final byte DCSTATUS_INVALID = (byte)0xff;
	
	//RealTimeDataEngine
	public static final String ENGINE_STATUS = "1004001";
	public static final byte ENGINE_ON = (byte)0x01;
	public static final byte ENGINE_OFF = (byte)0x02;
	public static final String CRANK_SHAFT_SPEED = "1004002";
	public static final String FUEL_CONSUME_PERCENT = "1004003";
	
	//RealTimeDataGPS
	public static final String GPS_VALID = "1005001";
	public static final String GPS_LATITUDE_VALUE = "1005005";
	public static final String GPS_LONGITUDE_VALUE = "1005004";

	//RealTimeDataExtermum
	//单条极值数据长度
	public static final int REALTIMEDATA_SINGLE_EXTERMUM_SIZE = 14;
	//单条极值数据在OTA 0.22版本后的 长度
	//1、MaxVoltCellId和MinVoltCellId的类型由Uint8改成Uint16
	//2、MaxTempProbeId和MinTempProbeId的类型由Uint8改成Uint16
	public static final int REALTIMEDATA_SINGLE_EXTERMUM_OTA22_SIZE = 18;
	
	public static final String MAX_VOLT_SUBSYSTEMID = "1006001";
	public static final String MAX_VOLT_CELLID = "1006002";
	public static final String MAX_VOLT_OF_CELL = "1006003";
	public static final String MIN_VOLT_SUBSYSTEMID = "1006004";
	public static final String MIN_VOLT_CELLID = "1006005";
	public static final String MIN_VOLT_OF_CELL = "1006006";
	public static final String MAX_TEMP_SUBSYSTEMID = "1006007";
	public static final String MAX_TEMP_PROBEID = "1006008";
	public static final String MAX_TEMP = "1006009";
	public static final String MIN_TEMP_SUBSYSTEMID = "1006010";
	public static final String MIN_TEMP_PROBEID = "1006011";
	public static final String MIN_TEMP = "1006012";
	//RealTimeDataAlarm
	public static final String MAX_ALARM_LEVEL = "1007001";
	public static final byte ALERT_LEVEL_0 = (byte)0x00;
	public static final byte ALERT_LEVEL_1 = (byte)0x01;
	public static final byte ALERT_LEVEL_2 = (byte)0x02;
	public static final byte ALERT_LEVEL_3 = (byte)0x03;

	public static final String ALARM_DIFFERTEMP = "1007002";
	public static final String ALARM_CELLHIGHTEMP = "1007003";
	public static final String ALARM_ENGINECONTAINER_OVER_PRESSURE = "1007004";
	public static final String ALARM_ENGINECONTAINER_LOW_PRESSURE = "1007005";
	public static final String ALARM_LOWSOC = "1007006";
	public static final String ALARM_SINGLECELL_HIGHVOTAGE = "1007007";
	public static final String ALARM_SINGLECELL_LOWVOTAGE = "1007008";
	public static final String ALARM_HIGHSOC = "1007009";
	public static final String ALARM_UNSTABLESOC= "1007010";
	public static final String ALARM_UNMATCHABLE_CELLCONTAINER = "1007011";
	public static final String ALARM_SINGLECELLDIFFER = "1007012";
	public static final String ALARM_CELLINSULATION = "1007013";
	public static final String ALARM_DCDCTEMP = "1007014";
	public static final String ALARM_BRAKESYSTEM = "1007015";
	public static final String ALARM_DCDCSTATUS = "1007016";
	public static final String ALARM_DRIVEMOTORCTRLTEMP = "1007017";
	public static final String ALARM_LOCKEDBYHIGNVOLTAGE = "1007018";
	public static final String ALARM_DRIVEMOTORTEMP = "1007019";
	public static final String ALARM_OVERCHARGE = "1007020";
	public static final String CHARGEDEV_FAULT_CNT = "1007021";
	public static final String CHARGEDEV_FAULT_LIST = "1007022";
	public static final String MOTOR_FAULT_LIST = "1007023";
	public static final String MOTOR_FAULT_CNT = "1007024";
	public static final String ENGINE_FAULT_LIST = "1007025";
	public static final String ENGINE_FAULT_CNT = "1007026";
	public static final String OTHER_FAULT_LIST = "1007027";
	public static final String OTHER_FAULT_CNT = "1007028";

	//votltage
	public static final String SUBSYSTEMID_VOLTAGE_8 = "1008001";
	public static final String TOTAL_VOLTAGE_8 = "1008002";
	public static final String TOTAL_CURRENT_8 = "1008003";
	public static final String CELL_COUNT = "1008004";
	public static final String VOLTAGES = "1008007";
	
	//temp
	public static final String SUB_SYSTEMID = "1009001";
	public static final String TEMP_CNT = "1009002";
	public static final String TEMPARATURES = "1009003";
	
	//carstatus
	public static final long CARCAN_DOOR_UNLOCK_CLOSE_NUM = 0;
	
	public static final long CARCAN_DLCK_COCKPIT_LOCK = 1L;
	public static final long CARCAN_DLOCK_COKPIT_CHECK = 3L;
	
	public static final long CARCAN_DLCK_COPILOT_LOCK = 4L;
	public static final long CARCAN_DLOCK_COPILOT_CHECK = 12L;
	
	public static final long CARCAN_DLCK_RTBCKSEAT_LOCK = 16L;
	public static final long CARCAN_DLOCK_RTBCKSEAT_CHECK = 48L;
	
	public static final long CARCAN_DLCK_LFTBCKSEAT_LOCK = 64L;
	public static final long CARCAN_DLOCK_LFTBCKSEAT_CHECK = 192L;
	
	public static final long CARCAN_DSTATUS_COCKPIT_OPEN = 256L;
	public static final long CARCAN_DSTATUS_COCKPIT_CHECK = 768L;
	
	public static final long CARCAN_DSTATUS_COPILOT_OPEN = 1024L;
	public static final long CARCAN_DSTATUS_COPILOT_CHECK = 3072L;
	
	public static final long CARCAN_DSTATUS_RGTBCKSEAT_OPEN = 4096L;
	public static final long CARCAN_DSTATUS_RGTBCKSEAT_CHECK = 12288L;
	
	public static final long CARCAN_DSTATUS_LFTBCKSEAT_OPEN = 16384L;
	public static final long CARCAN_DSTATUS_LFTBCKSEAT_CHECK = 49152L;
	
	public static final long CARCAN_DSTATUS_BCK_OPEN = 65536L;
	public static final long CARCAN_DSTATUS_BCK_CHECK = 196608L;
	
	public static final String CARCAN_DOOR_LOCK ="Lock";
	public static final String CARCAN_DOOR_UNLOCK ="UnLock";
	public static final String CARCAN_DOOR_OPEN ="Open";
	public static final String CARCAN_DOOR_CLOSE ="Close";
	public static final String CARCAN_INVALID_DATA = "-255";
	public static final String ILLEGAL_NUM = null;
	public static final String CARCAN_INVALID_STATUS = "Invalid";

	
	
	//国标类型编码
	public static final byte GB_DATA_VEHICLE_MARK = (byte)0x01;
	public static final byte GB_DATA_FUEL_CELL_MARK = (byte)0x03;
	public static final byte GB_DATA_ENGIN_MARK = (byte)0x04;
	public static final byte GB_DATA_VEHICLE_POS_MARK = (byte)0x05;
	public static final byte GB_DATA_EXTERMUM_MARK = (byte)0x06;
	public static final byte GB_DATA_ALARM_MARK = (byte)0x07;
	//国标整车数据长度20
	public static final int GB_DATA_VEHICLE_LENTH = 20;
	//国标位置信息长度5
	public static final int GB_DATA_VEHICLE_POS_LENTH = 9;
	//国标极值信息长度14
	public static final int GB_DATA_EXTERMUM_LENTH = 14;
	
	//车况长度；
	public static final int REPORT_CARCAN_LENGTH = 40;
	public static final int REPORT_CARCAN_ADD_POWERBATTERY_LENGTH = 43;
	public static final int REPORT_CARCAN_ADD_SPEEDLIMIT_LENGTH = 45;
	public static final int REPORT_CARCAN_ADD_EXTDATASIZE_LENGTH = 2;
	public static final String MOTOR_COUNT = "1002009";
	public static final String VOTLTAGE_COUNT = "1008008";
	public static final String TEMP_COUNT = "1009004";

	//车况上报空调状态
	public static final String REPORT_CARCAN_AIRCONDITION_STATUS_0 = "Stop";
	public static final String REPORT_CARCAN_AIRCONDITION_STATUS_1 = "BlowerOnly";
	public static final String REPORT_CARCAN_AIRCONDITION_STATUS_2 = "Auto";
	public static final String REPORT_CARCAN_AIRCONDITION_STATUS_3 = "FullCold";
	public static final String REPORT_CARCAN_AIRCONDITION_STATUS_4 = "FullHot";

	

	
	
}
