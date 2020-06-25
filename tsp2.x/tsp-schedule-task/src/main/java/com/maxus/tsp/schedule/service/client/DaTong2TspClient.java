package com.maxus.tsp.schedule.service.client;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.maxus.tsp.oms.service.model.NoticeMessage;
import com.maxus.tsp.platform.service.model.MaintenanceStationInfo;
import com.maxus.tsp.platform.service.model.ProductDataVo;
import com.maxus.tsp.platform.service.model.TimedReminder;

@FeignClient("tsp-platform-service")
public interface DaTong2TspClient {

	@RequestMapping(method=RequestMethod.GET,value="/tsp/avnapi/getDAIList")
	List<MaintenanceStationInfo> getDAIList();
	
	@RequestMapping(method=RequestMethod.GET,value="/tsp/mesapi/selectProductsInOneday")
	List<ProductDataVo> selectProductsInOneday();
	
	@RequestMapping(method=RequestMethod.GET,value="/tsp/mesapi/selectProductsNoSync")
	List<ProductDataVo> selectProductsNoSync();
	
	@RequestMapping(method=RequestMethod.GET,value="/app/api/selectAllCarMaintenanceInfo")
	List<TimedReminder> selectAllCarMaintenanceInfo(@RequestParam(value = "functionId")Integer functionId);
	
	@RequestMapping(method=RequestMethod.GET,value="/app/api/updateCarStatusCountByVin")
	Integer updateCarStatusCountByVin(@RequestParam(value = "vin")String vin, @RequestParam(value = "timingStatus")Integer timingStatus, @RequestParam(value = "i")Integer i);
	
	@RequestMapping(method=RequestMethod.GET,value="/app/api/selectCarStatusFromVin")
	Long selectCarStatusFromVin(@RequestParam(value = "vin")String vin);

	@RequestMapping(method=RequestMethod.GET,value="/app/api/insertMileageAccumulationCarStatus")
	Integer insertMileageAccumulationCarStatus(@RequestParam(value = "mileageAccumulation")Integer mileageAccumulation, @RequestParam(value = "vin")String vin);
	
	@RequestMapping(method=RequestMethod.POST,value="/app/api/insertNoticeMessageTiming")
	Integer insertNoticeMessageTiming(NoticeMessage nm);
	
	@RequestMapping(method=RequestMethod.POST,value="/app/updateTboxSyncSatsBySn")
	Integer updateTboxSyncSatsBySn(@RequestBody Map<String, List<String>> paramMap);
	@RequestMapping(method=RequestMethod.POST,value="/tsp/appapi/insertTokenSet")
	void SnychronizeRdsDataToDb();
	@RequestMapping(method=RequestMethod.POST,value="/tsp/appapi/SynchronizeDbToRedis")
	void SynchronizeDbToRedis();
	
}
