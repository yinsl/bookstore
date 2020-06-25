/**
 * WakeUpTask.java Create on 2017年7月17日
 * Copyright (c) 2017年7月17日 by 上汽集团商用车技术中心
 *
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>
 * @version 1.0
 */
package com.maxus.tsp.gateway.timer;

import java.util.concurrent.TimeUnit;

import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.enums.ResultStatus;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.model.BaseRmtCtrlItReq;
import com.maxus.tsp.gateway.common.model.HomeCtrlItRequest;
import com.maxus.tsp.gateway.common.model.RemoteCtrlItResponse;
import com.maxus.tsp.gateway.service.KafkaService;
import com.maxus.tsp.gateway.service.TboxService;
import com.maxus.tsp.platform.service.model.AppJsonResult;

/**
 * 唤醒计时器,当需要下发短信唤醒的时候启动定时器，在时间到达时检查一下操作结果报文是否已经收到，没有收到则给It发送超时响应
 */
public class WakeUpTask extends BaseTask {

	/**
	 * 启动75秒超时检查
	 * 
	 * @param kafkaService
	 * @param rmtRequest
	 * @param tboxService
	 */
	public void start() {
		logger.info("TBox({})不在线，开始执行计时器，等待唤醒和房车家居远控上行报文回复，共计时85秒", rmtRequest.getSn());

		// 不在线计时器，在线等待房车家居远控回包记录写入redis
		tboxService.setRedisRMTTimeoutKey(rmtRequest.getSeqNo() + rmtRequest.getSn());
		// 设置短信唤醒+等待报文回复时间:75s+10s,超时执行run发送kafka消息超时
		threadPool.schedule(this, OperationConstant.WAKEUP_WAIT_TIME_SEC
		        + OperationConstant.REMOTECONTROL_RESP_EXPIRED_TIME, TimeUnit.SECONDS);
	}

	/**
	 *
	 * 检查操作结果是否
	 */
	private TboxService tboxService;

	/**
	 * 设置kafkaService与redisAPI
	 * 
	 * @param kafkaService
	 * @param rmtRequest
	 */
	public WakeUpTask(KafkaService kafkaService, BaseRmtCtrlItReq rmtRequest, TboxService tboxService) {
		this.kafkaService = kafkaService;
		this.rmtRequest = rmtRequest;
		this.tboxService = tboxService;
	}

	/**
	 * 执行一次超时判断
	 */
	@Override
	public void run() {
		// 等待检查结果
		boolean existFlag = tboxService.existTimeoutKey(this.rmtRequest.getSeqNo() + this.rmtRequest.getSn());
		// 等到最后一秒还没有取消，需要给It投递处理超时接口
		if (existFlag) {
			// 做一个简单的分布式锁（该锁有缺陷），防止多节点情况下同时给tsp平台发送超时信息
			long opFlag = redisAPI.incrementExpire(RedisConstant.TIMEOUT_OP_LOCK + "_" + rmtRequest.getSeqNo()
			        + rmtRequest.getSn(), 1, 5000);
			if (opFlag == 1) {
				RemoteCtrlItResponse remoteCtrlItResponse = new RemoteCtrlItResponse();
				remoteCtrlItResponse.setSn(rmtRequest.getSn());
				remoteCtrlItResponse.setSeqNo(rmtRequest.getSeqNo());
				remoteCtrlItResponse.setEventTime(rmtRequest.getEventTime());

				AppJsonResult appJsonResult = new AppJsonResult(ResultStatus.RM_TBOX_NOT_REPLY_WITHIN_LIMIT_TIME, null);
				remoteCtrlItResponse.setStatus(appJsonResult.getStatus());
				remoteCtrlItResponse.setData(appJsonResult.getData());
				remoteCtrlItResponse.setDescription(appJsonResult.getDescription());
				logger.warn("rvm的房车家居远控请求消息超时，不执行:{},{},{},{}", rmtRequest.getSn(), rmtRequest.getSeqNo(),
				        rmtRequest.getEventTime(), ResultStatus.OP_UNDO_FOR_REQUEST_TIME_EXPIRED);
				// reids删除排他指令记录
				redisAPI.removeKey(RedisConstant.COMMAND_SEND + "_" + rmtRequest.getSn());
				kafkaService.transferRmtResponse(remoteCtrlItResponse);
				// 移除掉超时标志位
				redisAPI.removeHash(RedisConstant.RMT_TIMEOUT, this.rmtRequest.getSeqNo() + this.rmtRequest.getSn());
				if (HomeCtrlItRequest.class.isInstance(rmtRequest))
					kafkaService.transferHomeCtrlResponse(remoteCtrlItResponse);
				tboxService.rmRedisRMTTimeoutKey(this.rmtRequest.getSeqNo() + this.rmtRequest.getSn());
			}
		}
	}
}
