package com.maxus.tsp.gateway.mq.kafka;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.KafkaMsgConstant;
import com.maxus.tsp.common.constant.MqttConstant;
import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.enums.ResultStatus;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.DateUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.model.BaseRmtCtrlItReq;
import com.maxus.tsp.gateway.common.model.RemoteCtrlItResponse;
import com.maxus.tsp.gateway.common.model.RmtGroupRequestInfo;
import com.maxus.tsp.gateway.common.ota.OTAMessage;
import com.maxus.tsp.gateway.common.ota.RmtGroupData;
import com.maxus.tsp.gateway.conf.MqttConfiguration.MyGateway;
import com.maxus.tsp.gateway.service.DataProcessing;
import com.maxus.tsp.gateway.service.KafkaService;
import com.maxus.tsp.gateway.timer.RmtGroupTask;
import com.maxus.tsp.gateway.timer.SMSWakeUpTask;
import com.maxus.tsp.platform.service.model.AppJsonResult;

/**
 * @ClassName RmtGroupListener
 * @Description 监听组合远控kafka
 * @Author zijhm
 * @Date 2019/1/31 20:41
 * @Version 1.0
 **/
public class RmtGroupListener extends BaseListener {
	private static final Logger logger = LogManager.getLogger(RmtGroupListener.class);

	@Autowired
    private DataProcessing dataProcessing;
	
	@Autowired
	private KafkaService kafkaService;

	@Autowired
	private RedisAPI redisAPI;

	@Autowired
	private MyGateway myGateway;

	/**
	 * @Description 监听itKafka获取组合远控的请求信息
	 * @Date 2019/1/31 21:00
	 * @Param [record]
	 * @return void
	 **/
	@KafkaListener(topics = KafkaMsgConstant.TOPIC_IT_RM_GROUP_REQ, containerFactory = "KafkaItContainer")
	public void listenRmtGroupCtrl(ConsumerRecord<?, ?> record) {
		logger.debug("kafka日志测试-组合远控");
		try {
			Optional<?> kafkaMessage = Optional.ofNullable(record.value());
			if (kafkaMessage.isPresent()) {
				String message = (String) kafkaMessage.get();
				// 获取组合远控请求信息内容
				RmtGroupRequestInfo rmtGroupRequestInfo = JSONObject.parseObject(message, RmtGroupRequestInfo.class);
				if (rmtGroupRequestInfo == null) {
					logger.warn("rvm当前组合远控请求kafka数据为空!");
				} else {
					logger.debug("rvm当前组合远控请求kafka参数为:{}", JSONObject.toJSONString(rmtGroupRequestInfo));

					String serialNumber = rmtGroupRequestInfo.getSn();
					// 最终返回给IT的JSON
					AppJsonResult appJsonResult;
					try {
						// 判断处理kafka消息的时间是否超过10s
						if (!DateUtil.timeDifference(rmtGroupRequestInfo.getEventTime())) {
							logger.warn("TBox(SN:{})执行组合远控的过程中处理it kafka消息时间超过10s", serialNumber);
							// 最终返回给IT的JSON
							appJsonResult = new AppJsonResult(ResultStatus.OP_UNDO_FOR_REQUEST_TIME_EXPIRED, "");
							replayRmtGroupToIt(serialNumber, appJsonResult, rmtGroupRequestInfo);
						} else {
							logger.info("TBox(SN:{})开始执行组合远控参数校验!", serialNumber);
							// 检验组合远控的信息, 若校验无误, 则开始执行组合远控
							redisAPI.setValue(RedisConstant.RMT_GROUP_CTRL_REQ,
							        JSONObject.toJSONString(rmtGroupRequestInfo), 90000, TimeUnit.SECONDS);

							serialNumber = rmtGroupRequestInfo.getSn();
							if (StringUtils.isBlank(serialNumber)) {
								logger.warn("TBox当前组合远控参数sn为空!");
								appJsonResult = new AppJsonResult(ResultStatus.PARAM_NULL, "");
								replayRmtGroupToIt(serialNumber, appJsonResult, rmtGroupRequestInfo);
								return;
							}
							if (!dataProcessing.isTboxValid(serialNumber)) {
								logger.warn("TBox(SN:{})当前组合远控请求参数sn不可用!", serialNumber);
								appJsonResult = new AppJsonResult(ResultStatus.CAR_INFO_ERROR, "");
								replayRmtGroupToIt(serialNumber, appJsonResult, rmtGroupRequestInfo);
								return;
							}
							String otaType = rmtGroupRequestInfo.getOtaType();
							if (StringUtils.isBlank(otaType)) {
								logger.warn("TBox(SN:{})当前组合远控请求参数otaType为空!", serialNumber);
								appJsonResult = new AppJsonResult(ResultStatus.PARAM_NULL, "");
								replayRmtGroupToIt(serialNumber, appJsonResult, rmtGroupRequestInfo);
								return;
							}
							if (!"REMOTECTRL".equals(otaType) && !"REMOTECTRL_EXT".equals(otaType)) {
								logger.warn("TBox(SN:{})当前组合控制请求参数otaType错误!", serialNumber);
								appJsonResult = new AppJsonResult(ResultStatus.PARAM_ERROR, "");
								replayRmtGroupToIt(serialNumber, appJsonResult, rmtGroupRequestInfo);
								return;
							}
							if ("REMOTECTRL".equals(otaType)) {
								String comd = rmtGroupRequestInfo.getComd();
								if (StringUtils.isBlank(comd)) {
									logger.warn("TBox(SN:{})当前组合控制请求参数comd为空!", serialNumber);
									appJsonResult = new AppJsonResult(ResultStatus.PARAM_NULL, "");
									replayRmtGroupToIt(serialNumber, appJsonResult, rmtGroupRequestInfo);
									return;
								}
								if (comd.length() < 2 || comd.length() > 4 || !comd.matches("[a-fA-F0-9]+")) {
									logger.warn("TBox(SN:{})当前组合请求参数comd错误!", serialNumber);
									appJsonResult = new AppJsonResult(ResultStatus.PARAM_ERROR, "");
									replayRmtGroupToIt(serialNumber, appJsonResult, rmtGroupRequestInfo);
									return;
								}
								String value = rmtGroupRequestInfo.getValue();
								if (StringUtils.isBlank(value)) {
									logger.warn("TBox(SN:{})当前组合远控请求参数value为空!", serialNumber);
									appJsonResult = new AppJsonResult(ResultStatus.PARAM_NULL, "");
									replayRmtGroupToIt(serialNumber, appJsonResult, rmtGroupRequestInfo);
									return;
								}
								if (value.length() < 2 || value.length() > 4 || !value.matches("[a-fA-F0-9]+")
								        || value.length() % 2 != 0) {
									logger.warn("TBox(SN:{})当前组合远控请求参数value错误!", serialNumber);
									appJsonResult = new AppJsonResult(ResultStatus.PARAM_ERROR, "");
									replayRmtGroupToIt(serialNumber, appJsonResult, rmtGroupRequestInfo);
									return;
								}
								String temperature = rmtGroupRequestInfo.getTemperature();
								if (!StringUtils.isBlank(temperature)) {
									if (temperature.length() != 2 || !temperature.matches("[a-fA-F0-9]+")) {
										logger.warn("TBox(SN:{})当前组合远控请求参数temperature错误!", serialNumber);
										appJsonResult = new AppJsonResult(ResultStatus.PARAM_ERROR, "");
										replayRmtGroupToIt(serialNumber, appJsonResult, rmtGroupRequestInfo);
										return;
									}
								}
							} else if ("REMOTECTRL_EXT".equals(otaType)) {
								int parameSize = rmtGroupRequestInfo.getParamSize();
								if (parameSize == 0) {
									logger.warn("TBox(SN:{})当前组合控制请求参数parameSize为空!", serialNumber);
									appJsonResult = new AppJsonResult(ResultStatus.PARAM_NULL, "");
									replayRmtGroupToIt(serialNumber, appJsonResult, rmtGroupRequestInfo);
									return;
								} else {
									String param = rmtGroupRequestInfo.getParam();
									if (StringUtils.isBlank(param)) {
										logger.warn("TBox(SN:{})当前组合远控请求参数param为空!", serialNumber);
										appJsonResult = new AppJsonResult(ResultStatus.PARAM_NULL, "");
										replayRmtGroupToIt(serialNumber, appJsonResult, rmtGroupRequestInfo);
										return;
									}
									if (param.length() % 2 != 0 || !param.matches("[a-fA-F0-9]+")
									        || param.length() != parameSize * 2) {
										logger.warn("TBox(SN:{})当前组合远控请求参数param错误!", serialNumber);
										appJsonResult = new AppJsonResult(ResultStatus.PARAM_ERROR, "");
										replayRmtGroupToIt(serialNumber, appJsonResult, rmtGroupRequestInfo);
										return;
									}
								}
							}
							String seqNo = rmtGroupRequestInfo.getSeqNo();
							if (StringUtils.isBlank(seqNo)) {
								logger.warn("TBox(SN:{})当前组合远控请求seqNo为空!", serialNumber);
								appJsonResult = new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
								replayRmtGroupToIt(serialNumber, appJsonResult, rmtGroupRequestInfo);
								return;
							}
							if (rmtGroupRequestInfo.getEventTime() == 0) {
								logger.warn("TBox(SN:{})当前组合远控请求参数eventTime为0!", serialNumber);
								appJsonResult = new AppJsonResult(ResultStatus.REMOTE_CONTROL_PARAM_REQUIRED, "");
								replayRmtGroupToIt(serialNumber, appJsonResult, rmtGroupRequestInfo);
								return;
							}
							doRmtGroup(rmtGroupRequestInfo);
						}

					} catch (Exception e) {
						logger.error("TBox(SN:{})当前组合远控请求因发生异常失败, 异常原因:{}", serialNumber,
						        ThrowableUtil.getErrorInfoFromThrowable(e));
					}
				}
			}
		} catch (Exception e) {
			logger.error("网关监听rvm获取当前组合远控的请求信息因发生异常失败, 异常原因:{}", ThrowableUtil.getErrorInfoFromThrowable(e));
		}
	}

	private void replayRmtGroupToIt(String serialNumber, AppJsonResult appJsonResult,
	        RmtGroupRequestInfo rmtGroupRequestInfo) {
		// 封转组合远控的结果返回给RVM
		RemoteCtrlItResponse rmtGroupItResp = new RemoteCtrlItResponse();
		rmtGroupItResp.setSn(serialNumber);
		rmtGroupItResp.setStatus(appJsonResult.getStatus());
		rmtGroupItResp.setData(appJsonResult.getData());
		rmtGroupItResp.setDescription(appJsonResult.getDescription());
		rmtGroupItResp.setSeqNo(rmtGroupRequestInfo.getSeqNo());
		rmtGroupItResp.setEventTime(rmtGroupRequestInfo.getEventTime());
		logger.debug("TBox(SN:{})组合远控返回结果为:{}", serialNumber, JSONObject.toJSONString(rmtGroupItResp));
		kafkaService.sndMesToITForTemplate(KafkaMsgConstant.TOPIC_IT_RM_GROUP_RESP, rmtGroupItResp, serialNumber);
	}

	/**
	 * @Description 参数校验通过后执行组合远控
	 * @Date 2019/2/1 14:07
	 * @Param [rmtGroupRequestInfo]
	 * @return com.maxus.tsp.platform.service.model.AppJsonResult
	 **/
	private void doRmtGroup(RmtGroupRequestInfo rmtGroupRequestInfo) {
		// 标记TBox是否需要唤醒
		boolean doWakeUp;
		String serialNumber = rmtGroupRequestInfo.getSn();
		// 检查当前TBox的状态, 确保当前TBox控制指令唯一且TBox在线
		if (dataProcessing.isRmtGroupCtrlExist(serialNumber)) {
			// TBox存在控制指令或正处于唤醒中
			logger.warn("TBox(SN:{})已经存在控制指令, 或者当前TBox正处于唤醒中!", serialNumber);
			replayRmtGroupToIt(serialNumber, new AppJsonResult(ResultStatus.RM_TBOX_HAVE_ACCEPT_ONE_COMMAND, ""),
			        rmtGroupRequestInfo);
			return;
		} else {
			// 将当前组合远控的控制信息存入内存
			// htRmtGroup.put(serialNumber, RmtGroupUtil.this);
			doWakeUp = !redisAPI.hasKey(RedisConstant.ONLINE_TBOX, serialNumber);
			if (doWakeUp) {
				// TBox不在线, 需要进行唤醒
				dataProcessing.checkTBoxLogOutForWakeupWait(serialNumber);
				// 在redis中存入当前TBox的唤醒指令
				Date date = new Date();
				String currentTime = DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss");
				redisAPI.setValue(RedisConstant.WAKE_UP + "_" + serialNumber, RedisConstant.RM_GROUP_CTRL + "_" + currentTime,
				        OperationConstant.WAKEUP_WAIT_TIME_SEC, TimeUnit.SECONDS);
				
				logger.info("[组合远控]TBox(SN:{})离线唤醒可以执行!", serialNumber);
				logger.info("[组合远控]TBox(SN:{})开始离线唤醒, 异步发送唤醒短信等待返回结果中...", serialNumber);
				if (!dataProcessing.isSendingMessageSucceed(serialNumber)) {
					logger.warn("[组合远控]TBox(SN:{})短信唤醒失败!", serialNumber);
					// 移除redis中的唤醒指令
					redisAPI.delete(RedisConstant.WAKE_UP + "_" + serialNumber);
					// 移除内存中的控制指令
					// htRmtGroup.remove(serialNumber);
					replayRmtGroupToIt(serialNumber, new AppJsonResult(
					        ResultStatus.RM_TBOX_OFF_LINE_AND_SEND_WAKEUP_MSG_FAILED, ""), rmtGroupRequestInfo);
					return;
				} else {
					logger.info("[组合远控]TBox(SN:{})唤醒短信发送成功!", serialNumber);
					BaseRmtCtrlItReq rmtRequest = new BaseRmtCtrlItReq();
					rmtRequest.setEventTime(rmtGroupRequestInfo.getEventTime());
					rmtRequest.setSeqNo(rmtGroupRequestInfo.getSeqNo());
					rmtRequest.setSn(rmtGroupRequestInfo.getSn());
					new SMSWakeUpTask(kafkaService, rmtRequest, redisAPI).start();
				}
			} else {
				// TBox在线, 直接处理当前组合远控的请求
				logger.info("TBox(SN:{})在线, 可以执行当前组合远控的请求!", serialNumber);
				try {
					doWhenTBoxOnline(rmtGroupRequestInfo);
				} catch (Exception e) {
					logger.error("TBox(SN:{})在线直接执行组合远控因发生异常而失败, 异常原因:{}", serialNumber,
					        ThrowableUtil.getErrorInfoFromThrowable(e));
					replayRmtGroupToIt(serialNumber, new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, ""),
					        rmtGroupRequestInfo);
				}
			}
		}
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
	private boolean addCommandSend(String serialNumber, String value) {
			Date date = new Date();
			String currentTime = DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss");
			return redisAPI.setValueWithEspireTime(RedisConstant.COMMAND_SEND + "_" + serialNumber, value + "_"
			        + currentTime, OperationConstant.REMOTECONTROL_RESP_EXPIRED_TIME, TimeUnit.SECONDS);
	}

	/**
	 * @Description TBox在线或唤醒后执行当前的远程控制指令
	 * @Date 2019/2/4 16:02
	 * @Param [rmtGroupRequestInfo]
	 * @return com.maxus.tsp.platform.service.model.AppJsonResult
	 **/
	private void doWhenTBoxOnline(RmtGroupRequestInfo rmtGroupRequestInfo) {
		String serialNumber = rmtGroupRequestInfo.getSn();
		// 在线时的正常处理业务流程
		try {
			logger.info("TBox(SN:{})确定当前TBox在线, 将本次控制指令存储在Redis中!", serialNumber);
			addCommandSend(serialNumber, RedisConstant.RM_GROUP_CTRL);
			// 当前TBox在线, 判断redis中是否存在唤醒指令
			if (redisAPI.hasKey(RedisConstant.WAKE_UP + "_" + serialNumber)) {
				logger.info("TBox(SN:{})在线并且redis中存在唤醒指令, 清除唤醒指令!", serialNumber);
				redisAPI.delete(RedisConstant.WAKE_UP + "_" + serialNumber);
			}
			// 通过网关kafka发送一条消息, 用作内部通信
			// 默认下发时间
			String downTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
			// 将远程组合控制的网关下发topic存入到redis
			if (rmtGroupRequestInfo.getOtaType().equals("REMOTECTRL")) {
				redisAPI.setValue(OperationConstant.RM_GROUP_CTRL + serialNumber,
				        KafkaMsgConstant.TOPIC_SELF_RM_GROUP_DOWN);
			} else if (rmtGroupRequestInfo.getOtaType().equals("REMOTECTRL_EXT")) {
				redisAPI.setValue(OperationConstant.RM_GROUP_CTRL + serialNumber,
				        KafkaMsgConstant.TOPIC_SELF_RM_EXT_DOWN);
			}

			String[] cmd = { serialNumber, JSONObject.toJSONString(rmtGroupRequestInfo), downTime };

			// 开始组装下发报文
			OTAMessage sendOTAMsg = RmtGroupData.getSendDataForRmtGroup(cmd);
			if (sendOTAMsg != null) {
				if (sendOTAMsg.curMessage != null && sendOTAMsg.curMessage.length > 0) {
					logger.info("TBox(SN:{})开始下发组合远控EXT报文:{}", serialNumber, ByteUtil.byteToHex(sendOTAMsg.curMessage));
					// 写入报文
					String curVer = redisAPI.getHash(RedisConstant.CUR_OTA_VERSION, serialNumber);
					String replyTopic = curVer + MqttConstant.MQTT_GW + serialNumber;
//					myGateway.sendToMqtt(Asn1Util.encodeSeq(sendOTAMsg.curMessage), replyTopic, 1);
					logger.info("响应报文：" + String.valueOf(sendOTAMsg.curMessage));
					myGateway.sendToMqtt(sendOTAMsg.curMessage, replyTopic, 1);
					// 开启超时定时器，到房车指令下发回包，给定10秒，并设置redis全局标志位
					BaseRmtCtrlItReq rmtRequest = new BaseRmtCtrlItReq();
					rmtRequest.setSn(serialNumber);
					rmtRequest.setEventTime(rmtGroupRequestInfo.getEventTime());
					rmtRequest.setSeqNo(rmtGroupRequestInfo.getSeqNo());
					new RmtGroupTask(kafkaService, rmtRequest, redisAPI).start();
				} else {
					logger.warn("TBox(SN:{})组合远控EXT指令下发失败:{}", serialNumber, cmd[0] + "_" + cmd[1] + "_" + cmd[2]);
				}
			} else {
				logger.warn("TBox(SN:{})组装组合远控EXT报文出错, 组装后的报文为空!", serialNumber);
			}
		} catch (Exception e) {
			logger.error("TBox(SN:{})组合远控操作因发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
			replayRmtGroupToIt(serialNumber, new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, ""),
			        rmtGroupRequestInfo);
		}
	}

}
