package com.weijin.serialport;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class SystemInitConfig implements CommandLineRunner, EnvironmentAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemInitConfig.class);

	@Override
	public void setEnvironment(Environment environment) {
		// TODO Auto-generated method stub
	}

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		initListener();
	}

	public static void initListener(){
		// 通过串口通信管理类获得当前连接上的端口列表
		// （获取一个枚举对象，该CommPortIdentifier对象包含系统中每个端口的对象集[串口、并口]）
		// 有效连接上的端口的枚举
		List<CommPortIdentifier> portLists = SerialUtil.findPort();
		for (CommPortIdentifier commPortIdentifier : portLists) {
			// 获取相应串口对象
			// 通讯端口管理，控制对通信端口的访问的中心类
			/*
			 * 判断端口类型是否为串口 PORT_SERIAL = 1; 【串口】 PORT_PARALLEL = 2; 【并口】
			 * PORT_I2C = 3; 【I2C】 PORT_RS485 = 4; 【RS485】 PORT_RAW = 5; 【RAW】
			 */
			if (commPortIdentifier.getPortType() != CommPortIdentifier.PORT_SERIAL) {
				continue;
			}
			LOGGER.info("串口设备名称：" + commPortIdentifier.getName());
			// 判断模拟COM4串口存在，就打开该串口
			LOGGER.info("测试串口设备名称：" + commPortIdentifier.getName());
			SerialPort serialPort = SerialUtil.openPort(commPortIdentifier.getName(), 115200);
			// 在串口引用不为空时进行下述操作
			if (Objects.isNull(serialPort)) {
				continue;
			}
			// 2. 设置串口监听器
			SerialUtil.addListener(serialPort, new RXTXSerialPortEventListener(serialPort));
		}
	}
}