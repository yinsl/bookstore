/**        
 * JPushAPI.java Create on 2017年8月14日      
 * Copyright (c) 2017年8月14日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>      
 * @version 1.0  
 */
package com.maxus.tsp.common.jpush;

import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cn.jiguang.common.ClientConfig;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.PushPayload.Builder;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.audience.AudienceTarget;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;

/**
 * @ClassName: JPushAPI.java
 * @Description:  
 * @author 任怀宇
 * @version V1.0
 * @Date 2017年8月14日 下午2:55:54
 */
public class JPushAPI {
	private static Logger logger = LogManager.getLogger(JPushAPI.class);

	/**
	 * 手机端应用账户密码
	 */
	public static final String MASTER_SECRET = "020f65d91de8ebd88f32f6a8";
	/**
	 * 手机端应用账户
	 */
	public static final String APP_KEY = "a2f49e5bdb91e1c268480ed2";
	
	
	/**
	 * 娱乐主机应用账户密码
	 */
	public static final String MASTER_SECRET_ENTERTAINMENT = "41c5c7d1015621e834fbb050";
	/**
	 * 娱乐主机应用账户
	 */
	public static final String APP_KEY_ENTERTAINMENT = "74592cc0ca2d634b2c456cfc";
	
	
	public static JPushClient jpushClient = new JPushClient(MASTER_SECRET, APP_KEY, null, ClientConfig.getInstance());
	

	public static JPushClient jpushEstainmentClient = new JPushClient(MASTER_SECRET_ENTERTAINMENT, APP_KEY_ENTERTAINMENT, null, ClientConfig.getInstance());

	public static void setJPushClient( String appKey, String masterSecret, String appKeyEntertainment, String masterSecretEntertainment )
	{
		if (appKey != null && masterSecret != null && appKeyEntertainment != null && masterSecretEntertainment != null) {
		jpushClient = new JPushClient(masterSecret, appKey, null, ClientConfig.getInstance());
		jpushEstainmentClient = new JPushClient(masterSecretEntertainment, appKeyEntertainment, null, ClientConfig.getInstance());
		}
		else
		{
			logger.error("please provide valid prameter for Jpush Client configuration.");
		}
	}

	/**
	 * 
	 * @Title: sendMsg
	 * @Description: 推送一条消息
	 * @param: @param
	 *             alert
	 * @return: void
	 * @throws @author
	 *             fogmk
	 * @Date 2017年8月14日 下午3:03:15
	 */
	public static boolean pushAlert(String alias, String alert, String title) {

		// For push, all you need do is to build PushPayload object.
		boolean pushOk =false;
		try{
			PushPayload payload = buildPushObject_all_all_alert(alias, alert,title);
			logger.info("send pushAlert[ " + payload.toJSON().toString() + " ] to {" + alias + "}");
			pushOk = pushMsg(payload);
		}catch(Exception ex)
		{
			logger.error("Jpush Alert Exception:"+ex.getMessage());
			pushOk =false;
		}
		return pushOk;
	}

	/**
	 * 
	 * @Title: pushMessageWithExtras
	 * @Description:  
	 * @param: @param
	 *             extra
	 * @param: @param
	 *             alias
	 * @param: @param
	 *             title
	 * @param: @param
	 *             content
	 * @return: void
	 * @throws @author
	 *             fogmk
	 * @Date 2017年8月14日 下午3:33:33
	 */
	public static void pushMessageWithExtras(Hashtable<String, String> extra, String alias, String title,
			String content) {
		// For push, all you need do is to build PushPayload object.
		PushPayload payload = buildPushObject_audienceMore_messageWithExtras(extra, alias, title, content);
		payload.resetOptionsApnsProduction(true);
		pushMsg(payload);
	}

	/**
	 * 
	 * @Title: pushAlertEntertainment
	 * @Description: 推送娱乐主机一条消息
	 * @param: @param
	 *             alert
	 * @return: void
	 * @throws @author
	 *             yuji
	 * @Date 2017年8月14日 下午3:03:15
	 */
	public static boolean pushAlertEntertainment(String alias, String alert, String title) {

		// For push, all you need do is to build PushPayload object.
		boolean pushOk =false;
		try{
			PushPayload payload = buildPushObject_all_all_alert(alias, alert,title);
			pushOk = pushMsgEntertainment(payload);
		}catch(Exception ex)
		{
			logger.error("Jpush Alert to Entertainment Exception:"+ex.getMessage());
			pushOk =false;
		}
		return pushOk;
	}
	
	/**
	 * @Title: pushMsgEntertainment
	 * @Description: 推送报警信息至娱乐主机
	 * @param: @param
	 *             payload
	 * @return: void
	 * @throws @author
	 *             yuji
	 * @Date 2017年8月14日 下午3:31:16
	 */
	private static boolean pushMsgEntertainment(PushPayload payload) {

		try {
			PushResult result = jpushEstainmentClient.sendPush(payload);
			logger.info("Got result - " + result);
			return true;

		} catch (APIConnectionException e) {
			// Connection error, should retry later
			logger.error("Connection error, should retry later", e);
			return false;

		} catch (APIRequestException e) {
			// Should review the error, and fix the request
			logger.error("Should review the error, and fix the request", e);
			logger.info("HTTP Status: " + e.getStatus());
			logger.info("Error Code: " + e.getErrorCode());
			logger.info("Error Message: " + e.getErrorMessage());
			return false;
		}
	}
	
	/**
	 * @Title: pushMsg
	 * @Description:  
	 * @param: @param
	 *             payload
	 * @return: void
	 * @throws @author
	 *             fogmk
	 * @Date 2017年8月14日 下午3:31:16
	 */
	private static boolean pushMsg(PushPayload payload) {

		try {
			PushResult result = jpushClient.sendPush(payload);
			logger.info("Got result - " + result);
			return true;

		} catch (APIConnectionException e) {
			// Connection error, should retry later
			logger.error("Connection error, should retry later", e);
			return false;

		} catch (APIRequestException e) {
			// Should review the error, and fix the request
			logger.error("Should review the error, and fix the request", e);
			logger.info("HTTP Status: " + e.getStatus());
			logger.info("Error Code: " + e.getErrorCode());
			logger.info("Error Message: " + e.getErrorMessage());
			return false;
		}
	}

	/**
	 * 
	 * @Title: buildPushObject_all_all_alert
	 * @Description:  
	 * @param: @param
	 *             alias
	 * @param: @param
	 *             alert
	 * @param: @param
	 *             title
	 * @param: @param
	 *             isIOS    true:ios, false:android             
	 * @param: @return
	 * @return: PushPayload
	 * @throws @author
	 *             fogmk
	 * @Date 2017年8月14日 下午3:07:27
	 */
	private static PushPayload buildPushObject_all_all_alert(String alias, String alert,String title) {


		PushPayload curPushload = PushPayload.newBuilder()
				.setPlatform(Platform.android_ios())
				.setAudience(Audience.alias(alias))
				.setNotification(Notification.newBuilder()
						.addPlatformNotification(AndroidNotification.newBuilder()
								.addExtra("title", title)
								.setAlert(alert)
								.build())
						.addPlatformNotification(IosNotification.newBuilder()
								.addExtra("title", title)
								.setAlert(alert)
								.disableBadge()
								.build())
						.build())
				.build();
		curPushload.resetOptionsApnsProduction(true);
		return curPushload;	
	}

	/**
	 * 
	 * @Title: buildPushObject_audienceMore_messageWithExtras
	 * @Description:  
	 * @param: @param
	 *             extra
	 * @param: @param
	 *             alias
	 * @param: @param
	 *             title
	 * @param: @param
	 *             content
	 * @param: @return
	 * @return: PushPayload
	 * @throws @author
	 *             fogmk
	 * @Date 2017年8月14日 下午3:30:21
	 */
	public static PushPayload buildPushObject_audienceMore_messageWithExtras(Hashtable<String, String> extra,
			String alias, String title, String content) {
		Builder setAudience = PushPayload.newBuilder().setPlatform(Platform.all())
				.setAudience(Audience.newBuilder().addAudienceTarget(AudienceTarget.alias(alias)).build());
		cn.jpush.api.push.model.Message.Builder addExtra = Message.newBuilder().setMsgContent(content);
		if (title != null && title.length() > 0)
			addExtra.setTitle(title);
		if (extra != null)
			for (String key : extra.keySet()) {
				addExtra.addExtra(key, extra.get(key));
			}
		return setAudience.setMessage(addExtra.build()).build();
	}

	/**
	 * 
	 * @Title: main
	 * @Description:  
	 * @param: @param
	 *             args
	 * @return: void
	 * @throws @author
	 *             fogmk
	 * @Date 2017年8月16日 上午9:07:18
	 */
//	public static void main(String args[]) {
//		//boolean result =JPushAPI.pushAlert("3469d6935ccd45bdae095f1c9a04624d", "nihao","happenTime:20171025");
//		//boolean result =JPushAPI.pushAlert("3cc66d124f8a471ca48db6e820eb206f", "nihao","happenTime:20171025");
//		//JPushAPI.pushMessageWithExtras(null,"3469d6935ccd45bdae095f1c9a04624d","1","notice message warning test");
//		//JPushAPI.pushMessageWithExtras(null,"3cc66d124f8a471ca48db6e820eb206f","1","notice message warning test");
//		boolean result =JPushAPI.pushAlertEntertainment("0617090000213485213485", "nihao","happenTime:20171025");
//		System.out.println("test result:"+result);
//	}

}
