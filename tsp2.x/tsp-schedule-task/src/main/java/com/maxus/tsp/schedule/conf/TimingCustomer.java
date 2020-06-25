package com.maxus.tsp.schedule.conf;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import com.alibaba.fastjson.JSON;
import com.maxus.tsp.common.constant.AppApiConstant;
import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.constant.ScheduleConstant;
import com.maxus.tsp.common.jpush.JPushAPI;
import com.maxus.tsp.common.redis.RedisAPI;
import com.maxus.tsp.common.util.SMSUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.nosql.mongodb.model.CarCan;
import com.maxus.tsp.oms.service.model.NoticeMessage;
import com.maxus.tsp.platform.service.model.TimedReminder;
import com.maxus.tsp.schedule.service.client.DaTong2TspClient;
import com.maxus.tsp.schedule.service.client.MongoDbClient;
import com.maxus.tsp.schedule.utils.ConstantUtils;

/**
 * 
 * @ClassName: TimingCustomer.java
 * @Description: 维保的定时提醒
 * @author 赵伟阳
 * @version V1.0
 * @Date 2017年10月18日 下午4:50:20
 */

@Configuration
public class TimingCustomer {

	private static final Logger logger = LogManager.getLogger(TimingCustomer.class);
	@Autowired
	private DaTong2TspClient datong2tspClient;
	@Autowired
	private MongoDbClient mongoDbClient;
	@Autowired
	private RedisAPI redisAPI;
	private final static String REDCOUNT= "redCount";
	private final static String YELLOWCOUNT= "yellowCount";

	// @Scheduled(cron = ScheduleConstant.TIMER_REMIND_CUSTOMER_CAR_MAINTENANCE)
	public void remindCustomer() {
		logger.info("======remindCustomer=======running===" + System.currentTimeMillis() + "=======" + new Date());
		// 1符合条件的车辆:总里程数,维保里程数,维保时间,发送的次数
		List<TimedReminder> listmap = datong2tspClient.selectAllCarMaintenanceInfo(ScheduleConstant.TIMER_CAR_MAINTENANCE_FUNCTION_ID);
		for (TimedReminder info : listmap) {
			try {
				logger.info("userInfo===========" + listmap.size() + "=======1==" + JSON.toJSONString(info));
				if (info != null) {
					// redis里查找总里程
					String currentCan = redisAPI.getHash(RedisConstant.CURRENT_CAN, info.getVin());// 查询总的里程
					BigDecimal mileageAccumulation = new BigDecimal("0");
					if (currentCan == null) {
						mileageAccumulation = this.getMileageAccumulationFromMongDb(info.getVin());
						if (mileageAccumulation == null)
							continue;
						logger.info("the paramter mil is from boss_can1 mil " + mileageAccumulation);
					} else {

						Map<String, Object> mapTy = JSON.parseObject(currentCan);
						logger.info("the paramter is from redis currentCan " + currentCan);
						if (mapTy != null) {
							Object a = mapTy.get("mileageAccumulation");
							if (a != null) {
								try {
									mileageAccumulation = new BigDecimal(a.toString());
									logger.info("the paramter mil is from redis mil " + mileageAccumulation);
								} catch (Exception e) {
									logger.info(ThrowableUtil.getErrorInfoFromThrowable(e));
									mileageAccumulation = new BigDecimal("0");
								}
							} else {
								mileageAccumulation = this.getMileageAccumulationFromMongDb(info.getVin());
								logger.info("the paramter mil is from boss_can1 mil " + mileageAccumulation);
								if (mileageAccumulation == null)
									continue;
							}

						} else {
							// 获取总的里程数
							mileageAccumulation = this.getMileageAccumulationFromMongDb(info.getVin());
							if (mileageAccumulation == null)
								continue;
						}
					}
					// 判断是否要发送短信
					String resultExcption = judgeCount(info, mileageAccumulation.intValue());
					if (resultExcption == null)
						// 本次循环有异常,直接弹出
						continue;
				} else {
					// 本次车辆的信息为空,跳过
					continue;
				}
			} catch (Exception e) {
				logger.error(ThrowableUtil.getErrorInfoFromThrowable(e));
				continue;
			}
		}
	}
	/**
	 * mongdb里获取总的里程
	 * @param vin
	 * @return
	 */
	private BigDecimal getMileageAccumulationFromMongDb(String vin) {
		BigDecimal mileageAccumulation = null;
		CarCan map = mongoDbClient.findCarCan(vin);
		if (map == null)
			return null;
		if (map != null) {
			try {
				
				mileageAccumulation = new BigDecimal(map.getMA() == null ? "0" : map.getMA());
				logger.info("the paramter mil is from boss_can2 mil " + mileageAccumulation);
			} catch (Exception e) {
				logger.info(ThrowableUtil.getErrorInfoFromThrowable(e));
				mileageAccumulation = new BigDecimal("0");
			}
		}
		return mileageAccumulation;
	}
	/**
	 * 是否达到发送短信的条件
	 * @param info
	 * @param mileageAccumulation
	 * @return
	 */
	private String judgeCount(TimedReminder info, Integer mileageAccumulation) {
		Integer frequency = info.getFrequency();// 需保养得次数
		Integer status = info.getStatus();
		info.setMileageAccumulation(new BigDecimal(mileageAccumulation.toString()));// 作为推送消息的时候使用
		try {
			// 判断是否到达过期保养
			if (judgeMeilage(info, mileageAccumulation, REDCOUNT)) {

				if (status == ConstantUtils.STATUS_NEED_MAINTAIN || status == ConstantUtils.STATUS_NOMAL) {
					// 修改状态为2并发送短信
					sendMsg(info, mileageAccumulation);
					datong2tspClient.updateCarStatusCountByVin(info.getVin(), ConstantUtils.STATUS_OVERDUE_MAINTENANCE,
							null);
				} else if (status == ConstantUtils.STATUS_OVERDUE_MAINTENANCE && frequency < 10) {
					// 发送短信
					sendMsg(info, mileageAccumulation);
				}
			}

			// 判断是否到达需保养
			if (judgeMeilage(info, mileageAccumulation, YELLOWCOUNT)) {
				if (status == ConstantUtils.STATUS_NOMAL) {
					// 修改状态为1并发送短信
					sendMsg(info, mileageAccumulation);
					datong2tspClient.updateCarStatusCountByVin(info.getVin(), ConstantUtils.STATUS_NEED_MAINTAIN, null);
				} else if (status == ConstantUtils.STATUS_NEED_MAINTAIN && frequency < 5) {// 有易损件达到需保养的状态
					// 发送4次短信
					sendMsg(info, mileageAccumulation);
				} else if (status == ConstantUtils.STATUS_OVERDUE_MAINTENANCE && frequency >= 10 && frequency < 15) {// 清零时有过期未保养的情况
					sendMsg(info, mileageAccumulation);
				}
			}
		} catch (Exception e) {
			logger.error(ThrowableUtil.getErrorInfoFromThrowable(e));
			return null;
		}
		return "0";
	}

	/**
	 * 发送短信
	 * @param timeReminder
	 * @param mileageAccumulation
	 * @throws Exception
	 */
	private void sendMsg(TimedReminder timeReminder, Integer mileageAccumulation) throws Exception {

		String sendAdviceNotice = SMSUtil.sendAdviceNotice(timeReminder.getPhone(), timeReminder.getCarName(),
				timeReminder.getCarType());
		Long carStatusID = datong2tspClient.selectCarStatusFromVin(timeReminder.getVin());
		if (carStatusID != null) {
			datong2tspClient.updateCarStatusCountByVin(timeReminder.getVin(), null, 1);
		} else {
			datong2tspClient.insertMileageAccumulationCarStatus(mileageAccumulation, timeReminder.getVin());
		}
		insertNoticeMessageTiming(timeReminder, sendAdviceNotice);
	}

	private boolean judgeMeilage(TimedReminder info, Integer m, String str) throws Exception {
		Long s = System.currentTimeMillis() / 1000;
		boolean flag = false;
		// 重置易损件的里程数
		Integer airConditionFilter = info.getAirConditionFilter() == null ? 0 : info.getAirConditionFilter().intValue();// 空调滤清
		Integer airFilter = info.getAirFilter() == null ? 0 : info.getAirFilter().intValue();// 燃油滤清
		Integer engineOil = info.getEngineOil() == null ? 0 : info.getEngineOil().intValue();// 机油
		Integer engineOilFilter = info.getEngineOilFilter() == null ? 0 : info.getEngineOilFilter().intValue();// 机油滤清
		Integer fuelFilter = info.getFuelFilter() == null ? 0 : info.getFuelFilter().intValue();// 燃油滤清
		// 重置时间
		Long acfUpdateTime = info.getAcfUpdateTime() == null ? s : info.getAcfUpdateTime().getTime() / 1000;// 空调滤清
		Long afUpdateTime = info.getAfUpdateTime() == null ? s : info.getAfUpdateTime().getTime() / 1000;// 燃油滤清
		Long eoUpdateTime = info.getEoUpdateTime() == null ? s : info.getEoUpdateTime().getTime() / 1000;// 机油
		Long eofUpdateTime = info.getEofUpdateTime() == null ? s : info.getEofUpdateTime().getTime() / 1000;// 机油滤清
		Long fUpdateTime = info.getfUpdateTime() == null ? s : info.getfUpdateTime().getTime() / 1000;// 燃油滤清.getTime()/1000

		if (YELLOWCOUNT.equals(str)) {// 是否有需保养得
			if (mileageOutOfBind4(m - airConditionFilter, s - acfUpdateTime)
					|| mileageOutOfBind4(m - airFilter, s - afUpdateTime)
					|| mileageOutOfBind2(m - engineOil, s - eoUpdateTime)
					|| mileageOutOfBind2(m - engineOilFilter, s - eofUpdateTime)
					|| mileageOutOfBind6(m - fuelFilter, s - fUpdateTime)) {
				flag = true;
			}
		} else if (REDCOUNT.equals(str)) {// 是否有过期未保养
			if (mileageOutOfBind5(m - airConditionFilter, s - acfUpdateTime)
					|| mileageOutOfBind5(m - airFilter, s - afUpdateTime)
					|| mileageOutOfBind3(m - engineOil, s - eoUpdateTime)
					|| mileageOutOfBind3(m - engineOilFilter, s - eofUpdateTime)
					|| mileageOutOfBind7(m - fuelFilter, s - fUpdateTime)) {
				flag = true;
			}
		}
		return flag;
	}

	private boolean mileageOutOfBind2(Integer mileage, Long longdate) {
		boolean exist = false;
		if (mileage < 7500 && mileage > 6501 || longdate < 365 * 24 * 60 * 60 && longdate > 313 * 24 * 60 * 60) {// 需保养
			exist = true;
		}
		return exist;
	}

	private boolean mileageOutOfBind3(Integer mileage, Long longdate) {
		boolean exist = false;
		if (mileage > 7501 || longdate > 365 * 24 * 60 * 60) {// 过期未保养
			exist = true;
		}
		return exist;
	}

	private boolean mileageOutOfBind4(Integer mileage, Long longdate) {
		boolean exist = false;
		if (mileage < 15000 && mileage > 13001 || longdate < 365 * 24 * 60 * 60 && longdate > 313 * 24 * 60 * 60) {// 需保养
			exist = true;
		}
		return exist;
	}

	private boolean mileageOutOfBind5(Integer mileage, Long longdate) {
		boolean exist = false;
		if (mileage > 15001 || longdate > 365 * 24 * 60 * 60) {// 过期未保养
			exist = true;
		}
		return exist;
	}

	private boolean mileageOutOfBind6(Integer mileage, Long longdate) {
		boolean exist = false;
		if (mileage < 15000 && mileage > 13001 || longdate > 601 * 24 * 60 * 60 && longdate < 730 * 24 * 60 * 60) {// 需保养
			exist = true;
		}
		return exist;
	}

	private boolean mileageOutOfBind7(Integer mileage, Long longdate) {

		boolean exist = false;
		if (mileage > 15001 || longdate > 731 * 24 * 60 * 60) {// 易损件达到过期未保养
			exist = true;
		}
		return exist;
	}

	/**
	 * 记录到消息中心
	 * @param timeReminder
	 * @param sendAdviceNotice
	 * @throws ParseException
	 */
	private void insertNoticeMessageTiming(TimedReminder timeReminder, String sendAdviceNotice) throws ParseException {

		// 消息发送记录
		NoticeMessage nm = new NoticeMessage();
		SimpleDateFormat sdf = new SimpleDateFormat(AppApiConstant.APP_FORMAT_TIME_PATTERN);
		String str = sdf.format(new Date());
		Date parse;
		parse = sdf.parse(str);
		String sb = getContentString(timeReminder, str);
		nm.setContent(sb);
		nm.setLabel("维保信息");
		nm.setSendUser("2");
		nm.setOpenId(timeReminder.getOpenId());
		if ("false".equals(sendAdviceNotice)) {
			nm.setPushStatus(0);
			nm.setSmsStatus(0);// 未发送短信
		} else {
			nm.setPushStatus(1);
			nm.setSmsStatus(1);// 已发短信
		}
		nm.setSendTime(parse);
		nm.setStatus(0);
		nm.setTitle("车辆保养提醒");
		nm.setVin(timeReminder.getVin());
		Integer noticeId = datong2tspClient.insertNoticeMessageTiming(nm);
		logger.info(noticeId + "--------------->id" + nm.getSendTime() + "------->" + str + "----->"
				+ timeReminder.getOpenId()+"insertNoticeMessageTiming"+noticeId);

		// 激光推送
		boolean pushAlert = JPushAPI

				.pushAlert(timeReminder.getOpenId(), "您的爱车" + timeReminder.getCarName() + "("
						+ timeReminder.getCarType() + ")有零件已到保养周期,请打开" + '"' + "我行无限" + '"' + "APP查看详情",
						"车辆保养提醒#" +str+ "#" + noticeId);

		logger.info("维保信息#" + str + "#" + noticeId + "推送给手机App" + pushAlert);

		boolean pushAlert2 = JPushAPI .pushAlertEntertainment(
		 timeReminder.getVin(), "您的爱车(" + timeReminder.getCarName() + ")"
		 + timeReminder.getCarType() + "有零件已到保养周期,请打开"
				 + '"' + "娱乐主机查看详情", "维保信息#" + noticeId + "#" + str);
		 logger.info("推送给娱乐主机" + pushAlert2);
	}

	/**
	 * 拼接主题内容
	 * @param info
	 * @param str
	 * @return
	 */
	private String getContentString(TimedReminder info, String str) {
		StringBuilder sb = new StringBuilder();

		sb.append("您的爱车" + info.getCarName() + "(" + info.getCarType() + ")")
				.append("于" + str + "有零件已到保养周期," + "当前总里程" + info.getMileageAccumulation() + ";").append("易损件及对应状态:");
		Long s = System.currentTimeMillis() / 1000;
		// 重置易损件的里程数
		Integer airConditionFilter = info.getAirConditionFilter() == null ? 0 : info.getAirConditionFilter().intValue();// 空调滤清
		Integer airFilter = info.getAirFilter() == null ? 0 : info.getAirFilter().intValue();// 燃油滤清
		Integer engineOil = info.getEngineOil() == null ? 0 : info.getEngineOil().intValue();// 机油
		Integer engineOilFilter = info.getEngineOilFilter() == null ? 0 : info.getEngineOilFilter().intValue();// 机油滤清
		Integer fuelFilter = info.getFuelFilter() == null ? 0 : info.getFuelFilter().intValue();// 燃油滤清
		// 重置时间
		Long acfUpdateTime = info.getAcfUpdateTime() == null ? s : info.getAcfUpdateTime().getTime() / 1000;// 空调滤清
		Long afUpdateTime = info.getAfUpdateTime() == null ? s : info.getAfUpdateTime().getTime() / 1000;// 燃油滤清
		Long eoUpdateTime = info.getEoUpdateTime() == null ? s : info.getEoUpdateTime().getTime() / 1000;// 机油
		Long eofUpdateTime = info.getEofUpdateTime() == null ? s : info.getEofUpdateTime().getTime() / 1000;// 机油滤清
		Long fUpdateTime = info.getfUpdateTime() == null ? s : info.getfUpdateTime().getTime() / 1000;// 燃油滤清.getTime()/1000

		Integer m = info.getMileageAccumulation().intValue();
		// 空滤
		if (mileageOutOfBind4(m - airConditionFilter, s - acfUpdateTime)) {
			sb.append("空调滤清需保养状态;");
		} else if (mileageOutOfBind5(m - airConditionFilter, s - acfUpdateTime)) {
			sb.append("空调滤清过期未保养状态;");
		}
		// 空滤
		if (mileageOutOfBind4(m - airFilter, s - afUpdateTime)) {
			sb.append("空气滤清器:需保养状态;");
		} else if (mileageOutOfBind5(m - airFilter, s - afUpdateTime)) {
			sb.append("空气滤清器过期未保养状态;");
		}
		// 机油
		if (mileageOutOfBind2(m - engineOil, s - eoUpdateTime)) {
			sb.append("机油需保养状态;");
		} else if (mileageOutOfBind3(m - engineOil, s - eoUpdateTime)) {
			sb.append("机油过期未保养状态;");
		}
		// 机滤
		if (mileageOutOfBind2(m - engineOilFilter, s - eofUpdateTime)) {
			sb.append("机滤需保养状态;");
		} else if (mileageOutOfBind3(m - engineOilFilter, s - eofUpdateTime)) {
			sb.append("机滤过期未保养状态;");
		}
		// 然油
		if (mileageOutOfBind6(m - fuelFilter, s - fUpdateTime)) {
			sb.append("燃油滤清器需保养状态;");
		} else if (mileageOutOfBind7(m - fuelFilter, s - fUpdateTime)) {
			sb.append("燃油滤清器过期未保养状态;");
		}
		return sb.toString();
	}

}

