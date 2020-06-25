/**        
 * CommandEncoder.java Create on 2017年8月3日      
 * Copyright (c) 2017年8月3日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>      
 * @version 1.0  
 */
package com.maxus.tsp.socket.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class CommandEncoder extends MessageToByteEncoder<ByteBuf> {

	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
		out.writeBytes(msg);
		ctx.flush();
	}

}
