package com.maxus.tsp.schedule.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.maxus.tsp.platform.service.model.vo.AvnTboxVo;
import com.maxus.tsp.platform.service.model.vo.TboxVo;

@FeignClient("tsp-platform-service")
public interface TboxReplaceRepaireClient {
	
	@RequestMapping(method=RequestMethod.POST,value="/app/repairReplaceTbox")
	void repairReplaceTbox(AvnTboxVo avnTob);
	
	@RequestMapping(method=RequestMethod.GET,value="/app/findAvnTboxList")
	AvnTboxVo[] findAvnTboxList();
	
	@RequestMapping(value = "/app/getTboxVoBySn", method = { RequestMethod.POST })
	public TboxVo getTboxBySn(@RequestParam(value = "tboxSN") String tboxSN);
	
}
