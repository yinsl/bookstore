package com.maxus.tsp.schedule.conf;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.constant.ScheduleConstant;
import com.maxus.tsp.common.redis.RedisAPI;
import com.maxus.tsp.common.util.LianTongTboxUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.platform.service.model.vo.AvnTboxVo;
import com.maxus.tsp.platform.service.model.vo.TboxVo;
import com.maxus.tsp.schedule.service.client.TboxReplaceRepaireClient;

/**
 * TBOX更换服务失败时的补偿操作定时器
 * @author 李毅松
 *
 */
@Component
public class TboxReplaceRepairTask {
	private static final Logger logger = LogManager.getLogger(TboxReplaceRepairTask.class);
	
	@Value("${tbox.liantong.username}")
	private String username;

	@Value("${tbox.liantong.password}")
	private String password;

	@Value("${tbox.liantong.url}")
	private String url;
	
	@Autowired
	private RedisAPI redisAPI;

	@Autowired
	private TboxReplaceRepaireClient tboxReplaceRepairClient;
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	// 重试间隔 300秒
	private static final int RETRY_INTERVAL = 300000;
	// 重试次数
	private static final int RETRY_TIMES = 3;

	/**
	 * TBOX更换服务失败时的补偿操作定时器
	 * 每隔4小时运行
	 * @author 李毅松
	 *
	 */
//	@Scheduled(cron = "10 53 10 * * ?") //测试用
	@Scheduled(cron = ScheduleConstant.TIMER_REPARE_TBOX_REPLACE)
	public void TboxReplaceRepair() {
		logger.info("TboxReplaceRepairTask Timer:  Starting........");
		
		// 使用重试机制(首次执行加重试3次)，根据dms_avntbox表与Tbox表关联查找新tbox未同步至联通的数据记录
		// 重试计数器
		int retryCounter = 0;
		// 异常返回信息
		Map<String, String> rtnMessage = new HashMap<>();
		AvnTboxVo[] avnTboxVoList = retryFindAvnTboxList(RETRY_TIMES, retryCounter, rtnMessage);
		if (avnTboxVoList == null || avnTboxVoList.length <= 0) {
			String errorMsg = rtnMessage.get(ScheduleConstant.MSG).toString();
			if (StringUtils.isBlank(errorMsg)) {
				logger.info("TboxReplaceRepairTask Timer:  Based tables dms_avntbox union Tbox， tbox data for sync not found!");
			} else {
				logger.error("TboxReplaceRepairTask Timer: Exception occured when find avntbox replace records, See: " + rtnMessage);
			}
			return;
		}
		
		
		String token = LianTongTboxUtil.getLiantongToken(url, username, password);
		if (StringUtils.isBlank(token)) {
			logger.info("TboxReplaceRepairTask Timer:  Error occcured when get token, check 'verify.login' LianTong service please!");
			return;
		}
		
		for (AvnTboxVo avnTob : avnTboxVoList) {
			// 重新同步avntbox更换信息至联通
			String responseStr = changeTbox(url, token, avnTob.getVin(), avnTob.getOldCCID(), avnTob.getNewICCID());
			if (StringUtils.isBlank(responseStr)) {
				logger.error(MessageFormat.format("TboxReplaceRepairTask Timer: Error occcured when change tbox, check 'change.tbox' LianTong service please! oldTboxSn:[{0}], newTboxSn: [{1}], Vin: [{2}].", 
						avnTob.getOldTBOXNO(), avnTob.getNewTBOXNO(), avnTob.getVin()));
				continue;
			}
			
			// 与联通同步成功的数据，更改新、老tbox数据的状态
			JSONObject reponseJson = JSON.parseObject(responseStr);
			String returnCode = reponseJson.getString("ret_code");
			if (ScheduleConstant.RETURN_OK.equals(returnCode)) {  //同步至联通成功
				logger.info(MessageFormat.format("TboxReplaceRepairTask Timer: Call 'change.tbox' LianTong service, the repponse code is OK! oldTboxSn:[{0}], newTboxSn: [{1}], Vin: [{2}].", avnTob.getOldTBOXNO(), avnTob.getNewTBOXNO(), avnTob.getVin()));
				// 更新新、老tbox的状态
				tboxReplaceRepairClient.repairReplaceTbox(avnTob);
				
				// 在不影响主流程的情况下更新redis缓存[TBox绑定车辆车架号VIN_FOR_SN]
				try {
					updateTboxInRedis(avnTob.getOldTBOXNO(), avnTob.getNewTBOXNO(), avnTob.getVin());
					logger.info(MessageFormat.format("TboxReplaceRepairTask Timer: Update redis cache finished! oldTboxSn:[{0}], newTboxSn: [{1}], Vin: [{2}].", 
							avnTob.getOldTBOXNO(), avnTob.getNewTBOXNO(), avnTob.getVin()));
				} catch(Exception e) {
					logger.info(MessageFormat.format("TboxReplaceRepairTask Timer: Update redis cache failed!, check if redis is available! oldTboxSn:[{0}], newTboxSn: [{1}], Vin: [{2}].", 
							avnTob.getOldTBOXNO(), avnTob.getNewTBOXNO(), avnTob.getVin()));
				}
			} else {
				logger.warn(MessageFormat.format("AvnTboxReplaceEndpoint WebService: Call 'change.tbox' LianTong service, the repponse code is NOT OK!, Tsp Backend will try again later by TboxReplaceRepairTask! oldTboxSn:[{0}], newTboxSn: [{1}], Vin: [{2}].", 
						avnTob.getOldTBOXNO(), avnTob.getNewTBOXNO(), avnTob.getVin()));
			}
		}
		
		logger.info("TboxReplaceRepairTask 运行结束.......");
	}
	
	/**
	 * 发生异常的情况下重试获取要进行tbox更换的数据
	 * @param retryTimes 重试次数
	 * @param retryCounter 重试计数器
	 * @param rtnMessage 发生异常时错误信息
	 * @return tbox更换的记录数组
	 */
	public AvnTboxVo[] retryFindAvnTboxList(int retryTimes, int retryCounter, Map<String, String> rtnMessage) {
		AvnTboxVo[] avnTboxVoList = null;
		boolean isExceptionFlag = false;
		try {
			avnTboxVoList = tboxReplaceRepairClient.findAvnTboxList();
		} catch(Exception e) {
			isExceptionFlag = true;
			rtnMessage.put(ScheduleConstant.MSG, ThrowableUtil.getErrorInfoFromThrowable(e));
			logger.error("TboxReplaceRepairTask Timer: Exception occured when find avntbox replace records, See: " + ThrowableUtil.getErrorInfoFromThrowable(e));
		}
		if (isExceptionFlag && retryTimes > 0) {
			// 根据执行次数做线程延迟
			try {
				Thread.sleep(RETRY_INTERVAL * ++retryCounter);
			} catch (Exception e) {
				// Do nothing
			}
			avnTboxVoList = retryFindAvnTboxList(--retryTimes, retryCounter, rtnMessage);
		}
		return avnTboxVoList;
	}
	
	public String changeTbox(String url, String token, String vin, String oldiccid, String newiccid) {
		String serial_number = "maxus-" + String.valueOf(System.currentTimeMillis());
		String timestamp = dateFormat.format(new Date());
		String json = "{\"platform_type\": \"CUNT\",\"request_data\" : {\"vin\":\"" + vin
		        + "\",\"brandid\":\"MAXUS\",\"oldiccid\":\"" + oldiccid + "\",\"newiccid\":\"" + newiccid
		        + "\"},\"ret_type\": \"json\",\"serial_number\": \"" + serial_number + "\",\"service_name\": \""
		        + LianTongTboxUtil.CHANGE_TBOX + "\",\"timestamp\": \"" + timestamp + "\",\"token\":\"" + token + "\"}";
		logger.info(json);
		String response = LianTongTboxUtil.postJson(url, json);
		logger.info(response);
		return response;
	}
	
	private void updateTboxInRedis(String oldTboxSn, String newTboxSn, String vin) {
		// 老SN与vin关联的超时设置
		// 暂时采取删除方案，删除老Tbox与车架的关联
		redisAPI.removeHash(RedisConstant.VIN_FOR_SN, oldTboxSn);
		// 添加新的关联
		redisAPI.setHash(RedisConstant.VIN_FOR_SN, newTboxSn,vin);
		
		// 更新缓存TBOX_INFO,删除老tbox信息，添加新tbox信息(根据bug票 TSPSV61-830)
		redisAPI.removeHash(RedisConstant.TBOX_INFO, oldTboxSn);
		TboxVo newTbox = tboxReplaceRepairClient.getTboxBySn(newTboxSn);
		if (newTbox != null) {
			redisAPI.setHash(RedisConstant.TBOX_INFO, newTboxSn, JSONObject.toJSONString(newTbox, SerializerFeature.WriteMapNullValue));
		}
	}

	public static void main(String[] args) {
		TboxReplaceRepairTask p = new TboxReplaceRepairTask();
		String url = "https://123.125.218.12:28801/esb/json";
		String username = "sqdtop";
		String password = "96E79218965EB72C92A549DD5A330112";
		String token = LianTongTboxUtil.getLiantongToken(url, username, password);
		String response = p.changeTbox(url, token, "a", "oldiccid", "newiccid");
		System.out.println("response = " + response);
	}

}
