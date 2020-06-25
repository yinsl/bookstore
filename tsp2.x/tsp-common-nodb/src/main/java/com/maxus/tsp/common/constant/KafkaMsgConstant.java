/**        
 * KafkaMesConstant.java Create on 2017年6月5日      
 * Copyright (c) 2017年6月5日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>      
 * @version 1.0  
 */
package com.maxus.tsp.common.constant;

/**
 * @ClassName: KafkaMesConstant.java
 * @Description:  
 * @author 任怀宇
 * @version V1.0
 * @Date 2017年6月5日 上午9:08:51
 */
public class KafkaMsgConstant {
	public static final String SUCCESS_CODE = "00000";
	public static final String SUCCESS_MES = "成功";
	
	/**************** kafka-code *******************/
	public static final String KAFKA_SEND_ERROR_CODE = "30001";
	public static final String KAFKA_NO_RESULT_CODE = "30002";
	public static final String KAFKA_NO_OFFSET_CODE = "30003";
	
	/**************** kafka-mes *******************/
	public static final String KAFKA_SEND_ERROR_MES = "发送消息超时,联系相关技术人员";
	public static final String KAFKA_NO_RESULT_MES = "未查询到返回结果,联系相关技术人员";
	public static final String KAFKA_NO_OFFSET_MES = "未查到返回数据的offset,联系相关技术人员";
	
	/*************** kafka-topic **************/
	// 远程控制下行主题
	public static final String TOPIC_REMOTECTRL = "remotectrl-down-topic";
	// 远程控制下行主题参数长度
	public static final int TOPIC_REMOTECTRL_PARAM_SIZE = 4;
	// kafka主题中的tbox序列号偏移位置
	public static final int TOPIC_TBOX_SN_OFFSET = 0;
	// 远程控制下行主题指令名称偏移位置
	public static final int TOPIC_REMOTECTRL_CMD_OFFSET = 1;
	// 远程控制下行主题指令参数偏移位置
	public static final int TOPIC_REMOTECTRL_CMDVALUE_OFFSET = 2;
	// 远程控制下行主题发起时间偏移位置
	public static final int TOPIC_REMOTECTRL_EVENTIME_OFFSET = 3;
	// 远程控制指令响应主题
	public static final String TOPIC_REMOTECTRL_RES = "remotectrl-up-topic";
	// 远程控制指令响应主题参数长度
	public static final int TOPIC_REMOTECTRL_RES_PARAM_SIZE = 5;
	// 远程控制指令响应结果偏移位置
	public static final int TOPIC_REMOTECTRL_RES_RESULT_OFFSET = 2;
	// 远程控制指令响应主题发起时间偏移位置
	public static final int TOPIC_REMOTECTRL_RES_EVENTIME_OFFSET = 3;
	// 远程控制指令响应tbox错误码偏移位置
	public static final int TOPIC_REMOTECTRL_RES_ERRCODE_OFFSET = 4;
	// 远程控制唤醒TBOX
	public static final String TOPIC_REMOTECTRL_WAKEUP = "remotectrl-wakeup";
	// 远程控制唤醒TBOX发起时间偏移位置
	public static final int TOPIC_REMOTECTRL_WAKEUP_EVENTIME_OFFSET = 1;
	// 透传上行Topic
	public static final String TOPIC_FORWARD_UP = "forward-up-topic";
	// 透传上行Topic参数长度
	public static final int TOPIC_FORWARD_UP_PARAM_SIZE = 5;
	// 透传上行Topic发起时间偏移位置
	public static final int TOPIC_FORWARD_UP_CMD_OFFSET = 1;
	// 透传上行Topic返回结果偏移位置
	public static final int TOPIC_FORWARD_UP_RESULT_OFFSET = 2;
	// 透传上行Topic发起时间偏移位置
	public static final int TOPIC_FORWARD_UP_EVENTIME_OFFSET = 3;
	// 透传上行Topic状态更新偏移位置
	public static final int TOPIC_FORWARD_UP_STATUS_UPDATE = 4;
	// 透传下行Topic
	public static final String TOPIC_FORWARD_DOWN = "forward-down-topic";
	// 透传下行Topic参数长度
	public static final int TOPIC_FORWARD_DOWN_PARAM_SIZE = 4;
	// 透传下行Topic指令偏移位置
	public static final int TOPIC_FORWARD_DOWN_CMD_OFFSET = 1;
	// 透传下行Topic指令参数偏移位置
	public static final int TOPIC_FORWARD_DOWN_CMD_PARAM_OFFSET = 2;
	// 透传下行Topic发起时间偏移位置
	public static final int TOPIC_FORWARD_DOWN_EVENTIME_OFFSET = 3;
	//获取token出错时返回错误码topic
	public static final String TOPIC_GET_TOKEN_ERROR = "uploadDriver_erro_data";
	// 远程升级Topic
	public static final String TOPIC_TBOX_UPDATE = "tbox-update-topic";
	// 远程升级Topic参数长度
	public static final int TOPIC_TBOX_UPDATE_VERSION_OFFSET = 1;
	// 远程升级Topic参数长度
	public static final int TOPIC_TBOX_UPDATE_URL_OFFSET = 2;
	// 远程升级Topic参数长度
	public static final int TOPIC_TBOX_UPDATE_MD5_OFFSET = 3;
	// 远程升级Topic参数长度
	public static final int TOPIC_TBOX_UPDATE_PARAM_SIZE = 4;
	// 报警内部侦听
	public static final String TOPIC_WARNING_LISTEN = "warningListen-topic";
	// 注册返回 内部侦听
	public static final String TOPIC_REGISTER_RESULT_ACCEPT = "gateway-register-accept-topic";
	
	/****************   it相关kafka主题     **************************/
	//OTA数据
	public static final String TOPIC_OTA_DATA = "ota_data";
	//注册数据
	public static final String TOPIC_REGISTER_DATA = "register_data";
	//it的注册回复
	public static final String TOPIC_REGISTER_RESULT_DATA = "register_result_data";
	//位置信息
	public static final String TOPIC_LOCATON_DATA = "location_data";
	//it向网关请求远程控制
	public static final String TOPIC_IT_REMOTECTRL_REQUEST = "remotectrl_request";
	//网关向it返回远程控制回复
	public static final String TOPIC_IT_REMOTECTRL_RESPONSE = "remotectrl_response";
	//it向网关请求拍照
	public static final String TOPIC_IT_TAKEPHOTO_REQUEST = "takephoto_request";
	//网关向it返回拍照回复
	public static final String TOPIC_IT_TAKEPHOTO_RESPONSE = "takephoto_response";
	//网关向it返回远程升级回复
	public static final String TOPIC_IT_REMOTEUPDATE_REQUEST = "remoteupdate_request";
	public static final String TOPIC_IT_REMOTEUPDATE_RESPONSE = "remoteupdate_response";
	// 网关返回it文件下载相关主题
	public static final String TOPIC_IT_DOWNLOAD_FILE_REQUEST = "downloadfile_request";
	public static final String TOPIC_IT_DOWNLOAD_FILE_RESPONSE = "downloadfile_response";
	// 网关返回it文件上传相关主题
	public static final String TOPIC_IT_UPLOAD_FILE_REQUEST = "uploadfile_request";
	public static final String TOPIC_IT_UPLOAD_FILE_RESPONSE = "uploadfile_response";
	// 网关返回it预警信息相关主题
//	public static final String TOPIC_IT_EARLY_WARNING = "early_warning";
	
	//房车家居远控相关主题
	public static final String TOPIC_IT_HOME_CTRL_REQUEST = "home_ctrl_request";
	public static final String TOPIC_IT_HOME_CTRL_RESPONSE = "home_ctrl_response";
	
	/************* 远程配置相关KAFKA主题 ***************/
	//房车家居远控相关主题
	public static final String TOPIC_DOWN_HOME_CTRL = "home-ctrl-down-topic";
	public static final String TOPIC_UP_HOME_CTRL = "home-ctrl-up-topic";
	//主题
	public static final String TOPIC_DOWN_REMOTE_CONFIG = "remote-config-down-topic";
	//获取Tbox配置下行主题
//	public static final String TOPIC_DOWN_GET_REMOTE_CONFIG = "get-remote-config-down-topic";
	// 远程配置指令响应主题
	public static final String TOPIC_UP_REMOTE_CONFIG_RES = "remote-config-up-topic";
	//获取远配响应主题
	public static final String TOPIC_UP_GET_REMOTE_CONFIG_RES = "get-remote-config-up-topic";
	// 文件操作主题
	public static final String TOPIC_DOWM_LOAD_FILE_REQ = "download_file_req";
	public static final String TOPIC_DOWM_LOAD_FILE_RESP = "download_file_resp";
	public static final String TOPIC_UP_LOAD_FILE_REQ = "upload_file_req";
	public static final String TOPIC_UP_LOAD_FILE_RESP = "upload_file_resp";
	//蓝牙控制相关主题
	//it向网关请求蓝牙远程控制
	public static final String TOPIC_IT_BT_REQUEST = "btKey_request";
	//蓝牙钥匙请求网关远程控制结果返回it
	public static final String TOPIC_IT_BT_RESPONSE = "btKey_response";

	//verification code
	public static final String TOPIC_DOWN_BLUETOOTH_VERIFICATION_CODE = "bluetooth-verification-code-down-topic";
	public static final String TOPIC_UP_BLUETOOTH_VERIFICATION_CODE = "bluetooth-verification-code-up-topic";
	//add bt key
	public static final String TOPIC_DOWN_BLUETOOTH_ADD_BTKEY = "bluetooth-add-btkey-down-topic";
	public static final String TOPIC_UP_BLUETOOTH_ADD_BTKEY = "bluetooth-add-btkey-up-topic";
	//del bt key
	public static final String TOPIC_DOWN_BLUETOOTH_DEL_BTKEY = "bluetooth-del-btkey-down-topic";
	public static final String TOPIC_UP_BLUETOOTH_DEL_BTKEY = "bluetooth-del-btkey-up-topic";
	//GET bt key
	public static final String TOPIC_DOWN_BLUETOOTH_GET_BTKEY = "bluetooth-get-btkey-down-topic";
	public static final String TOPIC_UP_BLUETOOTH_GET_BTKEY = "bluetooth-get-btkey-up-topic";

	//远程组合控制相关主题
	//it topic
	public static final String TOPIC_IT_RM_GROUP_REQ = "remotectrl_group_request";
	public static final String TOPIC_IT_RM_GROUP_RESP = "remotectrl_group_response";
	//self topic
	public static final String TOPIC_SELF_RM_GROUP_UP = "remotectrl_group_up";
	public static final String TOPIC_SELF_RM_GROUP_DOWN = "remotectrl_group_down";
    //self topic
	public static final String TOPIC_SELF_RM_EXT_UP = "remotectrl_ext_up";
	public static final String TOPIC_SELF_RM_EXT_DOWN = "remotectrl_ext_down";
	// 获取车况主题
	public static final String GET_VEHICLE_STATUS_DOWN_CTRL = "get_vehicle_status_down_ctrl";
	public static final String GET_VEHICLE_STATUS_UP_CTRL = "get_vehicle_status_up_ctrl";
	//远程配置参数长度
	public static final int TOPIC_DOWN_REMOTE_CONFIG_PARAM_SIZE = 4;
	public static final int TOPIC_UP_REMOTE_CONFIG_RES_EVENTIME_OFFSET = 3;
	// 远程控制指令响应主题参数长度
	public static final int TOPIC_REMOTE_CONFIG_RES_PARAM_SIZE = 4;
	// 大数据
	public static final String TOPIC_BIG_DATA = "bigdata_data";
	// 工程数据
	public static final String TOPIC_ENG_DATA = "engdata_data";
	// 国家平台直连开启Topic
	public static final String TOPIC_TBOX_DIRECT_REPORT_START = "tbox-direct-report-start-topic";
	// 国家平台直连开启Topic url偏移
	public static final int TOPIC_TBOX_DIRECT_REPORT_START_URL_OFFSET = 1;
	// 国家平台直连开启Topic port偏移
	public static final int TOPIC_TBOX_DIRECT_REPORT_START_PORT_OFFSET = 2;
	// 国家平台直连开启Topic eventTime偏移
	public static final int TOPIC_TBOX_DIRECT_REPORT_START_EVENTTIME_OFFSET = 3;
	// 国家平台直连开启Topic 参数个数
	public static final int	TOPIC_TBOX_DIRECT_REPORT_START_PARAM_SIZE = 4;
	// 国家平台直连返回结果Topic
	public static final String TOPIC_TBOX_DIRECT_REPORT_RES = "tbox-direct-report-res-topic";
	// 国家平台直连操作返回结果Topic 参数个数
	public static final int	TOPIC_TBOX_DIRECT_REPORT_RES_PARAM_SIZE = 3;
	// 国家平台直连操作返回结果Topic status偏移
	public static final int TOPIC_TBOX_DIRECT_REPORT_RES_STATUS_OFFSET = 1;
	// 国家平台直连操作返回结果Topic eventTime偏移
	public static final int TOPIC_TBOX_DIRECT_REPORT_RES_EVENTTIME_OFFSET = 2;
	// 国家平台直连停止Topic
	public static final String TOPIC_TBOX_DIRECT_REPORT_STOP = "tbox-direct-report-stop-topic";
	// 国家平台直连停止Topic 参数个数
	public static final int	TOPIC_TBOX_DIRECT_REPORT_STOP_PARAM_SIZE = 2;
	// 国家平台直连停止Topic eventTime偏移
	public static final int	TOPIC_TBOX_DIRECT_REPORT_STOP_EVENTTIME_OFFSET = 1;
	// ota0.32
}
