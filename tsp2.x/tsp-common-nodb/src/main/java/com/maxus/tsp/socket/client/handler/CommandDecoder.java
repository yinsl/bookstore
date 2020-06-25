/**        
 * CommandDecoder.java Create on 2017年8月3日      
 * Copyright (c) 2017年8月3日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>      
 * @version 1.0  
 */
package com.maxus.tsp.socket.client.handler;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class CommandDecoder extends ByteToMessageDecoder{


	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
		throws Exception
	{
		int length = in.readableBytes();
		byte bs[] = new byte[length];
		while (  in.isReadable()){
			in.readBytes(bs);
		}
		out.add(bs);
	}
}
