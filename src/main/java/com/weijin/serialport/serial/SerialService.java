package com.weijin.serialport.serial;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * @author Wang Huan
 * @author 18501667737@163.com
 * @date 2021/1/19 12:28
 */
@Service
public class SerialService {

	private static final Logger logger = LoggerFactory.getLogger(SerialService.class);// slf4j 日志记录器
	public Map<String, SerialPort> serialPorts = new HashMap<String, SerialPort>();

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
	 * 停止位
	 */
	@Value("${serial.stopbits:1}")
	private int stopbits;

	/**
	 * 校验位
	 */
	@Value("${serial.parity:0}")
	private int parity;

	public void start() {
		// 通过串口通信管理类获得当前连接上的端口列表
		// （获取一个枚举对象，该CommPortIdentifier对象包含系统中每个端口的对象集[串口、并口]）
		// 有效连接上的端口的枚举
		List<CommPortIdentifier> portLists = findPort();
		for (CommPortIdentifier CommPortIdentifier : portLists) {
			logger.info("串口设备名称：" + CommPortIdentifier.getName());
			// 判断模拟COM4串口存在，就打开该串口
			logger.info("测试串口设备名称：" + CommPortIdentifier.getName());
			SerialPort serialPort = openComPort(CommPortIdentifier);
			// 在串口引用不为空时进行下述操作
			if (Objects.isNull(serialPort)) {
				continue;
			}
			// 2. 设置串口监听器
			addListener(serialPort, new RXTXSerialPortEventListener(serialPort));
			// closeComPort(serialPort);
			serialPorts.put(CommPortIdentifier.getName(), serialPort);
		}
	}

	/**
	 * 添加监听器
	 *
	 * @param port     串口对象
	 * @param listener 串口监听器 // * @throws TooManyListeners 监听类对象过多
	 */
	private void addListener(SerialPort port, SerialPortEventListener listener) {
		try {

			// 给串口添加监听器
			port.addEventListener(listener);
			// 设置当有数据到达时唤醒监听接收线程
			port.notifyOnDataAvailable(true);
			// 设置当通信中断时唤醒中断线程
			port.notifyOnBreakInterrupt(true);

			// 设置监听器在有数据时通知生效
			port.notifyOnDataAvailable(true);
			// 3. 设置串口相关读写参数
			// 比特率、数据位、停止位、校验位
			port.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
		} catch (Exception e) {
			// throw new TooManyListeners();
		}
	}

	/**
	 * 关闭监听
	 *
	 * @param port
	 */
	public void removeListener(SerialPort port) {
		port.notifyOnRingIndicator(false);
		port.notifyOnParityError(false);
		port.notifyOnOverrunError(false);
		port.notifyOnOutputEmpty(false);
		port.notifyOnFramingError(false);
		port.notifyOnDSR(false);
		port.notifyOnDataAvailable(false);
		port.notifyOnCTS(false);
		port.notifyOnCarrierDetect(false);
		port.notifyOnBreakInterrupt(false);
		port.removeEventListener();
	}

	/**
	 * 查找所有可用端口
	 *
	 * @return 可用端口名称列表
	 */
	public ArrayList<String> findPortName() {
		// 获得当前所有可用串口
		List<CommPortIdentifier> portList = findPort();
		ArrayList<String> portNameList = new ArrayList<>();
		// 将可用串口名添加到List并返回该List
		for (CommPortIdentifier serialPort : portList) {
			portNameList.add(serialPort.getName());
		}
		return portNameList;
	}

	private List<CommPortIdentifier> findPort() {
		// 获得当前所有可用串口
		List<CommPortIdentifier> list = new ArrayList<CommPortIdentifier>();
		Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
		while (portList.hasMoreElements()) {
			CommPortIdentifier commPort = portList.nextElement();
			/*
			 * 判断端口类型是否为串口 PORT_SERIAL = 1; 【串口】 PORT_PARALLEL = 2; 【并口】 PORT_I2C = 3; 【I2C】
			 * PORT_RS485 = 4; 【RS485】 PORT_RAW = 5; 【RAW】
			 */
			if (commPort.getPortType() != CommPortIdentifier.PORT_SERIAL) {
				continue;
			}
			list.add(commPort);
			}
		return list;
	}

	/**
	 * 往串口发送数据
	 *
	 * @param serialPort 串口对象
	 * @param order      待发送数据 // * @throws SendDataToSerialPortFailure 向串口发送数据失败 //
	 *                   * @throws SerialPortOutputStreamCloseFailure 关闭串口对象的输出流出错
	 */
	public void sendToPort(String portName, String context) {
		SerialPort serialPort = serialPorts.get(portName);
		if (serialPort != null) {
			sendToPort(serialPort, context.getBytes());
			closeComPort(serialPort);
		}
		// 获得当前所有可用串口
		List<CommPortIdentifier> portList = findPort();
		for (CommPortIdentifier CommPortIdentifier : portList) {
			if (CommPortIdentifier.getName().equals(portName)) {
				serialPort = openComPort(CommPortIdentifier);
				sendToPort(serialPort, context.getBytes());
				closeComPort(serialPort);
			}
		}
	}

	/**
	 * 往串口发送数据
	 *
	 * @param serialPort 串口对象
	 * @param order      待发送数据 // * @throws SendDataToSerialPortFailure 向串口发送数据失败 //
	 *                   * @throws SerialPortOutputStreamCloseFailure 关闭串口对象的输出流出错
	 */
	private void sendToPort(SerialPort serialPort, byte[] order) {
		OutputStream out = null;
		try {
			if (serialPort != null) {
				out = serialPort.getOutputStream();
				out.write(order);
				out.flush();
				logger.info("往串口 " + serialPort.getName() + " 发送数据：" + new String(order) + " 完成...");
			} else {
				logger.error("gnu.io.SerialPort 为null，取消数据发送...");
			}
		} catch (IOException e) {
			// throw new SendDataToSerialPortFailure();
		} finally {
			try {
				if (out != null) {
					out.close();
					out = null;
				}
			} catch (IOException e) {
				// throw new SerialPortOutputStreamCloseFailure();
			}
		}
	}

	/**
	 * 关闭串口
	 *
	 * @param serialport 待关闭的串口对象
	 */
	public void closeComPort(String name) {
		// 获得当前所有可用串口
		List<CommPortIdentifier> portList = findPort();
		for (CommPortIdentifier commPortIdentifier : portList) {
			if (commPortIdentifier.getName().equals(name)) {
				SerialPort serialPort = openComPort(commPortIdentifier);
				closeComPort(serialPort);
				break;
			}
		}
	}

	/**
	 * 关闭串口
	 *
	 * @param serialport 待关闭的串口对象
	 */
	private void closeComPort(SerialPort serialPort) {
		if (serialPort != null) {
			serialPort.close();
			logger.info("关闭串口 " + serialPort.getName());
		}
	}

	/**
	 * 打开电脑上指定的串口
	 *
	 * @param portName 端口名称，如 COM1，为 null 时，默认使用电脑中能用的端口中的第一个
	 * @param b        波特率(baudrate)，如 9600
	 * @param d        数据位（datebits），如 SerialPort.DATABITS_8 = 8
	 * @param s        停止位（stopbits），如 SerialPort.STOPBITS_1 = 1
	 * @param p        校验位 (parity)，如 SerialPort.PARITY_NONE = 0
	 * @return 打开的串口对象，打开失败时，返回 null
	 */
	private SerialPort openComPort(CommPortIdentifier portIdentifier) {
		CommPort commPort = null;
		try {
			logger.info("开始打开串口：portName=" + portIdentifier.getName() + ",baudrate=" + baudrate + ",datebits="
					+ datebits + ",stopbits=" + stopbits + ",parity=" + parity);
			// 通过端口名称识别指定 COM 端口

			/**
			 * open(String TheOwner, int i)：打开端口 TheOwner 自定义一个端口名称，随便自定义即可
			 * i：打开的端口的超时时间，单位毫秒，超时则抛出异常：PortInUseException if in use.
			 * 如果此时串口已经被占用，则抛出异常：gnu.io.PortInUseException: Unknown Application
			 */
			commPort = portIdentifier.open(portIdentifier.getName(), 5000);
			/**
			 * 判断端口是不是串口 public abstract class SerialPort extends CommPort
			 */
			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
				/**
				 * 设置串口参数：setSerialPortParams( int b, int d, int s, int p ) b：波特率（baudrate）
				 * d：数据位（datebits），SerialPort 支持 5,6,7,8 s：停止位（stopbits），SerialPort 支持 1,2,3
				 * p：校验位 (parity)，SerialPort 支持 0,1,2,3,4
				 * 如果参数设置错误，则抛出异常：gnu.io.UnsupportedCommOperationException: Invalid Parameter
				 * 此时必须关闭串口，否则下次 portIdentifier.open 时会打不开串口，因为已经被占用
				 */
				serialPort.setSerialPortParams(baudrate, datebits, stopbits, parity);
				logger.info("打开串口 " + commPort.getName() + " 成功...");
				return serialPort;
			} else {
				logger.error("当前端口 " + commPort.getName() + " 不是串口...");
			}
		} catch (PortInUseException e) {
			logger.warn("串口 " + portIdentifier.getName() + " 已经被占用，请先解除占用...");
			e.printStackTrace();
		} catch (UnsupportedCommOperationException e) {
			logger.warn("串口参数设置错误，关闭串口，数据位[5-8]、停止位[1-3]、验证位[0-4]...");
			e.printStackTrace();
			if (commPort != null) {// 此时必须关闭串口，否则下次 portIdentifier.open 时会打不开串口，因为已经被占用
				commPort.close();
			}
		}
		logger.error("打开串口 " + portIdentifier.getName() + " 失败...");
		return null;
	}

	public static void main(String[] args) {
		SerialService service = new SerialService();
		// 发送普通数据
		List<String> portLists = service.findPortName();
		for (String name : portLists) {
			service.sendToPort(name, "hello world!");
			service.closeComPort(name);
			// 发送16进制数据——实际应用中串口通信传输的数据，大都是 16 进制
			String hexStrCode = "455A432F5600";
			service.sendToPort(name, hexStrCode);
			service.closeComPort(name);
		}
	}
}