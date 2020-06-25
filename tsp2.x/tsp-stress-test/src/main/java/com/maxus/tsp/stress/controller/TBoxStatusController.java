package com.maxus.tsp.stress.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.redis.allredis.RedisAPI;

@RestController
public class TBoxStatusController {

	private static Logger logger = LogManager.getLogger(TBoxStatusController.class);

	@Autowired
	private RedisAPI redisAPI;

//	@RequestMapping("/tbox/online")
//	public String online(String sn) {
//		logger.info("tbox(" + sn + ")'s status has been setted to online.");
//		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		redisAPI.setHash(RedisConstant.ONLINE_TBOX, sn, df.format(new Date()));
//		return sn + " online";
//	}
	
	@RequestMapping("/tbox/online/{sn}")
	public String online(@PathVariable("sn") String sn) {
		logger.info("tbox(" + sn + ")'s status has been setted to online.");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		redisAPI.setHash(RedisConstant.ONLINE_TBOX, sn, df.format(new Date()));
		return sn + " online";
	}
	
	@RequestMapping("/tbox/offline/{sn}")
	public String offline(@PathVariable("sn") String sn) {
		logger.info("tbox(" + sn + ")'s status has been setted to offline.");
		redisAPI.removeHash(RedisConstant.ONLINE_TBOX, sn);
		return sn + " offline";
	}

}
