package com.maxus.tsp.gateway.service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.gateway.conf.MqttConfiguration.MyGateway;
import com.maxus.tsp.gateway.conf.MqttProperties;
import com.maxus.tsp.gateway.entity.TaskDetails;
import com.maxus.tsp.gateway.entity.TaskGroup;
import com.maxus.tsp.gateway.repository.JPATaskGroupRepository;

@Service
public class MqttService {
	private static Logger logger = LogManager.getLogger(MqttService.class);
	@Autowired
	private MyGateway myGateway;
	@Autowired
	private MqttProperties mqttProperties;
	@Autowired
	JPATaskGroupRepository JPATaskGroupRepository;
	@Autowired
	private RedisAPI redisAPI;
	
	DecimalFormat df = new DecimalFormat("0");
	public final static String CURRENT_GROUP = "currentTestGroup";
	public final static String MQTT_PARAMS ="MQTT_TEST_PARAMS";
	final static String TOPIC_SUB_ALL = "TOPIC_SUB_ALL/";
	final static String TOPIC_SUB_ONE ="TOPIC_SUB_ONE/";
	
	final static int defaultQos = 1;
	final static int default_maxRetry = 5;
	
	private ExecutorService executorService = new ThreadPoolExecutor(200, 1000, 0, TimeUnit.SECONDS,
	        new LinkedBlockingQueue<Runnable>());

	public CountBean countBean = new CountBean();

	public void cleanCountBean() {
		String beforeBean = countBean.toString();
		countBean = new CountBean();
		resetThreadPoolSize();
		logger.info("{} cleaned",beforeBean);
	}
	
	public void resetThreadPoolSize(){
		int corePoolSize= Integer.parseInt(redisAPI.getHash(MQTT_PARAMS, "subPoolSize"));
		executorService = new ThreadPoolExecutor(corePoolSize, 1000, 0, TimeUnit.SECONDS,
		        new LinkedBlockingQueue<Runnable>());
		logger.info("Thread pool corePoolSize set to "+corePoolSize);
	}

	public String[] getSendTopics() {
		return mqttProperties.getSendTopics();
	}

	public void sendWithRetry(byte[] payload, String topic) {
		sendWithRetry(payload, topic, defaultQos);
	}

	public void sendWithRetry(byte[] payload, String topic, int qos) {

		boolean sendSuccess = false;
		int retry = 0;

		while (!sendSuccess) {
			try {
				myGateway.sendToMqtt(payload, topic, qos);
				sendSuccess = true;
				countBean.responsed.incrementAndGet();

			} catch (MessageHandlingException me) {
				// 过多发布32202异常 可以重试
				retry++;
				logger.warn("MQTT send to many, retry = " + retry);
				if (retry > default_maxRetry) {
					logger.error("MQTT send retry over max: " + default_maxRetry);
					countBean.addErrorMsg("mqtt response failed ");
				}
			}
		}
		if (retry > 0) {
			countBean.addErrorMsg("mqtt response tried " + retry);
		}
	}

	public void taskStart() {
		countBean.startReceive();
	}

	public void taskEnd(String topic) {
		countBean.endReceive(topic);
	}

	public void addErrorMsg(String msg) {
		countBean.addErrorMsg(msg);
	}
	
	public void sendToSubAll(byte[] payload,String topic,int qos){
		sendWithRetry(payload,TOPIC_SUB_ALL+topic,qos);
	}
	
	public void sendToSubOne(byte[] payload,String topic){
		sendWithRetry(payload,TOPIC_SUB_ONE+topic);
	}

	public TaskGroup saveTaskGroup(String groupName,int groupNum) {
		TaskGroup currentGroup = JPATaskGroupRepository.findByGroupNameAndGroupNum(groupName, groupNum);
		
		if(currentGroup !=null) {
			currentGroup.getDetailsList().clear();
			JPATaskGroupRepository.save(currentGroup);
			logger.info("Clear exists group details {}",currentGroup);
		}else {
			currentGroup = new TaskGroup(groupName,groupNum,new Date());
			JPATaskGroupRepository.save(currentGroup);
			logger.info("Create group {}",currentGroup);
		}
		redisAPI.setHash(MQTT_PARAMS, CURRENT_GROUP, JSON.toJSONString(currentGroup));
		
		sendToSubAll(new byte[1], "cleanStatus",0);
		return currentGroup;
	}
	
	public void addPubDetail(int num,long number,String ip,long startTime,long endTime,long nanoCostAll) {
		buildAndSaveDetail(num,number,"Publish",ip,startTime,endTime,nanoCostAll);
	}
	
	@Async
	private void buildAndSaveDetail(long num,long number,String category,String address,long startTime,long endTime,long nanoCostAll) {
		TaskDetails taskDetails = new TaskDetails(category,number,startTime,endTime);
		taskDetails.setTps(df.format(num*1000/(endTime-startTime)));
		taskDetails.setAvgCost(df.format(nanoCostAll/num));
		taskDetails.setAddress(address);
	
		String group = redisAPI.getHash(MQTT_PARAMS, CURRENT_GROUP);
		TaskGroup taskGroup = JSON.parseObject(group, TaskGroup.class);
		taskGroup = JPATaskGroupRepository.findById(taskGroup.getId()).get();
		taskGroup.getDetailsList().add(taskDetails);
		JPATaskGroupRepository.save(taskGroup);
		
		logger.info("Saved: {}",taskDetails);
	}
	
	public void execute(Runnable command) {
		executorService.execute(command);
	}

	public class CountBean {
		
		private AtomicLong received = new AtomicLong();
		private AtomicInteger currentThreads = new AtomicInteger();
		private AtomicInteger maxThreads = new AtomicInteger();
		public AtomicLong responsed = new AtomicLong();
		
		private long taskStart = 0;
		private long lastLogTime = 0;
		private long lastReceived = 0;
		private long nanoCostAll =0;
		public int groupCount = 0;
		private String ip = getHostIP();
		private volatile int logNum =0;
		
		private List<String> errorList = new ArrayList<>();
		ThreadLocal<Long> costMap = new ThreadLocal<>();
		
		private synchronized void startReceive() {
			if (received.get() == 0) {
				taskStart = System.currentTimeMillis();
				lastLogTime = taskStart;
				logNum= Integer.parseInt(redisAPI.getHash(MQTT_PARAMS, "subLogNum"));
			}
			costMap.set(System.nanoTime());			
			currentThreads.incrementAndGet();
			
			if (maxThreads.get() < currentThreads.get()) {
				maxThreads.set(currentThreads.get());
			}
		}
		
		public void saveSubStatus() {
			long currentTime  = System.currentTimeMillis();
			long currentReceived = received.get();
			buildAndSaveDetail(currentReceived -lastReceived,received.get(),"Subscribe",ip,lastLogTime,currentTime,nanoCostAll);
			nanoCostAll= 0;
			lastLogTime = currentTime;
			lastReceived = currentReceived;
		}
		
		private synchronized void endReceive(String topic) {
			if(costMap.get()==null) {
				return;
			}
			received.incrementAndGet();
			currentThreads.decrementAndGet();
			nanoCostAll += System.nanoTime()-costMap.get();
			if(received.get()%logNum==0) {
				saveSubStatus();
				logger.warn("status: {}",countBean);
			}
			costMap.set(null);
		}
		
		private void addErrorMsg(String errorMsg) {
			errorList.add(errorMsg);
		}

		@Override
		public String toString() {
			return "CountBean [received=" + received + ", maxThreads=" + maxThreads + ", responsed=" + responsed
					+ ", taskStart=" + taskStart + ", errorList=" + errorList + "]";
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
