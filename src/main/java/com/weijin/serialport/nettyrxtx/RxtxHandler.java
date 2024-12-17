package com.weijin.serialport.nettyrxtx;

import org.springframework.stereotype.Service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.rxtx.RxtxChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.ReferenceCountUtil;
import io.netty.channel.ChannelHandler;

/**
 * 串口接收数据处理器
 *
 * @author 程就人生
 * @Date
 */
@Service("rxtxHandler")
@ChannelHandler.Sharable
public class RxtxHandler extends SimpleChannelInboundHandler<byte[]> {

	protected void initChannel(RxtxChannel rxtxChannel) {
		rxtxChannel.pipeline().addLast(
				// new LineBasedFrameDecoder(60000),
				// 文本形式发送编解码
				// new StringEncoder(StandardCharsets.UTF_8),
				// new StringDecoder(StandardCharsets.UTF_8),
				// 十六进制形式发送编解码
				new ByteArrayDecoder(), new ByteArrayEncoder());
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
		// 文本方式编解码，String
		// System.out.println("接收到："+msg);
		// 十六进制发送编解码
		int dataLength = msg.length;
		ByteBuf buf = Unpooled.buffer(dataLength);
		buf.writeBytes(msg);
		System.out.println("接收到：");
		while (buf.isReadable()) {
			System.out.println(" " + buf.readByte());
		}
		// 释放资源
		ReferenceCountUtil.release(msg);
	}

}
