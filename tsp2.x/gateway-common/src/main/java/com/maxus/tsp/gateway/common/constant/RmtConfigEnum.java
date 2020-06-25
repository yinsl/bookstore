package com.maxus.tsp.gateway.common.constant;
/**
 * 远程配置相关枚举类
 * @author lzgea
 *
 */
public enum RmtConfigEnum {

	//bit 0～1：TBox休眠后定期上传位置功能开关。0：Off；1：On；2：Invalid；3：Invalid
	//bit 2～3：大数据上传功能开关。0：Off；1：On；2：Invalid；3：Invalid
	RMT_CONFIG_TYPE_STALL("1","CmdRemoteStallConfig"),//熄火上传位置时间配置字段
	RMT_CONFIG_TYPE_BIGDATA("2","CmdRemoteBigDataConfig"),//大数据上传时间配置字段
	RMT_CONFIG_TYPE_ENGDATA("4","CmdemoteEngDataConfig"),// 工程数据上传频率配置字段
	RMT_CONFIG_TYPE_STALLSETTING_SWITCH("3","CmdRemoteStallSetting"),//位置周期配置开关字段
	RMT_CONFIG_TYPE_BIGDATA_SWITCH("3","CmdRemoteBigDataSetting"),//大数据配置开关字段
	RMT_CONFIG_TYPE_ENGDATA_SWITCH("3","CmdemoteEngDataSetting"),// 工程数据配置开关字段
	RMT_CONFIG_TYPE_GETCONFIG("0","GetConfig"),//取远程配置字段
	RMT_CONFIG_SWITCH_ON("11","远程配置开关-开"),
	RMT_CONFIG_SWITCH_OFF("00","远程配置开关-关"),
	RMT_CONFIG_INVALID("00","远程配置-无效值"),// 大数据和工程数据无效值为0
	RMT_CONFIG_STALLSETTING_OFF("60","远程配置-位置上传开关-关"),  //11 11 00
	RMT_CONFIG_STALLSETTING_ON("61","远程配置-位置上传开关-开"),   //11 11 01
	RMT_CONFIG_BIGDATASETTING_ON("55","远程配置-大数据开关-开"),  //11 01 11
	RMT_CONFIG_BIGDATASETTING_OFF("51","远程配置-位置上传开关-关"),//11 00 11
	RMT_CONFIG_ENGDATASETTING_ON("31","远程配置-工程数据上传开关-开"),//01 11 11
	RMT_CONFIG_ENGDATASETTING_OFF("15","远程配置-工程数据上传开关-关");//00 11 11
	
	private String code;
	private String info;
	
	RmtConfigEnum(String code, String info){
		this.code = code;
		this.info = info;
	}

	public String getCode() {
		return code;
	}

	public String getInfo() {
		return info;
	}	
}
