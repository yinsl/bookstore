package com.maxus.tsp.gateway.gb;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.platform.service.model.vo.RealTimeDataOperation;
import com.maxus.tsp.socket.client.handler.TCPHandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class GBTCPHandler extends TCPHandler {
	// 记录日志
	private static Logger log = LogManager.getLogger(GBTCPHandler.class);
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
		log.info("TCPHandler.channelRead0");
		if (msg.length == 0) {
			// recvnum = 0;
			log.info("msg.length");
			// ctx.close();
			// return;
		} else {

			recvnum++;
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
			log.info(str);
			// System.out.println(str);
			RealTimeDataOperation result = new RealTimeDataOperation(ByteUtil.hexStringToBytes(body));
			log.info("解析结果： " + JSONObject.toJSON(result));
			replayTimeHT.put(Integer.valueOf(recvnum), body);
		}
		return;
	}

	// 一连接就发送数据
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		log.info("TCPHandler.channelActive");
		this.ctx = ctx;
		super.channelActive(ctx);

	}

	public void sendMessage(String reMsg) {
		log.info("TCPHandler.sendMessage");
		this.msg = reMsg;
		if (ctx == null) {
			log.info("TCPHandler.sendMessage ctx is null ...");
			throw new RuntimeException("TCPHandler.sendMessage ctx is null ... ");
		} else {
			if (msg != null && sednum == recvnum) {
				log.info(sednum + ":" + recvnum + "发送" + reMsg);
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

	public String getServerMsgBySendNum(int sendNum) {
		if (replayTimeHT.containsKey(sendNum)) {
			return replayTimeHT.get(sendNum);
		} else {
			return null;
		}
	}

	// 解析服务器信息
	// public void dealWithServerMsg(String msg) {
	// if (msg != null) {
	// try {
	// byte[] respMsg = ByteUtil.hexStringToBytes(msg);
	// String serverInfo= "本次指令：";
	// for(CommandIdentifierEnum code: CommandIdentifierEnum.values()){
	// if(code.getCode() == (respMsg[2]))
	// serverInfo += code.getValue();
	// }
	// if (respMsg[3] == (byte)0x01){
	// serverInfo += "成功";
	// } if (respMsg[3] == (byte)0x02) {
	// serverInfo += "失败";
	// }
	//
	// log.info(serverInfo);
	// //检查包的长度是否符合标准
	// } catch (Exception e) {
	// e.printStackTrace();
	// log.error("解析服务器返回报文失败！");
	// }
	// }
	//
	// }
}
