/**        
 * TCPHandler.java Create on 2017年8月3日      
 * Copyright (c) 2017年8月3日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>      
 * @version 1.0  
 */
package com.maxus.tsp.socket.client.handler;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.maxus.tsp.common.util.ByteUtil;

//import com.maxus.socketClient.netty.data;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class TCPHandler extends SimpleChannelInboundHandler<byte[]> {

	private final Logger log = LogManager.getLogger(TCPHandler.class);

	private String msg;

	public Boolean state = true; // 通道是开通的
	ChannelHandlerContext ctx;
	byte data[];
	int sednum = 0;
	int recvnum = 0;
	long sendtime;
	// int code;
	int maxTime;
	Hashtable<Integer, String> replayTimeHT = new Hashtable<>();

	public void init(int maxTime) {

		// this.code = code;
		this.maxTime = maxTime;
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.fireExceptionCaught(cause);
	}

	// 清除以后报文的响应信息
	public void resetReplayTimeHT() {
		replayTimeHT.clear();
	}

	// 接收server端的消息，并打印出来
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
		log.debug("TCPHandler.channelRead0");
		recvnum++;
		if (msg.length == 0) {
			// recvnum = 0;
			log.info("connect close");
			ctx.close();
			return;
		} else {

			String body = "";

			Long time = System.currentTimeMillis() - sendtime;
			String sendLog = "s:" + sednum + "r:" + recvnum + "receivetime:" + time;
			log.debug(sendLog);
			// 输出响应时间大于maxTime毫秒的报文处理结果
			if (time > maxTime)
				System.out.println(sendLog);
			// HEX编码方式
			body = ByteUtil.byteToHex((byte[]) msg);

			// if(body.charAt(arg0))
			String str = " <-- " + body;
			log.debug(str);
			// System.out.println(str);
			replayTimeHT.put(Integer.valueOf(recvnum), time + " ota: " + body);
			return;
		}

	}

	// 一连接就发送数据
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		log.debug("TCPHandler.channelActive");
		this.ctx = ctx;
		super.channelActive(ctx);

	}

	public void sendMessage(String reMsg) {
		log.debug("TCPHandler.sendMessage");
		this.msg = reMsg;
		if (ctx == null) {
			log.info("TCPHandler.sendMessage ctx is null ...");
			throw new RuntimeException("TCPHandler.sendMessage ctx is null ... ");
		} else {
			if (msg != null && sednum == recvnum) {
				log.debug(sednum + ":" + recvnum + "发送" + reMsg);
				sednum++;

				// HEX编码方式
				data = ByteUtil.hexStringToBytes(msg);

				ByteBuf out = ctx.alloc().buffer(data.length);
				out.writeBytes(data);
				sendtime = System.currentTimeMillis();
				ctx.writeAndFlush(out);
				msg = null;

			} else {
				System.out.println(getSR());
			}
		}
	}

	public void getClose() {

		ctx.close();
	}

	/**
	 * 发送指定次数
	 * 
	 * @return
	 */
	public boolean sendOver(int count) {
		return this.sednum > count;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public boolean canwork() {

		return (sednum == recvnum);
	}

	public boolean ctxOK() {
		return (this.ctx != null);
	}

	public String getSR() {
		return "s:" + this.sednum + " r:" + this.recvnum;
	}

	/**
	 * 接收到指定次数
	 * 
	 * @return
	 */
	public boolean isFinish(int count) {
		return this.recvnum >= count;
	}

	public String getSocketReplyTime() {
		Enumeration<Integer> keys = replayTimeHT.keys();
		StringBuffer result = new StringBuffer("Replay: ");
		while (keys.hasMoreElements()) {
			Integer key = keys.nextElement();
			result.append(key).append(":").append(replayTimeHT.get(key)).append(", ");

		}
		return result.toString();

	}

}
