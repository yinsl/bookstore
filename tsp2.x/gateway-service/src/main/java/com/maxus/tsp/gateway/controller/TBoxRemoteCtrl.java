/**
 * TBoxRemoteCtrl.java Create on 2017年6月5日
 * Copyright (c) 2017年6月5日 by 上汽集团商用车技术中心
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>
 * @version 1.0
 */
package com.maxus.tsp.gateway.controller;

import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.maxus.tsp.common.constant.KafkaMsgConstant;
import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.enums.ResultStatus;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OTARemoteCommand;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.model.RemoteControlRequest;
import com.maxus.tsp.gateway.common.model.RemoteCtrlRespInfo;
import com.maxus.tsp.gateway.common.model.RemoteCtrlResponseData;
import com.maxus.tsp.gateway.common.model.TakePhotoRequest;
import com.maxus.tsp.gateway.common.model.TakePhotoRespInfo;
import com.maxus.tsp.gateway.common.ota.OpCarRemoteControl;
import com.maxus.tsp.gateway.common.ota.RemoteCtrlInfo;
import com.maxus.tsp.gateway.mq.kafka.KafkaProducer;
import com.maxus.tsp.gateway.ota.TspServiceProc;
import com.maxus.tsp.gateway.service.DataProcessing;
import com.maxus.tsp.gateway.service.TboxService;
import com.maxus.tsp.gateway.serviceclient.TspPlatformClient;
import com.maxus.tsp.platform.service.model.AppJsonResult;
import com.maxus.tsp.platform.service.model.vo.BaseCar;
import com.maxus.tsp.platform.service.model.vo.ItRedisInfo;


/**
 *
 * @ClassName: TBoxController.java
 * @Description: 远程控制（含OTA中的远程控制指令如寻车、解锁开锁等，以及拍照接口）
 * @author 任怀宇
 * @version V1.0
 * @Date 2017年7月18日 下午5:10:42
 */
@RestController
@RequestMapping(value = { "/tsp/appapi", "/tsp/commonApi"})
@Scope(value = "prototype")
public class TBoxRemoteCtrl {
	
	@Autowired
    private DataProcessing dataProcessing;
	
	@Autowired
	private RedisAPI redisAPI;

	private final Logger logger = LogManager.getLogger(getClass());


	@Autowired
	private KafkaProducer kafkaService;
	@Autowired
	private TboxService tboxService;

	@Value("${wakeUpDelayTime:10}")
	private int wakeUpDelayTime;

	@Autowired
	private TspPlatformClient tspPlatformClient;

	/**
	 * Tbox响应的报文处理结果
	 */
	private String result;
	/**
	 * 远程控制对应的车辆车架号
	 */
	private String vin;
	/**
	 * 远程控制对应的车辆绑定序列号
	 */
	private String tboxSN;

	/**
	 * 用于记录当前远程控制请求是否是需要唤醒阶段
	 */
	private boolean tboxOnlineStatusWhenCtrl = false;

	public boolean getTboxOnlineStatusWhenCtrl() {
		return tboxOnlineStatusWhenCtrl;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * 保存本节点处理远程控制请求的实例
	 */
	private static Hashtable<String, TBoxRemoteCtrl> htRemoteCtrl = new Hashtable<>();

	/**
	 * 保存本节点处理透传拍照请求的实例
	 */
	private static Hashtable<String, TBoxRemoteCtrl> htForwardCtrl = new Hashtable<>();


	// 配合新接口返回码前缀
	private static final String PREFIX_REMOTE_CTRL_STATUS_FOR_ASYN_INTERFACE = "1208";

	/**
	 * 获得指定tbox的处理的远程控制的实例
	 *
	 * @return the htRemoteCtrl
	 */
	public static TBoxRemoteCtrl getHtRemoteCtrl(String tboxsn) {
		return htRemoteCtrl.get(tboxsn);
	}

	/**
	 * 获得指定tbox的处理的透传控制的实例
	 *
	 * @return the htRemoteCtrl
	 */
	public static TBoxRemoteCtrl getHtForwardCtrl(String tboxsn) {
		return htForwardCtrl.get(tboxsn);
	}



	// 远程控制异步处理方案接口
	@RequestMapping(value = "/1.0/remoteControl", method = RequestMethod.POST)
	public String remoteControlAsyn(@RequestBody RemoteControlRequest remoteControlRequestParam) {
		String rmtResult = "";
		if (remoteControlRequestParam == null) {
			logger.warn("Enter /1.0/remoteControl: post param for /1.0/remoteControl is null.");
			rmtResult = JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
		} else {
			logger.info("Enter /1.0/remoteControl: comd is {}, value is {} , vin is {} ",
					remoteControlRequestParam.getComd(), remoteControlRequestParam.getValue(),
					remoteControlRequestParam.getVin());
			rmtResult = remoteControlAsyn(remoteControlRequestParam.getComd(), remoteControlRequestParam.getValue(),
					remoteControlRequestParam.getVin());
		}
		logger.info("Return remoteControl: {}", rmtResult);
		return rmtResult;
	}

	@RequestMapping(value = "/remoteControl", method = RequestMethod.POST)
	public String remoteControl(@RequestBody RemoteControlRequest remoteControlRequestParam) {

		if (remoteControlRequestParam == null) {
			logger.warn("远程控制post请求参数为空.");
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
		} else {
			return remoteControl(remoteControlRequestParam.getSerialNumber(), remoteControlRequestParam.getComd(),
					remoteControlRequestParam.getValue(), remoteControlRequestParam.getVin());
		}
	}

	@RequestMapping(value = "/remoteControl", method = RequestMethod.GET)
	public String remoteControl(String serialNumber, String comd, String value, String vin) {
		// 发送请求
		String code = "";
		String cmValue = value;
		String ret = null;
		try {
			// 优先使用vin是否为空，因为一辆车可能会改装tbox，但车架号固定
			if (StringUtils.isBlank(vin)) {
				// 如果vin为空再检查serialNumber参数是否为空(兼容老接口)
				if (StringUtils.isBlank(serialNumber)) {
					logger.warn("远程控制车架号或vin号都为空.");
					return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
				} else {
					code = serialNumber;
				}
			} else {
				// 根据vin获取serialNumber
				this.vin = vin;
				BaseCar bc = tspPlatformClient.selectBaseCarByVIN(vin);
				if (bc == null) {
					logger.warn("远程控制vin参数在数据库中不存在。 vin {}", vin);
					return JSONObject.toJSONString(new AppJsonResult(ResultStatus.VIN_ERROR, ""));
				}
				code = bc.getSn();
			}

			// 检查serialNumber参数是否为空
			if (StringUtils.isBlank(comd)) {
				logger.warn("远程控制comd参数为空。 vin {}, serialNumber {}", vin, serialNumber);
				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
			}

			// 检查serialNumber参数是否为空
			if (StringUtils.isBlank(value)) {
				logger.warn("远程控制value参数为空。 vin {}, serialNumber {}", vin, serialNumber);
				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
			}
			// 不需要调用数据库
			logger.info("当前远程控制请求是 TBox: {}, comd: {}, value: {}", code, comd, value);
			if (!comd.equals("TemperatureSetting") && !(value.equals(OperationConstant.REMOTE_CTRL_FUNCTION_OPEN)
					|| value.equals(OperationConstant.REMOTE_CTRL_FUNCTION_CLOSED))) {
				logger.warn("远程控制指令错误 comd:{}, TBox {}。", comd, code);
				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, ""));
			} else if (comd.equals("TemperatureSetting") && (!StringUtils.isNumeric(value)
					|| !(Integer.parseInt(value) <= 255 && Integer.parseInt(value) >= 0))) {
				logger.warn("远程控制指令错误 comd:{}, TBox {}。", comd, code);
				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, ""));
			}
			// 接收响应结果作为返回的类型:url来自配置文件
			switch (comd) {

				case "Search":
					ret = search(cmValue, code, comd);
					break;
				case "ControlDoor":
					ret = doorCtrl(cmValue, code, comd);
					break;
				case "VehicleStart":
					ret = vehicleStart(cmValue, code, comd);
					break;
				case "AirConditioning":
					ret = airConditioning(cmValue, code, comd);
					break;
				case "TemperatureSetting":
					ret = temperatureSet(cmValue, code, comd);
					break;
				case "SeatheatingFrontRight":
					ret = heatingFR(cmValue, code, comd);
					break;
				case "SeatheatingFrontLeft":
					ret = heatingFL(cmValue, code, comd);
					break;
				case "SeatheatingRearRight":
					ret = heatingRR(cmValue, code, comd);
					break;
				case "SeatheatingRearLeft":
					ret = heatingRL(cmValue, code, comd);
					break;
				case "LimitSpeed":
					ret = limitSpeed(cmValue, code, comd);
					break;
				default:
					logger.warn("远程控制指令错误 comd:{}, TBox {}。", comd, code);
					return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, ""));
			}
			// 对返回的ret进行判断,返回给手机的调用者
			return ret;
		} catch (Exception e) {
			logger.error("远程控制因发生异常失败。 TBox {}, 异常：{}", serialNumber,
					ThrowableUtil.getErrorInfoFromThrowable(e));
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
		}
	}

	@RequestMapping(value = "/remoteControlRVM", method = RequestMethod.GET)
	public AppJsonResult remoteControlRVM(String serialNumber, String comd, String value) {
		String result = remoteControl(serialNumber, comd, value, null);
		return getReturnAppJsonResultRVM(result);
	}
	
	// 1.0版本拍照接口，返回格式增加前缀统一长度
	@RequestMapping(value = "1.0/takePhoto", method = RequestMethod.POST)
	public String takePhotoAnsy(@RequestBody TakePhotoRequest takePhotoRequest) {
		if (takePhotoRequest == null) {
			logger.warn("拍照接口post请求参数为空.");
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
		} else {
			String result = takePhotoAnsy(takePhotoRequest.getVin(), takePhotoRequest.getCameraList());
			// 获取status并修正

			return result;
		}
	}


	private String checkTakePhotoParam(String vin, String cameraList) {
		String checkResult = "";
		// 检查vin参数是否为空
		if (StringUtils.isBlank(vin)) {
			logger.warn("拍照请求vin参数为空");
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.VIN_REQUIRED, ""));
		}
		// 根据vin码获取sn
		BaseCar bc = tspPlatformClient.selectBaseCarByVIN(vin);
		if (bc == null) {
			logger.warn("拍照请求vin参数在数据库中不存在。 vin {}", vin);
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.VIN_ERROR, ""));
		}
		this.vin = vin;
		this.tboxSN = bc.getSn();
		// 检查摄像头列表参数
		if (StringUtils.isBlank(cameraList)) {
			logger.warn("拍照请求cameraList参数为空。 vin {}", vin);
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAMERA_LIST_REQUIRED, ""));
		}
		// 检查摄像头参数在车辆上是否存在
		String[] cameraNolist = cameraList.trim().split(",");
		HashSet<String> checkList = new HashSet<String>();
		for (String cameraNoItem : cameraNolist) {
			if (!(cameraNoItem.compareTo(OperationConstant.SUPPORTED_CAMERA_NO_MIN) >= 0
					&& cameraNoItem.compareTo(OperationConstant.SUPPORTED_CAMERA_NO_MAX) <= 0)) {
				logger.warn("拍照请求cameraList参数不正确。 vin {}", vin);
				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAMERA_NO_PRIVED_ERROR, ""));
			}
			checkList.add(cameraNoItem);
		}
		if (cameraNolist.length != checkList.size()) {
			// 列表参数出现重复
			logger.warn("拍照请求摄像头编号重复。 vin {}，摄像头编号列表：{}", vin, cameraList);
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAMERA_NO_PRIVED_ERROR, ""));
		}
		if (cameraNolist.length > OperationConstant.TAKEPHOTO_SUPPORTED_CAMERA_NUMBER
				|| cameraNolist.length != tspPlatformClient.checkCamera(vin, cameraList.trim())) {
			// 个数不对，即列表参数不正确
			logger.warn("拍照请求摄像头编号错误。 vin {}，摄像头编号列表：{}", vin, cameraList);
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAMERA_NO_PRIVED_ERROR, ""));
		}
		logger.info("本次拍照请求是  vin:{}, 对应TBox：{},摄像头列表详情：{}", vin, this.tboxSN, cameraList);
		return checkResult;
	}

	private String checkTakePhotoParamForRVM(String tboxSn, String cameraList) {
		String checkResult = "";
		// 检查sn参数是否为空
		if (StringUtils.isBlank(tboxSn)) {
			logger.warn("拍照请求sn参数为空");
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.VIN_REQUIRED, ""));
		}
		this.tboxSN = tboxSn;
		// 根据sn确认是否存在tbox
		ItRedisInfo itRedisInfo = dataProcessing.getITTboxInfo(tboxSn);
		if (itRedisInfo == null) {
			logger.warn("拍照请求sn参数在redis中不存在。 sn {}", tboxSn);
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.VIN_ERROR, ""));
		}
		//获取it tbox的摄像头列表
		this.vin = itRedisInfo.getVin();
		List<Integer> cameraSupportList = itRedisInfo.getCameraNumList();
		// 检查摄像头列表参数
		if (StringUtils.isBlank(cameraList)) {
			logger.warn("拍照请求cameraList参数为空。 sn {}", tboxSn);
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAMERA_LIST_REQUIRED, ""));
		}
		// 检查摄像头参数在车辆上是否存在
		String[] cameraNolist = cameraList.trim().split(",");
		HashSet<String> checkList = new HashSet<String>();
		for (String cameraNoItem : cameraNolist) {
			if (!(cameraNoItem.compareTo(OperationConstant.SUPPORTED_CAMERA_NO_MIN) >= 0
					&& cameraNoItem.compareTo(OperationConstant.SUPPORTED_CAMERA_NO_MAX) <= 0)) {
				logger.warn("拍照请求cameraList参数不正确。 sn {}", tboxSn);
				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAMERA_NO_PRIVED_ERROR, ""));
			} else if (cameraSupportList != null && cameraSupportList.contains(Integer.valueOf(cameraNoItem))) {
				checkList.add(cameraNoItem);
			}
		}
		if (cameraNolist.length != checkList.size()) {
			// 列表参数出现重复
			logger.warn("拍照请求摄像头编号重复或错误。 sn {}，摄像头编号列表：{}", tboxSn, cameraList);
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAMERA_NO_PRIVED_ERROR, ""));
		}
		logger.info("本次拍照请求是  vin:{}, 对应TBox：{},摄像头列表详情：{}", vin, this.tboxSN, cameraList);
		return checkResult;
	}
	
	// post方法的拍照接口，返回status为老版本
	@RequestMapping(value = "/takePhoto", method = RequestMethod.POST)
	public String takePhoto(@RequestBody TakePhotoRequest takePhotoRequest) {
		if (takePhotoRequest == null) {
			logger.warn("拍照接口post请求参数为空.");
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
		} else {
			return takePhoto(takePhotoRequest.getVin(), takePhotoRequest.getCameraList());
		}
	}

	// get方法的拍照接口，老接口
	@RequestMapping(value = "/takePhoto", method = RequestMethod.GET)
	public String takePhoto(@RequestParam("vin") String vin, @RequestParam("cameraList") String cameraList) {
		String ret = "";
		try {
			ret = checkTakePhotoParam(vin, cameraList);
			if (StringUtils.isEmpty(ret)) {
				//参数检查没有问题，则进一步执行控制指令
				ret = doTakePhoto(vin, this.tboxSN, cameraList);
			}
			return ret;
		} catch (Exception e) {
			logger.error("拍照因发生异常失败。 vin {}, 异常：{}", vin, ThrowableUtil.getErrorInfoFromThrowable(e));
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.FAIL, ""));
		}
	}
	
	@RequestMapping(value = "/takePhotoForRVM", method = RequestMethod.GET)
	public AppJsonResult takePhotoForRVM(@RequestParam("tboxSN") String tboxSN, @RequestParam("cameraList") String cameraList) {
		String ret = "";
		try {
			ret = checkTakePhotoParamForRVM(tboxSN, cameraList);
			if (StringUtils.isEmpty(ret)) {
				//参数检查没有问题，则进一步执行控制指令
				ret = doTakePhoto(this.vin, this.tboxSN, cameraList);
			}
		} catch (Exception e) {
			logger.error("拍照因发生异常失败。 sn {}, 异常：{}", tboxSN, ThrowableUtil.getErrorInfoFromThrowable(e));
			ret = JSONObject.toJSONString(new AppJsonResult(ResultStatus.FAIL, ""));
		}
		//AppJsonResult curResult = JSONObject.parseObject(ret, AppJsonResult.class);
		AppJsonResult curResult = getReturnAppJsonResultRVM(ret);
		return curResult;
	}

	public String remoteControlAsyn(String comd, String value, String vin) {
		// 发送请求
		String serialNumber = "";
		String cmValue = value;
		String ret = null;
		try {
			// 优先使用vin是否为空，因为一辆车可能会改装tbox，但车架号固定
			if (StringUtils.isBlank(vin)) {
				// 如果vin为空再检查serialNumber参数是否为空(兼容老接口)
				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
			} else {
				// 根据vin获取serialNumber
				this.vin = vin;
				BaseCar bc = tspPlatformClient.selectBaseCarByVIN(vin);
				if (bc == null) {
					logger.warn("远程控制vin参数在数据库中不存在。 vin {}", vin);
					return JSONObject.toJSONString(new AppJsonResult(ResultStatus.VIN_ERROR, ""));
				}
				serialNumber = bc.getSn();
			}

			// 检查comd参数是否为空
			if (StringUtils.isBlank(comd)) {
				logger.warn("远程控制comd参数为空或null。 vin {}", vin);
				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
			}

			// 检查value参数是否为空
			if (StringUtils.isBlank(value)) {
				logger.warn("远程控制value参数为空或null。 vin {}", vin);
				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, ""));
			}
			// 记录当前远程控制参数
			logger.info("当前远程控制请求是 TBox: {}, comd: {}, value: {}", serialNumber, comd, value);
			if (!comd.equals("TemperatureSetting") && !(value.equals("00") || value.equals("11"))) {
				logger.warn("远程控制指令错误 comd:{}, TBox {}。", comd, serialNumber);
				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, ""));
			}
			// 根据指令不同进行远程控制
			switch (comd) {

				case "Search":
					ret = doRemoteCtrlAsyn(cmValue, serialNumber, OTARemoteCommand.Search);
					break;
				case "ControlDoor":
					ret = doRemoteCtrlAsyn(cmValue, serialNumber, OTARemoteCommand.ControlDoor);
					break;
				case "VehicleStart":
					ret = doRemoteCtrlAsyn(cmValue, serialNumber, OTARemoteCommand.VehicleStart);
					break;
				case "AirConditioning":
					ret = doRemoteCtrlAsyn(cmValue, serialNumber, OTARemoteCommand.AirConditioning);
					break;
				case "TemperatureSetting":
					ret = doRemoteCtrlAsyn(cmValue, serialNumber, OTARemoteCommand.TemperatureSetting);
					break;
				case "SeatheatingFrontRight":
					ret = doRemoteCtrlAsyn(cmValue, serialNumber, OTARemoteCommand.SeatheatingFrontRight);
					break;
				case "SeatheatingFrontLeft":
					ret = doRemoteCtrlAsyn(cmValue, serialNumber, OTARemoteCommand.SeatheatingFrontLeft);
					break;
				case "SeatheatingRearRight":
					ret = doRemoteCtrlAsyn(cmValue, serialNumber, OTARemoteCommand.SeatheatingRearRight);
					break;
				case "SeatheatingRearLeft":
					ret = doRemoteCtrlAsyn(cmValue, serialNumber, OTARemoteCommand.SeatheatingRearLeft);
					break;
				default:
					logger.warn("远程控制指令错误 comd:{} TBox: {}", comd, serialNumber);
					return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, ""));
			}
			// 对返回的ret进行判断,返回给手机的调用者
			return ret;
		} catch (Exception e) {
			logger.error("远程控制异常。 TBox {}, 异常：{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
		}
	}

	// 具体处理远程控制的流程
	private String doRemoteCtrlAsyn(String cmValue, String serialNumber, OTARemoteCommand commd) {
		// 重置本次远程指令的结果状态
		String result = "";
		boolean existRequest = false;
		boolean doWakeUp = false;
		Date curTime = new Date();
		String eventTime = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");
		try {
			// 判断是否非法tbox
			if (!dataProcessing.isTboxValid(serialNumber)) {
				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAR_INFO_ERROR, ""));
			}
			// 判断是否已经存在远程控制信息
			existRequest = dataProcessing.isRequestRmtAsynExist(serialNumber, this.vin);
			// 获取tbox在线情况
			doWakeUp = !dataProcessing.onlineTboxExistCheck(serialNumber);
			/*!redisAPI.hasKey(RedisConstant.ONLINE_TBOX, serialNumber)*/
		} catch (Exception e) {
			logger.error("远程控制因发生异常失败。 TBox {}, 异常:{}", serialNumber,
					ThrowableUtil.getErrorInfoFromThrowable(e));
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
		}
		// 已经存在远程控制时，拒绝接受新请求
		if (existRequest) {
			// 查询唤醒清单中是否已经存在请求
			// 提示用户不能进行操作
			logger.warn("TBox {} 正在唤醒或执行其他操控，不接受新的请求。", serialNumber);
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.RM_TBOX_HAVE_ACCEPT_ONE_COMMAND, ""));
		} else {
			int recordID = 0;
			String iccid = "";
			// 存入需要远程控制信息
			htRemoteCtrl.put(serialNumber, TBoxRemoteCtrl.this);
			if (doWakeUp) {
				// tbox不在线，需要进行唤醒
				try {
					// 当tbox刚刚登出不满10s，补全10s再进行唤醒，避免4g无法启动，接受不到短信的情况
					dataProcessing.checkTBoxLogOutForWakeupWait(serialNumber);
					// 增加唤醒记录
					addWakeUp(serialNumber);
					logger.info("离线TBox {}的唤醒及远程控制请求可以执行 ", serialNumber);

					// 远程控制指令记录于数据库
					recordID = this.tspPlatformClient.recordRemoteCtrl(vin, commd.getMessage(), cmValue);
					// 获取Tbox的ICCID
					iccid = this.tspPlatformClient.getTboxVo(serialNumber).getIccid();
				} catch (Exception e) {
					// 发生数据库访问异常，则拒绝进行远程控制，提示发生异常而无法执行
					logger.error("远程控制因发生异常失败。 TBox {}, 异常：{}", serialNumber,
							ThrowableUtil.getErrorInfoFromThrowable(e));
					redisAPI.delete(RedisConstant.WAKE_UP + "_" + serialNumber);
					htRemoteCtrl.remove(serialNumber);
					return JSONObject
							.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
				}
				logger.info("开始异步发送唤醒短信并等待TBox {}登陆返回处理结果。 ", serialNumber);
				// 执行异步处理TBox返回结果
				doRemoteCtrlWhenTboxOfflineAnsy(recordID, cmValue, serialNumber, iccid, commd, eventTime);
				return JSONObject.toJSONString(
						new AppJsonResult(ResultStatus.RM_SEND_WAKEUP_MSG_FOR_TBOX_AND_NEED_CHECK_RESULT, ""));
			} else {
				logger.info("在线TBox {}的远程控制请求可以执行 ", serialNumber);
				try {
					recordID = this.tspPlatformClient.recordRemoteCtrl(vin, commd.getMessage(), cmValue);
					// 继续等远程控制指令响应
					result = doRemoteCtrlWhenTboxOnline(recordID, cmValue, serialNumber, commd, eventTime);
					result = modifiedReturnByPrefix(result);
				} catch (Exception e) {
					logger.error("远程控制因发生异常失败。 TBox {}, 异常：{}", serialNumber,
							ThrowableUtil.getErrorInfoFromThrowable(e));
					return JSONObject
							.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
				}
			}
			return result;
		}
	}

/*	public AppJsonResult getReturnAppJsonResult(String tboxresult)
	{
		String status = modifiedStatusByPrefix(tboxresult);

		if (!ResultStatus.SUCCESS.getCode().equals(status)) {
			return new AppJsonResult(ResultStatus.getResultStatus(status), "");
		}
		return JSONObject.parseObject(tboxresult, AppJsonResult.class);
	}*/
	
	public AppJsonResult getReturnAppJsonResultRVM(String tboxresult)
	{
		String status = modifiedStatusByPrefix(tboxresult);
		AppJsonResult curResult = JSONObject.parseObject(tboxresult, AppJsonResult.class);
		Object data = null;
		if (curResult != null) {
			data = curResult.getData();
			if(data != null && data.toString().equals(""))
			{
				data = null;
			}
		}
		if (!ResultStatus.SUCCESS.getCode().equals(status)) {
			return new AppJsonResult(ResultStatus.getResultStatus(status), data);
		}
		return JSONObject.parseObject(tboxresult, AppJsonResult.class);
	}

	// 修正返回Json
	private String modifiedReturnByPrefix(String tboxresult) {
		String status = modifiedStatusByPrefix(tboxresult);

		if (!ResultStatus.SUCCESS.getCode().equals(status)) {
			tboxresult = JSONObject.toJSONString(new AppJsonResult(ResultStatus.getResultStatus(status), ""));
		}
		return tboxresult;
	}

	// 获取返回结果JSON中的status值，并且修正其对应结果
	private String modifiedStatusByPrefix(String tboxresult) {
		AppJsonResult curresult = JSONObject.parseObject(tboxresult, AppJsonResult.class);
		String status = curresult.getStatus();

		if (ResultStatus.TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG.getCode().equals(status)) {
			// 当老status code =2，则直接修正为12090
			status = ResultStatus.RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG.getCode();
		} else if (!ResultStatus.SUCCESS.getCode().equals(status) && status.length() == 1) {
			// 当非成功情况，且返回长度为1的，重新构造新版本远程控制返回的status值
			status = PREFIX_REMOTE_CTRL_STATUS_FOR_ASYN_INTERFACE + status;
		}
		return status;
	}

	// 获取返回结果JSON中的status值，并且修正其对应结果
	private String getDatafromTboxResult(String tboxresult) {
		AppJsonResult curresult = JSONObject.parseObject(tboxresult, AppJsonResult.class);
		String data = curresult.getData().toString();
		return data;
	}

	// tbox不在线，异步执行远程控制，并将结果写入redis
	public void doRemoteCtrlWhenTboxOfflineAnsy(int recordID, String cmValue, String serialNumber, String iccid,
			OTARemoteCommand commd, String eventTime) {
		// 读入数据库中对应当前tbox的电子围栏
		TspServiceProc.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				// 初始化异步处理结果的类
				RemoteCtrlRespInfo remoteRespInfo = new RemoteCtrlRespInfo();
				remoteRespInfo.setComd(commd.name());
				remoteRespInfo.setReqTime(eventTime);
				remoteRespInfo.setValue(cmValue);
				String result = "";
				try {
						dataProcessing.remoteCtrlRespDelete(serialNumber);
						if (!dataProcessing.isSendingMessageSucceed(serialNumber)) {
							// 唤醒失败，直接调用回复
							logger.warn("TBox {}的远程控制因唤醒失败而失败 ", serialNumber);
							// 清除wakeup记录
							redisAPI.delete(RedisConstant.WAKE_UP + "_" + serialNumber);
							htRemoteCtrl.remove(serialNumber);
							result = ResultStatus.RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED.getCode();
						} else {
							long startTime = 0;
							// 等待通知线程唤醒成功
							logger.info("远程控制流程唤醒短信发送成功并等待TBox {}登陆。 ", serialNumber);
							/*GlobalSessionChannel.removeRemoteCtrlRes(serialNumber);*/
							startTime = System.currentTimeMillis();
							synchronized (TBoxRemoteCtrl.this) {
								TBoxRemoteCtrl.this.wait(OperationConstant.WAKEUP_WAIT_TIME);
							}

							// 如果等待超时，则代表唤醒失败了，直接通知用户
							if (System.currentTimeMillis() - startTime >= OperationConstant.WAKEUP_WAIT_TIME) {
								// 清除wakeup记录
								logger.warn("远程控制失败，因为 TBox {}执行唤醒后未上线。请求时间：{}",
										serialNumber, eventTime);
								result = ResultStatus.RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG.getCode();
							} else {
								// 唤醒成功，执行远程控制流程
								String tboxresult = doRemoteCtrlWhenTboxOnline(recordID, cmValue, serialNumber,
										commd, eventTime);
								// 获取status结果，并修正长度
								result = modifiedStatusByPrefix(tboxresult);
							}
						}
				} catch (InterruptedException e) {
					// 等待唤醒中途如果失败，就必须清除记录，并且
					logger.error("远程控制失败，因为发生异常。 TBox {}, 异常：{}", serialNumber,
							ThrowableUtil.getErrorInfoFromThrowable(e));
					htRemoteCtrl.remove(serialNumber);
					result = ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION.getCode();
				} finally {
					// 记录返回结果至Redis，供查询接口查询
					remoteRespInfo.setResult(result);
					dataProcessing.remoteCtrlRespAdd(serialNumber, remoteRespInfo);
					redisAPI.delete(RedisConstant.WAKE_UP + "_" + serialNumber);
				}
			}
		});
	}
	
	/**
     * @method addCommandSend
     * @description 添加一条正在下发的指令
     * @param serialNumber
     * @param value
     * @return
     * @author zhuna
     * @date 2019/2/15 15:49
     */
    public  boolean addCommandSend(String serialNumber, String value) {
        try {
            Date date = new Date();
            String currentTime = DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss");
            return redisAPI.setValueWithEspireTime(RedisConstant.COMMAND_SEND + "_" + serialNumber, value + "_" + currentTime, OperationConstant.REMOTECONTROL_RESP_EXPIRED_TIME, TimeUnit.SECONDS);
        } catch (Exception ex) {
            logger.error("Redis addValue error, TBox({}) can't do addCommandSend! The error:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(ex));
            return false;
        }
    }
    
    /**
     * @Title: 根据操作指令确认是否有该指令引起的唤醒操作
     * @param tboxSn
     * @return
     * @author zhuna
     * @date 2018年12月4日
     */
    public boolean existWakeUp(String tboxSn) {
        return redisAPI.hasKey(RedisConstant.WAKE_UP + "_" + tboxSn);
    }
    
    /**
     * @Title: getRtDownSendTime
     * @Description: 获取tbox远程控制下发时间
     * @param: @param
     *             tboxsn
     * @return: void
     * @throws @author
     *             余佶
     * @Date 2017年8月12日 下午1:52:37
     */
    public String getRtDownSendTime(String tboxsn) {
        try {
            return redisAPI.getHash(RedisConstant.CAR_REMOTE_CTRL_DOWN, tboxsn);
        } catch (Exception ex) {
            logger.error("Redis connection error,TBox({}) can't do getRtDownSendTime:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            return null;
        }
    }

	// 在线情况下执行远程控制
	private String doRemoteCtrlWhenTboxOnline(int recordID, String cmValue, String serialNumber, OTARemoteCommand commd,
			String eventTime) {
		String rcResult = "";
		try {
			RemoteCtrlInfo ctrlInfo = new RemoteCtrlInfo();
			ctrlInfo.remoteCmd = commd;
			this.tboxOnlineStatusWhenCtrl = true;
			// 等响应报文上来时，设置命令执行状态
			//GlobalSessionChannel.addRemoteCtrl(serialNumber, ctrlInfo);
			addCommandSend(serialNumber, ctrlInfo.remoteCmd.toString());
			// tbox上线，将其移除唤醒，进一步发送远程控制
			if (existWakeUp(serialNumber)) {
				redisAPI.delete(RedisConstant.WAKE_UP + "_" + serialNumber);
			}
			/*if (GlobalSessionChannel.existWakeUp(serialNumber, OperationConstant.REMOTE_CONTROL)) {
				GlobalSessionChannel.removeWakeUp(serialNumber, OperationConstant.REMOTE_CONTROL);
			}*/
			// 这里得通过kafka生产一个远程控制指令下发
			kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_REMOTECTRL,
					serialNumber + "_" + commd.value() + "_" + cmValue + "_" + eventTime, serialNumber);
			Date curTime = new Date();
			//默认的下发时间
			String downDate  = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");
			// 发送远程控制命令后，等待回包10s
			long startTime = 0;
			startTime = System.currentTimeMillis();
			synchronized (TBoxRemoteCtrl.this) {
				TBoxRemoteCtrl.this.wait(OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME);
			}
			if (System.currentTimeMillis() - startTime >= OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME) {
				// 超过10s回复结果
				logger.warn("远程控制失败，因为 TBox {}没有及时回复远程控制报文。请求时间：{}", serialNumber, eventTime);
				// 更新远程控制下发时间
				downDate = getRtDownSendTime(serialNumber);
				this.tspPlatformClient.addRemoteCtrlDownStatus(recordID, downDate);
				rcResult = JSONObject
						.toJSONString(new AppJsonResult(ResultStatus.TBOX_NOT_REPLY_WITHIN_LIMIT_TIME, ""));
			} else {
				downDate = getRtDownSendTime(serialNumber);
				this.tspPlatformClient.addRemoteCtrlDownStatus(recordID, downDate);
				String[] stateValue = htRemoteCtrl.get(serialNumber).result.split("_");
				logger.info("TBox {} 已经回复远程控制结果：{}", serialNumber, stateValue[0]);
				RemoteCtrlResponseData tboxResultData = new RemoteCtrlResponseData();
				tboxResultData.setErrCode(stateValue[1]);
				if (!ResultStatus.SUCCESS.equals(ResultStatus.getResultStatus(stateValue[0]))) {
					rcResult = JSONObject.toJSONString(
							new AppJsonResult(ResultStatus.getResultStatus(stateValue[0]), tboxResultData));
				} else {
					rcResult = JSONObject.toJSONString(
							new AppJsonResult(ResultStatus.getResultStatus(stateValue[0]), ""));
				}
			}
		} catch (Exception e) {
			logger.error("远程控制因发生异常失败。 TBox {}, 异常：{}", serialNumber,
					ThrowableUtil.getErrorInfoFromThrowable(e));
			rcResult = JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
		} finally {
			// 可以获取到本次远程指令的处理结果了，该结果由RemoteCtrlListener的listenRes方法来设置
			htRemoteCtrl.remove(serialNumber);
			removeRtDownSendTime(serialNumber);
			if (redisAPI.hasKey(RedisConstant.COMMAND_SEND + "_" + serialNumber)) {
				redisAPI.removeKey(RedisConstant.COMMAND_SEND + "_" + serialNumber);
			}
		}
		return rcResult;
	}

	/**
     * @Title: removeRtDownSendTime
     * @Description: 清除tbox远程控制下发时间
     * @param: @param
     *             tboxsn
     * @return: void
     * @throws @author
     *             余佶
     * @Date 2017年8月12日 下午1:52:37
     */
    public void removeRtDownSendTime(String tboxsn) {
        try {
            redisAPI.removeHash(RedisConstant.CAR_REMOTE_CTRL_DOWN, tboxsn);
        } catch (Exception ex) {
            logger.error("Redis connection error, TBox({}) can't do removeRtDownSendTime:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
    }



	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public String search(@RequestParam String cmValue, @RequestParam String code, @RequestParam String comd) {
		return doRemoteCtrl(cmValue, code, OTARemoteCommand.Search);

	}

	@RequestMapping(value = "/search1", method = RequestMethod.GET)
	public Callable<String> search1(@RequestParam String cmValue, @RequestParam String code,
			@RequestParam String comd) {
		return new Callable<String>() {

			@Override
			public String call() throws Exception {
				// synchronized (remoteCtrlEvent) {
				// remoteCtrlEvent.wait();
				// }
				return "你好啊";

			}
		};
	}

	@RequestMapping(value = "/searchR", method = RequestMethod.GET)
	public String searchReset() {
		// synchronized (remoteCtrlEvent) {
		// remoteCtrlEvent.notify();
		// }

		return "reset";
	}

	@RequestMapping(value = "/doorCtrl", method = RequestMethod.GET)
	public String doorCtrl(@RequestParam String cmValue, @RequestParam String code, @RequestParam String comd) {
		return doRemoteCtrl(cmValue, code, OTARemoteCommand.ControlDoor);

	}

	@RequestMapping(value = "/vehicleStart", method = RequestMethod.GET)
	public String vehicleStart(@RequestParam String cmValue, @RequestParam String code, @RequestParam String comd) {
		return doRemoteCtrl(cmValue, code, OTARemoteCommand.VehicleStart);

	}

	@RequestMapping(value = "/airConditioning", method = RequestMethod.GET)
	public String airConditioning(@RequestParam String cmValue, @RequestParam String code, @RequestParam String comd) {
		return doRemoteCtrl(cmValue, code, OTARemoteCommand.AirConditioning);

	}

	@RequestMapping(value = "/temperatureSet", method = RequestMethod.GET)
	public String temperatureSet(@RequestParam String cmValue, @RequestParam String code, @RequestParam String comd) {
		return doRemoteCtrl(cmValue, code, OTARemoteCommand.TemperatureSetting);

	}

	@RequestMapping(value = "/heatingFR", method = RequestMethod.GET)
	public String heatingFR(@RequestParam String cmValue, @RequestParam String code, @RequestParam String comd) {
		return doRemoteCtrl(cmValue, code, OTARemoteCommand.SeatheatingFrontRight);

	}

	@RequestMapping(value = "/heatingFL", method = RequestMethod.GET)
	public String heatingFL(@RequestParam String cmValue, @RequestParam String code, @RequestParam String comd) {
		return doRemoteCtrl(cmValue, code, OTARemoteCommand.SeatheatingFrontLeft);

	}

	@RequestMapping(value = "/heatingRR", method = RequestMethod.GET)
	public String heatingRR(@RequestParam String cmValue, @RequestParam String code, @RequestParam String comd) {
		return doRemoteCtrl(cmValue, code, OTARemoteCommand.SeatheatingRearRight);

	}

	@RequestMapping(value = "/heatingRL", method = RequestMethod.GET)
	public String heatingRL(@RequestParam String cmValue, @RequestParam String code, @RequestParam String comd) {
		return doRemoteCtrl(cmValue, code, OTARemoteCommand.SeatheatingRearLeft);

	}

	@RequestMapping(value = "/limitSpeed", method = RequestMethod.GET)
	public String limitSpeed(@RequestParam String cmValue, @RequestParam String code, @RequestParam String comd) {
		return doRemoteCtrl(cmValue, code, OTARemoteCommand.LimitSpeed);

	}

	/*
	 * 获取最近一次远程控制结果参数
	 */
	@RequestMapping(value = "/getLastRmtCtrlResult", method = RequestMethod.GET)
	public String getLastRmtCtrlResult(@RequestParam String vin) {
		String serialNumber = "";

		logger.info("Enter getLastRmtCtrlResult, vin:{}", vin);
		// 判断Vin号是否为空
		if (StringUtils.isBlank(vin)) {
			logger.warn("查询最后一次远程控制或拍照结果的请求中，vin不能为空");
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.VIN_REQUIRED, ""));
		} else {
			// 从数据库查询对应Vin号的SN号
			try {
				BaseCar bc = tspPlatformClient.selectBaseCarByVIN(vin);
				if (bc == null) {
					logger.warn("查询最后一次远程控制或拍照结果的请求中，vin {}在数据库中不存在。", vin);
					return JSONObject.toJSONString(new AppJsonResult(ResultStatus.VIN_ERROR, ""));
				}
				serialNumber = bc.getSn();
				return searchRemoteCtrlResult(serialNumber);
			} catch (Exception ex) {
				logger.error("查询最后一次远程控制或拍照结果的请求因异常而失败，vin {},异常：{} ", vin, ThrowableUtil.getErrorInfoFromThrowable(ex));
				return JSONObject.toJSONString(new AppJsonResult(
						ResultStatus.REMOTE_CONTROL_FAILED_SEARCH_RESULT_FOR_EXCEPTION, ""));
			}
		}
	}

	private String searchRemoteCtrlResult(String serialNumber) {
		String result = "";
		try {
			// 从redis获取对应远程控制返回结果
			String respJson = dataProcessing.remoteCtrlRespGet(serialNumber);
			/*GlobalSessionChannel.getRemoteCtrlRes(serialNumber);*/
			// 判断结果是否为空
			if (StringUtils.isBlank(respJson)) {
				logger.info("TBox: {}的最后一次远程控制或拍照查询结果为空", serialNumber);
				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.NOTFOUND, ""));
			}
			// 删除远程控制结果，失败则警告
			if (!dataProcessing.remoteCtrlRespDelete(serialNumber)) {
				logger.warn("TBox: {}的最后一次远程控制或拍照查询结果删除失败", serialNumber);
			}
			RemoteCtrlRespInfo curresult = JSONObject.parseObject(respJson, RemoteCtrlRespInfo.class);
			//将data去掉转义字符
			JSONObject jsonDataResult = JSONObject.parseObject(curresult.getData());
			result = curresult.getResult();

			ResultStatus currentResultStatus = ResultStatus.getResultStatus(result);

			logger.info("TBox: {}的远程控制或拍照结果为：{}", serialNumber, jsonDataResult);
			switch (currentResultStatus) {
				case SUCCESS:
				case RM_TBOX_NOT_REPLY_WITHIN_LIMIT_TIME:
				case RM_SEND_WAKEUP_MSG_FOR_TBOX_AND_NEED_CHECK_RESULT:
				case RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED:
				case RM_TBOX_HAVE_ACCEPT_ONE_COMMAND:
				case RM_REMOTE_CONTROL_CANNOT_DONE_FOR_DRIVE_MODE:
				case RM_REMOTE_CONTROL_CANNOT_DONE_FOR_CAR_IS_DOING_ANOTHER_OP:
				case RM_REMOTE_CONTROL_CANNOT_DONE_FOR_CAR_DOOR_UNCLOSED:
				case RM_REMOTE_CONTROL_CANNOT_DONE_FOR_CAR_DOOR_UNLOCKED:
				case RM_REMOTE_CONTROL_CANNOT_DONE_FOR_OTHER_REASONS:
				case RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG:
					break;
				default:
					currentResultStatus = ResultStatus.NOTFOUND;
			}
			return JSONObject.toJSONString(new AppJsonResult(currentResultStatus, jsonDataResult),
					SerializerFeature.WriteMapNullValue);
		} catch (Exception ex) {
			logger.error("查询最后一次远程控制或拍照结果的请求因异常而失败，TBox {},异常：{} ", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(ex));
			return JSONObject.toJSONString(
					new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_SEARCH_RESULT_FOR_EXCEPTION, ""));
		}
	}

	// 非异步的远程控制流程
	private String doRemoteCtrl(String cmValue, String serialNumber, OTARemoteCommand commd) {
		// 重置本次远程指令的结果状态
		String rcResult = "";
		boolean existRequest = false;
		boolean doWakeUp = false;
		Date curTime = new Date();
		String eventTime = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");
		// 首先得确认code是合法的tbox编号
		try {
			if (!dataProcessing.isTboxValid(serialNumber)) {
				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAR_INFO_ERROR, ""));
			}
			if (this.vin == null) {
				this.vin = tboxService.getVINForTbox(serialNumber);
			}
			// 查询唤醒清单中是否已经存在正在唤醒的请求
			existRequest = dataProcessing.isRequestRmtExist(serialNumber, this.vin);
			doWakeUp = !dataProcessing.onlineTboxExistCheck(serialNumber);
			/*!redisAPI.hasKey(RedisConstant.ONLINE_TBOX, serialNumber);*/
		} catch (Exception e) {
			logger.error("远程控制因发生异常失败。 TBox {}, 异常：{}", serialNumber,
					ThrowableUtil.getErrorInfoFromThrowable(e));
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
		}
		// TBox已经存在相应的远程控制指令
		if (existRequest) {
			// 提示用户不能进行操作
			logger.warn("TBox {} 正在唤醒或执行其他操控，不接受新的请求。", serialNumber);
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.TBOX_HAVE_ACCEPT_ONE_COMMAND, ""));
		} else {
			// 远程控制指令记录于数据库

			int recordID = 0;
			// 存入需要远程控制信息
			htRemoteCtrl.put(serialNumber, TBoxRemoteCtrl.this);
			if (doWakeUp) {
				dataProcessing.checkTBoxLogOutForWakeupWait(serialNumber);
				addWakeUp(serialNumber);
				logger.info("离线TBox {}的唤醒及远程控制请求可以执行 ", serialNumber);
				try {
					recordID = this.tspPlatformClient.recordRemoteCtrl(vin, commd.getMessage(), cmValue);
				} catch (Exception e) {
					logger.error("远程控制因发生异常失败。 TBox {}, 异常：{}", serialNumber,
							ThrowableUtil.getErrorInfoFromThrowable(e));
					redisAPI.delete(RedisConstant.WAKE_UP + "_" + serialNumber);
					htRemoteCtrl.remove(serialNumber);
					return JSONObject.toJSONString(new AppJsonResult(
							ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
				}
				if (!dataProcessing.isSendingMessageSucceed(serialNumber)) {
					// 唤醒失败，直接调用回复
					logger.warn("TBox {}的远程控制因唤醒失败而失败 ", serialNumber);
					// 清除wakeup记录
					redisAPI.delete(RedisConstant.WAKE_UP + "_" + serialNumber);
					htRemoteCtrl.remove(serialNumber);
					return JSONObject.toJSONString(new AppJsonResult(
							ResultStatus.TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED, ""));
				} else {
					logger.info("远程控制流程唤醒短信发送成功并开始等待TBox {}登陆。 ", serialNumber);
					// 等TBox唤醒后在继续，最多等一分钟
					long startTime = 0;
					try {
						startTime = System.currentTimeMillis();
						synchronized (TBoxRemoteCtrl.this) {
							TBoxRemoteCtrl.this.wait(OperationConstant.WAKEUP_WAIT_TIME);
						}
					} catch (InterruptedException e) {
						// 等待唤醒中途如果失败，就必须清除记录，并且
						logger.error("远程控制因发生异常失败。 TBox {}, 异常：{}", serialNumber,
								ThrowableUtil.getErrorInfoFromThrowable(e));
						htRemoteCtrl.remove(serialNumber);
						return JSONObject.toJSONString(new AppJsonResult(
								ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
					} finally {
						redisAPI.delete(RedisConstant.WAKE_UP + "_" + serialNumber);
					}

					// 如果超时，则代表唤醒失败了，直接通知用户
					if (System.currentTimeMillis() - startTime >= OperationConstant.WAKEUP_WAIT_TIME) {
						// 清除wakeup记录
						logger.warn("远程控制失败，因为 TBox {}执行唤醒后未上线。请求时间：{}", serialNumber, eventTime);
						htRemoteCtrl.remove(serialNumber);
						return JSONObject.toJSONString(new AppJsonResult(
								ResultStatus.TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG, ""));
					}
				}
			} else {
				logger.info("在线TBox {}的远程控制请求可以执行 ", serialNumber);
				try {
					recordID = this.tspPlatformClient.recordRemoteCtrl(vin, commd.getMessage(), cmValue);
				} catch (Exception e) {
					logger.error("远程控制因发生异常失败。 TBox {}, 异常：{}", serialNumber,
							ThrowableUtil.getErrorInfoFromThrowable(e));
					htRemoteCtrl.remove(serialNumber);
					return JSONObject.toJSONString(new AppJsonResult(
							ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
				}
			}
			// 继续等远程控制指令响应
			rcResult = doRemoteCtrlWhenTboxOnline(recordID, cmValue, serialNumber, commd, eventTime);
			return rcResult;
		}
	}
	
	/**
     * redis添加  正在唤醒记录
     * @param tboxsn
     * @author zhuna
     * @date 2018年12月4日
     */
    public  void addWakeUp(String tboxsn) {
        // 设置75s超时,清楚redis wake up记录
        try {
            Date date = new Date();
            String currentTime = DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss");
            redisAPI.setValue(RedisConstant.WAKE_UP + "_" + tboxsn, currentTime, OperationConstant.WAKEUP_WAIT_TIME_SEC, TimeUnit.SECONDS);
        } catch (Exception ex) {
            logger.error("Redis connection error, TBox(SN:{}) can't do addWakeUp! The error:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
    }

	//异步处理拍照
	public String takePhotoAnsy(@RequestParam("vin") String vin, @RequestParam("cameraList") String cameraList) {
		String ret = "";
		try {
			ret = checkTakePhotoParam(vin, cameraList);
			if (StringUtils.isEmpty(ret)) {
				ret = doTakePhotoAnsy(vin, this.tboxSN, cameraList);
			}
			return ret;
		} catch (Exception e) {
			logger.error("拍照因发生异常失败。 vin {}, 异常：{}", vin, ThrowableUtil.getErrorInfoFromThrowable(e));
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.FAIL, ""));
		}
	}

	//异步处理拍照流程
	private String doTakePhotoAnsy(String vin, String serialNumber, String cameraList) {
		String result = "";
		boolean existRequest = false;
		boolean doWakeUp = false;
		Date curTime = new Date();
		String eventTime = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");
		// 首先得确认code是合法的tbox编号
		try {
			if (!dataProcessing.isTboxValid(serialNumber)) {
				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAR_INFO_ERROR, ""));
			}
			existRequest = dataProcessing.isRequestRmtAsynExist(serialNumber, vin);
			doWakeUp = !dataProcessing.onlineTboxExistCheck(serialNumber);
					/*!redisAPI.hasKey(RedisConstant.ONLINE_TBOX, serialNumber)*/
		} catch (Exception e) {
			logger.error("拍照请求因发生异常失败。vin:{}, tbox:{}, 异常:{}", vin, serialNumber,
					ThrowableUtil.getErrorInfoFromThrowable(e));
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
		}
		// 对Tbox进行短信唤醒
		if (existRequest) {
			// 提示用户不能进行操作
			logger.warn("TBox {} 正在唤醒或执行其他操控，不接受新的请求。", serialNumber);
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.RM_TBOX_HAVE_ACCEPT_ONE_COMMAND, ""));
		} else {
			// 这里得通过kafka生产一个透传指令下发
			String iccid = "";
			// 存入需要远程控制信息
			htForwardCtrl.put(serialNumber, TBoxRemoteCtrl.this);
			if (doWakeUp) {
				// tbox不在线，需要进行唤醒
				try {
					// 增加唤醒记录
					addWakeUp(serialNumber);
					// 判断是否delay进行唤醒
					dataProcessing.checkTBoxLogOutForWakeupWait(serialNumber);
					// 写入log信息，状态为熄火
					tspPlatformClient.insertCarCaptureLog(vin, eventTime, 1);
					logger.info("离线TBox {}的唤醒及拍照请求可以执行 ", serialNumber);
					// 获取TBox的ICCID
					iccid = this.tspPlatformClient.getTboxVo(serialNumber).getIccid();
					doTakePhotoWhenTboxOfflineAnsy(vin, serialNumber, cameraList, eventTime, iccid);
					return JSONObject.toJSONString(new AppJsonResult(
							ResultStatus.RM_SEND_WAKEUP_MSG_FOR_TBOX_AND_NEED_CHECK_RESULT, ""));
				} catch (Exception e) {
					logger.error("拍照请求因发生异常失败。vin {}, TBox {}, 异常：{}",
							vin, serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
					htForwardCtrl.remove(serialNumber);
					return JSONObject.toJSONString(new AppJsonResult(ResultStatus.FAIL, ""));
				}
			} else {
				logger.info("在线TBox {}的拍照请求可以执行 ", serialNumber);
			}
			result = doTakePhotoWhenTboxOnline(vin, serialNumber, cameraList, eventTime,
					OperationConstant.TAKE_PHOTO_RESPONSE_WAIT_TIME_FOR_ONLINE);
			result = modifiedReturnByPrefix(result);
			return result;
		}
	}

	//tbox离线的拍照处理流程
	private void doTakePhotoWhenTboxOfflineAnsy(String vin, String serialNumber,
			String cameraList, String eventTime, String iccid) {
		// 读入数据库中对应当前tbox的电子围栏
		TspServiceProc.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				// 初始化异步处理结果的类
				RemoteCtrlRespInfo remoteRespInfo = new RemoteCtrlRespInfo();
				remoteRespInfo.setComd(OperationConstant.FORWARD_TAKEPHOTO);
				remoteRespInfo.setReqTime(eventTime);
				remoteRespInfo.setValue("");
				remoteRespInfo.setData("");
				String result = "";
				try {
						dataProcessing.remoteCtrlRespDelete(serialNumber);
						if (!dataProcessing.isSendingMessageSucceed(serialNumber)) {
							// 唤醒失败，直接调用回复
							logger.warn("Vin {}, TBox {}的拍照因唤醒失败而失败 ",
									vin, serialNumber);
							// 清除wakeup记录
							redisAPI.delete(RedisConstant.WAKE_UP + "_" + serialNumber);
							htForwardCtrl.remove(serialNumber);
							result = ResultStatus
									.RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED.getCode();
						} else {
							/*GlobalSessionChannel.removeRemoteCtrlRes(serialNumber);*/
							logger.info("Vin {}拍照流程唤醒短信发送成功并开始等待TBox {}登陆。 ",
									vin, serialNumber);
							// 等tbox唤醒后在继续，最多等一分钟
							long startTime = 0;
							startTime = System.currentTimeMillis();
							synchronized (TBoxRemoteCtrl.this) {
								TBoxRemoteCtrl.this.wait(OperationConstant.WAKEUP_WAIT_TIME);
							}
							// 如果等满了一分钟，则代表唤醒失败了，直接通知用户
							if (System.currentTimeMillis() - startTime >= OperationConstant.WAKEUP_WAIT_TIME) {
								logger.warn("拍照请求失败，因为 TBox {}执行唤醒后未上线。"
												+ "vin:{}， cameraList:{}，请求时间：{}",
										serialNumber, vin, cameraList, eventTime);
								result = ResultStatus.RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG.getCode();
							} else {
								//TBox在时间范围内登陆了
								String tboxResult = doTakePhotoWhenTboxOnline(
										vin, serialNumber, cameraList, eventTime,
										OperationConstant.TAKE_PHOTO_RESPONSE_WAIT_TIME_FOR_OFFLINE);
								result = modifiedStatusByPrefix(tboxResult);
								remoteRespInfo.setData(getDatafromTboxResult(tboxResult));
							}
						}
					} catch (InterruptedException e) {
					logger.error("拍照请求因发生异常失败。vin {}, tBox {}, 异常：{}",
							vin, serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
					//htForwardCtrl.remove(serialNumber);
					result = ResultStatus.FAIL.getCode();
				} finally {
						// 清除wakeup记录
						remoteRespInfo.setResult(result);
						dataProcessing.remoteCtrlRespAdd(serialNumber, remoteRespInfo);
						redisAPI.delete(RedisConstant.WAKE_UP + "_" + serialNumber);
					}
				}
		});
	}

	private void updateCarCaptureStatus(String vin, String shootID, int status) {
		try {
			this.tspPlatformClient.updateCarCaptureStatus(vin, shootID, status);
		} catch (Exception ex) {
			logger.error("拍照状态更新数据库失败。vin: {} 拍照序列号 : {}, 异常: {}",
					vin, shootID, ThrowableUtil.getErrorInfoFromThrowable(ex));
		}
	}

	//处理在线tbox拍照流程
	private String doTakePhotoWhenTboxOnline(String vin, String serialNumber,
			String cameraList, String eventTime, long waitRespTime) {
		String operResult = "";
		String operName = OperationConstant.FORWARD_TAKEPHOTO;
		this.tboxOnlineStatusWhenCtrl = true;
		try {
			// tbox上线，将其移除唤醒，进一步发送远程控制
			if (existWakeUp(serialNumber)) {
				redisAPI.delete(RedisConstant.WAKE_UP + "_" + serialNumber);
			}
			/*if (GlobalSessionChannel.existWakeUp(serialNumber, OperationConstant.FORWARD_TAKEPHOTO)) {
				GlobalSessionChannel.removeWakeUp(serialNumber, OperationConstant.FORWARD_TAKEPHOTO);
			}*/
			eventTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
			addCommandSend(serialNumber, operName);
			// 插入数据库有关抓拍信息
			tspPlatformClient.insertCarCapture(vin, eventTime);
			// 写入log信息，状态为拍照时间
			tspPlatformClient.insertCarCaptureLog(vin, eventTime,
					OperationConstant.TAKE_PHOTO_LOG_EVENT_TYPE_TAKE_PHOTO_TIME);
			//logger.info("Record log for car capture create date. vin: " + vin + " event date: " + eventTime);
			kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_FORWARD_DOWN,
					serialNumber + "_" + OperationConstant.FORWARD_TAKEPHOTO
							+ "_" + cameraList + "_" + eventTime, serialNumber);
			// 发送远程控制命令后，等待回包10s
			String shootID = OpCarRemoteControl.getShootIDByEventTime(eventTime);
			long startTime = 0;
			startTime = System.currentTimeMillis();
			synchronized (TBoxRemoteCtrl.this) {
				TBoxRemoteCtrl.this.wait(waitRespTime);
			}
			if (System.currentTimeMillis() - startTime >= waitRespTime) {
				// 超过10s回复结果
				logger.warn("TBox {}没有在有限时间 {}毫秒范围内返回拍照结果。 vin：{}， cameraList： {}，请求时间：{}。",
						serialNumber, waitRespTime, vin, cameraList, eventTime);
				updateCarCaptureStatus(vin, shootID, OperationConstant.TAKEPHOTO_FAILED_FOR_OTHER_REASON);
				return JSONObject
						.toJSONString(new AppJsonResult(ResultStatus.TBOX_NOT_REPLY_WITHIN_LIMIT_TIME, ""));
			}
			// 获取设置的结果
			String[] kafkaReturn = htForwardCtrl.get(serialNumber).result.split("_");
			// 获取tbox返回结果，设置相应的拍照序列号后，再用于返回
			TakePhotoRespInfo stateValue = JSONObject.parseObject(kafkaReturn[0], TakePhotoRespInfo.class);
			stateValue.setTakePhotoID(shootID);
			// 更新数据库中的拍照状态
			updateCarCaptureStatus(vin, shootID, Integer.parseInt(kafkaReturn[1]));
			operResult = JSONObject.toJSONString(new AppJsonResult(ResultStatus.SUCCESS, stateValue));
			logger.info("TBox {}已经返回拍照结果{}。vin：{}，请求时间：{}", serialNumber, JSONObject.toJSONString(stateValue), vin, eventTime);			;
		} catch (Exception e) {
			logger.error("拍照请求因发生异常失败。vin {}, TBox {}, 异常：{}",
					vin, serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.FAIL, ""));
		} finally {
			// 可以获取到本次远程指令的处理结果了，该结果由RemoteCtrlListener的listenRes方法来设置
			htForwardCtrl.remove(serialNumber);
			if (redisAPI.hasKey(RedisConstant.COMMAND_SEND + "_" + serialNumber)) {
				redisAPI.removeKey(RedisConstant.COMMAND_SEND + "_" + serialNumber);
			}
		}
		return operResult;
	}
	
	/**
     * 根据操作删除相应唤醒指令
     * @param tboxsn
     * @param operName
     */
    public void removeWakeUp(String tboxsn, String operName) {
        try {
            redisAPI.delete(RedisConstant.WAKE_UP + "_" + operName + "_" + tboxsn);
        } catch (Exception ex) {
            logger.error("Redis connection error,TBox({}) can't do removeWakeUp:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
    }
    
    /**
     * 添加实时位置信息
     *
     * @Title: addWakeUp
     * @Description: 根据操作指令添加相应唤醒记录
     * @param: @param
     *             tboxsn
     * @param: @param
     *             reportPos
     * @return: void
     * @throws @author
     *             余佶
     * @Date 2017年8月13日 下午3:04:49
     */
    public void addWakeUp(String tboxsn, String operName) {
        // 设置90s超时
        try {
            Date operTime = new Date();
            redisAPI.setValue(RedisConstant.WAKE_UP + "_" + operName + "_" + tboxsn, String.valueOf(operTime.getTime()),
                    OperationConstant.WAKEUP_WAIT_TIME_SEC, TimeUnit.SECONDS);
        } catch (Exception ex) {
            logger.error("Redis connection error,TBox({}) can't do addWakeUp:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
    }
    
	//同步处理拍照
	private String doTakePhoto(String vin, String serialNumber, String cameraList) {
		String result = "";
		boolean existRequest = false;
		boolean doWakeUp = false;
		Date curTime = new Date();
		String eventTime = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");
		// 首先得确认code是合法的tbox编号
		try {
			if (!dataProcessing.isTboxValid(serialNumber)) {
				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAR_INFO_ERROR, ""));
			}
			existRequest = dataProcessing.isRequestTakePhotoExist(serialNumber, vin);
			doWakeUp = !dataProcessing.onlineTboxExistCheck(serialNumber);
			/*!redisAPI.hasKey(RedisConstant.ONLINE_TBOX, serialNumber);*/
		} catch (Exception e) {
			logger.error("拍照请求因发生异常失败。vin {}, TBox {}, 异常：{}",
					vin, serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
		}
		//存在正在执行的下发指令
		if (existRequest) {
			// 提示用户不能进行操作
			logger.warn("TBox {} 正在唤醒或执行其他操控，不接受新的请求。", serialNumber);
			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.TBOX_HAVE_ACCEPT_ONE_COMMAND, ""));
		} else {
			// 这里得通过kafka生产一个透传指令下发
			// 存入需要远程控制信息
			htForwardCtrl.put(serialNumber, TBoxRemoteCtrl.this);
			if (doWakeUp) {
				// tbox不在线，需要进行唤醒
				try {
					// 增加唤醒记录
					addWakeUp(serialNumber, OperationConstant.FORWARD_TAKEPHOTO);
					// 判断是否delay进行唤醒
					dataProcessing.checkTBoxLogOutForWakeupWait(serialNumber);
					// 写入log信息，状态为熄火
					tspPlatformClient.insertCarCaptureLog(vin, eventTime, 1);
					logger.info("离线TBox {}的唤醒及拍照请求可以执行 ", serialNumber);
				} catch (Exception e) {
					logger.error("拍照请求因发生异常失败。vin {}, TBox {}s, 异常：{}",
							vin, serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));					htForwardCtrl.remove(serialNumber);
					return JSONObject.toJSONString(new AppJsonResult(ResultStatus.FAIL, ""));
				}
				// 对Tbox进行短信唤醒
				if (!dataProcessing.isSendingMessageSucceed(serialNumber)) {
					// 唤醒失败，直接调用回复
					logger.warn("Vin {}, TBox {}的拍照因唤醒失败而失败 ", vin, serialNumber);
					// 清除wakeup记录
					removeWakeUp(serialNumber, OperationConstant.FORWARD_TAKEPHOTO);
					htForwardCtrl.remove(serialNumber);
					return JSONObject.toJSONString(new AppJsonResult(
							ResultStatus.TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED, ""));
				} else {
					logger.info("Vin {}拍照流程唤醒短信发送成功并等待TBox {}登陆。 ", vin, serialNumber);
					// 等TBox唤醒后在继续，最多等一分钟
					long startTime = 0;
					try {
						startTime = System.currentTimeMillis();
						synchronized (TBoxRemoteCtrl.this) {
							TBoxRemoteCtrl.this.wait(OperationConstant.WAKEUP_WAIT_TIME);
						}
					} catch (InterruptedException e) {
						logger.error("拍照请求因发生异常失败。vin {}, TBox {}, 异常：{}",
								vin, serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
						htForwardCtrl.remove(serialNumber);
						return JSONObject.toJSONString(new AppJsonResult(ResultStatus.FAIL, ""));
					} finally {
						// 清除wakeup记录
						redisAPI.delete(RedisConstant.WAKE_UP + "_" + serialNumber);
					}
					// 如果等满了一分钟，则代表唤醒失败了，直接通知用户
					if (System.currentTimeMillis() - startTime >= OperationConstant.WAKEUP_WAIT_TIME) {
						logger.warn("拍照请求失败，因为 TBox {}执行唤醒后未上线。vin:{}， cameraList:{}，请求时间:{}",
								serialNumber, vin, cameraList, eventTime);
						htForwardCtrl.remove(serialNumber);
						return JSONObject.toJSONString(new AppJsonResult(
								ResultStatus.TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG, ""));
					} else {
						result = doTakePhotoWhenTboxOnline(vin, serialNumber, cameraList, eventTime,
								OperationConstant.TAKE_PHOTO_RESPONSE_WAIT_TIME_FOR_OFFLINE);
					}
				}
			} else {
				logger.info("在线TBox {}的拍照请求可以执行 ", serialNumber);
				result = doTakePhotoWhenTboxOnline(vin, serialNumber, cameraList, eventTime,
						OperationConstant.TAKE_PHOTO_RESPONSE_WAIT_TIME_FOR_ONLINE);
			}
			return result;
		}
	}

}
