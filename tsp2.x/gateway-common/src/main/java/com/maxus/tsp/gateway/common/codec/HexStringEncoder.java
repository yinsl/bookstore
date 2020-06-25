package com.maxus.tsp.gateway.common.codec;

import com.maxus.tsp.common.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;

/**
 * @author Administrator
 *
 */
public class HexStringEncoder extends MessageToByteEncoder {
	//	private static Logger logger = LogManager.getLogger(HexStringEncoder.class);
	//@Override
	private void encode1(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
		//logger.error("IntegerEncoder encode msg is " + msg);

		//ByteBuf encoded = ctx.alloc().buffer(msg.length());
		ByteBuf encoded = ctx.alloc().directBuffer(msg.length());
		encoded.writeBytes(ByteUtil.hexStringToBytes(msg));
		out.add(encoded);
		encoded.release();
	}

	@Override
	protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
		byteBuf.writeBytes((byte[]) o);
	}
}
