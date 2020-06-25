package com.maxus.tsp.gateway.common.constant;

public class OTABlueToothCtrlConstant {

	//挑战鉴权钥匙长度
	public static final int AUTHKEY_LENGTH = 16;
	//long（开始结束时间）时间默认值
	public static final int INVALID_TIME = 0;
	//验证码长度
	public static final int VERIFICATION_CODE_LENGTH = 8;
	//增删tbox蓝牙返回结果最大值
	public static final int ADD_DEL_KEY_TBOX_RESULT_MAX_NO = 5;
	//增删tbox蓝牙返回结果字节数
	public static final int ADD_DEL_KEY_TBOX_RESULT_LEN = 1;
	//增删tbox蓝牙返回结果偏移
	public static final int ADD_DEL_KEY_TBOX_RESULT_OFFSET = 0;
	//单个蓝牙钥匙ID长度
	public static final int BTKEY_ID_LENGTH = 4;
	//获取tbox 蓝牙信息固定字节
	public static final int GET_BTKEY_FIX_BYTE = 3;
	//蓝牙控制的控制指令
	//新增蓝牙钥匙
	public static final String COMMAND_ADD_BT_KEY = "AddBTKey";
	//删除蓝牙钥匙
	public static final String COMMAND_DEL_BT_KEY = "DelBTKey";
	//获取蓝牙钥匙
	public static final String COMMAND_GET_BT_KEY = "GetBTKey";
	//发送蓝牙钥匙验证码
	public static final String COMMAND_SEND_VERIFICATION_CODE = "SendVerificationCode";

}
