/**        
 * RedisConstant.java Create on 2017年6月5日      
 * Copyright (c) 2017年6月5日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>      
 * @version 1.0  
 */
package com.maxus.tsp.common.constant;

/**
 * Redis相关常量定义
 * 
 * @ClassName: RedisConstant.java
 * @Description:  
 * @author 任怀宇
 * @version V1.0
 * @Date 2017年8月3日 下午2:00:17
 */
public class RedisConstant {
	// 有效的TBox/avn(数据库存在）
	public static final String VALID_PRE = "valid";
	// app api 远程控制下行指令
	public static final String CAR_REMOTE_CTRL_REQ = "CAR_REMOTE_CTRL_REQ";
	// app api 远程控制下发时间
	public static final String CAR_REMOTE_CTRL_DOWN = "CAR_REMOTE_CTRL_DOWN";
	// app api 远程控制回复结果
	public static final String CAR_REMOTE_CTRL_RESP = "CAR_REMOTE_CTRL_RESP";
	// 娱乐主机远程控制下行指令
	public static final String ENTERTAINMENT_REMOTE_CTRL_REQ = "ENTERTAINMENT_REMOTE_CTRL_REQ";
	// 远程配置请求
	public static final String CAR_REMOTE_CONFIG_REQ = "CAR_REMOTE_CONFIG_CTRL";
	// 蓝牙请求
	public static final String CAR_BLUETOOTH_CTRL_REQ = "CAR_BLUETOOTH_CTRL";
	// 获取车况请求
	public static final String GET_VEHICLE_STATUS_REQ = "GET_VEHICLE_STATUS_CTRL";
	
	// app api 房车家居远程控制下行指令
	public static final String CAR_HOME_CTRL_REQ = "CAR_HOME_CTRL_REQ";

	// 最新车况数据缓存
	public static final String CURRENT_CAN = "CURRENT_CAN";

	// 记录需要进行超时提醒的操作
	public static final String RMT_TIMEOUT = "RMT_TIMEOUT";
	// 最新收集时间
	public static final String CURRENT_CAN_COLLECT_DATE = "CURRENT_CAN_COLLECT_DATE";
	// 车辆实时定位数据
	public static final String CAR_CUR_POS = "CAR_CUR_POS";
	// tbox 临时公钥缓存
	public static final String TBOX_PUBLIC_KEY_TEMP = "TBOX_PUBLIC_KEY_TEMP";
	// tbox 信息缓存(与数据库tbox表同步)
	public static final String TBOX_INFO = "TBOX_INFO";
	// tbox 当前绑定的车架号vin
	public static final String VIN_FOR_SN = "VIN_FOR_SN";
	// tbox含fota升级类型
	public static final String TBOX_WITH_FOTA = "TBOX_WITH_FOTA";
	// 在线tbox情况
	public static final String ONLINE_TBOX = "ONLINE_TBOX";
	// tbox发送离线报文的时间
	public static final String TBOX_LOGOUT_TIME = "TBOX_LOGOUT_TIME";
	// 唤醒 tbox 集合
	public static final String WAKE_UP = "WAKE_UP";
	//指令下发集合拼接 排他
	public static final String COMMAND_SEND = "COMMAND_SEND";
	// 在线车辆电子围栏
	public static final String TBOX_ELE_FENCE = "TBOX_ELE_FENCE";
	// 记录tbox当前socket
	public static final String TBOX_CHANNEL = "TBOX_CHANNEL";
	
    /**
	 * redis可用性检测时间间隔为5分钟一次
	 */
	public static final long FIVE_MINUTE_TIME = 300000;
	

	//记录国际平台登录数据的key
	public static final String GB_PLATFORM_LOGIN_NO = "GB_PLATFORM_LOGIN_NO";
	//记录国际车辆登录数据的key
	public static final String GB_CAR_LOGIN_NO = "GB_CAR_LOGIN_NO";
	//最大流水号
	public static final long KING_NO = 65531;

	//国家平台直连操作
	public static final String GB_DIRECT_REPORT = "GB_DIRECT_REPORT";
	

	// TBOX远程升级问题
	public static final String ON_REMOTE_UPDATE_OTA = "ON_REMOTE_UPDATE_OTA";
	// 记录文件下载记录及状态
	public static final String TBOX_DOWNLOAD_FILE_INFO = "TBOX_DOWNLOAD_FILE_INFO"; 
	// 记录tbox文件上传记录及状态
	public static final String TBOX_UPLOAD_FILE_INFO = "TBOX_UPLOAD_FILE_INFO";

	/*******************************FOTA相关请求保存在Redis中的HashKey********************************/
	//FOTA版本查询请求
	public static final String FOTA_VERSION_QUERY_REQ = "FOTA_VERSION_QUERY";
	//FOTA请求网关车主答复下发
	public static final String FOTA_VERSION_UPGRADE_REQ = "FOTA_VERSION_UPGRADE";

	//FOTA证书更新请求
	public static final String FOTA_CERTIFICATION_UPGRADE_REQ = "FOTA_CERTIFICATION_UPGRADE";

	//FOTA请求保存在Redis统一的HashKey
	public static final String FOTA_REMOTE_CTRL_REQ = "FOTA_REMOTE_CTRL";
	/***********************************************************************************************/

	//远程组合控制操作名称
	public static final String RM_GROUP_CTRL = "REMOTE_GROUP_CTRL";

	//远程配置操作名称
	public static final String RM_CONFIG_CTRL = "REMOTE_CONFIG_CTRL";
	
	/** 
	 * 当前tbox的ota协议版本号 
	 */
	public static final String CUR_OTA_VERSION = "CUR_OTA_VERSION";
	
	/**
	 * 超时操作锁
	 */
	public static final String TIMEOUT_OP_LOCK = "TIMEOUT_OP_LOCK";
	
	/**
	 * 发唤醒短信后等待超时
	 */
	public static final String RMT_SMS_TIMEOUT = "RMT_SMS_TIMEOUT";
	
	/**
	 * 组合远控请求指令
	 */
	public static final String RMT_GROUP_CTRL_REQ = "RMT_GROUP_CTRL_REQ";
	
	/**
	 * SECURITY_KEY
	 */
	public static final String SECURITY_KEY = "SECURITY_KEY";
}
