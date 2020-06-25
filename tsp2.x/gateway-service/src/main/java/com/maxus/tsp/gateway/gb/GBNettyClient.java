package com.maxus.tsp.gateway.gb;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.maxus.tsp.gateway.ota.TspServiceProc;
import com.maxus.tsp.gateway.serviceclient.TspPlatformClient;
import com.maxus.tsp.platform.service.model.Platform;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

@Component()
public class GBNettyClient implements ApplicationContextAware {
	private final Logger log = LogManager.getLogger(GBNettyClient.class);
	@Autowired
	private TspPlatformClient tspPlatformClient;
	// 国家平台地址ip
	public static String platformIp = null;
	// 国家平台地址端口号
	public static int platformPort;
	// 国家平台登录用户名
	public static String userName = null;
	// 国家平台登录密码
	public static String userPassword = null;
	// 国家平台连接状态
	public static boolean isConnected = false;
	// private String reMsg;
	private String logStr;
	public GBTCPHandler data;

	/**
	 * 获取国家平台信息
	 */
	//@PostConstruct
	public void initGBPlatformInfo() {
		try {
			List<Platform> platformInfo = tspPlatformClient.getPlatformList();
			if (platformInfo != null) {
				platformIp = platformInfo.get(0).getIp();
				platformPort = platformInfo.get(0).getPort();
				userName = platformInfo.get(0).getUsername();
				userPassword = platformInfo.get(0).getPassword();
			}
			TspServiceProc.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					if (getNCConnect(platformIp, String.valueOf(platformPort), true, 10000)) {
						log.info("国家平台登陆报文成功");
						
					}

				}

			});

		} catch (Exception ex) {
			log.error("国家平台登陆报文失败");
			isConnected = false;
		}

	}

	/**
	 * 
	 * 
	 * @return
	 */
	public boolean canwork() {
		return data.canwork();
	}

	public String getSR() {
		return data.getSR();
	}
	
	public int getSendNum(){
		return data.sednum;
	}
	
	public int getRecvNum(){
		return data.recvnum;
	}

	public boolean ctxOK() {
		return data.ctxOK();
	}

	/**
	 * 发送消息，并返回发送消息序列号和接收消息序号。从连接建立开始从0开始的整数
	 * 
	 * @Title: sendMessage
	 * @Description:  
	 * @param: @param
	 *             msgs
	 * @param: @return
	 * @param: @throws
	 *             Exception
	 * @return: String
	 * @throws @author
	 *             fogmk
	 * @Date 2018年4月17日 上午11:05:01
	 */
	public String sendMessage(String msgs) throws Exception {

		data.sendMessage(msgs);
		return getSR();
	}

	ChannelFuture f;
	Bootstrap b;
	EventLoopGroup group;

	public boolean getNCConnect(String url, String port, boolean codeFlag, int maxTime) {
		boolean flag = true;
		logStr = url + ":" + port + " start connect ";
		log.debug(logStr);

		int iport = Integer.parseInt(port);
		data = new GBTCPHandler();
		try {
			group = new NioEventLoopGroup();
			data.init(maxTime);
			b = new Bootstrap();
			b.group(group);
			b.channel(NioSocketChannel.class);
			b.option(ChannelOption.TCP_NODELAY, true);
			b.handler(new ChannelInitializer<SocketChannel>() {
				@Override

				protected void initChannel(SocketChannel ch) {
					ch.pipeline().addLast(new CommandDecoder());
					ch.pipeline().addLast(new CommandEncoder());
					ch.pipeline().addLast(data);

				}
			});

			f = b.connect(url, iport).sync();
			isConnected = true;
			
			logStr = url + ":" + port + " connect success ";
			log.debug(logStr);
			f.channel().closeFuture().sync();

		} catch (InterruptedException e) {
			flag = false;
			logStr = url + ":" + port + " connect fail ";
			log.info(logStr);

		} catch (Exception e) {
			flag = false;
			logStr = url + ":" + port + "  connect fail ";
			log.info(logStr);
		} finally

		{
			group.shutdownGracefully();
		}
		return flag;

	}

	/**
	 * 清除掉已有的响应报文
	 */
	public void resetReplayTimeHT() {
		data.resetReplayTimeHT();
	}

	public String getSocketReplyTime() {
		return data.getSocketReplyTime();
	}
	
	public String getServerMsgBySendNum(int sendNum) {
		return data.getServerMsgBySendNum(sendNum);
	}

	/**
	 * 发送指定次数
	 * 
	 * @return
	 */
	public boolean sendOver(int count) {
		return data.sendOver(count);
	}

	public boolean isFinish(int count) {
		return data.isFinish(count);
	}

	public void getNCClose() {
		// String Str ="connect close 111111";

		log.debug("NettyClient.getNCClose");

		// data.init(codeFlag);
		if (data != null) {
			data.getClose();
		}
		if (group != null)
			group.shutdownGracefully();
		return;
	}

	@Override
	public void setApplicationContext(ApplicationContext arg0) throws BeansException {

	}

}
