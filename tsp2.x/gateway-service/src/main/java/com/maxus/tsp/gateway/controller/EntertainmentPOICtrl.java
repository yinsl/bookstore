package com.maxus.tsp.gateway.controller;
//
//import java.util.Date;
//import java.util.Hashtable;
//
//import com.maxus.tsp.gateway.service.DataProcessing;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.lang3.time.DateFormatUtils;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Scope;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.alibaba.fastjson.JSONObject;
//import com.maxus.tsp.common.constant.KafkaMsgConstant;
//import com.maxus.tsp.common.enums.ResultStatus;
//import com.maxus.tsp.common.util.ThrowableUtil;
//import com.maxus.tsp.gateway.common.constant.OperationConstant;
//import com.maxus.tsp.gateway.common.model.PoiData;
//import com.maxus.tsp.gateway.common.model.PoiRespInfo;
//import com.maxus.tsp.gateway.common.ota.GlobalSessionChannel;
//import com.maxus.tsp.gateway.mq.kafka.KafkaProducer;
//import com.maxus.tsp.platform.service.model.AppJsonResult;
//import com.maxus.tsp.platform.service.model.vo.ItRedisInfo;
///**
// * poi透传指令
// * @author lzgea
// *
// */
//@RestController
//@RequestMapping(value = { "/tsp/appapi"})
//@Scope(value = "prototype")
public class EntertainmentPOICtrl {
//
//	private final Logger logger = LogManager.getLogger(EntertainmentPOICtrl.class);
//
//	@Autowired
//	private KafkaProducer kafkaService;
//
//
//	/**
//	 * Tbox响应的报文处理结果
//	 */
//	private String result;
//	/**
//	 * 远程控制对应的车辆车架号
//	 */
//	private String vin;
//	/**
//	 * 远程控制对应的车辆绑定序列号
//	 */
//	private String tboxSN;
//
//	/**
//	 * @param result
//	 *            the result to set
//	 */
//	public void setResult(String result) {
//		this.result = result;
//	}
//
//	//解决对在线Tbox远控请求,回复周期内（10s）Tbox频繁登录登出造成的线程通知错误；
//	private boolean tboxOnlineStatusWhenCtrl = false;
//
//	public boolean getTboxOnlineStatusWhenCtrl(){
//		return tboxOnlineStatusWhenCtrl;
//	}
//
//	/**
//	 * 保存本节点处理透传poi请求的实例
//	 */
//	private static Hashtable<String, EntertainmentPOICtrl> htForwardCtrl = new Hashtable<>();
//
//	/**
//	 * 获得指定tbox的处理的透传控制的实例
//	 *
//	 * @return the htRemoteCtrl
//	 */
//	public static EntertainmentPOICtrl getHtForwardCtrl(String tboxsn) {
//		return htForwardCtrl.get(tboxsn);
//	}
//
//
//	@RequestMapping(value = "/poiConfigRVM", method = RequestMethod.GET)
//	public AppJsonResult poiConfigRVM(@RequestParam("serialNumber")String serialNumber,
//			@RequestParam("gPSType")int gPSType,
//			@RequestParam("posType")int posType,
//			@RequestParam("longitude")int longitude,
//			@RequestParam("latitude")int latitude,
//			@RequestParam("address")String address) {
//		String ret = "";
//		try {
//			// 检查参数
//			ret = checkPoiParamForRVM(serialNumber, longitude, latitude, address);
//			if (StringUtils.isEmpty(ret)) {
//				// 参数检查没有问题，则进一步执行控制指令
//				ret = doPoiConfig(this.tboxSN, this.vin, longitude, latitude, address, gPSType, posType);
//			}
//		} catch (Exception e) {
//			logger.error("poi因发生异常失败。 sn {}, 异常：{}", this.tboxSN, ThrowableUtil.getErrorInfoFromThrowable(e));
//			ret = JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
//		}
//		AppJsonResult curResult = JSONObject.parseObject(ret, AppJsonResult.class);
//		return curResult;
//	}
//
//	private String doPoiConfig(String serialNumber, String vin, int longitude, int latitude, String address, int gPSType, int posType) {
//		String result = "";
//		boolean existRequest = false;
//		boolean doWakeUp = false;
//		Date curTime = new Date();
//		String eventTime = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");
//		try {
//			if (!DataProcessing.isTboxValid(serialNumber)) {
//				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAR_INFO_ERROR, ""));
//			}
//			existRequest = DataProcessing.isEntertainmentRequestExist(serialNumber);
//			doWakeUp = !DataProcessing.onlineTboxExistCheck(serialNumber);
//		} catch (Exception e) {
//			logger.error("poi请求因发生异常失败。vin {}, TBox {}, 异常：{}", vin, serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
//			;
//			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
//		}
//		if (existRequest) {
//			// 若已存在请求，不接受新的请求
//			logger.warn("TBox(SN:{}):正在唤醒或执行其他操控，不接受新的请求。", serialNumber);
//			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.RM_TBOX_HAVE_ACCEPT_ONE_COMMAND, ""));
//		} else {
//			// 存入需要远程控制信息
//			htForwardCtrl.put(serialNumber, EntertainmentPOICtrl.this);
//			if (doWakeUp) {
//				logger.info("TBox({})不在线，poi请求不执行.", serialNumber);
//				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_TBOX_OFFLINE, ""));
//			} else {
//				logger.info("在线TBox {}的poi请求可以执行 ", serialNumber);
//				result = doPoiConfigWhenTboxOnline(serialNumber, vin, longitude, latitude,
//						gPSType, posType, address, eventTime, OperationConstant.TAKE_PHOTO_RESPONSE_WAIT_TIME_FOR_ONLINE);
//			}
//			return result;
//		}
//	}
//
//	private String doPoiConfigWhenTboxOnline(String serialNumber, String vin, int longitude, int latitude,int gPSType,int posType, String address, String eventTime, long waitRespTime) {
//		String operResult = "";
//		String operName = OperationConstant.FORWARD_POI;
//		try {
//			// tbox上线，将其移除唤醒，进一步发送远程控制
//			if (GlobalSessionChannel.existWakeUp(serialNumber)) {
//				GlobalSessionChannel.removeWakeUp(serialNumber);
//			}
//			eventTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
//			PoiData poiData = new PoiData(longitude, latitude, address,gPSType,posType);
//			kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_FORWARD_DOWN,
//					serialNumber + "_" + OperationConstant.FORWARD_POI
//					 + "_" + JSONObject.toJSONString(poiData) + "_" + eventTime, serialNumber);
//			// 发送远程控制命令后，等待回包10s
//			long startTime = 0;
//			// 插入redis
//			GlobalSessionChannel.addCommandSend(serialNumber, operName);
//			startTime = System.currentTimeMillis();
//			tboxOnlineStatusWhenCtrl = true;
//			synchronized (EntertainmentPOICtrl.this) {
//				EntertainmentPOICtrl.this.wait(waitRespTime);
//			}
//			if (System.currentTimeMillis() - startTime >= waitRespTime) {
//				// 超过10s回复结果
//				logger.warn("TBox{}没有在有限时间 {}毫秒范围内返回poi结果。 vin：{}，请求时间：{}。",
//						serialNumber, waitRespTime, vin, eventTime);
//				return JSONObject
//						.toJSONString(new AppJsonResult(ResultStatus.RM_TBOX_NOT_REPLY_WITHIN_LIMIT_TIME, ""));
//			}
//			// 获取设置的结果
//			String[] kafkaReturn = htForwardCtrl.get(serialNumber).result.split("_");
//			// 获取tbox返回结果
//			PoiRespInfo stateValue = JSONObject.parseObject(kafkaReturn[0], PoiRespInfo.class);
//			if (stateValue.getData().equals(0)) {
//				logger.info("TBox {}已经返回poi结果, 结果为成功。vin：{}，请求时间：{}", serialNumber, vin, eventTime);
//				operResult = JSONObject.toJSONString(new AppJsonResult(ResultStatus.SUCCESS, ""));
//			} else {
//				logger.warn("TBox {}已经返回poi结果, 结果为失败。vin：{}，请求时间：{}", serialNumber, vin, eventTime);
//				operResult = JSONObject.toJSONString(new AppJsonResult(ResultStatus.FAIL, ""));
//			}
//		} catch (Exception e) {
//			logger.error("poi请求因发生异常失败。vin {}, TBox {}, 异常：{}",
//					vin, serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
//			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.FAIL, ""));
//		} finally {
//			// 可以获取到本次远程指令的处理结果了，该结果由RemoteCtrlListener的listenRes方法来设置
//			htForwardCtrl.remove(serialNumber);
//			if (GlobalSessionChannel.existCommandSend(serialNumber)) {
//				GlobalSessionChannel.removeCommandSend(serialNumber);
//			}
//		}
//		return operResult;
//	}
//
//	private String checkPoiParamForRVM(String serialNumber, int longitude, int latitude, String address) {
//		String checkResult = "";
//		// 检查sn参数是否为空
//		if (StringUtils.isBlank(serialNumber)) {
//			logger.warn("poi请求sn参数为空");
//			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAR_INFO_ERROR, ""));
//		}
//		this.tboxSN = serialNumber;
//		// 根据sn确认是否存在tbox
//		ItRedisInfo itRedisInfo = DataProcessing.getITTboxInfo(serialNumber);
//		if (itRedisInfo == null) {
//			logger.warn("poi请求sn参数在redis中不存在。 sn {}", serialNumber);
//			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAR_INFO_ERROR, ""));
//		}
//		// 查询vin号
//		this.vin = itRedisInfo.getVin();
//		// 检查longitude参数是否为空
//		if (StringUtils.isBlank(String.valueOf(longitude))) {
//			logger.warn("poi请求longitude参数为空");
//			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
//		}
//		// 检查latitude参数是否为空
//		if (StringUtils.isBlank(String.valueOf(latitude))) {
//			logger.warn("poi请求latitude参数为空");
//			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
//		}
//		// 检查address参数是否为空
//		if (StringUtils.isBlank(address)) {
//			logger.warn("poi请求address参数为空");
//			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
//		}
//		logger.info("本次poi请求是  vin:{}, 对应TBox：{}, 经度：{}, 纬度：{}, 具体地址：{}", this.vin, serialNumber, longitude, latitude, address);
//		return checkResult;
//	}
}
