package com.maxus.tsp.gateway.common.constant;

public class OperationConstant {

	// 网关线程池线程个数
	public static final int FIX_POOL_NUMBER = 100;
	// 远程控制的操作名称
	public static final String REMOTE_CONTROL = "remoteControl";
	// 唤醒等待时间(毫秒)
	public static final long WAKEUP_WAIT_TIME = 75000; // origin: 90000;
														// modified because app
														// api should wait for
														// 90s include response
														// then it should less
														// than 90s
	// 唤醒等待时间(秒)
	public static final int WAKEUP_WAIT_TIME_SEC = 75; // 需要与毫秒常量同步设置
	// long型时间和秒之间转换的乘除数
	public final static long MS_IN_A_SECOND = 1000;
	// 用于判断时间时间是否小于10秒，
	public final static long differ = 10;
	// 唤醒请求过期时间
	public static final int WAKEUP_EXPIRED_TIME = 90;
	//在线请求IT kafka等待时间
	public static final long ONLINE_COMMAND_WAIT_TIME = 10000;
	// 远程控制回复等待时间
	public static final long REMOTE_CONTROL_RESPONSE_WAIT_TIME = 10000;
	// 唤醒后下行指令下发间隔时间
	public static final long OTA_DOWN_MESSAGE_SEND_DELAY_TIME = 2000;
	// 远程控制及拍照请求过期时间
	public static final int REMOTECONTROL_REQ_EXPIRED_TIME = 90;
	// 远程控制回复过期时间
	public static final int REMOTECONTROL_RESP_EXPIRED_TIME = 10;
	// 在线拍照回复等待时间
	public static final long TAKE_PHOTO_RESPONSE_WAIT_TIME_FOR_ONLINE = 10000;
	// 拍照请求等待离线娱乐主机上线并回复时间
	public static final long TAKE_PHOTO_RESPONSE_WAIT_TIME_FOR_OFFLINE = 80000;
	// 拍照请求过期时间
	public static final int TAKEPHOTO_REQ_EXPIRED_TIME = 140;
	// 拍照回复过期时间
	public static final int TAKEPHOTO_RESP_EXPIRED_TIME = 10;
	// 摄像头个数
	public static final int TAKEPHOTO_SUPPORTED_CAMERA_NUMBER = 5;
	// 最小摄像头编号
	public static final String SUPPORTED_CAMERA_NO_MIN = "1";
	// 最大摄像头编号
	public static final String SUPPORTED_CAMERA_NO_MAX = "5";
	// 拍照日志的事件类型: 拍照时间
	public static final int TAKE_PHOTO_LOG_EVENT_TYPE_TAKE_PHOTO_TIME = 3;
	// 拍照完全成功
	public static final int TAKEPHOTO_ORIGIN_STATUS = -1;
	// 拍照完全成功
	public static final int TAKEPHOTO_COMPLETE_SUCCESS = 0;
	// 拍照其他失败
	public static final int TAKEPHOTO_FAILED_FOR_OTHER_REASON = 4;
	// 拍照部分成功
	public static final int TAKEPHOTO_PARTLY_SUCCESS = 5;
	// MD5长度
	public static final int MD5_SIZE = 32;
//	// 报警限制时间内不重复发送
//	public static final int CHECK_WARNING_DUPLICATED_LIMIT_TIME = -30;
	// 唤醒短信内容
	public static final String WAKE_UP_MSG_CONTENT = "0000";
	// ota协议回复
	public static final long OTA_REPLAY_TIME_MAX_FLAG = 5000;
	// 拍照的操作名称
	public static final String FORWARD_TAKEPHOTO = "takePhoto";
	// poi的操作名称
	public static final String FORWARD_POI = "poi";
	// vin码长度
	public static final int VIN_LENGTH = 17;
	// iccid长度
	public static final int TBOX_ICCID_LENGTH = 20;
	// 报警信息有效时间判断边界
	public static final String WARNING_VALID_COLLECT_TIME = "2018-01-01 00:00:00";
	// 逗号分隔符
	public static final String COMMA = ",";
	// 北纬
	public static final String NORTH_LATITUDE = "NORTH";
	// 南纬
	public static final String SOUTH_LATITUDE = "SOUTH";
	// 西经
	public static final String WEST_LONGITUDE = "WEST";
	// 东经
	public static final String EAST_LONGITUDE = "EAST";
	// 合法
	public static final String VALID = "VALID";
	// 非法
	public static final String INVALID = "INVALID";
	// gps
	public static final double GPS_PRECISION = 1000000.00f;
	// gps 精度位数
	public static final int GPS_PRECISION_SCALE = 6; 
	// it确认注册成功回复等待时间
	public static final long REGISTER_DATA_RESPONSE_WAIT_TIME = 10000;
	// it确认注册成功
	public static final String REGISTER_SUCCESS = "0";
	// it Redis中关于设备激活的状态
	public static final String DEVICE_LOCK_SUCCESS = "ACTIVE";
	// tbox网关已经激活(需后台确认，但未得到确认)
	public static final int REGISTER_GATEWAY_SUCCESS = 3;
	// tbox激活后台已经确认或仅需要网关激活确认
	public static final int REGISTER_COMPLETE_SUCCESS = 1;
	// tbox初始化
	public static final int REGISTER_TBOXSTATUS_INITIAL = 0;
	// 远程配置回复等待时间10s
	public static final int REMOTE_CONFIG_RESPONSE_WAIT_TIME = 10000;
	// 远程配置消息回复处理过期时间10s
	public static final int REMOTE_CONFIG_RESPONSE_EXPIRED_TIME = 10;
	// 远程配置下行消息处理过期时间90s
	public static final int REMOTE_CONFIG_REQ_EXPIRED_TIME = 90;
	// 远程配置值最大值
	public static final int REMOTE_CONFIG_REQ_MAX_VALUE = (Short.MAX_VALUE * 2 + 1);
	// 远程控制指令开启
	public static final String REMOTE_CTRL_FUNCTION_OPEN = "11";
	// 远程控制指令关闭
	public static final String REMOTE_CTRL_FUNCTION_CLOSED = "00";
	// 蓝牙操作名称
	public static final String BLUE_TOOTH_CTRL = "bluetooth_ctrl";
	// 故障报警支持的总个数
	public static final int MAX_FAULT_NO_SUPPORTED = 24;
	// 故障报警报文固定字节
	public static final int FIX_FAULT_MSG_BYTES = 17;

	// 短信服务运营商常量CM
	public static final String MESSAGE_SERVICE_CM = "CM";
	// 短信服务运营商常量CU
	public static final String MESSAGE_SERVICE_CU = "CU";
	// 短信服务运营商常量YM(移动的一类)
	public static final String MESSAGE_SERVICE_YM = "YM";
	
	//房车家居远程控制的操作名称
	public static final String HOME_CONTROL = "homeControl";

	// 配合新接口返回码前缀
	public static final String PREFIX_REMOTE_CTRL_STATUS_FOR_ASYN_INTERFACE = "1208";

	/******************************FOTA功能控制常量*************************************/
	//Fota版本查询的操作名称(用来标记存入redis中的控制指令与唤醒指令)
	public static final String FOTA_VERSION_QUERY = "version_query";
	//Fota平台请求网关车主答复下发操作名称
	public static final String FOTA_VERSION_UPGRADE = "version_upgrade";
	//Fota平台请求网关继续升级操作名称
	public static final String FOTA_UPGRADE_RESUME = "upgrade_resume";
	

	//Fota证书更新的操作名称(用来标记存入redis中的控制指令与唤醒指令)
	public static final String FOTA_CERTIFICATION_UPGRADE = "certification_upgrade";


	//组合远控操作名称
	public static final String RM_GROUP_CTRL = "remote_group_ctrl";


}
