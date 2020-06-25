package com.maxus.tsp.gateway.common.constant;

import java.util.HashMap;
import java.util.Map;

public enum OTACommand {
	CMD_UP_LOGIN((short) 0x0001, "登录"),
	CMD_DOWN_LOGIN((short) 0x8001, "登录结果"),
	CMD_UP_HEARTBEAT((short) 0x0002, "发送心跳"),
	CMD_DOWN_HEARTBEAT((short) 0x8002, "回复心跳"),
	CMD_UP_GETPOS((short) 0x0003, "发送当前车辆位置"),
	CMD_DOWN_GETPOS((short) 0x8003, "请求发送当前车辆位置"),
	CMD_UP_REPORTPOS((short) 0x0004, "报告车辆位置"),
	CMD_DOWN_REPORTPOS((short) 0x8004, "收到数据之后的应答"),
	CMD_UP_REMOTECTRL((short) 0x0005, "远程控制指令的执行结果"),
	CMD_DOWN_REMOTECTRL((short) 0x8005, "发送远程控制指令"),
	CMD_UP_REPORTCAN((short) 0x0006, "上报车辆数据"),
	CMD_DOWN_REPORTCAN((short) 0x8006, "收到数据之后的应答"),
	CMD_UP_LOGOUT((short) 0x0007, "登出"),
	CMD_DOWN_LOGOUT((short) 0x8007, "回复登出"),
	CMD_UP_REALTIME_DATA((short) 0x0008, "实时信息上报"),
	CMD_DOWN_REALTIME_DATA((short) 0x8008, "收到实时信息之后的应答"),
	CMD_UP_REGISTER((short) 0x0009, "TBox下线注册。发送该指令之前无需登录"),
	CMD_DOWN_REGISTER((short) 0x8009, "注册结果"),
	CMD_UP_QCPASSED((short) 0x000A, "表明TBox状态检查成功"),
	CMD_DOWN_QCPASSED((short) 0x800A, "指令处理结果"),
	CMD_UP_UPDATE_PUBKEY((short) 0x000B, "更新TSP公钥的结果"),
	CMD_DOWN_UPDATE_PUBKEY((short) 0x800B, "更新TSP公钥"),
	CMD_UP_START_DIRECT_REPORT((short) 0x000C, "开始往国家平台发数据指令执行结果"),
	CMD_DOWN_START_DIRECT_REPORT((short) 0x800C, "开始往国家平台发数据"),
	CMD_UP_STOP_DIRECT_REPORT((short) 0x000D, "停止往国家平台发数据指令执行结果"),
	CMD_DOWN_STOP_DIRECT_REPORT((short) 0x800D, "停止往国家平台发数据"),
	CMD_UP_REMOTE_UPDATE((short) 0x000E, "远程更新指令执行状态"),
	CMD_DOWN_REMOTE_UPDATE((short) 0x800E, "远程更新"),
	CMD_UP_WARNING((short) 0x000F, "上传报警信息"),
	CMD_DOWN_WARNING((short) 0x800F, "报警信息处理结果"),
	CMD_UP_FORWARD_4IVI((short) 0x0010, "TSP请求透传指令或指令结果给IVI"),
	CMD_DOWN_FORWARD_4IVI((short) 0x8010, "IVI请求透传指令或指令结果给TSP"),
	CMD_UP_ADD_BT_KEY((short) 0x0011, "添加蓝牙钥匙结果"),
	CMD_DOWN_ADD_BT_KEY((short) 0x8011, "添加蓝牙钥匙"),
	CMD_UP_DEL_BT_KEY((short) 0x0012, "删除蓝牙钥匙结果"),
	CMD_DOWN_DEL_BT_KEY((short) 0x8012, "删除蓝牙钥匙"),
	CMD_UP_GET_BT_KEY((short) 0x0013, "获取蓝牙钥匙结果"),
	CMD_DOWN_GET_BT_KEY((short) 0x8013, "获取蓝牙钥匙"),
	CMD_UP_VERIFICATION_CODE((short) 0x0014, "蓝牙钥匙失效通知"),
	CMD_DOWN_VERIFICATION_CODE((short) 0x8014, "蓝牙钥匙失效处理结果"),
	CMD_UP_GB_LOGIN((short) 0x0015, "国标功能的登录"),
	CMD_DOWN_GB_LOGIN((short) 0x8015, "国标功能登陆结果"),
	CMD_UP_GB_LOGOUT((short) 0x0016, "国标功能的登出"),
	CMD_DOWN_GB_LOGOUT((short) 0x8016, "国标功能登出结果"),
	CMD_UP_FAULT((short) 0x0018, "上传故障信息"),
	CMD_DOWN_FAULT((short) 0x8018, "故障信息处理结果"),
	CMD_UP_BIG_DATA((short) 0x0019, "上传大数据"),
	CMD_UP_GET_CONFIG((short) 0x001B, "获取配置值列表"),
	CMD_DOWN_GET_CONFIG((short) 0x801B, "取得配置值"),
	CMD_DOWN_BIG_DATA((short) 0x8019, "大数据处理结果"),
	CMD_DOWN_REMOTE_CONFIG((short) 0x801A, "远程配置"),
	CMD_UP_REMOTE_CONFIG((short) 0x001A, "远程配置上行"),
	CMD_UP_GET_VEHICLE_STATUS((short) 0x001C, "返回当前车况"),
	CMD_DOWN_GET_VEHICLE_STATUS((short) 0x801C, "取得当前车况"),
	CMD_UP_ENG_DATA((short) 0x001D, "上传工程数据"),
	CMD_DOWN_ENG_DATA((short) 0x801D, "指令处理结果"),
	CMD_UP_DOWNLOAD_FILE((short) 0x001E, "文件下载结果"),
	CMD_DOWN_DOWNLOAD_FILE((short) 0x801E, "文件下载"),
	CMD_UP_UPLOAD_FILE((short) 0x001F, "文件上传结果"),
	CMD_DOWN_UPLOAD_FILE((short) 0x801F, "上传文件"),
	CMD_UP_EARLY_WARNING((short) 0x0020, "危险行为预警上报"),
	CMD_DOWN_EARLY_WARNING((short) 0x8020, "指令处理结果"),
	CMD_UP_HOME_CTRL((short) 0x0021, "远程家居控制结果"),
	CMD_DOWN_HOME_CTRL((short) 0x8021, "远程家居控制"),
	CMD_UP_GB_EMISSION((short) 0x0022, "国六B排放数据上报"),
	CMD_DOWN_GB_EMISSION((short) 0x8022, "收到数据之后的应答"),
	CMD_UP_GB_LOGIN_EMSN((short) 0x0023, "国六B功能的登入"),
	CMD_DOWN_GB_LOGIN_EMSN((short) 0x8023, "登入结果"),
	CMD_UP_GB_LOGOUT_EMSN((short) 0x0024, "国六B功能的登出"),
	CMD_DOWN_GB_LOGOUT_EMSN((short) 0x8024, "登出结果"),
	CMD_UP_REGISTER_CERT((short) 0x0025, "FOTA注册"),
	CMD_DOWN_REGISTER_CERT((short) 0x8025, "FOTA注册结果"),
	CMD_UP_GET_ECU_LIST((short) 0x0026, "获取ECU列表信息"),
	CMD_DOWN_GET_ECU_LIST((short) 0x8026, "下发ECU列表信息"),
	CMD_UP_OTA_QUERY_NOTIFY((short) 0x0027, "收到版本查询通知的答复"),
	CMD_DOWN_OTA_QUERY_NOTIFY((short) 0x8027, "通知TBox版本查询"),
	CMD_UP_REPORT_VERSION((short) 0x0028, "请求版本更新"),
	CMD_DOWN_REPORT_VERSION((short) 0x8028, "下发更新包信息"),
	CMD_UP_DOWNLOAD_PROGRESS((short) 0x0029, "下载进度上报"),
	CMD_DOWN_DOWNLOAD_PROGRESS((short) 0x8029, "下载进度上报的应答"),
	CMD_UP_UPGRADE_REQ((short) 0x002A, "收到车主答复的应答"),
	CMD_DOWN_UPGRADE_REQ((short) 0x802A, "车主答复下发"),
	CMD_UP_UPGRADE_RESUME((short) 0x002B, "上报继续升级结果"),
	CMD_DOWN_UPGRADE_RESUME((short) 0x802B, "车主发起继续升级"),
	CMD_UP_UPGRADE_RESULT((short) 0x002C, "上报升级进度"),
	CMD_DOWN_UPGRADE_RESULT((short) 0x802C, "收到升级进度的应答"),
//	CMD_UP_UPD_GW_CERT((short) 0x002D, "收到证书更新的应答,PKI信息安全专用"),
//	CMD_DOWN_UPD_GW_CERT((short) 0x802D, "后台向TBox发送更新的后台证书的url,PKI信息安全专用"),
//	CMD_UP_AVN_UPD_GW_CERT((short) 0x002E, "AVN收到证书更新的应答,PKI信息安全专用"),
//	CMD_DOWN_AVN_UPD_GW_CERT((short) 0x802E, "后台向AVN发送更新的后台证书的url,PKI信息安全专用"),
	CMD_UP_AVN_UPGRADE_NOTIFY((short) 0x002D, "车主AVN端是否同意升级"),
	CMD_DOWN_AVN_UPGRADE_NOTIFY((short) 0x802D, "收到是否同意升级的应答"),
	CMD_UP_REMOTECTRL_EXT((short) 0x0040,"远程控制指令的执行结果"),
	CMD_DOWN_REMOTECTRL_EXT((short) 0x8040,"发送远程控制指令");

	private static Map<Short, OTACommand> codeMap = new HashMap<Short, OTACommand>();

	static {
		for (OTACommand item : OTACommand.values()) {
			codeMap.put(item.value(), item);
		}
	}

	private final short code;
	private final String message;

	private OTACommand(short code, String message) {
		this.code = code;
		this.message = message;
	}

	public static final OTACommand getByCode(short code) {
		return codeMap.get(code);
	}

	public short value() {
		return code;
	}

	public String getMessage() {
		return message;
	}

}
