package com.maxus.tsp.gateway.common.model;

import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.platform.service.model.vo.OwnerBusNichNamesVO;
import com.maxus.tsp.platform.service.model.vo.ReportPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class WarningInfo implements Cloneable {

	private static Logger logger = LogManager.getLogger(WarningInfo.class);
	//报警标题
	private String warnTitle = "";
	//报警内容
	private String warnContent = "";
	//当前车辆车架号
	private String currentVIN = "";
	//报警报文中的报警类型
	private int warnType;
	//报警报文中的报警细节
	private int warnDetail;
	//当前车辆昵称信息
	private OwnerBusNichNamesVO ownerBusInfo;
	//短信报警信息
	private String smsWarningText = "";
	//车辆绑定tbox序列号
	private String tboxsn = "";
	//报警位置
	private ReportPos rpPosData;
	//发生时间
	private String happenTime = "";
	//极光推送内容
	private String appPush = "";
	// 故障列表
	private List<Integer> fault;

	public String getWarnTitle() {
		return warnTitle;
	}

	public void setWarnTitle(String warnTitle) {
		this.warnTitle = warnTitle;
	}

	public String getWarnContent() {
		return warnContent;
	}

	public void setWarnContent(String warnContent) {
		this.warnContent = warnContent;
	}

	public String getCurrentVIN() {
		return currentVIN;
	}

	public void setCurrentVIN(String currentVIN) {
		this.currentVIN = currentVIN;
	}

	public int getWarnType() {
		return warnType;
	}

	public void setWarnType(int warnType) {
		this.warnType = warnType;
	}

	public int getWarnDetail() {
		return warnDetail;
	}

	public void setWarnDetail(int warnDetail) {
		this.warnDetail = warnDetail;
	}

	public OwnerBusNichNamesVO getOwnerBusInfo() {
		return ownerBusInfo;
	}

	public void setOwnerBusInfo(OwnerBusNichNamesVO ownerBusInfo) {
		this.ownerBusInfo = ownerBusInfo;
	}

	public String getSmsWarningText() {
		return smsWarningText;
	}

	public void setSmsWarningText(String smsWarningText) {
		this.smsWarningText = smsWarningText;
	}

	public String getTboxsn() {
		return tboxsn;
	}

	public void setTboxsn(String tboxsn) {
		this.tboxsn = tboxsn;
	}

	public ReportPos getRpPosData() {
		return rpPosData;
	}

	public void setRpPosData(ReportPos rpPosData) {
		this.rpPosData = rpPosData;
	}

	public String getHappenTime() {
		return happenTime;
	}

	public void setHappenTime(String happenTime) {
		this.happenTime = happenTime;
	}

	public String getAppPush() {
		return appPush;
	}

	public void setAppPush(String appPush) {
		this.appPush = appPush;
	}

	public List<Integer> getFault() {
		return fault;
	}

	public void setFault(List<Integer> fault) {
		this.fault = fault;
	}

	//复制警告信息对象，由于用户信息和用户车辆信息为固定数据，故只需浅克隆
	public static WarningInfo copyValueOf(WarningInfo inputWarnInfo) {
		try {
			return (WarningInfo) inputWarnInfo.clone();
		} catch (CloneNotSupportedException ex) {
			logger.error("WarningInfo clone failed.{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
			return null;
		}
	}

}
