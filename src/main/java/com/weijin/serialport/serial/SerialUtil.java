package com.weijin.serialport.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * @author Wang Huan
 * @author 18501667737@163.com
 * @date 2021/1/19 12:28
 */
public class SerialUtil {
	private static SerialUtil serialTool = null;

	static {
		// 在该类被ClassLoader加载时就初始化一个SerialTool对象
		if (serialTool == null) {
			serialTool = new SerialUtil();
		}
	}

	// 私有化SerialTool类的构造方法，不允许其他类生成SerialTool对象
	private SerialUtil() {
	}

	/**
	 * 获取提供服务的SerialTool对象
	 *
	 * @return serialTool
	 */
	public static SerialUtil getSerialTool() {
		if (serialTool == null) {
			serialTool = new SerialUtil();
		}
		return serialTool;
	}

	/**
	 * 查找所有可用端口
	 *
	 * @return 可用端口名称列表
	 */
	public static final ArrayList<String> findPortName() {
		// 获得当前所有可用串口
		Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
		ArrayList<String> portNameList = new ArrayList<>();
		// 将可用串口名添加到List并返回该List
		while (portList.hasMoreElements()) {
			String portName = portList.nextElement().getName();
			portNameList.add(portName);
		}
		return portNameList;
	}
	

	 public static final ArrayList<CommPortIdentifier> findPort() {
	     //获得当前所有可用串口
	     Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
	     ArrayList<CommPortIdentifier> portNameList = new ArrayList<CommPortIdentifier>();
	     //将可用串口名添加到List并返回该List
	     while (portList.hasMoreElements()) {
	    	 CommPortIdentifier port = portList.nextElement();
	         portNameList.add(port);
	     }
	     return portNameList;
	 }

	/**
	 * 打开串口
	 *
	 * @param portName
	 *            端口名称
	 * @param baudrate
	 *            波特率
	 * @return 串口对象 // * @throws SerialPortParameterFailure 设置串口参数失败 //
	 *         * @throws NotASerialPort 端口指向设备不是串口类型 // * @throws NoSuchPort
	 *         没有该端口对应的串口设备 // * @throws PortInUse 端口已被占用
	 */
	public static final SerialPort openPort(String portName, int baudrate) {
		try {
			// 通过端口名识别端口
			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
			// 打开端口，并给端口名字和一个timeout（打开操作的超时时间）
			CommPort commPort = portIdentifier.open(portName, 2000);
			// 判断是不是串口
			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
				try {
					// 设置一下串口的波特率等参数
					serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
				} catch (UnsupportedCommOperationException e) {
					// throw new SerialPortParameterFailure();
				}
				// System.out.println("Open " + portName + " sucessfully !");
				return serialPort;
			} else {
				// 不是串口
				// throw new NotASerialPort();
				new Exception("");
			}
		} catch (NoSuchPortException | PortInUseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * 打开串口
	 *
	 * @param portName
	 *            端口名称
	 * @param baudrate
	 *            波特率
	 * @return 串口对象 // * @throws SerialPortParameterFailure 设置串口参数失败 //
	 *         * @throws NotASerialPort 端口指向设备不是串口类型 // * @throws NoSuchPort
	 *         没有该端口对应的串口设备 // * @throws PortInUse 端口已被占用
	 */
	public static final SerialPort openPort(CommPortIdentifier portIdentifier, int baudrate) {
		try {
			// 打开端口，并给端口名字和一个timeout（打开操作的超时时间）
			CommPort commPort = portIdentifier.open(portIdentifier.getName(), 2000);
			// 判断是不是串口
			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
				try {
					// 设置一下串口的波特率等参数
					serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
				} catch (UnsupportedCommOperationException e) {
					// throw new SerialPortParameterFailure();
				}
				// System.out.println("Open " + portName + " sucessfully !");
				return serialPort;
			} else {
				// 不是串口
				// throw new NotASerialPort();
				new Exception("");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 关闭串口 // * @param serialport 待关闭的串口对象
	 */
	public static void closePort(SerialPort serialPort) {
		if (serialPort != null) {
			serialPort.close();
			serialPort = null;
		}
	}

	/**
	 * 往串口发送数据
	 *
	 * @param serialPort
	 *            串口对象
	 * @param order
	 *            待发送数据 // * @throws SendDataToSerialPortFailure 向串口发送数据失败 //
	 *            * @throws SerialPortOutputStreamCloseFailure 关闭串口对象的输出流出错
	 */
	public static void sendToPort(SerialPort serialPort, byte[] order) {
		OutputStream out = null;
		try {
			out = serialPort.getOutputStream();
			out.write(order);
			out.flush();
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
	 * 往串口发送数据
	 *
	 * @param serialPort
	 *            串口对象
	 * @param order
	 *            待发送数据 // * @throws SendDataToSerialPortFailure 向串口发送数据失败 //
	 *            * @throws SerialPortOutputStreamCloseFailure 关闭串口对象的输出流出错
	 */
	public static void sendToPort(String portName, byte[] order) {
		SerialPort serialPort = openPort(portName, 115200);
		sendToPort(serialPort, order);
	}
	
	/**
	 * 从串口读取数据
	 *
	 * @param serialPort
	 *            当前已建立连接的SerialPort对象
	 * @return 读取到的数据 // * @throws ReadDataFromSerialPortFailure 从串口读取数据时出错 //
	 *         * @throws SerialPortInputStreamCloseFailure 关闭串口对象输入流出错
	 */
	public static byte[] readFromPort(SerialPort serialPort) {
		InputStream in = null;
		byte[] bytes = {};
		try {
			in = serialPort.getInputStream();
			byte[] readBuffer = new byte[1];
	             int bytesNum = in.read(readBuffer);
	            while (bytesNum > 0) {
	                bytes = concat(bytes, readBuffer);
	                bytesNum = in.read(readBuffer);
	            }
		} catch (IOException e) {
			// throw new ReadDataFromSerialPortFailure();
		} finally {
			try {
				if (in != null) {
					in.close();
					in = null;
				}
			} catch (IOException e) {
				// throw new SerialPortInputStreamCloseFailure();
			}
		}
		return bytes;
	}

	/**
	 * 添加监听器
	 *
	 * @param port
	 *            串口对象
	 * @param listener
	 *            串口监听器 // * @throws TooManyListeners 监听类对象过多
	 */
	public static void addListener(SerialPort port, SerialPortEventListener listener) {
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
			port.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
		} catch (Exception e) {
			// throw new TooManyListeners();
		}
	}
	
	/**
	 * 将两字节数组合并为同一个
	 * @param firstArray
	 * @param secondArray
	 * @return 返回合并后的字节数组
	 */
	public static byte[] concat(byte[] firstArray, byte[] secondArray) {
	    if (firstArray == null || secondArray == null) {
	        return null;
	    }
	    byte[] bytes = new byte[firstArray.length + secondArray.length];
	    System.arraycopy(firstArray, 0, bytes, 0, firstArray.length);
	    System.arraycopy(secondArray, 0, bytes, firstArray.length, secondArray.length);
	    return bytes;
	}
	
	/**
     * 从串口读取数据
     * @param serialPort 要读取的串口（不建议）
     * @return 读取的数据
     */
    private static byte[] readData(SerialPort serialPort) {
        InputStream is = null;
        byte[] bytes = null;
        try {
            is = serialPort.getInputStream();//获得串口的输入流
            int bufflenth = is.available();//获得数据长度
            while (bufflenth != 0) {
                bytes = new byte[bufflenth];//初始化byte数组
                is.read(bytes);
                bufflenth = is.available();
            }
        } catch (IOException e) {
            //logger.error("串口异常，停止服务。", e);
            System.exit(-1);
        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }
}