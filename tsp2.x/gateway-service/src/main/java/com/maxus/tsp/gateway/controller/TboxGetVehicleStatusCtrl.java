package com.maxus.tsp.gateway.controller;
//
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Hashtable;
//
//import com.maxus.tsp.gateway.service.DataProcessing;
//import org.apache.commons.lang.StringUtils;
//import org.apache.commons.lang3.time.DateFormatUtils;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Scope;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.alibaba.fastjson.JSONObject;
//import com.maxus.tsp.common.constant.KafkaMsgConstant;
//import com.maxus.tsp.common.constant.RedisConstant;
//import com.maxus.tsp.common.enums.ResultStatus;
//import com.maxus.tsp.common.redis.allredis.RedisAPI;
//import com.maxus.tsp.common.util.ThrowableUtil;
//import com.maxus.tsp.gateway.common.constant.OperationConstant;
//import com.maxus.tsp.gateway.common.model.RemoteCtrlResponseData;
//import com.maxus.tsp.gateway.common.ota.GlobalSessionChannel;
//import com.maxus.tsp.gateway.mq.kafka.KafkaProducer;
//import com.maxus.tsp.platform.service.model.AppJsonResult;
//
//@RestController
//@RequestMapping(value = "/tsp/appapi")
//@Scope(value = "prototype")
public class TboxGetVehicleStatusCtrl {
//	private final Logger logger = LogManager.getLogger(getClass());
//
//	@Value("${wakeUpDelayTime:10}")
//	private int wakeUpDelayTime;
//	@Autowired
//	private KafkaProducer kafkaService;
//	@Autowired
//	private RedisAPI redisAPI;
//
//	/**
//	 * Tbox响应的报文处理结果
//	 */
//	private String result;
//
//	private String data;
//
//	public void setData(String data) {
//		this.data = data;
//	}
//
//	public String getData() {
//		return data;
//	}
//
//	/**
//	 * @param result
//	 *            the result to set
//	 */
//	public void setResult(String result) {
//		this.result = result;
//	}
//	public String getResult(){
//		return this.result;
//	}
//
//	// 解决对在线Tbox远控请求,回复周期内（10s）Tbox频繁登录登出造成的线程通知错误；
//	private boolean tboxOnlineStatusWhenCtrl = false;
//
//	public boolean getTboxOnlineStatusWhenCtrl() {
//		return tboxOnlineStatusWhenCtrl;
//	}
//
//	public static Hashtable<String, TboxGetVehicleStatusCtrl> tboxGetVehicleStatus = new Hashtable<>();
//	public static TboxGetVehicleStatusCtrl getTboxGetVehicleStatus(String sn){
//		return tboxGetVehicleStatus.get(sn);
//	}
//
//	@RequestMapping(value = "/tboxGetVehicleStatusRvm", method = RequestMethod.GET)
//	public AppJsonResult tboxGetVehicleStatus(@RequestParam("sn") String sn, @RequestParam("value") String value,  @RequestParam("eventTime") long eventTime){
//		String result = "";
//		// 执行参数检查
//		if (StringUtils.isBlank(value) || StringUtils.isBlank(sn)) {
//			logger.warn("获取车况失败：SN或者value为空！(sn:{},value:{})", sn, value);
//			return new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
//		}
//		try {
//			String[] requestID = StringUtils.split(value, ",");
//			System.out.println(requestID.length);
//			for(String id: requestID){
//				if(StringUtils.isBlank(id)) {
//					logger.warn("TBox(SN:{}):请求ID有误；id:{} ", sn, value);
//					return new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, "");
//				}
//			}
//		} catch (Exception ex) {
//			logger.error("TBox SN:{},错误原因：{}", sn, ThrowableUtil.getErrorInfoFromThrowable(ex));
//			return new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, "");
//		}
//
//		String tboxOnlineCheck = initCheck(sn);
//		if (!StringUtils.isBlank(tboxOnlineCheck)){
//			return JSONObject.parseObject(tboxOnlineCheck, AppJsonResult.class);
//		}else{
//			result = doWhenTboxOnline(sn, value, eventTime);
//		}
//		return JSONObject.parseObject(result, AppJsonResult.class);
//	}
//
//	private String doWhenTboxOnline(String sn, String value, long eventTime) {
//		String serialNum = sn;
//		String reqValue = value;
//		String rectResult = "";
//		try {
//			//GlobalSessionChannel.addGetVehicleStatusCtrl(serialNum, reqValue);
//			GlobalSessionChannel.addCommandSend(serialNum, reqValue);
//			logger.info("记录此次获取车况请求至Redis：TBoxSn:{},Value:{}", serialNum, value);
//			if (GlobalSessionChannel.existWakeUp(serialNum)) {
//				GlobalSessionChannel.removeWakeUp(serialNum);
//			}
//			String dateTime = DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd HH:mm:ss");
//			kafkaService.sndMesForTemplate(KafkaMsgConstant.GET_VEHICLE_STATUS_DOWN_CTRL, sn + "_" + value + "_" + dateTime, sn);
//			// 默认的下发时间
//			String downDate = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
//			// 发送远程控制命令后，等待回包10s
//			long startTime = 0;
//			startTime = System.currentTimeMillis();
//			tboxOnlineStatusWhenCtrl = true;
//			synchronized (TboxGetVehicleStatusCtrl.this) {
//				// 等10s
//				TboxGetVehicleStatusCtrl.this.wait(OperationConstant.REMOTE_CONFIG_RESPONSE_WAIT_TIME);
//			}
//			if (System.currentTimeMillis() - startTime >= OperationConstant.REMOTE_CONFIG_RESPONSE_WAIT_TIME) {
//				// 超过10s回复结果
//				logger.warn("获取车况失败，因为 TBox:{}没有及时回复。请求时间：{}", sn, downDate);
//				rectResult = JSONObject
//						.toJSONString(new AppJsonResult(ResultStatus.RM_TBOX_NOT_REPLY_WITHIN_LIMIT_TIME, ""));
//			} else {
//				String[] result = StringUtils.split(data, "@");
//				if(result[0].equals("0")){
//					rectResult = JSONObject
//							.toJSONString(new AppJsonResult(ResultStatus.SUCCESS, JSONObject.parseObject(result[2], RemoteCtrlResponseData.class)));
//				} else {
//					rectResult = JSONObject
//							.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, result[2]));
//				}
//			}
//		} catch(Exception ex) {
//			logger.error("获取车况因发生异常失败。 TBox {}, 异常：{}", sn,
//					ThrowableUtil.getErrorInfoFromThrowable(ex));
//			rectResult = JSONObject
//					.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
//		} finally{
//			try{
//				tboxGetVehicleStatus.remove(serialNum);
//				if(GlobalSessionChannel.existCommandSend(serialNum)){
//					GlobalSessionChannel.removeCommandSend(serialNum);
//				}
//			}catch(Exception ex) {
//				logger.error("TBox(SN:{})删除远程车况发生异常！原因：{}", serialNum, ThrowableUtil.getErrorInfoFromThrowable(ex));
//			}
//		}
//		return rectResult;
//	}
//
//	public String initCheck(String sn){
//		String tboxOnlineCheck = "";
//		if (tboxGetVehicleStatus.containsKey(sn)|| DataProcessing.isGetVehicleStatusExist(sn)){
//			logger.warn("TBox(SN:{}):该TBox已经存在一条唤醒或正在执行的指令", sn);
//			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.RM_TBOX_HAVE_ACCEPT_ONE_COMMAND, ""));
//		} else {
//			boolean doWakeUp = !redisAPI.hasKey(RedisConstant.ONLINE_TBOX, sn);
//			tboxGetVehicleStatus.put(sn, TboxGetVehicleStatusCtrl.this);
//			if (doWakeUp) {
//				checkTBoxLogOutForWakeupWait(sn);
//				GlobalSessionChannel.addWakeUp(sn);
//				logger.info("TBox(SN:{}):离线唤醒及获取车况请求可以执行！ ", sn);
//				logger.info("TBox(SN:{}):获取车况请求-开始异步发送唤醒短信并等待登陆返回处理结果。 ", sn);
//				if (!DataProcessing.isSendingMessageSucceed(sn)) {
//					logger.info("TBox(SN:{}):蓝牙配置-短信发送失败。 ", sn);
//					GlobalSessionChannel.removeWakeUp(sn);
//					tboxGetVehicleStatus.remove(sn);
//					return JSONObject.toJSONString(
//							new AppJsonResult(ResultStatus.RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED, ""));
//				}// 执行等待
//				logger.info("TBox(SN:{}):蓝牙配置-短信发送成功，开始等待TBox登陆。 ", sn);
//
//				long startTime = 0;
//				try {
//					startTime = System.currentTimeMillis();
//					synchronized (TboxGetVehicleStatusCtrl.this) {
//						TboxGetVehicleStatusCtrl.this.wait(OperationConstant.WAKEUP_WAIT_TIME);
//					}
//				} catch (InterruptedException e) {
//					// 等待唤醒中途如果失败，就必须清除记录，并且
//					logger.error("TBox(SN:{}):蓝牙配置因发生异常失败, 异常：{}", sn,
//							ThrowableUtil.getErrorInfoFromThrowable(e));
//					tboxGetVehicleStatus.remove(sn);
//					return JSONObject
//							.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
//				} finally {
//					GlobalSessionChannel.removeWakeUp(sn);
//				}
//				// 如果超时，则代表唤醒失败了，直接通知用户
//				if (System.currentTimeMillis() - startTime >= OperationConstant.WAKEUP_WAIT_TIME) {
//					logger.warn("获取车况请求失败，因为 TBox {} 执行唤醒后未上线。", sn);
//					tboxGetVehicleStatus.remove(sn);
//					return JSONObject.toJSONString(
//							new AppJsonResult(ResultStatus.RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG, ""));
//				} else {
//					// TBOX在线
//					logger.info("TBox {}上线了，获取车况请求可以执行 ", sn);
//				}
//
//			} else {
//				logger.info("在线TBox {}的获取车况请求可以执行 ", sn);
//			}
//		}
//		return tboxOnlineCheck;
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
//				logger.error("TBox(SN:{})确认延迟唤醒流程发生异常: {}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(ex));
//				return;
//			}
//		}
//		return;
//	}
//
}
