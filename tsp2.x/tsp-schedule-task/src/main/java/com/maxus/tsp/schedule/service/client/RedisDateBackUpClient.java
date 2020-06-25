package com.maxus.tsp.schedule.service.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.maxus.tsp.platform.service.model.car.RedisTboxInfo;

@FeignClient("tsp-platform-service")
public interface RedisDateBackUpClient {

	//表格 rds_car_remote_ctrl_resp
	//获取记录
	@RequestMapping(value = "/api/getAllRecordsInrds_car_remote_ctrl_resp", method = RequestMethod.GET)
	public RedisTboxInfo[] getAllRecordsInrds_car_remote_ctrl_resp(); 
	//批量更新记录
	@RequestMapping(value = "/api/insertAllRecordsInrds_car_remote_ctrl_resp", method = RequestMethod.POST)
	public boolean insertAllRecordsInrds_car_remote_ctrl_resp(@RequestBody Map<String,String> records);
	//清空表格记录
	@RequestMapping(value = "/api/clearAllRecordsInrds_car_remote_ctrl_resp", method = RequestMethod.GET)
	public void clearAllRecordsInrds_car_remote_ctrl_resp(); 
	
	//表格 rds_online_tbox
	//获取记录
	@RequestMapping(value = "/api/getAllRecordsInrds_online_tbox", method = RequestMethod.GET)
	public RedisTboxInfo[] getAllRecordsInrds_online_tbox(); 
	//批量更新记录
	@RequestMapping(value = "/api/insertAllRecordsInrds_online_tbox", method = RequestMethod.POST)
	public boolean insertAllRecordsInrds_online_tbox(@RequestBody Map<String, String> records);
	//清空表格记录
	@RequestMapping(value = "/api/clearAllRecordsInrds_online_tbox", method = RequestMethod.GET)
	public void clearAllRecordsInrds_online_tbox(); 
	
	
	//表格 rds_tbox_channel
	//获取记录
	@RequestMapping(value = "/api/getAllRecordsInrds_tbox_channel", method = RequestMethod.GET)
	public RedisTboxInfo[] getAllRecordsInrds_tbox_channel(); 
	//批量更新记录
	@RequestMapping(value = "/api/insertAllRecordsInrds_tbox_channel", method = RequestMethod.POST)
	public boolean insertAllRecordsInrds_tbox_channel(@RequestBody Map<String, String> records);
	//清空表格记录
	@RequestMapping(value = "/api/clearAllRecordsInrds_tbox_channel", method = RequestMethod.GET)
	public void clearAllRecordsInrds_tbox_channel(); 
	
	
	//表格 rds_tbox_logout_time
	//获取记录
	@RequestMapping(value = "/api/getAllRecordsInrds_tbox_logout_time", method = RequestMethod.GET)
	public RedisTboxInfo[] getAllRecordsInrds_tbox_logout_time(); 
	//批量更新记录
	@RequestMapping(value = "/api/insertAllRecordsInrds_tbox_logout_time", method = RequestMethod.POST)
	public boolean insertAllRecordsInrds_tbox_logout_time(@RequestBody Map<String, String> records);
	//清空表格记录
	@RequestMapping(value = "/api/clearAllRecordsInrds_tbox_logout_time", method = RequestMethod.GET)
	public void clearAllRecordsInrds_tbox_logout_time(); 
	
}
