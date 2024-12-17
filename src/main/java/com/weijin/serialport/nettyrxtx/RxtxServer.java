package com.weijin.serialport.nettyrxtx;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.rxtx.RxtxChannel;
import io.netty.channel.rxtx.RxtxChannelConfig;
import io.netty.channel.rxtx.RxtxChannelConfig.Databits;
import io.netty.channel.rxtx.RxtxChannelConfig.Paritybit;
import io.netty.channel.rxtx.RxtxChannelConfig.Stopbits;
import io.netty.channel.rxtx.RxtxChannelOption;
import io.netty.channel.rxtx.RxtxDeviceAddress;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 串口接收数据的服务端
 *
 * @author 程就人生
 * @Date
 */
@Slf4j
@Component
public class RxtxServer {

	private RxtxChannel channel;

	/**
	 * 波特率
	 */
	@Value("${serial.baudrate:115200}")
	private int baudrate;
	/**
	 * 串口数据位
	 */
	@Value("${serial.datebits:8}")
	private int datebits;

	/**
	 * 主线程组数量
	 */
	@Value("${netty.bossThread:1}")
	private int bossThread;

	/**
	 * 数据位 默认8位
	 * 可以设置的值：SerialPort.DATABITS_5、SerialPort.DATABITS_6、SerialPort.DATABITS_7、SerialPort.DATABITS_8
	 */
	private Databits dataBits = RxtxChannelConfig.Databits.DATABITS_8;
	/**
	 * 停止位
	 * 可以设置的值：SerialPort.STOPBITS_1、SerialPort.STOPBITS_2、SerialPort.STOPBITS_1_5
	 */
	private Stopbits stopBits = RxtxChannelConfig.Stopbits.STOPBITS_1;
	/**
	 * 校验位
	 * 可以设置的值：SerialPort.PARITY_NONE、SerialPort.PARITY_ODD、SerialPort.PARITY_EVEN、SerialPort.PARITY_MARK、SerialPort.PARITY_SPACE
	 */
	private Paritybit parity = RxtxChannelConfig.Paritybit.NONE;

	public void start() {
		CompletableFuture.runAsync(() -> {
			try {
				// 阻塞的函数
				createRxtx();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, Executors.newSingleThreadExecutor());// 不传默认使用ForkJoinPool，都是守护线程
	}

	public void createRxtx() throws Exception {
		// 串口使用阻塞io
		EventLoopGroup group = new NioEventLoopGroup(this.bossThread);
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group).channel(RxtxChannel.class)
					.option(RxtxChannelOption.BAUD_RATE, baudrate)
					.option(RxtxChannelOption.DATA_BITS, dataBits).option(RxtxChannelOption.PARITY_BIT, parity)
					.option(RxtxChannelOption.STOP_BITS, stopBits).handler(new RxtxHandler());
			ChannelFuture f = bootstrap.connect(new RxtxDeviceAddress("COM1")).sync();
			f.addListener(connectedListener);
			f.channel().closeFuture().sync();
		} finally {
			group.shutdownGracefully();
		}
	}

	// 连接监听
	GenericFutureListener<ChannelFuture> connectedListener = (ChannelFuture f) -> {
		f.channel().eventLoop();
		if (!f.isSuccess()) {
			log.info("连接失败");
		} else {
			channel = (RxtxChannel) f.channel();
			log.info("连接成功");
			sendData();
		}
	};

	/**
	 * 发送数据
	 */
	public void sendData() {
		// 十六机制形式发送
		ByteBuf buf = Unpooled.buffer(2);
		buf.writeByte(3);
		buf.writeByte(2);
		channel.writeAndFlush(buf.array());

		// 文本形式发送
		// channel.writeAndFlush("2");
	}
}