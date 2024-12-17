package com.weijin.serialport.nettyrxtx;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * TODO
 *
 * @author GuestZ
 * @version 1.0
 * @date 2021/11/9 12:01
 */
public class RxtxClientHandler extends SimpleChannelInboundHandler<String> {

	private ChannelHandlerContext ctx;

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.writeAndFlush("success\n");
		this.ctx = ctx;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		System.out.println("接收到：" + msg);
		// ctx.writeAndFlush("已收到\n");
		// ctx.close();
	}

	public void write(String msg) {
		if (ctx != null) {
			ctx.writeAndFlush(msg);
		}
	}

}