/**
 * Nettyfinalant.java Create on 2017年6月5日
 * Copyright (c) 2017年6月5日 by 上汽集团商用车技术中心
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>
 * @version 1.0
 */
package com.maxus.tsp.gateway.common.constant;

/**
 * Created by patterncat on 2016/4/6.
 */
public class OTAConstant {

	// OTA response byte constants
	public static final byte COMMON_RESULT_SUCCESS = 0x00;
	public static final byte COMMON_RESULT_FAILED = 0x01;

	// CMD_DOWN_LOGOUT_RESULT
	// 0：表示登录成功
	// 1：表示T-Box公钥不存在
	// 2：表示协议版本不支持
	// 3：表示其它错误
	// 4：表示给定的TBox序列号不存在
	// 5: 表示Vin号错误
	public static final byte LOGIN_RESULT_FAILED_FOR_VERSION_IS_NOT_SUPPORT = 0x02;
	public static final byte LOGIN_RESULT_FAILED_FOR_OTHER_REASON = 0x03;
	public static final byte LOGIN_RESULT_FAILED_FOR_TBOX_SN_NOT_EXIST = 0x04;
	public static final byte LOGIN_RESULT_FAILED_FOR_WRONG_VIN = 0x05;

	// CMD_DOWN_REGISTER_RESULT
	// 0：处理数据成功
	// 1：失败，原因为错误的SN号
	// 2：失败，原因为设备已锁定
	// 3：失败，原因为错误的MD5值
	// 4：失败，原因为其它
	public static final byte REGISTER_RESULT_FAILED_FOR_WRONG_SN_NUMBER = 0x01;
	public static final byte REGISTER_RESULT_FAILED_FOR_TBOX_HAS_BEEN_LOCKED = 0x02;
	public static final byte REGISTER_RESULT_FAILED_FOR_WRONG_MD5_NUMBER = 0x03;
	public static final byte REGISTER_RESULT_FAILED_FOR_OTHER_REASON = 0x04;
	public static final int TBOX_RIGISTER_LOCKED = 1;
	public static final int TBOX_RIGISTER_UNLOCKED = 0;
	// CMD_DOWN_QCPASSED_RESULT
	// 0：处理数据成功
	// 1：失败，原因为错误的SN号
	// 2：失败，原因为设备已锁定
	// 3：失败，原因为其它
	public static final byte QCPASSED_RESULT_FAILED_FOR_WRONG_SN_NUMBER = 0x01;
	public static final byte QCPASSED_RESULT_FAILED_FOR_TBOX_HAS_BEEN_LOCKED = 0x02;
	public static final byte QCPASSED_RESULT_FAILED_FOR_OTHER_REASON = 0x03;

	// CMD_UP_WARNING_TYPE
	// 1：车门被非法打开
	// 2：车辆被非法移动
	// 3：引擎盖非法打开
	// 4：轮胎非法拆卸
	// 5：车辆被撞击
	// 6：疲劳驾驶
	// 7：CAN数据中断
	// 8：车辆故障状态有更新
	// 9：GPS未定位报警
	// 10：车道偏离
	public static final byte WARNING_FOR_OPENING_CARDOORS_ILLEGALLY = 0x01;
	public static final byte WARNING_FOR_MOVING_VEHICLE_ILLEGALLY = 0x02;
	public static final byte WARNING_FOR_OPENING_HOOD_ILLEGALLY = 0x03;
	public static final byte WARNING_FOR_DISASSEMBLING_TIRES_ILLEGALLY = 0x04;
	public static final byte WARNING_FOR_VEHICLE_COLLISION = 0x05;
	public static final byte WARNING_FOR_FATIGUE_DRIVING = 0x06;
	public static final byte WARNING_FOR_CAN_DATA_INTERRUPTION = 0x07;
	public static final byte WARNING_FOR_VEHICLE_MULFUNCTION_UPDATED = 0x08;
	public static final byte WARNING_FOR_GETTING_GPSPOS_FAILURE = 0x09;
	public static final byte WARNING_FOR_DRIVING_LANE_DEPARTURE = 0x0A;
	// WARNING_DETAIL
	// WarningType为1
	// bit 0: 1：驾驶座车门为打开状态；0：驾驶座车门为关闭状态
	// bit 1: 1：副驾座车门为打开状态；0：副驾座车门为关闭状态
	// bit 2: 1：右后座车门为打开状态；0：右后座车门为关闭状态
	// bit 3: 1：左后座车门为打开状态；0：左后座车门为关闭状态
	// bit 4: 1：尾门为打开状态；0：尾门为关闭状态
	public static final byte WARNING_DETAIL_FOR_OPENING_FL_DOOR_ILLEGALLY = 0x01;
	public static final byte WARNING_DETAIL_FOR_OPENING_FR_DOOR_ILLEGALLY = 0x02;
	public static final byte WARNING_DETAIL_FOR_OPENING_BR_DOOR_ILLEGALLY = 0x04;
	public static final byte WARNING_DETAIL_FOR_OPENING_BL_DOOR_ILLEGALLY = 0x08;
	public static final byte WARNING_DETAIL_FOR_OPENING_TAIL_DOOR_ILLEGALLY = 0x10;
	// WarningType为5
	// bit 0: 1：前部撞击；0：非前部撞击
	// bit 1: 1：后部撞击；0：非后部撞击
	// bit 2: 1：左侧撞击；0：非左侧撞击
	// bit 3: 1：右侧撞击；0：非右侧撞击
	public static final byte WARNING_FOR_VEHICLE_FRONTSIDE_COLLISION = 0x01;
	public static final byte WARNING_FOR_VEHICLE_BACKSIDE_COLLISION = 0x02;
	public static final byte WARNING_FOR_VEHICLE_LEFTSIDE_COLLISION = 0x04;
	public static final byte WARNING_FOR_VEHICLE_RIGHTSIDE_COLLISION = 0x08;
	// WarningType为8
	// bit 0: 1：有稳定控制系统故障；0：无稳定控制系统故障
	// bit 1: 1：有ABS制动防抱死系统故障；0：无ABS制动防抱死系统故障
	// bit 2: 1：有排放故障；0：无排放故障
	// bit 3: 1：有发动机故障；0：无发动机故障
	// bit 4: 1：有安全气囊故障；0：无安全气囊故障
	// bit 5: 1：有EBD故障；0：无EBD故障
	public static final byte WARNING_DETAIL_FOR_ESP_MULFUNCTION = 0x01;
	public static final byte WARNING_DETAIL_FOR_ABS_MULFUNCTION = 0x02;
	public static final byte WARNING_DETAIL_FOR_EMISSION_MULFUNCTION = 0x04;
	public static final byte WARNING_DETAIL_FOR_ENGINE_MULFUNCTION = 0x08;
	public static final byte WARNING_DETAIL_FOR_AIRBAG_MULFUNCTION = 0x10;
	public static final byte WARNING_DETAIL_FOR_EBD_MULFUNCTION = 0x20;
	// WarningType为10
	// bit 0: 1：向左偏离；0：无向左偏离
	// bit 1: 1：向右偏离；0：无向右偏离
	public static final byte WARNING_FOR_DRIVING_LEFTLANE_DEPARTURE = 0x01;
	public static final byte WARNING_FOR_DRIVING_RIGHTLANE_DEPARTURE = 0x02;

	// 鸣笛成功标志位
	public static final byte WHISTLE_SUCCESS_BIT = 0x01;
	// 闪灯成功标志位
	public static final byte FLASH_SUCCESS_BIT = 0x02;
	// 上锁解锁成功标志位
	public static final byte CONTROL_DOOR_SUCCESS_BIT = 0x04;
	// 开关空调成功标志位
	public static final byte CONTROL_AC_SUCCESS_BIT = 0x08;
	// 设置空调温度成功标志位
	public static final byte CONTROL_AC_TEMP_SUCCESS_BIT = 0x10;
	// 座椅加热成功标志位
	public static final byte WARM_SEAT_SUCCESS_BIT = 0x20;
	// 限速或解除限速成功标志位
	public static final byte LIMIT_SPEED_SEAT_SUCCESS_BIT = 0x40;

	// 远程控制错误码 0x01= Power mode !== OFF
	public static final short REMOTE_CONTROL_ERR_CODE_POWER_MODE_IS_NOT_OFF = 0x01;
	// 远程控制错误码0x03= Other Command in progress =您有其他车辆操作正在执行，请稍候执行
	public static final short REMOTE_CONTROL_ERR_CODE_OTHER_CMD_IN_PROGRESS = 0x03;
	// 远程控制错误码0x15= Door Open =车门未关，执行失败
	public static final short REMOTE_CONTROL_ERR_CODE_DOOR_OPEN = 0x15;
	// 远程控制错误码0x19= Door is unlocked =车门未锁，执行失败
	public static final short REMOTE_CONTROL_ERR_CODE_DOOR_IS_UNLOCKED = 0x19;
	// 时间字节长度
	public static final int DATETIME_BYTES_SIZE = 7;
	// 软件终端版本号长度
	public static final int OTA_SOFT_VERSION_OFFSET = 1;
	// 默认arry起始偏移
	public static final int ARRAY_OFFSET_ZERO = 0;
	// 0
	public static final int ZERO = 0;
	// 拍照上行报文摄像头编号的偏移位置
	public static final int TAKEPHOTO_UP_MSG_CAMERANO_OFFSET = 7;
	// 拍照上行报文摄像头拍摄结果的偏移位置
	public static final int TAKEPHOTO_UP_MSG_CAMERA_RESULT_OFFSET = 8;
	// 非法OTA最短报文
	public static final int OTA_INVALID_LENGTH_MIN = 4;
	// 报警报文长度
	public static final int OTA_WARNING_MSG_LENGTH = 19;
	// 报警细节长度
	public static final int OTA_WARNING_DETAIL_LENGTH = 2;
	// 报警细节在参数中的偏移
	public static final int OTA_WARNING_DETAIL_OFFSET = 1;
	// 报警gps在参数中的偏移
	public static final int OTA_WARNING_GPS_OFFSET = 3;
	// TBOX公钥报文长度验证 MD5(32字节)+长度字节(2字节)
	public static final int OTA_REGISTER_MSG_LENGTH_FOR_LEN_AND_MS5 = 34;
	// 回复报文中参数值默认格式
	public static final byte[] DEFAULT_RETURN_BYTES = new byte[] { '0', '1' };
	// AES 长度
	public static final int AES_KEY_LENGTH = 16;
	// OTA 版本号 长度
	public static final int OTA_VERSION_LENGTH = 2;
	// OTA 版本号 偏移
	public static final int OTA_VERSION_OFFSET = 16;
	// OTA 登陆报文最小长度
	public static final int OTA_LOGIN_MSG_LENGTH_MIN = 25;
	// OTA 登陆报文含vin，iccid， battery信息的最小长度
	public static final int OTA_LOGIN_MSG_LENGTH_MIN_WITH_EXTEND_INFO = 63;
	public static final int OTA_STRING_TYPE_BYTE_SIZE = 2;
	public static final int OTA_LOGIN_MSG_EXTEND_VIN_OFFSET = 25;
	public static final int OTA_LOGIN_MSG_EXTEND_ICCID_OFFSET = 42;
	public static final int OTA_LOGIN_MSG_EXTEND_BATTERY_OFFSET = 62;
	// 危险行为报警信息长度
	public static final int OTA_EARLYWARNING_MSG_OFFSET = 26;

	// 预警位置状态长度
	public static final int OTA_EARLYWARNING_POS_STATUS_OFFSET = 1;
	// 预警位置经纬度长度
	public static final int OTA_EARLYWARNING_LATITUDE_OFFSET = 4;
	public static final int OTA_EARLYWARNING_LONGITUDE_OFFSET = 4;
	// 预警车速信息长度
	public static final int OTA_EARLYWARNING_VEHICLE_SPEED_OFFSET = 2;


	// 远程配置结果
	// 成功
	public static final byte REMOTE_CONFIG_SUCCESS = 0x00;
	// 大数据参数-压缩类型偏移
	public static final int BIG_DATA_COMPRESS_TYPE_OFFSET = 0;
	// 大数据参数-压缩类型字节长度
	public static final int BIG_DATA_COMPRESS_TYPE_SIZE = 1;
	// 大数据参数-大数据内容长度偏移
	public static final int BIG_DATA_CONTENT_SIZE_OFFSET = 1;
	// 大数据参数-大数据内容长度
	public static final int BIG_DATA_CONTENT_SIZE = 2;
	// 大数据参数-大数据内容长度
	public static final int BIG_DATA_CONTENT_OFFSET = 3;

	// 上传工程数据格式常量
	// 工程数据-压缩数据长度
	public static final byte ENG_DATA_COMPRESS_TYPE_SIZE = 1;
	// 工程数据-内容长度字段偏移
	public static final byte ENG_DATA_CONTENT_SIZE_OFFSET = 1;
	// 工程数据-内容长度字段长度
	public static final byte ENG_DATA_CONTENT_SIZE = 2;
	// 工程数据-工程数据偏移
	public static final byte ENG_DATA_CONTENT_OFFSET = 3;

	// 开始往国家平台发数据 指令执行结果
	// 0:成功
	public static final byte START_DIRECT_REPORT_SUCCESS = 0x00;
	// 1：失败，原因为连接服务器失败
	public static final byte START_DIRECT_REPORT_SERVER_CONNECTRING_FAILED = 0x01;
	// 2：失败，原因为登入失败
	public static final byte START_DIRECT_REPORT_LOGIN_FAILED = 0x02;
	// 3：失败，原因为其它
	public static final byte START_DIRECT_REPORT_OTHER_FAILED = 0x03;

	// 停止往国家平台发数据 指令执行结果
	// 0:成功
	public static final byte STOP_DIRECT_REPORT_SUCCESS = 0x00;
	// 1：失败
	public static final byte STOP_DIRECT_REPORT_FAILED = 0x01;

	// 智能家居UP指令常量
	public static final byte HOME_CTRL_UP_PARAMSIZE_LENGTH = 2;
	public static final byte HOME_CTRL_UP_ID_OFFSET = 2;
	public static final byte HOME_CTRL_UP_RESULT_OFFSET = 2;

	/*************************************************
	 *                国六B数据处理常量                *
	 *************************************************/
	public static final int UINT8_OFFSET = 1;
	public static final int UINT16_OFFSET = 2;
	public static final int UINT32_OFFSET = 4;
	public static final int DATA_STREAM_INFO_OFFSET=37;
	public static final int FAULT_CNT_INDEX = 105;
	public static final int OBD_INFO_CNT_INDEX = 9;
	public static final int FAULT_CNT_MIN = 0;
	public static final int FAULT_CNT_MAX = 253;
	public static final int OBD_INFO_CNT_MIN = 0;
	public static final int OBD_INFO_CNT_MAX = 1;
	public static final int FAULT_LIST_OFFSET = 4;
	//除faultList以外的obdInfo的长度
	public static final int EXTRA_OBD_INFO_OFFSET = 96;
	//字段obdInfo之前的报文长度
	public static final int PREFIXAL_OFFSET = 10;
	//除两个info集合以外的长度
	public static final int EXTRA_OFFSET = 11;
	public static final int DATA_STREAM_CNT_OFFSET = 1;
	//CodeValue封装要求OBDInfo的个数
	public static final int OBD_INFO_NUM = 9;
	//CodeValue封装要求DataStreamInfo的个数
	public static final int DATA_STREAM_INFO_NUM = 19;

	//获取token不论成功返回的公用部分长度(command+paramSize+tokenSize)
	public static final int ERROR_OUT_DATA_SIZE = 6;

	//远程组合控制回复报文长度验证
	public static final int RMT_GROUP_RESP_OFFSET = 4;
	
	//远控车窗ParamSize长度偏移
	public static final int RMT_GROUP_EXT_PARAMSIZE_OFFSET = 2;
}
