//package com.maxus.tsp.schedule.conf;
//
///**
// * 
// * @author guhaowei
// * 
// */
//import java.util.HashMap;
//import java.util.Map;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.Scheduled;
//
//import com.maxus.tsp.common.constant.ScheduleConstant;
//import com.maxus.tsp.common.redis.RedisAPI;
//import com.maxus.tsp.common.util.ThrowableUtil;
//import com.maxus.tsp.platform.service.model.car.RedisTboxInfo;
//import com.maxus.tsp.schedule.service.client.RedisDateBackUpClient;
//
//@Configuration
//public class RedisDateBackUpTask {
//
//	private static final Logger logger = LogManager.getLogger(RedisDateBackUpTask.class);
//
//	// 记录上一次Redis状态
//	private boolean lastRedisStatus = true;
//
//	// 需要同步表格的Redis本地内存
//	private Map<String, String> localBackUpRemotCtrlRespMap = new HashMap();
//	private Map<String, String> localBackUpOnLineTboxMap = new HashMap();
//	private Map<String, String> localBackUpTboxChannelMap = new HashMap();
//	private Map<String, String> localBackUpTboxLogoutTimeMap = new HashMap();
//
//	// 用于保存从redis获取的数据
//	private Map<String, String> currentRemotCtrlRespMap = new HashMap<>();
//	private Map<String, String> currentOnLineTboxMap = new HashMap<>();
//	private Map<String, String> currentTboxChannelMap = new HashMap<>();
//	private Map<String, String> currentTboxLogoutTimeMap = new HashMap<>();
//
//	@Autowired
//	private RedisAPI redisAPI;
//
//	@Autowired
//	private RedisDateBackUpClient redisDateBackUpClient;
//
//	// 每隔5分钟备份Redis数据
//	@Scheduled(fixedDelay = ScheduleConstant.TIMER_SYNCHRONIZE_REDIS_DATA_TO_MYSQL)
//	private void getRedisStatus() {
//		if (redisAPI.isValid()) {
//
//			// redis可用,先判断上一次状态
//			if (lastRedisStatus) {
//				// 先存一份至本地内存，防止同步至MySQL过程中Redis突然崩溃
//				if (localBackUp()) {
//					// 上一次状态可用，则备份Redis至MySQL
//					backUpDateRedistoMySQL();
//				} else {
//					// 本地备份出错，只更新状态
//					lastRedisStatus = false;
//					return;
//				}
//			} else {
//				// 上一次状态不可用,当前可用，则还原MySQL数据至Redis
//				backUpDateMySQLtoRedis();
//			}
//			lastRedisStatus = true;
//
//		} else {
//			// 当前redis不可用，只更新状态
//			if (lastRedisStatus) {
//				lastRedisStatus = false;
//			}
//		}
//
//	}
//
//	// 本地备份
//	public boolean localBackUp() {
//		try {
//			if (redisAPI.hasKey("CAR_REMOTE_CTRL_RESP")) {
//				localBackUpRemotCtrlRespMap = redisAPI.getHashAll("CAR_REMOTE_CTRL_RESP");
//			} else {
//				localBackUpRemotCtrlRespMap.clear();
//			}
//			if (redisAPI.hasKey("ONLINE_TBOX")) {
//				localBackUpOnLineTboxMap = redisAPI.getHashAll("ONLINE_TBOX");
//			} else {
//				localBackUpOnLineTboxMap.clear();
//			}
//			if (redisAPI.hasKey("TBOX_CHANNEL")) {
//				localBackUpTboxChannelMap = redisAPI.getHashAll("TBOX_CHANNEL");
//			} else {
//				localBackUpTboxChannelMap.clear();
//			}
//			if (redisAPI.hasKey("TBOX_LOGOUT_TIME")) {
//				localBackUpTboxLogoutTimeMap = redisAPI.getHashAll("TBOX_LOGOUT_TIME");
//			} else {
//				localBackUpTboxLogoutTimeMap.clear();
//			}
//			return true;
//		} catch (Exception ex) {
//			logger.error(String.format("本地备份过程中redis崩溃，原因：%s", ThrowableUtil.getErrorInfoFromThrowable(ex)));
//			return false;
//		}
//	}
//
//	// redis可用，则将本地数据备份至MySQL
//	public void backUpDateRedistoMySQL() {
//		// 1.CAR_REMOTE_CTRL_RESP
//		if (localBackUpRemotCtrlRespMap.isEmpty()) {
//			redisDateBackUpClient.clearAllRecordsInrds_car_remote_ctrl_resp();
//		} else {
//			if (!redisDateBackUpClient.insertAllRecordsInrds_car_remote_ctrl_resp(localBackUpRemotCtrlRespMap)) {
//				logger.info("同步本地CAR_REMOTE_CTRL_RESP至MySQL失败。");
//			}
//		}
//
//		// 2.ONLINE_TBOX
//		if (localBackUpOnLineTboxMap.isEmpty()) {
//			redisDateBackUpClient.clearAllRecordsInrds_online_tbox();
//		} else {
//			if (!redisDateBackUpClient.insertAllRecordsInrds_online_tbox(localBackUpOnLineTboxMap)) {
//				logger.info("同步本地ONLINE_TBOX至MySQL失败。");
//			}
//		}
//
//		// 3.TBOX_CHANNEL
//		if (localBackUpTboxChannelMap.isEmpty()) {
//			redisDateBackUpClient.clearAllRecordsInrds_tbox_channel();
//		} else {
//			if (!redisDateBackUpClient.insertAllRecordsInrds_tbox_channel(localBackUpTboxChannelMap)) {
//				logger.info("同步本地TBOX_CHANNEL至MySQL失败。");
//			}
//		}
//
//		// 4.TBOX_LOGOUT_TIME
//		if (localBackUpTboxLogoutTimeMap.isEmpty()) {
//			redisDateBackUpClient.clearAllRecordsInrds_tbox_logout_time();
//		} else {
//			if (!redisDateBackUpClient.insertAllRecordsInrds_tbox_logout_time(localBackUpTboxLogoutTimeMap)) {
//				logger.info("同步本地TBOX_LOGOUT_TIME至MySQL失败。");
//			}
//		}
//	}
//
//	// redis不可用变为可用，则从MySQL还原对应数据至Redis
//	public void backUpDateMySQLtoRedis() {
//		try {
//			// 1. CAR_REMOTE_CTRL_RESP
//			if (redisDateBackUpClient.getAllRecordsInrds_car_remote_ctrl_resp() != null) {
//				currentRemotCtrlRespMap.clear();
//				for (RedisTboxInfo records : redisDateBackUpClient.getAllRecordsInrds_car_remote_ctrl_resp()) {
//					currentRemotCtrlRespMap.put(records.getTboxSn(), records.getValue());
//				}
//				if (redisAPI.hasKey("CAR_REMOTE_CTRL_RESP")) {
//					redisAPI.removeKey("CAR_REMOTE_CTRL_RESP");
//				}
//				redisAPI.setHashAll("CAR_REMOTE_CTRL_RESP", currentRemotCtrlRespMap);
//			} else {
//				logger.error("MySQL还原对应数据至Redis错误，原因：从CAR_REMOTE_CTRL_RESP取得数据为NULL。");
//			}
//
//			// 2. ONLINE_TBOX
//			if (redisDateBackUpClient.getAllRecordsInrds_online_tbox() != null) {
//				currentOnLineTboxMap.clear();
//				for (RedisTboxInfo records : redisDateBackUpClient.getAllRecordsInrds_online_tbox()) {
//					currentOnLineTboxMap.put(records.getTboxSn(), records.getValue());
//				}
//				if (redisAPI.hasKey("ONLINE_TBOX")) {
//					redisAPI.removeKey("ONLINE_TBOX");
//				}
//				redisAPI.setHashAll("ONLINE_TBOX", currentOnLineTboxMap);
//			} else {
//				logger.error("MySQL还原对应数据至Redis错误，原因：从ONLINE_TBOX取得数据为NULL。");
//			}
//
//			// 3. TBOX_CHANNEL
//			if (redisDateBackUpClient.getAllRecordsInrds_tbox_channel() != null) {
//				currentTboxChannelMap.clear();
//				for (RedisTboxInfo records : redisDateBackUpClient.getAllRecordsInrds_tbox_channel()) {
//					currentTboxChannelMap.put(records.getTboxSn(), records.getValue());
//				}
//				if (redisAPI.hasKey("TBOX_CHANNEL")) {
//					redisAPI.removeKey("TBOX_CHANNEL");
//				}
//				redisAPI.setHashAll("TBOX_CHANNEL", currentTboxChannelMap);
//			} else {
//				logger.error("MySQL还原对应数据至Redis错误，原因：从TBOX_CHANNEL取得数据为NULL。");
//			}
//
//			// 4. TBOX_LOGOUT_TIME
//			if (redisDateBackUpClient.getAllRecordsInrds_tbox_logout_time() != null) {
//				currentTboxLogoutTimeMap.clear();
//				for (RedisTboxInfo records : redisDateBackUpClient.getAllRecordsInrds_tbox_logout_time()) {
//					currentTboxLogoutTimeMap.put(records.getTboxSn(), records.getValue());
//				}
//				if (redisAPI.hasKey("TBOX_LOGOUT_TIME")) {
//					redisAPI.removeKey("TBOX_LOGOUT_TIME");
//				}
//				redisAPI.setHashAll("TBOX_LOGOUT_TIME", currentTboxLogoutTimeMap);
//			} else {
//				logger.error("MySQL还原对应数据至Redis错误，原因：从TBOX_LOGOUT_TIME取得数据为NULL。");
//			}
//
//		} catch (Exception ex) {
//			logger.info(String.format("从MySQL还原redis数据表出错，原因：%s", ThrowableUtil.getErrorInfoFromThrowable(ex)));
//		}
//	}
//
//	// 测试
//	// 每隔30s备份Redis数据
//	// @Scheduled(cron = "*/30 * * * * ?")
//	// public void testBackUp() {
//	// if (redisAPI.isValid()) {
//	// if (localBackUp()) {
//	// logger.info("本地备份成功");
//	// logger.info("OnLineTbox"+localBackUpOnLineTboxMap.size()+","+"RemotCtrlResp"+localBackUpRemotCtrlRespMap.size()+","+
//	// "TboxChannel"+localBackUpTboxChannelMap.size()+","+"TboxLogoutTime"+localBackUpTboxLogoutTimeMap.size());
//	// } else {
//	// lastRedisStatus = false;
//	// return;
//	// }
//	// if (lastRedisStatus) {
//	// backUpDateRedistoMySQL();
//	// logger.info("Redis------>MySQL");
//	// lastRedisStatus = false;
//	// } else {
//	// backUpDateMySQLtoRedis();
//	// logger.info("MySQL------>Redis");
//	// lastRedisStatus = true;
//	// }
//	// }
//	//
//	// }
//
//}
