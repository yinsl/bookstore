package com.maxus.tsp.common.enums;

public enum ResultStatus {
	SUCCESS("0", "操作成功"),
	TBOX_NOT_REPLY_WITHIN_LIMIT_TIME("1", "TBox在有限时间内没有返回结果"),
	TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG("2", "TBox在唤醒情况下，有限时间内没有登陆TSP"),
	TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED("3", "Tbox离线，短信唤醒Tbox，短信没有发送成功"),
	TBOX_HAVE_ACCEPT_ONE_COMMAND("4", "Tbox正在执行操控过程中，不能接受新的控制指令"),
	REMOTE_CONTROL_CANNOT_DONE_FOR_DRIVE_MODE("5", "表示车辆未熄火或行驶状态，不能执行远程控制"),
	REMOTE_CONTROL_CANNOT_DONE_FOR_CAR_IS_DOING_ANOTHER_OP("6", "表示车辆有其他操作正在执行，不能执行远程控制"),
	REMOTE_CONTROL_CANNOT_DONE_FOR_CAR_DOOR_UNCLOSED("7", "表示车辆车门未关，不能执行远程控制"),
	REMOTE_CONTROL_CANNOT_DONE_FOR_CAR_DOOR_UNLOCKED("8", "表示车辆车门未锁，不能执行远程控制"),
	REMOTE_CONTROL_CANNOT_DONE_FOR_OTHER_REASONS("9", "表示车辆由于其他原因不满足远程控制条件，无法执行"),
	TBOX_SEND_WAKEUP_SUCCESS("10", "已经下发唤醒短信"),
	NO_VALID_VERICODE("12001", "验证码无效"),
	USER_NOT_FOUND("12002", "用户不存在"),
	PARAM_ERROR("12003", "参数不合法，为无效值"),
	DB_EXECUTION_FAILURE("12005", "数据库操作失败"),
	USER_EXIST("12006", "用户已经存在"),
	BIND_CAR_WRONG("12007", "绑定车辆信息不一致（该车与当前用户无绑定关系"),
	FAIL("12009", "操作失败"),
	VIN_ERROR("12011", "车架号错误"),
	CAR_INFO_ERROR("12012", "车辆信息错误"),
	CAR_ALREADY_BIND("12013", "车辆已绑定"),
	BINDCAR_PHONE_NOT_MATCH("12014", "购车手机号不正确"),
	BIND_CAR_FAIL("12015", "车辆-车主绑定失败"),
	VERTIFICATION_FROZEN("12016", "用户验证码冻结"),
	TOKEN_AUTH_FAILURE("12017", "用户Token鉴权失败"),
	EMAIL_USER_NOT_MATCH("12020", "更改邮箱用户信息不一致"),
	PWD_WRONG("12021", "密码错误"),
	NOTFOUND("12022", "无查询结果"),
	USER_EMAIL_EXIST("12023", "填写用户资料邮箱已存在"),
	INSERT_NICKNAME_FAIL("12025", "车辆昵称更新失败"),
	OUT_OF_RANGE("12026", "电子围栏数量不能超过5个"),
	ILLEGAL_POINT("12027", "非法的有效半径"),
	ELEPOSITION_REQUIRED("12028", "围栏的中心位置为必填数据"),
	VIN_REQUIRED("12029", "车架号为必填项"),
	MOBILEPHONE_REQUIRED("12030", "手机号码为必填数据"),
	WRONG_ELEID("12031", "删除围栏的ID不正确"),
	ELENAME_UNCORECCT_FORMAT("12032", "电子围栏名称不符合规范"),
	ELEREMINDWAY_UNMATCHED("12033", "电子围栏提醒方式不符合规范"),
	ELEREMINDCONDITIONS_UNMATCHED("12034", "电子围栏提醒条件不符合规范"),
	MOBILENUMBER_LENGTH_EXP("12035", "手机号长度不正确"),
	VIOLATION_QUERY_NETWORK_ERROR("12036", "违章查询第三方接口网络错误"),
	VIOLATION_QUERY_NETWORK_CLOSE("12037", "违章查询城市在第三方接口中未开通查询"),
	VIN_LENGTH_ERROR("12039", "车架号末尾长度错误"),
	ENGINENO_CARNO_ERROR("12040", "违章查询车牌号车架号发动机号错误"),
	ENGINENO_PARAMTER_ERROR("12041", "违章查询输入信息或参数错误"),
	NO_CARNO("12042", "车牌号为必填数据"),
	BINDCAR_NAME_NOT_MATCH("12043", "购车车主姓名信息错误"),
	// BINDCAR_UID_NOT_MATCH("12043", "用户的身份和预留身份不一致"),
	BINDCAR_UPDATE_USERINFO_FAIL("12044", "购车车主身份证错误"),
	DISTRIBUTOR_SHOP_NOTBEFOUND("12045", "经销商店铺不存在"),
	MAINTENANCE_PROPOSER_REQUIRED("12047", "维保预约申请人信息为必填项"),
	VEHICLE_ORDER_NOTFOUND("12049", "该车辆无订单"),
	VEHICLE_UNRELATED_ORDERID("12050", "该车辆与订单号无关联"),
	ORDERID_REQUIRED("12051", "订单号为必填数据"),
	ORDER_CANNOT_BEDELETED("12052", "订单状态不支持删除"),
	ORDER_CANNOT_BECANCElLED("12053", "订单状态不支持取消"),
	SIGNIN_USER_EXIST("12054", "场景为注册但用户已存在"),
	CAMERA_LIST_REQUIRED("12055", "摄像头列表为必填项"),
	CAMERAID_REQUIRED("12056", "摄像头编号为必填数据"),
	CAMERA_NO_PRIVED_ERROR("12057", "摄像头编号不正确"),
	USER_UNRELATE_MSG("12059", "用户与消息无关联"),
	ORDERID_NOT_EXIST("12061", "消息ID不存在"),
	BLUE_TIME_IS_EMPTY("12062", "授权时间为必填数据"),
	BLUE_STIME_LATE_ETIMEY("12063", "结束授权时间必须比开始授权时间晚"),
	BLUE_USER_NOT_EXIST("12064", "授权用户不存在"),
	BLUE_ORDER_NOT_EXIST("12065", "授权订单编号不存在"),
	BLUE_ORDER_TOKEN_ERROR("12066", "没有正确生成授权密钥"),
	BLUE_USER_NOT_ORDER("12067", "授权参数不正确"),
	BLUE_ORDER_ID_EMPTY("12069", "授权编号为必填数据"),
	VIOLATION_CITY_NOT_OPEN("12070", "所查询的城市正在维护或未开通查询"),
	PING_FROZEN("12071", "设置PIN码功能已冻结"),
	REMOTE_CONTROL_PARAM_REQUIRED("12072", "远程控制请求参数不能为空"),
	REMOTE_CONTROL_INVALID_COMMAND("12073", "远程控制控制指令非法"),
	REMOTE_CONTROL_FAILED_FOR_EXCEPTION("12074", "远程控制异常失败"),
	REMOTE_CONTROL_FAILED_SEARCH_RESULT_FOR_EXCEPTION("12076", "远程控制查询结果异常失败"),
	UNEXPECT_PAGENO("12075", "分页页码不正确"),
	UNEXPECT_MSGTYPE("12078", "非法的消息类型"),
	REPEAT_PERIOD_ORDER("12079", "该时段内已有预约中的订单，请等候处理"),
	APP_US_ERROR_TEN_COUNT("12080", "app登录密码错误超10次"),

	RM_TBOX_NOT_REPLY_WITHIN_LIMIT_TIME("12081", "TBox在有限时间内没有返回结果"),
	RM_SEND_WAKEUP_MSG_FOR_TBOX_AND_NEED_CHECK_RESULT("12082", "TBox不在线，准备发送唤醒，请稍后查询远程控制结果"),
	RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED("12083", "Tbox离线，短信唤醒Tbox，短信没有发送成功"),
	RM_TBOX_HAVE_ACCEPT_ONE_COMMAND("12084", "Tbox正在执行操控过程中，不能接受新的控制指令"),
	RM_REMOTE_CONTROL_CANNOT_DONE_FOR_DRIVE_MODE("12085", "表示车辆未熄火或行驶状态，不能执行远程控制"),
	RM_REMOTE_CONTROL_CANNOT_DONE_FOR_CAR_IS_DOING_ANOTHER_OP("12086", "表示车辆有其他操作正在执行，不能执行远程控制"),
	RM_REMOTE_CONTROL_CANNOT_DONE_FOR_CAR_DOOR_UNCLOSED("12087", "表示车辆车门未关，不能执行远程控制"),
	RM_REMOTE_CONTROL_CANNOT_DONE_FOR_CAR_DOOR_UNLOCKED("12088", "表示车辆车门未锁，不能执行远程控制"),
	RM_REMOTE_CONTROL_CANNOT_DONE_FOR_OTHER_REASONS("12089", "表示车辆由于其他原因不满足远程控制条件，无法执行"),
	RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG("12090", "TBox在唤醒情况下，有限时间内没有登陆TSP"),
	NO_PLATFORMTYPE("12091", "请输入App平台类型"),
	NO_APPVERSION("12092", "请输入App版本号"),
	UNCURRENT_ORDER("12093", "该订单状态非执行中"),
	REMOTE_UPDATE_FAILED_FOR_EXCEPTION("12094", "远程升级异常失败"),
	REMOTE_UPDATE_FAILED_FOR_WITHOUT_CORRECT_PACKAGE("12095", "远程升级因数据库不存在升级包信息失败"),
	REMOTE_CONTROL_FAILED_FOR_TBOX_OFFLINE("12096", "tbox不在线失败"),
	REMOTE_UPDATE_FAILED_FOR_EMPTY_UPDATE_INFO("12097", "远程升级因数据库升级包信息字段为空失败"),
	REMOTE_UPDATE_FAILED_FOR_ERROR_UPDATE_INFO("12098", "远程升级因数据库升级包信息格式不正确失败，请确认MD5、版本及链接信息"),
	USER_NO_CAR_TYPE_AUTH("12099", "用户的车型没有相关的操作权限"),
	CAR_PREFIX_FIND_CITY_EXCEPTION("12100", "车辆前缀查询城市发生异常"),
	CAR_PREFIX_FIND_CITY_WRONG("12101", "根据车牌前缀获取查询规则相关错误"),
	CAR_PREFIX_FIND_CITY_RESULT_NULL("12102", "根据车牌前缀查询不到城市"),
	OP_UNDO_FOR_REQUEST_TIME_EXPIRED("12103", "操作由于请求时间格式不正确或过期不执行。"),
	DIRECT_REPORT_FAILED_FOR_TBOX_OFFLINE("12104", "国家平台直连操作因tbox不在线而不执行。"),
	DIRECT_REPORT_START_FAILED_FOR_CONNECT_SERVICE_ERROR("12105", "国家平台直连启动功能因连接服务器失败。"),
	DIRECT_REPORT_START_FAILED_FOR_LOGIN_SERVICE_ERROR("12106", "国家平台直连启动功能因登入失败而失败。"),
	DIRECT_REPORT_START_FAILED_FOR_OTHER_REASON("12107", "国家平台直连启动因其他原因失败而失败。"),
	DIRECT_REPORT_STOP_FAILED("12108", "国家平台直连停止失败。"),

	BLUETOOTH_CONTROL_FAILED_FOR_PARAM_ILLEGAL("12109", "蓝牙操作失败，原因为参数为空或无效"),
	BLUETOOTH_CONTROL_FAILED_FOR_COMMAND_ILLEGAL("12110", "蓝牙操作失败，原因为控制指令非法"),
	BLUETOOTH_CONTROL_FAILED_FOR_EXCEPTION("12111", "蓝牙操作因发生异常失败"),


	NIO_SUCCESS("00000000", "操作成功"),
	NIO_AUTH_FAIL("12017", "用户鉴权失败"),
	NIO_NO_COMPANYNO("13084", "企业编号未提供"),
	NIO_COMPANYNO_PARTNOTEXIST("13085", "企业编码部分不存在"),
	NIO_COMPANYNO_NOTEXIST("13086", "企业编码全部不存在"),
	NIO_QUERY_EXP("13087", "后台异常，查询车辆失败"),
	NIO_NOUPDATETIME("13088", "更新时间未提供"),
	NIO_UNEXPECT_UPDATETIME("13089", "更新时间格式不准确"),

	// token透传错误码
	TOKEN_TBOX_INFO_NULL("14001", "sn对应的TBox信息不存在!"),
	TOKEN_TBOX_INFO_TOKEN_MSG_NULL("14002", "sn对应的TBox信息token不存在!"),
	TOKEN_GET_NULL("14003", "系统出错，获取token过程中发生异常!"),

	/**********************FOTA返回码******************************/
	// 版本请求通用状态码
	OPT_FAILED_BY_EXCEPTION("15001", "系统操作异常失败"),
	//定位model接收参数后, 整个model为空
	PARAM_NULL("15002", "当前控制请求参数为空!"),
	//redis可用性判断暂时不用, 该返回码暂时保留
	REDIS_NOT_AVAILABLE("15003", "当前redis不可用!"),
	SN_NULL("15004", "sn号为空!"),
	SN_ERROR("15005", "sn号不正确，请检查!"),
	SEQ_NO_NULL("15006", "seqNo为空!"),
	SEQ_NO_ERROR("15007", "seqNo长度不为22或内容不全为数字!"),
	EVENT_TIME_NULL("15008", "当前请求时间为空!"),

	// 版本升级专用返回码
	VERSION_UPGRADE_ID_ERROR("15020", "版本升级任务id为空或不为数字!"),
	VERSION_UPGRADE_OPERATE_NULL("15021", "版本升级操作指令operate为空!"),
	VERSION_UPGRADE_OPERATE_ERROR("15022", "版本升级操作指令operate为无效值!"),

	//继续升级专用返回码
	UPGRADE_RESUME_ID_ERROR("15023", "继续升级任务id为空或不为数字!"),

	//证书更新专用返回码
	CERTIFICATE_CMD_NULL("15024", "请求证书更新指令cmd为空!"),
	CERTIFICATE_CMD_ERROR("15025", "请求证书更新指令cmd无效, 不为avn或tbox!"),
	CERTIFICATE_TYPE_NULL("15026", "请求证书更新证书类型type为空!"),
	CERTIFICATE_TYPE_ERROR("15027", "请求证书更新证书类型type为无效值!"),
	CERTIFICATE_SIZE_ERROR("15028", "请求证书更新后台证书url长度与size不符!"),
	CERTIFICATE_URL_NULL("15029", "请求证书更新后台证书url为空!"),
	CERTIFICATE_URL_ERROR("15030", "请求证书更新后台证书url格式不是16进制报文!"),

	//TBox请求信息发送专用返回码
//	INFORMATION_CMD_NULL("15020", "请求信息发送指令为空!"),
//	INFORMATION_CMD_ERROR("15021", "请求信息发送指令为无效值!"),
//	INFORMATION_DATA_SIZE_ERROR("15022", "请求信息发送指令进度上报数据data长度与dataSize不符!"),
//	INFORMATION_DATA_NULL("15023", "请求信息发送指令进度上报数据为空!"),
//	INFORMATION_DATA_ERROR("15024", "请求信息发送指令进度上报数据格式错误, 不为16进制报文!"),

	//进度上报专用返回码
//	PROGRESS_CMD_NULL("15025", "进度上报cmd为空!"),
//	PROGRESS_CMD_ERROR("15026", "进度上报cmd为无效值!"),
//	PROGRESS_ID_ERROR("15027", "进度上报id为空或不为数字!"),
//	PROGRESS_DATA_SIZE_ERROR("15028", "进度上报数据data长度与dataSize不符!"),
//	PROGRESS_DATA_NULL("15029", "进度上报数据为空!"),
//	PROGRESS_DATA_ERROR("15030", "进度上报数据格式错误, 不为16进制报文!"),

	//车主AVN端是否同意升级专用返回码
//	AGREE_DATA_NULL("15031", "AVN确认升级指令车主选择结果为空!"),
//	AGREE_DATA_ERROR("15032", "AVN确认升级指令车主选择结果为无效值!"),

	/******************************************************/

	REMOTE_UPDATE_SUCCESS("50000", "升级成功"),
	REMOTE_UPDATE_DOWNLOADING("50001", "正在下载"),
	REMOTE_UPDATE_FINISH_DOWNLOAD("50002", "下载完成"),
	REMOTE_UPDATE_DOWNLOAD_FAILED("50003", "升级失败，原因为升级包下载失败"),
	REMOTE_UPDATE_CHECK_FAILED("50004", "升级失败，原因为升级包校验失败"),
	REMOTE_UPDATE_OTHERREASON_FAILED("50005", "升级失败，原因为其它"),
	REMOTE_UPDATING("50006", "正在升级中"),
	REMOTE_UPDATING_FAILED_LOW_SOC("50007", "电量不足，建议启动车辆，档位在P档处"),
	REMOTE_UPDATING_FAILED_DRIVING("50008", "车辆行驶中，不能升级"),


	//	0：下载成功
//	1：开始下载
//	2：下载完成（此时还没有校验文件）
//	3：下载失败，原因为文件下载失败
//	4：下载失败，原因为文件校验失败
//	5：下载失败，原因为其它
//	其它值：无效
	DOWNLOAD_FILE_SUCCESS("50009", "下载成功"),//	0：下载成功
	DOWNLOAD_FILE_BEGIN_DOWNLOADING("50010", "开始下载"),//	1：开始下载
	DOWNLOAD_FILE_DOWNLOADED("50011", "下载完成（此时还没有校验文件）"),//	2：下载完成（此时还没有校验文件）
	DOWNLOAD_FILE_DOWNLOAD_FAILED("50012", "下载失败，原因为文件下载失败"),//	3：下载失败，原因为文件下载失败
	DOWNLOAD_FILE_CHECK_FAILED("50013", "下载失败，原因为文件校验失败"),//	4：下载失败，原因为文件校验失败
	DOWNLOAD_FILE_FAILED("50014", "下载失败，原因为其它"),//	5：下载失败，原因为其它
	DOWNLOAD_FILR_PARAM_WRONG("50015", "文件下载请求参数错误！"),
	//	0：上传成功
//	1：开始上传
//	2：上传失败，原因为其它
//	其它值：无效
	UPLOAD_FILE_SUCCESS("50016", "上传成功"),
	UPLOAD_FILE_UPLOADING("50017", "开始上传"),
	UPLOAD_FILE_FAILED("50018", "上传失败，原因为其它"),
	UPLOAD_FILE_PARAM_WARONG("50019", "文件上传请求参数错误！"),
	;

	private String code;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	private String description;

	ResultStatus(String code, String des) {
		setCode(code);
		setDescription(des);
	}

	public static ResultStatus getResultStatus(String code) {
		for (ResultStatus r : ResultStatus.values()) {
			if (r.getCode().equals(code)) {
				return r;
			}
		}
		return null;
	}
}
