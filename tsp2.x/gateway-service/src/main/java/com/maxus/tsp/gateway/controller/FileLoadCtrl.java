package com.maxus.tsp.gateway.controller;
//
//import com.alibaba.fastjson.JSONObject;
//import com.maxus.tsp.common.constant.KafkaMsgConstant;
//import com.maxus.tsp.common.enums.ResultStatus;
//import com.maxus.tsp.common.util.ThrowableUtil;
//import com.maxus.tsp.gateway.common.constant.OperationConstant;
//import com.maxus.tsp.gateway.common.model.DownLoadFileMo;
//import com.maxus.tsp.gateway.common.model.TboxFileLoadStatus;
//import com.maxus.tsp.gateway.common.model.UpLoadFileMo;
//import com.maxus.tsp.gateway.common.ota.GlobalSessionChannel;
//import com.maxus.tsp.gateway.mq.kafka.KafkaProducer;
//
//import com.maxus.tsp.gateway.service.DataProcessing;
//import com.maxus.tsp.gateway.service.TboxService;
//import com.maxus.tsp.platform.service.model.AppJsonResult;
//import org.apache.commons.lang.StringUtils;
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
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Hashtable;
//
///**
// * 文件操作ctrl
// *
// * @author lzgea
// *
// */
//@RestController
//@RequestMapping(value = "/tsp/api")
//@Scope(value = "prototype")
public class FileLoadCtrl {
//	private final Logger logger = LogManager.getLogger(getClass());
//
//	@Value("${wakeUpDelayTime:10}")
//	private int wakeUpDelayTime;
//	@Autowired
//	private KafkaProducer kafkaService;
//
//	@Autowired
//	private TboxService tboxService;
//	// 解决对在线Tbox远控请求,回复周期内（10s）Tbox频繁登录登出造成的线程通知错误；
//	private boolean tboxOnlineStatusWhenCtrl = false;
//
//	public boolean getTboxOnlineStatusWhenCtrl() {
//		return tboxOnlineStatusWhenCtrl;
//	}
//
//	/**
//	 * 保存本节点处理远程控制请求的实例
//	 */
//	private static Hashtable<String, FileLoadCtrl> fileLoadCtrl = new Hashtable<>();
//
//	public static FileLoadCtrl getFileLoadCtrl(String tboxsn) {
//		return fileLoadCtrl.get(tboxsn);
//	}
//
//	/**
//	 * 文件下载
//	 *
//	 * @param downLoadFile
//	 * @return
//	 */
//	@RequestMapping(value = "/downLoadFileRVM", method = RequestMethod.POST)
//	public AppJsonResult downLoadFile(@RequestBody DownLoadFileMo downLoadFile) {
//
//		// 检查参数
//		AppJsonResult result = checkdownLoadFileParam(downLoadFile);
//		if (null != result) {
//			return result;
//		}
//		// 检查是否在线
//		result = checkOnlineTbox(downLoadFile.getSn(), "downLoadFile");
//		if (null != result) {
//			return result;
//		}
//		result = doDownLoadFile(downLoadFile);
//		return result;
//	}
//
//	@RequestMapping(value = "/upLoadFileRVM", method = RequestMethod.POST)
//	public AppJsonResult upLoadFile(@RequestBody UpLoadFileMo upLoadFile) {
//
//		// 检查参数
//	AppJsonResult result = checkupLoadFileParam(upLoadFile);
//		if (null != result) {
//		return result;
//	}
//	// 检查是否在线
//	result = checkOnlineTbox(upLoadFile.getSn(), "upLoadFile");
//		if (null != result) {
//		return result;
//	}
//	result = doUpLoadFile(upLoadFile);
//		return result;
//}
//
//	private AppJsonResult checkupLoadFileParam(UpLoadFileMo upLoadFile) {
//		// 检查参数
//		if (null == upLoadFile) {
//			logger.warn("TBox upLoadFile：上传文件指令参数为空！");
//			return new AppJsonResult(ResultStatus.UPLOAD_FILE_PARAM_WARONG , "");
//		}
//		// 文件类型1有效
//		if (upLoadFile.getFileType() != 1 && upLoadFile.getFileType() != 2) {
//			logger.warn("TBox(SN:{})upLoadFile：文件类型为无效值！", upLoadFile.getSn());
//			 return new AppJsonResult(ResultStatus.UPLOAD_FILE_PARAM_WARONG, "");
//		}
//		// 首先得确认sn是合法的TBox编号
//		if (!DataProcessing.isTboxValid(upLoadFile.getSn())) {
//			 return new AppJsonResult(ResultStatus.CAR_INFO_ERROR, "");
//		}
//		return null;
//	}
//
//	/*
//	 * 在线下发指令
//	 */
//	private AppJsonResult doUpLoadFile(UpLoadFileMo upLoadFile) {
//		String sn = upLoadFile.getSn();
//		String cmd = "upLoadFile";
//		try {
//			// redis是否记录此次远程配置
//			TboxFileLoadStatus updateInfo = new TboxFileLoadStatus();
//			updateInfo.setSeqNo(upLoadFile.getSeqNo());
//			updateInfo.setEventTime(upLoadFile.getCurrentTime());
//
//			if (GlobalSessionChannel.existTboxUpLoadFile(sn) != null) {
//				updateInfo.setResult(JSONObject.parseObject(GlobalSessionChannel.existTboxUpLoadFile(sn),
//						TboxFileLoadStatus.class).getResult());
//			}
//			//GlobalSessionChannel.setTboxUpLoadFile(sn, JSONObject.toJSONString(updateInfo));
//			GlobalSessionChannel.addCommandSend(sn, JSONObject.toJSONString(updateInfo));
//			logger.info("记录此次请求上传文件指令至Redis：TBoxSn:{},Value:{}", sn,
//					JSONObject.toJSONString(upLoadFile));
//			// TBox上线，将其移除唤醒
//			if (GlobalSessionChannel.existWakeUp(sn)) {
//				GlobalSessionChannel.removeWakeUp(sn);
//			}
//			String dateTime = DateFormatUtils.format(upLoadFile.getCurrentTime(), "yyyy-MM-dd HH:mm:ss");
//			kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_UP_LOAD_FILE_REQ,
//					sn + "_" + JSONObject.toJSONString(upLoadFile) + "_" + dateTime, sn);
//			tboxOnlineStatusWhenCtrl = true;
//			return new AppJsonResult(ResultStatus.SUCCESS, "");
//		} catch (Exception ex) {
//			logger.error("TBox(SN:{})在线下发指令({})操作因发生异常而失败，异常原因:{}", sn, cmd, ThrowableUtil.getErrorInfoFromThrowable(ex));
//		}
//		return null;
//	}
//
//
//	/*
//	 * 在线下发指令
//	 */
//	private AppJsonResult doDownLoadFile(DownLoadFileMo downLoadFile) {
//		String sn = downLoadFile.getSn();
//		String cmd = "downLoadFile";
//		try {
//			// redis是否记录此次远程配置
//			TboxFileLoadStatus updateInfo = new TboxFileLoadStatus();
//			updateInfo.setSeqNo(downLoadFile.getSeqNo());
//			updateInfo.setEventTime(downLoadFile.getCurrentTime());
//
//			//if (GlobalSessionChannel.existTboxDownLoadFile(sn) != null) {
//			if (GlobalSessionChannel.existCommandSend(sn)) {
//				updateInfo.setResult(JSONObject.parseObject(GlobalSessionChannel.existTboxDownLoadFile(sn),
//						TboxFileLoadStatus.class).getResult());
//			}
//			//GlobalSessionChannel.setTboxDownLoadFile(sn, JSONObject.toJSONString(updateInfo));
//			GlobalSessionChannel.addCommandSend(sn, JSONObject.toJSONString(updateInfo));
//			logger.info("记录此次请求下载文件指令至Redis：TBoxSn:{},Value:{}", sn,
//					JSONObject.toJSONString(downLoadFile));
//			// TBox上线，将其移除唤醒
//			if (GlobalSessionChannel.existWakeUp(sn)) {
//				GlobalSessionChannel.removeWakeUp(sn);
//			}
//			String dateTime = DateFormatUtils.format(downLoadFile.getCurrentTime(), "yyyy-MM-dd HH:mm:ss");
//			kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_DOWM_LOAD_FILE_REQ,
//					sn + "_" + JSONObject.toJSONString(downLoadFile) + "_" + dateTime, sn);
//			tboxOnlineStatusWhenCtrl = true;
//			return new AppJsonResult(ResultStatus.SUCCESS, "");
//		} catch (Exception ex) {
//			logger.error("TBox(SN:{})在线下发指令({})因发生异常而失败，异常原因:{}", sn, cmd, ThrowableUtil.getErrorInfoFromThrowable(ex));
//		}
//		return null;
//	}
//
//	/**
//	 * 唤醒Tbox服务
//	 * @return
//	 */
//	private AppJsonResult checkOnlineTbox(String sn, String cmd) {
//		boolean doWakeUp = false;
//		try {
//			String vin = tboxService.getVINForTbox(sn);
//			if (StringUtils.isEmpty(vin)) {
//				logger.warn("TBox(SN:{})VIN号为空或异常，vin: {}", sn, vin);
//			}
//
//			// Tbox是否在线
//			doWakeUp = !DataProcessing.onlineTboxExistCheck(sn);
//		} catch (Exception ex) {
//			logger.error("文件操作指令发生异常失败。cmd:{}, TBox {}, 异常：{}", cmd, sn,
//					ThrowableUtil.getErrorInfoFromThrowable(ex));
//			return new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, "");
//		}
//		fileLoadCtrl.put(sn, FileLoadCtrl.this);
//		if (doWakeUp) {
//			checkTBoxLogOutForWakeupWait(sn);
//			// 将TBOX添加到唤醒队列中
//			GlobalSessionChannel.addWakeUp(sn);
//
//			logger.info("离线TBox {}的唤醒及{}请求可以执行 ", sn, cmd);
//			logger.info("开始异步发送唤醒短信并等待TBox {}登录返回处理结果。 ", sn);
//			if (!DataProcessing.isSendingMessageSucceed(sn)) {
//				// 唤醒失败直接通知
//				logger.warn("TBox {}的{}因唤醒失败而失败 ", sn, cmd);
//				GlobalSessionChannel.removeWakeUp(sn);
//				fileLoadCtrl.remove(sn);
//				return new AppJsonResult(ResultStatus.RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED, "");
//			} else {
//				logger.info("{}流程唤醒短信发送成功并开始等待TBox {}登陆。 ", cmd, sn);
//				long startTime = 0;
//				try {
//					startTime = System.currentTimeMillis();
//					synchronized (FileLoadCtrl.this) {
//						FileLoadCtrl.this.wait(OperationConstant.WAKEUP_WAIT_TIME);
//					}
//				} catch (InterruptedException e) {
//					// 等待唤醒中途如果失败，就必须清除记录，并且
//					logger.error("{}因发生异常失败。 TBox {}, 异常：{}", cmd, sn,
//							ThrowableUtil.getErrorInfoFromThrowable(e));
//					fileLoadCtrl.remove(sn);
//					return new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, "");
//				} finally {
//					GlobalSessionChannel.removeWakeUp(sn);
//				}
//
//				// 如果超时，则代表唤醒失败了，直接通知用户
//				if (System.currentTimeMillis() - startTime >= OperationConstant.WAKEUP_WAIT_TIME) {
//					logger.warn("{}失败，因为 TBox {}执行唤醒后未上线。", cmd, sn);
//					fileLoadCtrl.remove(sn);
//					return new AppJsonResult(ResultStatus.RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG, "");
//				} else {
//					// TBox在线
//					logger.info("TBox {}上线了，{}请求可以执行 ", sn, cmd);
//				}
//			}
//		}
//		return null;
//	}
//
//	/**
//	 * 检查请求参数是否合法
//	 *
//	 * @param downLoadFile
//	 * @return
//	 */
//	private AppJsonResult checkdownLoadFileParam(DownLoadFileMo downLoadFile) {
//		// 检查参数
//		if (null == downLoadFile) {
//			logger.warn("TBox downLoadFile：下载文件参数为空！");
//			return new AppJsonResult(ResultStatus.DOWNLOAD_FILR_PARAM_WRONG, "");
//		}
//		// 文件类型1有效
//		if (downLoadFile.getFileType() != 1) {
//			logger.warn("TBox(SN:{})downLoadFile：文件类型为无效值！", downLoadFile.getSn());
//			 return new AppJsonResult(ResultStatus.DOWNLOAD_FILR_PARAM_WRONG, "");
//		}
//		if (null == downLoadFile.getUrl()) {
//			logger.warn("TBox(SN:{})downLoadFile：url为空！", downLoadFile.getSn());
//			 return new AppJsonResult(ResultStatus.DOWNLOAD_FILR_PARAM_WRONG, "");
//		}
//		if (null == downLoadFile.getMd5Data()) {
//			logger.warn("TBox(SN:{})downLoadFile：文件md5为空！", downLoadFile.getSn());
//			return new AppJsonResult(ResultStatus.DOWNLOAD_FILR_PARAM_WRONG, "");
//		}
//		// 首先得确认sn是合法的tbox编号
//		if (!DataProcessing.isTboxValid(downLoadFile.getSn())) {
//			 return new AppJsonResult(ResultStatus.CAR_INFO_ERROR, "");
//		}
//		return null;
//	}
//
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
//					logger.info("由于TBox {}刚发送登出报文，唤醒请求将延迟  {}毫秒", serialNumber, diff);
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
