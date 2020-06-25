package com.maxus.tsp.stress;

import java.util.HashMap;
import java.util.Map;

public enum TboxCommand {
	CMD_UP_LOGIN("0001", "上行登录"), 
	CMD_DOWN_LOGIN("8001", "下行登录"), 
	CMD_UP_HEARTBEAT("0002", "上行心跳"), 
	CMD_DOWN_HEARTBEAT("8002", "上行心跳"), 
	CMD_UP_GETPOS("0003", "上行发送当前车辆位置"), 
	CMD_DOWN_GETPOS("8003", "下行发送当前车辆位置"), 
	CMD_UP_REPORTPOS("0004", "上行报告车辆位置"), 
	CMD_DOWN_REPORTPOS("8004", "上行报告车辆位置"), 
	CMD_UP_REMOTECTRL("0005", "上行远程控制指令 "), 
	CMD_DOWN_REMOTECTRL("8005", "下行远程控制指令 "), 
	CMD_UP_REPORTCAN("0006", "上行上报车辆数据 "), 
	CMD_DOWN_REPORTCAN("8006", " 下行上报车辆数据"), 
	CMD_UP_LOGOUT("0007", "上行登出 "), 
	CMD_DOWN_LOGOUT("8007", "下行登出 "), 
	CMD_UP_REALTIME_DATA("8008", "上行实时信息上报"), 
	CMD_DOWN_REALTIME_DATA("0008", "下行实时信息上报"), 
	CMD_UP_REGISTER("0009", "上行TBox下线注册"), 
	CMD_DOWN_REGISTER("8009", "下行注册结果"), 
	CMD_UP_QCPASSED("000A", "上行表明TBox状态检查成功"), 
	CMD_DOWN_QCPASSED("800A", "下行指令处理结果"), 
	CMD_UP_UPDATE_PUBKEY("000B","上行更新TSP公钥的结果"), 
	CMD_DOWN_UPDATE_PUBKEY("800B", "下行更新TSP公钥"), 
	CMD_UP_START_DIRECT_REPORT("000C","上行国家平台发数据结果"), 
	CMD_DOWN_START_DIRECT_REPORT("800C", "下行国家平台发数据"), 
	CMD_UP_STOP_DIRECT_REPORT("000D","上行停止往国家平台发数据果"), 
	CMD_DOWN_STOP_DIRECT_REPORT("800D", "下行停止往国家平台发数据"), 
	CMD_UP_REMOTE_UPDATE("000E","上行远程更新指令执行状态"), 
	CMD_DOWN_REMOTE_UPDATE("800E", "下行远程更新");
	
	private static Map<String, TboxCommand> codeMap = new HashMap<String, TboxCommand>();

	static {
		for (TboxCommand item : TboxCommand.values()) {

			// System.out.println("item = " +item);
			codeMap.put(item.getCode(), item);
		}
	}

	private String messages;
	private String code;

	TboxCommand(String code, String messages) {
		this.code = code;
		this.messages = messages;
	}

	public static TboxCommand getByCode(String code) {

		if (code == null || code.trim().length() == 0) {
			return null;
		}
		if (codeMap.containsKey(code)) {
			return codeMap.get(code);
		}

		return null;
	}

	public String getMessages() {
		return messages;
	}

	public void setMessages(String messages) {
		this.messages = messages;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
