package com.maxus.tsp.gateway.controller;
//
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Hashtable;
//
//import com.maxus.tsp.gateway.service.DataProcessing;
//import org.apache.commons.lang3.StringUtils;
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
//import com.maxus.tsp.gateway.common.model.TboxFileLoadStatus;
//import com.maxus.tsp.gateway.common.model.TboxUpdateRvmReq;
//import com.maxus.tsp.gateway.common.ota.GlobalSessionChannel;
//import com.maxus.tsp.gateway.mq.kafka.KafkaProducer;
//import com.maxus.tsp.gateway.serviceclient.TspPlatformClient;
//import com.maxus.tsp.platform.service.model.AppJsonResult;
//import com.maxus.tsp.platform.service.model.vo.TboxUpdateInfoVo;
//
///**
// * 远程升级（含元61oms服务调用接口，及蜘蛛智行项目rvm调用接口）
// *
// * @author uwczo
// *
// */
//@RestController
//@RequestMapping(value = { "/tsp/appapi" })
//@Scope(value = "prototype")
public class TBoxUpdateCtrl {
//	private final Logger logger = LogManager.getLogger(getClass());
//
//	@Autowired
//	private KafkaProducer kafkaService;
//
//	@Autowired
//	private TspPlatformClient tspPlatformClient;
//
//	@Value("${wakeUpDelayTime:10}")
//	private int wakeUpDelayTime;
//
//	public static TBoxUpdateCtrl getTBoxUpdateCtrl(String tboxsn) {
//		return tBoxUpdateCtrl.get(tboxsn);
//	}
//
//	private static Hashtable<String, TBoxUpdateCtrl> tBoxUpdateCtrl = new Hashtable<>();
//	// 解决对在线Tbox远控请求,回复周期内（10s）Tbox频繁登录登出造成的线程通知错误；
//	private boolean tboxOnlineStatusWhenCtrl = false;
//
//	public boolean getTboxOnlineStatusWhenCtrl() {
//		return tboxOnlineStatusWhenCtrl;
//	}
//
//	/**
//	 * 基于升级包id的远程升级（原61项目oms调用的服务接口）
//	 *
//	 * @param serialNumber
//	 * @param id
//	 * @return
//	 */
//	@RequestMapping(value = "/tboxUpdate", method = { RequestMethod.GET, RequestMethod.POST })
//	public String tboxUpdate(String serialNumber, int id) {
//		String ret = "";
//		try {
//			// 检查tbox参数是否为空
//			if (StringUtils.isBlank(serialNumber)) {
//				logger.warn("远程升级的TBox序列号为空.");
//				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAR_INFO_ERROR, ""));
//			}
//			if (id <= 0) {
//				logger.warn("远程升级的升级包id不正确:{}", serialNumber);
//				return JSONObject.toJSONString(
//						new AppJsonResult(ResultStatus.REMOTE_UPDATE_FAILED_FOR_WITHOUT_CORRECT_PACKAGE, ""));
//			}
//			ret = doTboxUpdate(serialNumber, id);
//			return ret;
//		} catch (Exception e) {
//			logger.error("TBox({})升级发生异常:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
//			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_UPDATE_FAILED_FOR_EXCEPTION, ""));
//		}
//	}
//
//	/**
//	 * 私有方法，升级tbox唤醒业务
//	 *
//	 * @param serialNumber
//	 * @param id
//	 * @return
//	 */
//	private String doTboxUpdate(String serialNumber, int id) {
//		// 首先得确认code是合法的tbox编号
//		if (!DataProcessing.isTboxValid(serialNumber)) {
//			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAR_INFO_ERROR, ""));
//		}
//		// 这里得通过kafka生产一个透传指令下发
//		boolean doWakeUp = !DataProcessing.onlineTboxExistCheck(serialNumber);
//
//		tBoxUpdateCtrl.put(serialNumber, TBoxUpdateCtrl.this);
//		if (doWakeUp) {
//			// 检查TBOX是否符合唤醒条件
//			checkTBoxLogOutForWakeupWait(serialNumber);
//
//			// 将TBOX添加到唤醒队列中
//			GlobalSessionChannel.addWakeUp(serialNumber);
//			logger.info("离线TBox {}的唤醒及远程配置请求可以执行 ", serialNumber);
//			logger.info("开始异步发送唤醒短信并等待TBox {}登陆返回处理结果。 ", serialNumber);
//			if (!DataProcessing.isSendingMessageSucceed(serialNumber)) {
//				// 唤醒失败直接通知
//				logger.warn("TBox {}的远程升级因唤醒失败而失败 ", serialNumber);
//				GlobalSessionChannel.removeWakeUp(serialNumber);
//				tBoxUpdateCtrl.remove(serialNumber);
//				return JSONObject
//						.toJSONString(new AppJsonResult(ResultStatus.RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED, ""));
//			} else {
//				// 执行等待
//				logger.info("远程升级流程唤醒短信发送成功并开始等待TBox {}登录。 ", serialNumber);
//
//				long startTime = 0;
//				try {
//					startTime = System.currentTimeMillis();
//					synchronized (TBoxUpdateCtrl.this) {
//						TBoxUpdateCtrl.this.wait(OperationConstant.WAKEUP_WAIT_TIME);
//					}
//				} catch (InterruptedException e) {
//					// 等待唤醒中途如果失败，就必须清除记录，并且
//					logger.error("远程升级因发生异常失败。 TBox {}, 异常：{}", serialNumber,
//							ThrowableUtil.getErrorInfoFromThrowable(e));
//					tBoxUpdateCtrl.remove(serialNumber);
//					return JSONObject
//							.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
//				} finally {
//					GlobalSessionChannel.removeWakeUp(serialNumber);
//				}
//
//				// 如果超时，则代表唤醒失败了，直接通知用户
//				if (System.currentTimeMillis() - startTime >= OperationConstant.WAKEUP_WAIT_TIME) {
//					logger.warn("远程升级失败，因为 TBox {}执行唤醒后未上线。", serialNumber);
//					tBoxUpdateCtrl.remove(serialNumber);
//					return JSONObject
//							.toJSONString(new AppJsonResult(ResultStatus.RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG, ""));
//				} else {
//					// TBOX在线
//					logger.info("TBox {}上线了，远程配置请求可以执行 ", serialNumber);
//					// 继续等远程控制指令响应
//					return doUpdataTboxOnline(serialNumber, id);
//				}
//			}
//		} else {
//			logger.info("TBox({})在线，升级信息将进行发送。", serialNumber);
//			// 继续等远程控制指令响应
//			return doUpdataTboxOnline(serialNumber, id);
//		}
//	}
//	// 原先61升级方式
//		private String doUpdataTboxOnline(String serialNumber, int id) {
//			String result="";
//			try {
//				// 获取最新版本url
//				TboxUpdateInfoVo updateInfo = this.tspPlatformClient.getTboxUpdateInfo(id);
//				if (updateInfo == null) {
//					result = JSONObject.toJSONString(
//							new AppJsonResult(ResultStatus.REMOTE_UPDATE_FAILED_FOR_EMPTY_UPDATE_INFO, ""));
//				} else {
//					String version = updateInfo.getUpgrade_version();
//					String url = updateInfo.getUpgrade_url();
//					String md5 = updateInfo.getMd5();
//					if (md5.length() != OperationConstant.MD5_SIZE || url.length() < 0 || version.length() < 0) {
//						logger.warn("TBox({})远程升级信息错误. 请确认数据库中的版本号, url及 md5信息.", serialNumber);
//						result = JSONObject.toJSONString(
//								new AppJsonResult(ResultStatus.REMOTE_UPDATE_FAILED_FOR_ERROR_UPDATE_INFO, ""));
//					} else {
//						this.tspPlatformClient.updateVerStatus(serialNumber, -1);
//						kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_TBOX_UPDATE,
//								serialNumber + "_" + version + "_" + url + "_" + md5, serialNumber);
//						result = JSONObject.toJSONString(new AppJsonResult(ResultStatus.SUCCESS, ""));
//					}
//				}
//			} catch (Exception e) {
//				logger.error("原61升级因发生异常失败，原因:{}", ThrowableUtil.getErrorInfoFromThrowable(e));
//			}
//			return result;
//		}
//
//	/**
//	 * RVM调用的tbox升级接口
//	 *
//	 * @param tboxUpdateRvmReq
//	 * @return
//	 */
//	@RequestMapping(value = "/tboxUpdateRvm", method = { RequestMethod.POST })
//	public AppJsonResult tboxUpdateRvm(@RequestBody TboxUpdateRvmReq tboxUpdateRvmReq) {
//		if (tboxUpdateRvmReq == null) {
//			return new AppJsonResult(ResultStatus.REMOTE_UPDATE_FAILED_FOR_ERROR_UPDATE_INFO, "");
//		}
//		String serialNumber = tboxUpdateRvmReq.getSn();
//		String version = tboxUpdateRvmReq.getVersion();
//		String url = tboxUpdateRvmReq.getUrl();
//		String md5 = tboxUpdateRvmReq.getMd5();
//		try {
//			// 检查参数
//			String checkParam = checkParam(serialNumber, version, url, md5);
//			if (checkParam != null) {
//				return JSONObject.parseObject(checkParam, AppJsonResult.class);
//			}
//
//			return JSONObject.parseObject(doTboxUpdateRVM(tboxUpdateRvmReq), AppJsonResult.class);
//
//		} catch (Exception e) {
//			logger.error("TBox({})升级发生异常:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
//			return new AppJsonResult(ResultStatus.REMOTE_UPDATE_FAILED_FOR_EXCEPTION, "");
//		}
//	}
//	/**
//	 * RVM唤醒业务
//	 * @param tboxUpdateRvmReq
//	 * @return
//	 */
//	private String doTboxUpdateRVM(TboxUpdateRvmReq tboxUpdateRvmReq) {
//		String serialNumber = tboxUpdateRvmReq.getSn();
//		// 首先得确认code是合法的tbox编号
//		if (!DataProcessing.isTboxValid(serialNumber)) {
//			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAR_INFO_ERROR, ""));
//		}
//		// 这里得通过kafka生产一个透传指令下发
//		boolean doWakeUp = !DataProcessing.onlineTboxExistCheck(serialNumber);
//
//		tBoxUpdateCtrl.put(serialNumber, TBoxUpdateCtrl.this);
//		if (doWakeUp) {
//			// 检查TBOX是否符合唤醒条件
//			checkTBoxLogOutForWakeupWait(serialNumber);
//			// 将TBox添加到唤醒队列中
//			GlobalSessionChannel.addWakeUp(serialNumber);
//			logger.info("离线TBox {}的唤醒及远程配置请求可以执行 ", serialNumber);
//			logger.info("开始异步发送唤醒短信并等待TBox {}登陆返回处理结果。 ", serialNumber);
//			if (!DataProcessing.isSendingMessageSucceed(serialNumber)) {
//				// 唤醒失败直接通知
//				logger.warn("TBox {}的远程升级因唤醒失败而失败 ", serialNumber);
//				GlobalSessionChannel.removeWakeUp(serialNumber);
//				tBoxUpdateCtrl.remove(serialNumber);
//				return JSONObject
//						.toJSONString(new AppJsonResult(ResultStatus.RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED, ""));
//			} else {
//				// 执行等待
//				logger.info("远程升级流程唤醒短信发送成功并开始等待TBox {}登录。 ", serialNumber);
//
//				long startTime = 0;
//				try {
//					startTime = System.currentTimeMillis();
//					synchronized (TBoxUpdateCtrl.this) {
//						TBoxUpdateCtrl.this.wait(OperationConstant.WAKEUP_WAIT_TIME);
//					}
//				} catch (InterruptedException e) {
//					// 等待唤醒中途如果失败，就必须清除记录，并且
//					logger.error("远程升级因发生异常失败。 TBox {}, 异常：{}", serialNumber,
//							ThrowableUtil.getErrorInfoFromThrowable(e));
//					tBoxUpdateCtrl.remove(serialNumber);
//					return JSONObject
//							.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
//				} finally {
//					GlobalSessionChannel.removeWakeUp(serialNumber);
//				}
//
//				// 如果超时，则代表唤醒失败了，直接通知用户
//				if (System.currentTimeMillis() - startTime >= OperationConstant.WAKEUP_WAIT_TIME) {
//					logger.warn("远程升级失败，因为 TBox {}执行唤醒后未上线。", serialNumber);
//					tBoxUpdateCtrl.remove(serialNumber);
//					return JSONObject
//							.toJSONString(new AppJsonResult(ResultStatus.RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG, ""));
//				} else {
//					// TBOX在线
//					logger.info("TBox {}上线了，远程配置请求可以执行 ", serialNumber);
//					// 继续等远程控制指令响应
//					return doUpdataTboxOnlineRVM(tboxUpdateRvmReq);
//				}
//			}
//		} else {
//			logger.info("TBox({})在线，升级信息将进行发送。", serialNumber);
//			// 继续等远程控制指令响应
//			return doUpdataTboxOnlineRVM(tboxUpdateRvmReq);
//		}
//	}
//
//	/**
//	 * RVM在线执行
//	 * @param tboxUpdateRvmReq
//	 * @return
//	 */
//	private String doUpdataTboxOnlineRVM(TboxUpdateRvmReq tboxUpdateRvmReq) {
//		logger.info("TBox({})在线，升级信息将进行发送。", tboxUpdateRvmReq.getSn());
//		// 通过存储在redis中的数据判断是否下发Tbox远程升级指令
//		TboxFileLoadStatus updateInfo = new TboxFileLoadStatus();
//		// 更新成最新的seqnum和发起时间
//		updateInfo.setSeqNo(tboxUpdateRvmReq.getSeqNo());
//		updateInfo.setEventTime(tboxUpdateRvmReq.getEventTime());
//
//		//if (GlobalSessionChannel.existRemoteUpdate(tboxUpdateRvmReq.getSn())) {
//		if (GlobalSessionChannel.existCommandSend(tboxUpdateRvmReq.getSn())) {
//			updateInfo.setResult(JSONObject.parseObject(GlobalSessionChannel.getRemoteUpdate(tboxUpdateRvmReq.getSn()),
//					TboxFileLoadStatus.class).getResult());
//		}
//		//GlobalSessionChannel.addRemoteUpdate(tboxUpdateRvmReq.getSn(), JSONObject.toJSONString(updateInfo));
//		GlobalSessionChannel.addCommandSend(tboxUpdateRvmReq.getSn(), JSONObject.toJSONString(updateInfo));
//
//		kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_TBOX_UPDATE,
//				tboxUpdateRvmReq.getSn() + "_" + tboxUpdateRvmReq.getVersion() + "_" + tboxUpdateRvmReq.getUrl() + "_" + tboxUpdateRvmReq.getMd5(), tboxUpdateRvmReq.getSn());
//		tboxOnlineStatusWhenCtrl = true;
//		return JSONObject.toJSONString(new AppJsonResult(ResultStatus.SUCCESS, ""));
//	}
//
//	private String checkParam(String serialNumber, String version, String url, String md5) {
//		// 检查tbox参数是否为空
//		if (StringUtils.isBlank(serialNumber)) {
//			logger.warn("远程升级的TBox序列号为空.");
//			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAR_INFO_ERROR, ""));
//		}
//		// 确认sn是合法的tbox编号
//		if (!DataProcessing.isTboxValid(serialNumber)) {
//			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAR_INFO_ERROR, ""));
//		}
//		// 检查tbox参数是否为空
//		if (StringUtils.isBlank(version)) {
//			logger.warn("远程升级的version为空.");
//			return JSONObject
//					.toJSONString(new AppJsonResult(ResultStatus.REMOTE_UPDATE_FAILED_FOR_ERROR_UPDATE_INFO, ""));
//		}
//		// 检查tbox参数是否为空
//		if (StringUtils.isBlank(url)) {
//			logger.warn("远程升级的url为空.");
//			return JSONObject
//					.toJSONString(new AppJsonResult(ResultStatus.REMOTE_UPDATE_FAILED_FOR_ERROR_UPDATE_INFO, ""));
//		}
//		// 检查tbox参数是否为空
//		if (StringUtils.isBlank(md5)) {
//			logger.warn("远程升级的md5为空.");
//			return JSONObject
//					.toJSONString(new AppJsonResult(ResultStatus.REMOTE_UPDATE_FAILED_FOR_ERROR_UPDATE_INFO, ""));
//		}
//		// 确认版本号, url及 md5信息
//		if (md5.length() != OperationConstant.MD5_SIZE || url.length() < 0 || version.length() < 0) {
//			logger.warn("TBox({})远程升级信息错误. 请确认版本号, url及 md5信息.", serialNumber);
//			return JSONObject
//					.toJSONString(new AppJsonResult(ResultStatus.REMOTE_UPDATE_FAILED_FOR_ERROR_UPDATE_INFO, ""));
//		}
//		try {
//			// 检查TboxSN是否合法
//			if (!DataProcessing.isTboxValid(serialNumber)) {
//				return JSONObject.toJSONString(new AppJsonResult(ResultStatus.CAR_INFO_ERROR, ""));
//			}
//		} catch (Exception ex) {
//			logger.error("远程配置因发生异常失败。 TBox {}, 异常：{}", serialNumber,
//					ThrowableUtil.getErrorInfoFromThrowable(ex));
//			return JSONObject.toJSONString(new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, ""));
//		}
//		return null;
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
//					logger.warn("由于TBox {}刚发送登出报文，唤醒请求将延迟 {}毫秒", serialNumber, diff);
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
