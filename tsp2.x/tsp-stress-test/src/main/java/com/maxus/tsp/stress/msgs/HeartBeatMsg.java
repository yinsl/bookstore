package com.maxus.tsp.stress.msgs;

import org.springframework.stereotype.Component;

import com.maxus.tsp.common.util.StringUtil;
import com.maxus.tsp.gateway.common.constant.OTACommand;
import com.maxus.tsp.stress.BusinessSuper;
import com.maxus.tsp.stress.GroupPackage;

@Component
public class HeartBeatMsg extends BusinessSuper {
	
	private String currentDate;
	
	private String currentTime;

	public String getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(String currentDate) {
		this.currentDate = currentDate;
	}

	public String getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(String currentTime) {
		this.currentTime = currentTime;
	}

	/**
	 * 组装报文
	 * 
	 * @param sn
	 * @param encyptMode
	 * @param seqNum
	 * @return
	 */
	public String getHeartBeatPackage(String sn, String encyptMode, String seqNum) {
		String command = String.valueOf(OTACommand.CMD_UP_HEARTBEAT.value());
		GroupPackage gp = new GroupPackage();
		String param = StringUtil.getHexDateString();
		String result = gp.getMessagess(sn, encyptMode, command, seqNum, param);
		return result;
	}

	/**
	 * 解析报文
	 */
	@Override
	public String resolveMessage(String message) {
		String result = null;
		if (message.length() == 14) {
			// 上行报文
			result = "\n\t 当前时间 :  " + StringUtil.hextoDateString(message);
		} else {
			// 下行报文
			result = "\n\t 当前时间 :  " + StringUtil.hextoDateString(message);
		}
		return result;
	}
	
	public static void main(String[] args) {
		HeartBeatMsg hb = new HeartBeatMsg();
		
		String  bp = hb.getHeartBeatPackage(" 0000000070596256", "0", "1");
		
		System.out.println("心跳包：" + bp);
	}

}