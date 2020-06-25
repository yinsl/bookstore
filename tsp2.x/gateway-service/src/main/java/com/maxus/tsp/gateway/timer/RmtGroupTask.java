package com.maxus.tsp.gateway.timer;

import java.util.concurrent.TimeUnit;

import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.enums.ResultStatus;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.model.BaseRmtCtrlItReq;
import com.maxus.tsp.gateway.common.model.RemoteCtrlItResponse;
import com.maxus.tsp.gateway.service.KafkaService;
import com.maxus.tsp.platform.service.model.AppJsonResult;

public class RmtGroupTask extends BaseTask {

	/**
	 * 设置kafkaService与redisAPI
	 * 
	 * @param kafkaService
	 * @param rmtRequest
	 */
	public RmtGroupTask(KafkaService kafkaService, BaseRmtCtrlItReq rmtRequest, RedisAPI redisAPI) {
		this.kafkaService = kafkaService;
		this.rmtRequest = rmtRequest;
		this.redisAPI = redisAPI;
	}

	/**
	 * 发送报文后，启动10秒是否回复报文超时检查
	 * 
	 * @param kafkaService
	 * @param rmtRequest
	 * @param tboxService
	 */
	public void start() {
		logger.info("TBox({})在线，开始执行计时器，等待组合远控上行报文回复，共计时10秒", rmtRequest.getSn());
		// 开启定时器，并设置redis全局标志位
		redisAPI.setHash(RedisConstant.RMT_TIMEOUT, rmtRequest.getSeqNo() + rmtRequest.getSn(), "1");
		// 设置等待报文回复时间10s,超时执行run发送kafka消息超时
		threadPool.schedule(this, OperationConstant.REMOTECONTROL_RESP_EXPIRED_TIME, TimeUnit.SECONDS);
	}

	@Override
	public void run() {
		// 等待检查结果
		boolean existFlag = redisAPI.hasKey(RedisConstant.RMT_TIMEOUT, rmtRequest.getSeqNo() + rmtRequest.getSn());
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
				logger.warn("rvm的组合远控请求消息超时，不执行:{},{},{},{}", rmtRequest.getSn(), rmtRequest.getSeqNo(),
				        rmtRequest.getEventTime(), ResultStatus.OP_UNDO_FOR_REQUEST_TIME_EXPIRED);
				// reids删除排他指令记录
				redisAPI.removeKey(RedisConstant.COMMAND_SEND + "_" + rmtRequest.getSn());
					kafkaService.transferRmtResponse(remoteCtrlItResponse);
				// 移除掉超时标志位
				redisAPI.removeHash(RedisConstant.RMT_TIMEOUT, this.rmtRequest.getSeqNo() + this.rmtRequest.getSn());
				//移除同步锁标志
				redisAPI.removeKey(RedisConstant.TIMEOUT_OP_LOCK + "_" + rmtRequest.getSeqNo()
				        + rmtRequest.getSn());
			}
		}
	}

}
