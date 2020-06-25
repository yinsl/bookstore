package com.maxus.tsp.schedule.conf;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.ScheduleConstant;
import com.maxus.tsp.common.util.LianTongTboxUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.platform.service.model.ProductDataVo;
import com.maxus.tsp.platform.service.model.thirdparty.liantong.Vehicle;
import com.maxus.tsp.schedule.service.client.DaTong2TspClient;
/**
 * 下线车辆批量同步联通接口
 * @author 李毅松
 *
 */
@Component
public class TboxLianTongSchedule {
	
	private static final Logger logger = LogManager.getLogger(TboxLianTongSchedule.class);

	@Value("${tbox.liantong.username}")
	private String username;

	@Value("${tbox.liantong.password}")
	private String password;

	@Value("${tbox.liantong.url}")
	private String url;

	@Autowired
	private DaTong2TspClient datong2tspClient;

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * 下线车辆批量同步联通接口
	 * 每日2点运行
	 *
	 */
//	@Scheduled(cron = "20 28 15 * * ?") //测试用
	@Scheduled(cron = ScheduleConstant.TIMER_SYNC_CAR_TO_LIANTONG)
	public void pushVinIccidMapping() {
		logger.info("TboxLianTongSchedule Timer:  Starting........");
		try {
			String token = LianTongTboxUtil.getLiantongToken(url, username, password);
			if (StringUtils.isBlank(token)) {
				logger.error("TboxLianTongSchedule Timer:  Error occcured when get token, check 'verify.login' LianTong service please!");
			} else {
				logger.info("TboxLianTongSchedule Timer:  Get token successfully!");
				pushVinIccidMapping(token);
			}
		} catch (Exception e) {
			logger.error("TboxLianTongSchedule Timer:  An unexpected error occcured when timer run, See: " +ThrowableUtil.getErrorInfoFromThrowable(e));
		}

		logger.info("TboxLianTongSchedule Timer: Run end........");
	}

	/**
	 * 
	 * @Title: pushVinIccidMapping
	 * @Description: 车辆批量同步实时接口
	 * @param: @param token
	 * @return: void
	 * @throws UnsupportedEncodingException 
	 * @throws
	 * @author zekym
	 * @Date 2017年11月20日 下午4:45:17
	 */
	private void pushVinIccidMapping(String token) throws UnsupportedEncodingException {
		List<ProductDataVo> products = datong2tspClient.selectProductsNoSync();
		if (products == null || products.size() <= 0) {
			logger.info("TboxLianTongSchedule Timer:  Production car data not found, timer quit now!");
			return;
		}

		List<Vehicle> vehicles = new ArrayList<Vehicle>();
		Map<String,String> carTboxMap = new HashMap<String, String>();
		
		for (ProductDataVo vo : products) {
			Vehicle vehicle = new Vehicle();
			vehicle.setBrand(ScheduleConstant.BRAND);
			vehicle.setVin(vo.getVin());
			// 以utf8编码传送至联通后，联通方说中文依然存在乱码。
			// 联通方建议，可送“MAXUS”或直接不送值。现改为送“MAXUS"  20180413 by liyisong
//			vehicle.setVendors(new String(ScheduleConstant.VENDORS.getBytes(),"UTF-8"));
			vehicle.setVendors(ScheduleConstant.BRAND);
			vehicle.setIccid(vo.getIccId());
//			vehicle.setTboxSn(vo.gettBoxSn());  // 接口规范报文结构里没有该字段，屏蔽之  20180413 by liyisong
			carTboxMap.put(vo.getIccId(), vo.gettBoxSn());

			vehicles.add(vehicle);
			logger.info(MessageFormat.format("TboxLianTongSchedule Timer:  Production car will sync to LianTong, vin: [{0}], tboxSn: [{1}].", vo.getVin(), vo.gettBoxSn()));
		}
		int size = vehicles.size();
		int cnt = size % 200 == 0 ? size / 200 : size / 200 + 1;
		for (int i = 0; i < cnt; i++) {
			int from = i * 200;
			int to = (i + 1) * 200 > size ? size : (i + 1) * 200;
			String json = JSON.toJSONString(vehicles.subList(from, to));
			logger.info("TboxLianTongSchedule Timer: Jason string: " + json);
			
			String serial_number = "maxus-" + String.valueOf(System.currentTimeMillis());
			String timestamp = dateFormat.format(new Date());
			json = "{\"platform_type\": \"CUNT\",\"request_data\" : {\"vehicles\":" + json
			        + "},\"ret_type\": \"json\",\"serial_number\": \"" + serial_number
			        + "\",\"service_name\": \"" + LianTongTboxUtil.SYNC_VEHICLE + "\",\"timestamp\": \"" + timestamp + "\",\"token\":\"" + token
			        + "\"}";
			String responseStr = LianTongTboxUtil.postJson(url, json);
			//获取返回信息中，已经成功传送的车辆信息
			if (StringUtils.isBlank(responseStr)) {
				logger.info("TboxLianTongSchedule Timer: Sync to LianTong failed! Production car: " + json);
				continue;
			}
			
			JSONObject reponseJson = JSON.parseObject(responseStr);
			if (reponseJson != null && reponseJson.get("response_data") != null) {
				JSONObject response_data = reponseJson.getJSONObject("response_data");
				if (response_data != null) {
					JSONArray vcbacks = response_data.getJSONArray("vcbacks");
					
					if (vcbacks != null && vcbacks.size() > 0) {
						List<String> sendVehicleOKList = new ArrayList<String>();
						List<String> sendVehicleNGList = new ArrayList<String>();
						for(int j = 0; j < vcbacks.size(); j++) {
						    JSONObject vcBack = vcbacks.getJSONObject(j);
						    String iccid = vcBack.getString("iccid");
						    String retCode = vcBack.get("retcode").toString();
						    //根据联通接口文档，“0”：成功；“1”：失败
						    if (ScheduleConstant.RETURN_OK.equals(retCode)) {
						    	sendVehicleOKList.add(carTboxMap.get(iccid));
//						    	logger.info("TboxLianTongSchedule Timer:  Sync status is OK, iccid: [" + iccid + "], tboxSn: [" + carTboxMap.get(iccid) + "].");
						    	logger.info(MessageFormat.format("TboxLianTongSchedule Timer:  Sync status is OK, iccid: [{0}], tboxSn: [{1}].", iccid, carTboxMap.get(iccid)));
						    } else {
						    	sendVehicleNGList.add(carTboxMap.get(iccid));
						    	logger.info(MessageFormat.format("TboxLianTongSchedule Timer:  Sync status is NG, iccid: [{0}], tboxSn: [{1}].", iccid, carTboxMap.get(iccid)));
						    }
						}
						
						// 根据返回的每辆车的同步状态，更新tbox数据的与联通同步状态和时间，更新base_car
						// 成功：更新同步状态为1、同步时间；失败：仅更新同步时间；
						Map<String, List<String>> paramMap = new HashMap<String, List<String>>();
						paramMap.put("OK", sendVehicleOKList);
						paramMap.put("NG", sendVehicleNGList);
						datong2tspClient.updateTboxSyncSatsBySn(paramMap);
						logger.info("TboxLianTongSchedule Timer:  Updating tbox data finished!");
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		 String url = "https://123.125.218.12:28801/esb/json";
//		String url = "https://220.194.48.3:28800/esb/json";
		 String password="96E79218965EB72C92A549DD5A330112";
		 String username="sqdtop";
		 String token = LianTongTboxUtil.getLiantongToken(url, username, password);
		 System.out.println("token: " + token);
		 DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		 String d = df.format(new Date());

		String json = "{\"platform_type\": \"CUNT\",\"request_data\" : {\"vehicles\":[{\"brand\":\"MAXUS\",\"capacity\":0,\"iccid\":\"89860617090000212560\",\"vendors\":\"上汽大通\",\"vin\":\"SQJ14400SI1050001\"}]},\"ret_type\": \"json\",\"serial_number\": \"maxus-" + System.currentTimeMillis() / 1000 + "\",\"service_name\": \"" + LianTongTboxUtil.SYNC_VEHICLE + "\",\"timestamp\": \"" + d + "\",\"token\":\"" + token + "\"}";
		String responseStr = LianTongTboxUtil.postJson(url, json);
		if (responseStr != null && !"".equals(responseStr)) {
			JSONObject reponseJson = JSON.parseObject(responseStr);
			if (reponseJson != null && reponseJson.get("response_data") != null) {
				JSONObject response_data = reponseJson.getJSONObject("response_data");
				if (response_data != null) {
					JSONArray vcbacks = response_data.getJSONArray("vcbacks");
					
					if (vcbacks != null && vcbacks.size() > 0) {
						List<String> sendVehicleOKList = new ArrayList<String>();
						for(int j = 0; j < vcbacks.size(); j++) {
						    JSONObject vcBack = vcbacks.getJSONObject(j);
						    //根据联通接口文档，“0”：成功；“1”：失败
						    if ("0".equals(vcBack.get("retcode"))) {
						    	String iccid = vcBack.getString("iccid");
						    	sendVehicleOKList.add(iccid);
						    }
						}
					}
				}
			}
		}
		System.out.println(responseStr);
	}

}
