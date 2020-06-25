/**
 * 设置当前sn号对应版本，并根据sn号判断是否支持某一版本的具体功能
 * Copyright (c) 2018年3月26日 by 上汽集团商用车技术中心
 * @author 任怀宇
 * @version 1.0
 */
package com.maxus.tsp.gateway.common.constant;

import java.util.HashMap;
import java.util.Map;


public enum OtaVersionFeature {

	REPROTCAN((short) 0X0003, "车况"), REGISTER((short) 0X0005, "登录"), QCPASSED((short) 0X0006, "TBOX状态检查,UPDATE_PUBKEY"),
	UPDATEPUBKEY((short) 0X0007, "更新公钥"), STARTDIRECTREPORT((short) 0X0008, "往国家平台发数据"),
	REMOTEUPDATE((short) 0X0009, "远程更新"), DOORSTATUS((short) 0X000B, "远程更新"), UP_WARN((short) 0X000B, "上传报警信息"),
	POWERBATTSTATUS((short) 0X000B, "动力电池状态"), STATUS_TAIL_DOOR((short) 0X000C, "尾门"),
	WARNINGTAILDOOR((short) 0X000D, "尾门报警"), WARNINGLANEDEPARTURE((short) 0X000E, "车道偏离报警"),
	VINLOGIN((short) 0X000F, "登录支持VIN、ICCID、BATTERYNUM、BATTERYCODE"), FORWARD4IVI((short) 0X0010, "透传指令"),
	BTKEY((short) 0X0011, "支持蓝牙"), GBPLATFORM((short) 0X0012, "支持国标平台"), POWERBATTTEMP((short) 0X0014, "引擎电池温度"),
	BIGDATA((short) 0X0015, "大数据"), REALDATEEXTERMUM((short) 0X0016, "实时数据"), SPEEDLIMITCMD((short) 0X0017, "限速指令"),
	EXTDATA((short) 0X0020, "扩展车况数据"), EARLYWARNING((short) 0X001F, "支持危险行为预警"),
	RMTGROUP_ENGINE_AND_TRUNK((short) 0X0026, "组合远控支持后备箱解锁及发动机启动"), NIODATA((short) 0XC812, "蔚来版本"),
	FOTA_BASE((short) 0X0100, "fota版本");

	private final short code;
	private final String desc;

	OtaVersionFeature(short code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	private static Map<Short, OtaVersionFeature> codeMap = new HashMap<>();

	static {
		for (OtaVersionFeature item : OtaVersionFeature.values()) {
			codeMap.put(item.code, item);
		}

	}

	// key:sn号 value：版本号
	private static Map<String, String> tboxMsg = new HashMap<>();

	public short getCode() {
		return code;
	}

	/**
	 * @Description: set tboxSn和对应的otaVersion
	 * @param tboxSn
	 * @param otaVersion
	 * @return
	 */
	public static void setVersion(String tboxSn, String otaVersion) {
		tboxMsg.put(tboxSn, otaVersion);
	}

	/**
	 * @Description: get tboxSn对应的otaVersion
	 * @param tboxSn
	 * @return String
	 */
	public static String getVersion(String tboxSn) {
		return tboxMsg.get(tboxSn);
	}

	/**
	 * @Description: 确认当前tbox是否支持本OTA feature 版本
	 * @param tboxSn
	 * @return boolean
	 */
	public boolean isSupported(String tboxSn) {
		// tboxSn 对应的ota协议版本
		String correspondingVersion = getVersion(tboxSn);
		// 若tboxSn对应的otaVersion为空，返回false
		if (correspondingVersion == null) {
			return false;
		}
		//若sn版本为C812(200.18)使用Integer解析 其他版本使用Short解析版本
		if("C812".equalsIgnoreCase(correspondingVersion)){
			return Integer.parseInt(correspondingVersion, 16) - Short.toUnsignedInt(this.getCode()) >= 0;
		}else{
			return Short.parseShort(correspondingVersion, 16) - Short.toUnsignedInt(this.getCode()) >= 0;
		}
		// 若给定版本不小于tboxSn对应版本，返回true；否则，返回else
		// return Integer.parseInt(correspondingVersion, 16) - Short.toUnsignedInt(this.getCode()) >= 0;
	}
}
