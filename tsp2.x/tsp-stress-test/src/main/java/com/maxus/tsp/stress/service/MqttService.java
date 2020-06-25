package com.maxus.tsp.stress.service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.StringUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.stress.conf.MqttConfiguration.MyGateway;

@Service
public class MqttService {
	private static Logger logger = LogManager.getLogger(MqttService.class);
	@Autowired
	private MyGateway myGateway;
	
	static int defaultQos = 1;
	static int default_maxRetry =5;
	DecimalFormat df = new DecimalFormat("0");
	final static String MQTT_PARAMS = "MQTT_TEST_PARAMS";
	final static String INFINITE = "infinite";
	final static String TOPIC_PUB_ALL = "TOPIC_PUB_ALL/";
	final static String TOPIC_PUB_ONE ="TOPIC_PUB_ONE/";
	final static String TOPIC_SUB_ALL = "TOPIC_SUB_ALL/";
	final static String TOPIC_SUB_ONE ="TOPIC_SUB_ONE/";
	
	public final static String CMD_STRART_STRESS ="stressStart";
	
	@Autowired
	RedisAPI redisApi;
	public CountBean countBean = new CountBean();
	public void clearBean() {
		countBean = new CountBean();
	}
	
	public void addRunningTask() {
		countBean.addRunningTask();
	}
	
	public void addErrorMsg(String msg) {
		countBean.addErrorMsg(msg);
	}
	
	public boolean sendToSubOne(byte[] payload,String topic){
		return sendWithRetry(payload,TOPIC_SUB_ONE+topic);
	}
	
	public boolean sendToSubAll(byte[] payload,String topic) {
		return sendWithRetry(payload, TOPIC_SUB_ALL + topic);
	}

	public boolean sendToPubOne(byte[] payload, String topic) {
		return sendWithRetry(payload, TOPIC_PUB_ONE + topic);
	}

	public boolean sendToPubAll(byte[] payload, String topic) {
		return sendWithRetry(payload, TOPIC_PUB_ALL + topic);
	}
	
	public boolean sendToPubAll(byte[] payload, String topic,int qos) {
		return sendWithRetry(payload, TOPIC_PUB_ALL + topic,qos);
	}

	public boolean sendWithRetry(byte[] payload, String topic) {
		return sendWithRetry(payload, topic, defaultQos);
	}

	public boolean sendWithRetry(byte[] payload,String topic,int qos) {
		
		boolean sendSuccess =false;
		int retry =0;
		
		while(!sendSuccess) {
			try {
				myGateway.sendToMqtt(payload, topic, qos);
				sendSuccess = true;
			}catch(MessageHandlingException me) {
				//过多发布32202异常 可以重试
				retry++;
				if(retry > default_maxRetry) {
					logger.error("MQTT send retry over "+default_maxRetry);
					addErrorMsg("Send failed");
				}
				countBean.resend.incrementAndGet();
				logger.info("MQTT retry= "+retry);
			}
		}
		return sendSuccess;
	}
	
	public void sendWithCount(String name,String str,String topic) {
		
		try {
			addRunningTask();
			sendWithRetry(StringUtil.hexStringToBytes(str),topic);
		}catch (Exception e) {
			logger.error("{} task send error :{}", name, e.getMessage());
			addErrorMsg(e.getMessage());
		} finally {
			endTask();
		}
	}
	
	public Boolean switchStatus() {
		boolean status =false;
		if (Boolean.parseBoolean(redisApi.getHash(MQTT_PARAMS, INFINITE))) {
			redisApi.setHash(MQTT_PARAMS, INFINITE, String.valueOf(status));
		}else {
			status=true;
			redisApi.setHash(MQTT_PARAMS, INFINITE, String.valueOf(status));
		}
		logger.info("Status switch to {}",status);
		return status;
	}
	
	public boolean addGroup(String groupName,int groupNum) {
		JSONObject group = new JSONObject();
		group.put("groupName", groupName);
		group.put("groupNum", groupNum);
		logger.info("Send group: {}",group);
		return sendToSubOne(group.toJSONString().getBytes(),"addGroup");
	}
	
	public boolean addPubDetails(long num, long number,
			String ip, long startTime,
			long endTime,long nanoCostAll) {
		
		JSONObject detail = new JSONObject();
		detail.put("num", num);
		detail.put("number", number);
		detail.put("ip", ip);
		detail.put("startTime", startTime);
		detail.put("endTime", endTime);
		detail.put("nanoCostAll", nanoCostAll);
		logger.info("Send detail {}",detail);
		return sendToSubOne(detail.toJSONString().getBytes(),"addDetail");
	}
	
	public JSONObject loadParams() {
		JSONObject params = new JSONObject();
		
		redisApi.getHashAll(MQTT_PARAMS).forEach((k,v)->{
			params.put(k,v);
		});
		logger.info("Load mqtt params: {}",params);
		return params;
	}
	
	public void modifyParams(String subPoolSize,String subLogNum,String pubLogNum) {
		redisApi.setHash(MQTT_PARAMS, "subPoolSize", subPoolSize);
		redisApi.setHash(MQTT_PARAMS, "subLogNum", subLogNum);
		redisApi.setHash(MQTT_PARAMS, "pubLogNum",pubLogNum);
		logger.info("Params modified to {}, {}, {}",subPoolSize,subLogNum,pubLogNum);
	}

	public void stressTest(String topic, String msg, Integer start, Integer end, Integer corePoolSize,
			Integer maxPoolSize, boolean isClear,boolean isShare) {

		if (isClear) {
			clearBean();
		}

		logger.info("压力测试开始  topic {}, start {}, end {}", topic, start, end);
		try {
			ExecutorService executorService = new ThreadPoolExecutor(corePoolSize == null ? 2 : corePoolSize,
					maxPoolSize == null ? 200 : maxPoolSize, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

			startTask();
			int count = 0;

			do {
				logger.info("开始执行 " + ++count);

				for (int i = start == null ? 1 : start; i <= (end == null ? 1 : end); i++) {
					String name = String.valueOf(i);
					executorService.execute(() -> {
						
						sendWithCount(name, msg == null ? " " : msg, isShare?(TOPIC_SUB_ONE+topic):(TOPIC_SUB_ALL+topic));
					});
				}
				while(countBean.runningTask.get() > 0) {
					logger.info("等待发送结束...");
					executorService.awaitTermination(20,TimeUnit.SECONDS);
				}
				
			} while (Boolean.parseBoolean(redisApi.getHash(MQTT_PARAMS, INFINITE)));

			logger.info("压力测试结束  {}",countBean);

		} catch (Exception e) {
			addErrorMsg(ThrowableUtil.getErrorInfoFromThrowable(e));
			logger.error(ThrowableUtil.getErrorInfoFromThrowable(e));
		}
	}

	private void startTask() {
		countBean.startTask();
	}
	
	private void endTask() {
		countBean.endTask();
	}
	
	public class CountBean {
		
		public AtomicInteger runningTask = new AtomicInteger();
		public AtomicInteger maxRunning = new AtomicInteger();
		public AtomicLong send = new AtomicLong();
		public AtomicInteger resend = new AtomicInteger();
		public AtomicLong received = new AtomicLong();

		private long taskStartTime = 0;
		private long lastLogTime=0;
		private long nanoCostAll =0;
		private String ip = getHostIP();
		private long lastSend = 0;
		
		private int logNum = 0;
		
		private List<String> errorList = new LinkedList<>();
		private ThreadLocal<Long> costMap = new ThreadLocal<>();
		
		public void addErrorMsg(String msg) {
			errorList.add(msg);
		}

		@Override
		public String toString() {
			return "CountBean "+ip+" [maxRunning=" + maxRunning + ", sent=" + send.get() + ", resend=" + resend + "] error list:" + JSON.toJSONString(errorList);
		}

		public void startTask() {
			taskStartTime = System.currentTimeMillis();
			lastLogTime = taskStartTime;
			logNum = Integer.parseInt(redisApi.getHash(MQTT_PARAMS, "pubLogNum"));
		}

		public synchronized void endTask() {
			send.incrementAndGet();
			runningTask.decrementAndGet();
			nanoCostAll += System.nanoTime()-costMap.get();
			if(send.get()%logNum==0) {
				savePubStatus();
			}
		}
		
		public void savePubStatus() {
			long currentSend = send.get();
			long currentTime = System.currentTimeMillis();
			addPubDetails(currentSend -lastSend,currentSend,ip,lastLogTime,currentTime,nanoCostAll);
			nanoCostAll= 0;
			logger.info("Save pub details {}",this);
			lastLogTime = currentTime;
			lastSend = currentSend;
		}

		public synchronized void addRunningTask() {
			runningTask.incrementAndGet();
			costMap.set(System.nanoTime());
			if (maxRunning.intValue() < runningTask.intValue()) {
				maxRunning.set(runningTask.intValue());
			}
		}
	}
	
	public String getHostIP() {

		Enumeration<NetworkInterface> allNetInterfaces = null;
		String resultIP = null;
		try {
			allNetInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		InetAddress ip = null;
		while (allNetInterfaces.hasMoreElements()) {
			NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
			Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
			while (addresses.hasMoreElements()) {
				ip = (InetAddress) addresses.nextElement();
				if (ip != null && ip instanceof Inet4Address && !ip.isLoopbackAddress()) {
					if (resultIP == null) {
						return ip.getHostAddress();
					}

				}
			}
		}
		return resultIP;
	}


}
