package com.maxus.tsp.stress.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.StringUtil;
import com.maxus.tsp.stress.conf.MqttConfiguration.MyGateway;
import com.maxus.tsp.stress.conf.MqttProperties;
import com.maxus.tsp.stress.msgs.HeartBeatMsg;
import com.maxus.tsp.stress.msgs.LoginMsg;
import com.maxus.tsp.stress.service.MqttService;

@RestController
@EnableConfigurationProperties(MqttProperties.class)
public class ProducerController {

	@Autowired
	private MqttProperties mqttProperties;

	// private static final String TBOX_GW = "A/B/C/D/E1/";
	// private static final String GW_TBOX = "GW/TBOX/";

	private static Logger logger = LogManager.getLogger(ProducerController.class);

	@Autowired
	private MyGateway gateway;
	@Autowired
	private MqttService mqttService;
	@Autowired
	private LoginMsg loginMsg;

	@Autowired
	private HeartBeatMsg heartBeatMsg;

	@Autowired
	private RedisAPI redisAPI;
	
	ThreadLocal<JSONObject> local = new ThreadLocal<>();
	
	@RequestMapping("/sendPlain")
	public void send(String topic, String msg) throws Exception {
		String message = "send topic: {} , payload: {} nano cost: {}";
		long nano = System.nanoTime();
		gateway.sendToMqtt(StringUtil.hexStringToBytes((msg==null?" ":msg)), topic, 1);		
		logger.info(message,topic,msg,System.nanoTime() - nano);
	}

	@RequestMapping("/testUploadAll")
	public void testUploadAll(String topic, String msg, Integer start, Integer end,Integer corePoolSize,Integer maxPoolSize,Boolean isClearStatus,Boolean isShare) throws InterruptedException {
		JSONObject request = new JSONObject();
		request.put("topic", topic);
		request.put("msg", msg);
		request.put("start", start);
		request.put("end", end);
		request.put("corePoolSize", corePoolSize);
		request.put("maxPoolSize", maxPoolSize);
		request.put("isClearStatus", isClearStatus==null?true:isClearStatus);
		request.put("isShare", isShare==null?true:isShare);
		
		mqttService.sendToPubAll(request.toJSONString().getBytes(), "stressStart",0);
	}
	
	@RequestMapping("/loadParams")
	public JSONObject loadParams() {
		return mqttService.loadParams();
	}
	@RequestMapping("/modifyParams")
	public void modifyParams(String subCorePoolSize,String subLogNum,String pubLogNum) {
		mqttService.modifyParams(subCorePoolSize, subLogNum, pubLogNum);
	}
	@RequestMapping("/switch")
	public String  switchInfinite() {
		return String.valueOf(mqttService.switchStatus());
	}
	@RequestMapping("/addGroup")
	public boolean  addGroup(String groupName,int groupNum) {
		return mqttService.addGroup(groupName, groupNum);
	}

	@RequestMapping("/login")
	public void login(String sn, String encyptMode, String seqNum, String secureKey, String majorVer, String minorVer) throws Exception {
		String topic = (mqttProperties.getSendTopics()[0]).replaceAll("#", sn);
		logger.info("[sn : {}] [encyptMode : {}] [seqNum : {}] [secureKey : {}] [majorVer : {}] [minorVer : {}]", sn,
				encyptMode, seqNum, secureKey, majorVer, minorVer);
		
		String msg = loginMsg.getLoginPackage(sn, encyptMode, seqNum, secureKey, majorVer, minorVer);
		logger.info("发送登陆报文 : topic: [{}] msg : [{}]", topic, msg);
		
		mqttService.sendWithRetry(StringUtil.hexStringToBytes(msg), topic);
	}
	
	@RequestMapping("/heartBeat")
	public void heartBeat(String sn, String encyptMode, String seqNum) throws Exception {
		String msg = heartBeatMsg.getHeartBeatPackage(sn, encyptMode, seqNum);
		boolean online = redisAPI.hasKey(RedisConstant.ONLINE_TBOX, sn);
		logger.info("tbox(" + sn + ") is online? " + online);
		logger.info("tbox(" + sn + ")'s heartBeat msg is  " + msg);
		String topic = (mqttProperties.getSendTopics()[0]).replaceAll("#", sn);
		logger.info("topic is  " + topic);
		// topic = TBOX_GW + sn;
		// logger.info("topic is  " + topic);
		gateway.sendToMqtt(StringUtil.hexStringToBytes(msg), topic, 1);
//		topic = TBOX_GW + sn;
//		logger.info("topic is  " + topic);
		mqttService.sendWithRetry(StringUtil.hexStringToBytes(msg), topic);
	}
	
}
