package com.maxus.tsp.gateway.controller;
//
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Hashtable;
//
//import com.maxus.tsp.gateway.service.DataProcessing;
//import com.maxus.tsp.gateway.service.TboxService;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.lang3.time.DateFormatUtils;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Scope;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.alibaba.fastjson.JSONObject;
//import com.maxus.tsp.common.constant.KafkaMsgConstant;
//import com.maxus.tsp.common.enums.ResultStatus;
//import com.maxus.tsp.common.util.ThrowableUtil;
//import com.maxus.tsp.gateway.common.constant.OperationConstant;
//import com.maxus.tsp.gateway.common.constant.RmtConfigEnum;
//import com.maxus.tsp.gateway.common.model.RemoteControlItRequest;
//import com.maxus.tsp.gateway.common.model.RemoteCtrlRespInfo;
//import com.maxus.tsp.gateway.common.model.RemoteCtrlResponseData;
//import com.maxus.tsp.gateway.common.ota.GlobalSessionChannel;
//import com.maxus.tsp.gateway.mq.kafka.KafkaProducer;
//import com.maxus.tsp.gateway.serviceclient.TspPlatformClient;
//import com.maxus.tsp.platform.service.model.AppJsonResult;
//import com.maxus.tsp.platform.service.model.vo.BaseCar;
//
///**
// * 远程配置Tbox
// *
// * @author lzgea
// *
// */
//@RestController
//@RequestMapping(value = "/tsp/api")
//@Scope(value = "prototype")
public class TboxRemoteConfigCtrl {
//	private final Logger logger = LogManager.getLogger(getClass());
//
//	@Autowired
//	private KafkaProducer kafkaService;
//
//	@Autowired
//	private TboxService tboxService;
//
//	@Value("${wakeUpDelayTime:10}")
//	private int wakeUpDelayTime;
//
//	@Autowired
//	private TspPlatformClient tspPlatformClient;
//
//	public void setTspPlatformClient(TspPlatformClient inputPlatformClient, KafkaProducer inputkafkaService) {
//		tspPlatformClient = inputPlatformClient;
//		kafkaService = inputkafkaService;
//	}
//
//	/**
//	 * Tbox响应的报文处理结果
//	 */
//	private String result;
//
//	/**
//	 * 远程配置data
//	 */
//	private String data;
//	/**
//	 * 远程配置对应的车辆车架号
//	 */
//	private String vin;
//
//	// 远程配置信息
//	private RemoteControlItRequest tboxRemoteConfigInfo;
//
//	private RemoteCtrlRespInfo remoteConfigInfo;
//
//	/**
//	 * @param result
//	 *            the result to set
//	 */
//	public void setResult(String result) {
//		this.result = result;
//	}
//
//	/**
//	 * @param data
//	 *            the data to set
//	 */
//	public void setData(String data) {
//		this.data = data;
//	}
//
//	public static TboxRemoteConfigCtrl getRemoteConfigCtrl(String tboxsn) {
//		return htRemoteConfig.get(tboxsn);
//	}
//
//	// 解决对在线Tbox远控请求,回复周期内（10s）Tbox频繁登录登出造成的线程通知错误；
//	private boolean tboxOnlineStatusWhenCtrl = false;
//
//	public boolean getTboxOnlineStatusWhenCtrl() {
//		return tboxOnlineStatusWhenCtrl;
//	}
//
//	/**
//	 * 保存本节点处理远程配置请求的实例
//	 */
//	private static Hashtable<String, TboxRemoteConfigCtrl> htRemoteConfig = new Hashtable<>();
//	// 远程配置接口
//	@RequestMapping(value = "/remoteConfig", method = RequestMethod.POST)
//	public String remoteConfig(@RequestBody RemoteControlItRequest remoteConfigInfo) {
//		String rmtConfigResult = "";
//		if (remoteConfigInfo == null) {
//			logger.warn("远程配置参数不能为空.");
//			// 远程配置参数不能为空
//			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
//		} else {
//			tboxRemoteConfigInfo = new RemoteControlItRequest();
//			tboxRemoteConfigInfo = remoteConfigInfo;
//			logger.info("Enter /remoteConfig: comd is {}, value is {} , sn is{} , eventTime is {}," + "seqNum is {}",
//					remoteConfigInfo.getComd(), remoteConfigInfo.getValue(), remoteConfigInfo.getSn(),
//					remoteConfigInfo.getEventTime(), remoteConfigInfo.getSeqNo());
//			rmtConfigResult = doRemoteConfig(remoteConfigInfo.getComd(), remoteConfigInfo.getValue(),
//					remoteConfigInfo.getSn(), remoteConfigInfo.getEventTime(), remoteConfigInfo.getSeqNo(), null);
//		}
//		logger.info("Return remoteConfig: {}", rmtConfigResult);
//		return rmtConfigResult;
//	}
//
//	/**
//	 *
//	 * @param comd
//	 * @param value
//	 * @param serialNum
//	 * @param eventTime
//	 * @param seqNo
//	 * @param vin
//	 * @return
//	 */
//	@RequestMapping(value = "/remoteConfigRVM", method = RequestMethod.GET)
//	public AppJsonResult remoteConfigRVM(String comd, String value, String serialNum, long eventTime, String seqNo,
//			String vin) {
//		String ret = doRemoteConfig(comd, value, serialNum, eventTime, seqNo, vin);
//		tboxRemoteConfigInfo = new RemoteControlItRequest();
//		AppJsonResult result = JSONObject.parseObject(ret, AppJsonResult.class);
//		return result;
//	}
//
//	@RequestMapping(value = "/doremoteConfig", method = RequestMethod.GET)
//	public String doRemoteConfig(String comd, String value, String serialNum, long eventTime, String seqNo,
//			String vin) {
//
//		remoteConfigInfo = new RemoteCtrlRespInfo();
//		remoteConfigInfo.setComd(comd);
//		remoteConfigInfo.setValue(value);
//		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		remoteConfigInfo.setReqTime(format.format(new Date(eventTime)));
//
//		String reqSN = serialNum;
//		String reqComd = comd;
//		String reqValue = value;
//		long reqTime = eventTime;
//		String reqSeqNo = seqNo;
//		String rect = null;
//		int idCode = 0;
//
//		try {
//			// 改装车
//			long reqNumber = Long.parseUnsignedLong(reqValue);
//			if (StringUtils.isBlank(vin)) {
//				// 如果vin为空再检查serialNumber参数是否为空(兼容老接口)
//				if (StringUtils.isBlank(reqSN)) {
//					logger.warn("远程控制(远程配置)车架号或vin号都为空.");
//					return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
//				}
//			} else {
//				// 根据vin获取serialNumber
//				this.vin = vin;
//				BaseCar bc = tspPlatformClient.selectBaseCarByVIN(vin);
//				if (bc == null) {
//					logger.warn("远程控制（远程配置）vin参数在数据库中不存在。 vin {}", vin);
//					return JSONObject.toJSONString(new AppJsonResult(ResultStatus.VIN_ERROR, ""));
//				}
//
//				reqSN = bc.getSn();
//			}
//
//			// 检查参数是否为空
//			if (StringUtils.isBlank(reqSN)) {
//				logger.warn("远程配置参数错误：sn为空.");
//				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
//			}
//			if (StringUtils.isBlank(reqComd)) {
//				logger.warn("远程配置参数错误：Comd为空.");
//				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
//			}
//			if (StringUtils.isBlank(reqValue)) {
//				logger.warn("远程配置参数错误：value为空.");
//				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
//			}
//			if (reqNumber > OperationConstant.REMOTE_CONFIG_REQ_MAX_VALUE) {
//				logger.warn("远程配置参数错误：value非法.");
//				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, ""));
//			}
//			if (StringUtils.isBlank(String.valueOf(reqTime))) {
//				logger.warn("远程配置参数错误：eventTime为空.");
//				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
//			}
//			if (StringUtils.isBlank(reqSeqNo)) {
//				logger.warn("远程配置参数错误：seqNo为空.");
//				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
//			}
//			// cmd:1.熄火后上传周期[0,2880]；2.大数据上传频率；3.tbox开关；4.工程数据0.请求获取远程配置
//			int valueNum = Integer.parseUnsignedInt(reqValue);
//			switch (reqComd) {
//			case "CmdRemoteStallConfig":
//				if (valueNum<0 || valueNum>2880) {
//					logger.warn("TBox(SN:{}) cmd:{} value:{} 远程配置请求参数错误！", reqSN, reqComd, reqValue);
//					return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
//				}
//				idCode = Integer.parseInt(RmtConfigEnum.RMT_CONFIG_TYPE_STALL.getCode());
//				rect = doRmtConfigCtrl(reqSN, reqValue, reqTime, reqSeqNo, idCode, reqComd);
//				return rect;
//			case "CmdRemoteBigDataConfig":
//				if (valueNum<=0 || valueNum>60) {
//					logger.warn("TBox(SN:{}) cmd:{} value:{} 远程配置请求参数错误！", reqSN, reqComd, reqValue);
//					return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
//				}
//				idCode = Integer.parseInt(RmtConfigEnum.RMT_CONFIG_TYPE_BIGDATA.getCode());
//				if (reqValue.equals(RmtConfigEnum.RMT_CONFIG_INVALID.getCode())) {
//					logger.warn("TBox(SN:{})远程配置指令(CmdRemoteBigDataConfig)参数为无效值0。", reqSN);
//					return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, ""));
//				}
//				rect = doRmtConfigCtrl(reqSN, reqValue, reqTime, reqSeqNo, idCode, reqComd);
//				return rect;
//			case "CmdRemoteStallSetting":
//				idCode = Integer.valueOf(RmtConfigEnum.RMT_CONFIG_TYPE_STALLSETTING_SWITCH.getCode());
//				if (reqValue.equals(RmtConfigEnum.RMT_CONFIG_SWITCH_OFF.getCode())) {
//					reqValue = RmtConfigEnum.RMT_CONFIG_STALLSETTING_OFF.getCode();
//				} else if (reqValue.equals(RmtConfigEnum.RMT_CONFIG_SWITCH_ON.getCode())) {
//					reqValue = RmtConfigEnum.RMT_CONFIG_STALLSETTING_ON.getCode();
//				} else {
//					return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, ""));
//				}
//				rect = doRmtConfigCtrl(reqSN, reqValue, reqTime, reqSeqNo, idCode, reqComd);
//				return rect;
//			case "CmdRemoteBigDataSetting":
//				idCode = Integer.valueOf(RmtConfigEnum.RMT_CONFIG_TYPE_BIGDATA_SWITCH.getCode());
//				if (reqValue.equals(RmtConfigEnum.RMT_CONFIG_SWITCH_OFF.getCode())) {
//					reqValue = RmtConfigEnum.RMT_CONFIG_BIGDATASETTING_OFF.getCode();
//
//				} else if (reqValue.equals(RmtConfigEnum.RMT_CONFIG_SWITCH_ON.getCode())) {
//					reqValue = RmtConfigEnum.RMT_CONFIG_BIGDATASETTING_ON.getCode();
//				} else {
//					return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, ""));
//				}
//				rect = doRmtConfigCtrl(reqSN, reqValue, reqTime, reqSeqNo, idCode, reqComd);
//				return rect;
//			// 获取远程配置
//			case "GetConfig":
//				idCode = Integer.valueOf(RmtConfigEnum.RMT_CONFIG_TYPE_GETCONFIG.getCode());
//				if (!reqValue.equals(RmtConfigEnum.RMT_CONFIG_SWITCH_ON.getCode())) {
//					return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, ""));
//				}
//				rect = doRmtConfigCtrl(reqSN, reqValue, reqTime, reqSeqNo, idCode, reqComd);
//				return rect;
//			case "CmdRemoteEngDataConfig":
//				if (valueNum<=0 || valueNum>60) {
//					logger.warn("TBox(SN:{}) cmd:{} value:{} 远程配置请求参数错误！", reqSN, reqComd, reqValue);
//					return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
//				}
//				idCode = Integer.valueOf(RmtConfigEnum.RMT_CONFIG_TYPE_ENGDATA.getCode());
//				if (reqValue.equals(RmtConfigEnum.RMT_CONFIG_INVALID.getCode())) {
//					logger.warn("TBox(SN:{})远程配置指令(CmdRemoteEngDataConfig)参数为无效值0。", reqSN);
//					return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, ""));
//				}
//				rect = doRmtConfigCtrl(reqSN, reqValue, reqTime, reqSeqNo, idCode, reqComd);
//				return rect;
//			case "CmdRemoteEngDataSetting":
//				idCode = Integer.valueOf(RmtConfigEnum.RMT_CONFIG_TYPE_ENGDATA_SWITCH.getCode());
//				if (reqValue.equals(RmtConfigEnum.RMT_CONFIG_SWITCH_OFF.getCode())) {
//					reqValue = RmtConfigEnum.RMT_CONFIG_ENGDATASETTING_OFF.getCode();
//
//				} else if (reqValue.equals(RmtConfigEnum.RMT_CONFIG_SWITCH_ON.getCode())) {
//					reqValue = RmtConfigEnum.RMT_CONFIG_ENGDATASETTING_ON.getCode();
//				} else {
//					return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, ""));
//				}
//				rect = doRmtConfigCtrl(reqSN, reqValue, reqTime, reqSeqNo, idCode, reqComd);
//				return rect;
//			default:
//				logger.warn("远程配置指令错误 comd:{}, tbox {}。", reqComd, reqSN);
//				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, ""));
//			}
//		} catch (Exception e) {
//			logger.error(
//					"远程配置因发生异常失败。 TBox {}, 异常：{}", reqSeqNo, ThrowableUtil.getErrorInfoFromThrowable(e));
//			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
//		}
//	}
//
//	// 非异步执行远程配置
//	private String doRmtConfigCtrl(String reqSN, String reqValue, long reqTime, String reqSeqNo, int idCode,
//			String reqComd) {
//
//		// Tbox是否已经唤醒
//		boolean doWakeUp = false;
//		boolean existRequest = false;
//		try {
//			// 首先得确认sn是合法的tbox编号
//			if (!DataProcessing.isTboxValid(reqSN)) {
//				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAR_INFO_ERROR, ""));
//			}
//
//			if (this.vin == null) {
//				this.vin = tboxService.getVINForTbox(reqSN);
//			}
//
//			// 查询唤醒清单中是否已经存在正在唤醒的请求
//			existRequest = DataProcessing.isRmtConfigExist(reqSN, this.vin, reqComd);
//			if (existRequest) {
//				// 提示用户不能进行操作
//				logger.warn("TBox {} 正在执行该远程配置指令，不接受本次配置，本次配置指令为：{}。", reqSN, reqComd);
//				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.TBOX_HAVE_ACCEPT_ONE_COMMAND, ""));
//			}
//			// TBox是否在线
//			doWakeUp = !DataProcessing.onlineTboxExistCheck(reqSN);
//
//		} catch (Exception e) {
//			logger.error(
//					"远程配置因发生异常失败。 TBox {}, 异常：{}", reqSN, ThrowableUtil.getErrorInfoFromThrowable(e));
//			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
//		}
//
//		htRemoteConfig.put(reqSN, TboxRemoteConfigCtrl.this);
//
//		// 需要唤醒
//		if (doWakeUp) {
//
//			// 检查TBOX是否符合唤醒条件
//			checkTBoxLogOutForWakeupWait(reqSN);
//
//			// 将TBOX添加到唤醒队列中
//			GlobalSessionChannel.addWakeUp(reqSN);
//
//			logger.info("离线TBox {}的唤醒及远程配置请求可以执行 ", reqSN);
//			logger.info("开始异步发送唤醒短信并等待TBox {}登陆返回处理结果。 ", reqSN);
//
//			if (!DataProcessing.isSendingMessageSucceed(reqSN)) {
//				// 唤醒失败直接通知
//				logger.warn("TBox {}的远程配置因唤醒失败而失败 ", reqSN);
//				GlobalSessionChannel.removeWakeUp(reqSN);
//				htRemoteConfig.remove(reqSN);
//				return JSONObject
//						.toJSONString(new AppJsonResult(ResultStatus.RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED, ""));
//			} else {
//				// 执行等待
//				logger.info("远程配置流程唤醒短信发送成功并开始等待TBox {}登陆。 ", reqSN);
//
//				long startTime = 0;
//				try {
//					startTime = System.currentTimeMillis();
//					synchronized (TboxRemoteConfigCtrl.this) {
//						TboxRemoteConfigCtrl.this.wait(OperationConstant.WAKEUP_WAIT_TIME);
//					}
//				} catch (InterruptedException e) {
//					// 等待唤醒中途如果失败，就必须清除记录，并且
//					logger.error("远程配置因发生异常失败。 TBox {}, 异常：{}", reqSN,
//							ThrowableUtil.getErrorInfoFromThrowable(e));
//					htRemoteConfig.remove(reqSN);
//					return JSONObject
//							.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
//				} finally {
//					GlobalSessionChannel.removeWakeUp(reqSN);
//				}
//
//				// 如果超时，则代表唤醒失败了，直接通知用户
//				if (System.currentTimeMillis() - startTime >= OperationConstant.WAKEUP_WAIT_TIME) {
//					logger.warn("远程配置失败，因为 TBox {}执行唤醒后未上线。", reqSN);
//					htRemoteConfig.remove(reqSN);
//					return JSONObject
//							.toJSONString(new AppJsonResult(ResultStatus.RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG, ""));
//				} else {
//					// TBOX在线
//					logger.info("TBox {}上线了，远程配置请求可以执行 ", reqSN);
//					// 继续等远程控制指令响应
//					result = doRemoteConfigWhenTboxOnline(reqSN, reqValue, reqTime, reqSeqNo, idCode, reqComd);
//				}
//			}
//
//		} else {
//
//			// TBOX在线
//			logger.info("在线TBox {}的远程配置请求可以执行 ", reqSN);
//			// 继续等远程控制指令响应
//			result = doRemoteConfigWhenTboxOnline(reqSN, reqValue, reqTime, reqSeqNo, idCode, reqComd);
//		}
//
//		return result;
//
//	}
//
//	/**
//	 * TBox在线执行远程配置指令
//	 *
//s	 *            reqSN,String reqValue,String reqTime,String reqSeqNo,int
//	 *            idCode
//	 * @return
//	 */
//	private String doRemoteConfigWhenTboxOnline(String reqSN, String reqValue, long reqTime, String reqSeqNo,
//			int idCode, String reqComd) {
//
//		String rcresult = "";
//		String serialNumber = reqSN;
//		String stateValue = "";
//		String returnData = "";
//		RemoteCtrlResponseData param = new RemoteCtrlResponseData();
//
//		try {
//			// redis是否记录此次远程配置
//			//GlobalSessionChannel.addRemoteConfigCtrl(reqSN, reqComd);
//			GlobalSessionChannel.addCommandSend(reqSN, reqComd);
//			logger.info("记录此次远程配置至Redis：TBoxSn:{},Value:{}", serialNumber,
//					JSONObject.toJSONString(tboxRemoteConfigInfo));
//			// TBox上线，将其移除唤醒
//			if (GlobalSessionChannel.existWakeUp(serialNumber)) {
//				GlobalSessionChannel.removeWakeUp(serialNumber);
//			}
//			// 处理long 型 dateTime
//			String dateTime = DateFormatUtils.format(reqTime, "yyyy-MM-dd HH:mm:ss");
//			// 这里得通过kafka生产一个远程配置指令下发
//			kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_DOWN_REMOTE_CONFIG,
//					serialNumber + "_" + idCode + "_" + reqValue + "_" + dateTime, serialNumber);
//			// 默认的下发时间
//			String downDate = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
//
//			// 发送远程控制命令后，等待回包10s
//			long startTime = 0;
//			startTime = System.currentTimeMillis();
//			System.out.println("消息发送完毕等待回包~");
//
//			tboxOnlineStatusWhenCtrl = true;
//			synchronized (TboxRemoteConfigCtrl.this) {
//				// 等10s
//				TboxRemoteConfigCtrl.this.wait(OperationConstant.REMOTE_CONFIG_RESPONSE_WAIT_TIME);
//			}
//
//			if (System.currentTimeMillis() - startTime >= OperationConstant.REMOTE_CONFIG_RESPONSE_WAIT_TIME) {
//				// 超过10s回复结果
//				logger.warn("远程控制失败，因为 TBox: {}没有及时回复远程控制报文。请求时间：{}", serialNumber, downDate);
//				// 更新远程控制下发时间
//				rcresult = JSONObject
//						.toJSONString(new AppJsonResult(ResultStatus.RM_TBOX_NOT_REPLY_WITHIN_LIMIT_TIME, ""));
//			} else {
//				stateValue = htRemoteConfig.get(serialNumber).result;
//				returnData = htRemoteConfig.get(serialNumber).data;
//				param = JSONObject.parseObject(returnData, RemoteCtrlResponseData.class);
//				logger.info("TBox {} 已经回复远程控制(远程配置)结果：{}", serialNumber, stateValue);
//				if (stateValue.equals("0")) {
//					rcresult = JSONObject.toJSONString(new AppJsonResult(ResultStatus.SUCCESS, param));
//				} else {
//					rcresult = JSONObject.toJSONString(
//							new AppJsonResult(ResultStatus.RM_REMOTE_CONTROL_CANNOT_DONE_FOR_OTHER_REASONS, param));
//				}
//			}
//
//		} catch (Exception e) {
//			logger.error("远程配置因发生异常失败。 TBox {}, 异常：{}", serialNumber,
//					ThrowableUtil.getErrorInfoFromThrowable(e));
//			rcresult = JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
//		} finally {
//			// 可以获取到本次远程指令的处理结果了
//			htRemoteConfig.remove(serialNumber);
//			if (GlobalSessionChannel.existCommandSend(serialNumber)) {
//				GlobalSessionChannel.removeCommandSend(serialNumber);
//			}
//		}
//		return rcresult;
//	}
//
//	/**
//	 * 如果该TBOX登出距现在不足10S则等待到指定时间在
//	 *
//	 * @param serialNumber
//	 */
//	private void checkTBoxLogOutForWakeupWait(String serialNumber) {
//		// 获取tbox最新的LOGOUT报文形式登出时间
//		String lastLogoutTime = DataProcessing.logoutTboxGet(
//				serialNumber)/* GlobalSessionChannel.getTboxLogout(tboxsn) */;
//		Date lastLogoutDT;
//		if (lastLogoutTime != null) {
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			// 计算时间是否相差5s，是则退出，否则sleep相应时间差
//			try {
//				lastLogoutDT = sdf.parse(lastLogoutTime);
//				Calendar curCalendar = Calendar.getInstance();
//				logger.info("当前操控请求的原始时间:{}", sdf.format(curCalendar.getTime()));
//				curCalendar.add(Calendar.SECOND, -wakeUpDelayTime);
//				logger.info("请求时间计算偏移后结果为:{}", sdf.format(curCalendar.getTime()));
//				if (lastLogoutDT.compareTo(curCalendar.getTime()) > 0) {
//					// 需要计算差值时间进行sleep
//					long diff = lastLogoutDT.getTime() - curCalendar.getTime().getTime();
//					logger.warn("由于TBox {}刚发送登出报文，唤醒请求将延迟  {}毫秒", serialNumber, diff);
//					Thread.sleep(diff);
//					logger.info("TBox {}结束唤醒请求延迟等待，将执行唤醒。", serialNumber);
//				}
//
//			} catch (Exception ex) {
//				logger.error("确认延迟唤醒流程发生异常: {}", ThrowableUtil.getErrorInfoFromThrowable(ex));
//				return;
//			}
//		}
//		return;
//
//	}
}
