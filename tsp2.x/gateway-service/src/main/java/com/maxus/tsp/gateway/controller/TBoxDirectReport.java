package com.maxus.tsp.gateway.controller;
//
//import java.util.Hashtable;
//
//import com.maxus.tsp.gateway.service.DataProcessing;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Scope;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.maxus.tsp.common.constant.KafkaMsgConstant;
//import com.maxus.tsp.common.enums.ResultStatus;
//import com.maxus.tsp.common.util.DateUtil;
//import com.maxus.tsp.common.util.ThrowableUtil;
//import com.maxus.tsp.gateway.common.constant.OperationConstant;
//import com.maxus.tsp.gateway.common.ota.GlobalSessionChannel;
//import com.maxus.tsp.gateway.mq.kafka.KafkaProducer;
//import com.maxus.tsp.platform.service.model.AppJsonResult;
//
///**
// * 国家平台直连，服务平台进行请求，将国家平台的地址（ip，端口）等信息，通过网关发送给tbox。
// * @author uwczo
// *
// */
//@RestController
//@RequestMapping(value = { "/tsp/appapi"})
//@Scope(value = "prototype")
public class TBoxDirectReport {
//	private final Logger logger = LogManager.getLogger(getClass());
//
//
//	@Autowired
//	private KafkaProducer kafkaService;
//
//
//	/**
//	 * Tbox响应的报文处理结果
//	 */
//	private String result;
//
//	/**
//	 * 远程控制对应的车辆绑定序列号
//	 */
//	private String tboxSN;
//
//	/**
//	 * port的格式
//	 */
//	private static final String PORT_REGEX = "^[0-9]*$";
//	/**
//	 * IP(域名)与端口号的分隔符
//	 */
//	private static final String IP_PORT_DEVIDE_MASK = ":";
//	/**
//	 * @param result
//	 *            the result to set
//	 */
//	public void setResult(String result) {
//		this.result = result;
//	}
//
//	/**
//	 * 保存本节点处理处理国家平台直连的实例
//	 */
//	private static Hashtable<String, TBoxDirectReport> htDirectReport = new Hashtable<>();
//
//	/**
//	 * 获得指定tbox的处理国家平台平台直连的实例
//	 *
//	 * @return the htRemoteCtrl
//	 */
//	public static TBoxDirectReport getDirectReport(String tboxsn) {
//		return htDirectReport.get(tboxsn);
//	}
//
//	@RequestMapping(value = "/directReportRVM", method = { RequestMethod.GET, RequestMethod.POST })
//	public AppJsonResult directReportRVM(String serialNumber, String value, long eventTime) {
//		AppJsonResult ret = null;
//		try {
//			// 检查tbox序列号是否为空
//			if (StringUtils.isBlank(serialNumber)) {
//				logger.warn("国家平台直连直连中TBox序列号为空.");
//				return new AppJsonResult(ResultStatus.CAR_INFO_ERROR, "");
//			}
//			this.tboxSN = serialNumber;
//			// 检查value参数是否为空
//			if (StringUtils.isBlank(value)) {
//				logger.warn("国家平台直连value参数为空。  serialNumber {}", this.tboxSN);
//				return new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
//			}
//			ret = doTboxDirectReport(this.tboxSN, value, eventTime);
//			return ret;
//		} catch (Exception e) {
//			logger.error("TBox{}的国家平台直连操作因异常失败，异常：{}", serialNumber,
//					ThrowableUtil.getErrorInfoFromThrowable(e));
//			return new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, "");
//		}
//	}
//
//
//	public boolean isDirectReportRequestExist(String serialNumber, String eventTime) {
//		//判断tbox是否已经存在正在执行的国家平台直连请求
//		boolean existRequest = false;
//		//如果Redis可以访问，使用redis进行查询
//		logger.warn("TBox {} 的正在下发指令存在状态：{}", serialNumber,
//				GlobalSessionChannel.existCommandSend(serialNumber));
//		existRequest = GlobalSessionChannel.existCommandSend(serialNumber);
//		return existRequest;
//	}
//
//	private AppJsonResult doTboxDirectReport(String serialNumber, String value, long eventTime) {
//	    String eventTimeStr = DateUtil.longToDateStr(eventTime);
//	    AppJsonResult result = null;
//		// 首先得确认code是合法的tbox编号
//		if (!DataProcessing.isTboxValid(serialNumber)) {
//			return new AppJsonResult(ResultStatus.CAR_INFO_ERROR, "");
//		}
//		// 这里得通过kafka生产一个透传指令下发
//		boolean doWakeUp = !DataProcessing.onlineTboxExistCheck(serialNumber);
//
//		if (doWakeUp) {
//			logger.info("TBox{}不在线, 无法执行国家平台直连的操作", serialNumber);
//			return new AppJsonResult(ResultStatus.DIRECT_REPORT_FAILED_FOR_TBOX_OFFLINE, "");
//		} else if (isDirectReportRequestExist(serialNumber, String.valueOf(eventTime))) {
//			logger.info("TBox{}正在执行国家平台直连操作，不能接受新的请求", serialNumber);
//			return new AppJsonResult(ResultStatus.RM_TBOX_HAVE_ACCEPT_ONE_COMMAND, "");
//		} else {
//			logger.info("TBox{}在线, 可以执行国家平台直连指令的操作", serialNumber);
//			// 继续等远程控制指令响应
//			try {
//				htDirectReport.put(serialNumber, this);
//				GlobalSessionChannel.addCommandSend(serialNumber, String.valueOf(eventTime));
//				//获取最新版本url
//				if (!value.equals(OperationConstant.REMOTE_CTRL_FUNCTION_CLOSED)) {
//					//判断url参数是否合法
//					int portOffset = value.lastIndexOf(IP_PORT_DEVIDE_MASK);
//					if (portOffset <= 0) {
//						logger.warn("国家平台直连指令错误 value:{}, TBox {}。", value, serialNumber);
//						result = new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, "");
//					} else {
//						String url = value.substring(0, portOffset);
//						String port = value.substring(portOffset + 1, value.length());
//						if (!port.matches(PORT_REGEX)) {
//							logger.warn("国家平台直连指令端口号错误:{}, TBox {}。", value, serialNumber);
//							result = new AppJsonResult(ResultStatus.REMOTE_CONTROL_INVALID_COMMAND, "");
//						} else {
//							//启动
//							kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_TBOX_DIRECT_REPORT_START,
//									serialNumber + "_" + url + "_" + port + "_" + eventTimeStr, serialNumber);
//						}
//					}
//				} else {
//					//停止
//					kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_TBOX_DIRECT_REPORT_STOP,
//							serialNumber + "_" + eventTimeStr, serialNumber);
//				}
//				if (result == null) {
//					//确认可以等待结果
//					//发送远程控制命令后，等待回包10s
//					long startTime = 0;
//					startTime = System.currentTimeMillis();
//					synchronized (TBoxDirectReport.this) {
//						TBoxDirectReport.this.wait(OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME);
//					}
//					if (System.currentTimeMillis() - startTime >= OperationConstant.REMOTE_CONTROL_RESPONSE_WAIT_TIME) {
//						// 超过10s回复结果
//						logger.warn("国家平台直连操作失败，因为 TBox {}没有及时回复远程控制报文。请求时间：{}",
//								serialNumber, eventTime);
//						result = new AppJsonResult(ResultStatus.RM_TBOX_NOT_REPLY_WITHIN_LIMIT_TIME, "");
//					} else {
//						//10s内回复结果
//						if (this.result == null) {
//							if (value.equals(OperationConstant.REMOTE_CTRL_FUNCTION_CLOSED)) {
//								result = new AppJsonResult(ResultStatus.DIRECT_REPORT_STOP_FAILED, "");
//							} else {
//								result = new AppJsonResult(ResultStatus
//										.DIRECT_REPORT_START_FAILED_FOR_OTHER_REASON, "");
//							}
//						} else if (this.result.equals(ResultStatus.SUCCESS.getCode())) {
//							result = new AppJsonResult(ResultStatus.SUCCESS, "");
//						} else {
//							result = new AppJsonResult(ResultStatus.getResultStatus(this.result), "");
//						}
//					}
//				}
//			} catch (Exception e) {
//				logger.error("TBox{}的国家平台直连操作因异常失败，异常：{}", serialNumber,
//						ThrowableUtil.getErrorInfoFromThrowable(e));
//				result = new AppJsonResult(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION, "");
//			} finally {
//				htDirectReport.remove(serialNumber);
//				GlobalSessionChannel.removeCommandSend(serialNumber);
//			}
//			return result;
//		}
//	}
}
