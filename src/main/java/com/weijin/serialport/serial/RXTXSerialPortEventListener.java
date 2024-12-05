package com.weijin.serialport.serial;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class RXTXSerialPortEventListener implements SerialPortEventListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(RXTXSerialPortEventListener.class);
    // 堵塞队列：用来存放串口发送到服务端的数据
    private static final BlockingQueue<String> MSG_QUEUE = new ArrayBlockingQueue<>(1024);
  
    // 串口对象引用
    private SerialPort serialPort;

    public RXTXSerialPortEventListener(SerialPort serialPort){
    	this.serialPort = serialPort;
    }
    
    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        switch (serialPortEvent.getEventType()) {
            /*
             *  SerialPortEvent.BI:/*Break interrupt,通讯中断
             *  SerialPortEvent.OE:/*Overrun error，溢位错误
             *  SerialPortEvent.FE:/*Framing error，传帧错误
             *  SerialPortEvent.PE:/*Parity error，校验错误
             *  SerialPortEvent.CD:/*Carrier detect，载波检测
             *  SerialPortEvent.CTS:/*Clear to send，清除发送
             *  SerialPortEvent.DSR:/*Data set ready，数据设备就绪
             *  SerialPortEvent.RI:/*Ring indicator，响铃指示
             *  SerialPortEvent.OUTPUT_BUFFER_EMPTY:/*Output buffer is empty，输出缓冲区清空
             */
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                break;
            case SerialPortEvent.BI:
                LOGGER.error("break interrupt[通讯中断].");
                break;
            // 当有可用数据时读取数据
            case SerialPortEvent.DATA_AVAILABLE:
                // 数据接收缓冲容器
                try {
                	byte[] readBuffer = SerialUtil.readFromPort(serialPort);
                    MSG_QUEUE.add(" 收到的串口发送数据为：" + Hex.encodeHexString(readBuffer));
                } catch (Exception e) {
                    LOGGER.error("IO异常", e);
                    reset();
                }
                break;
            default:
                break;
        }
    }
    
    public void reset() {
        serialPort.removeEventListener();
        serialPort.close();
        serialPort = null;
    }

    public String take() throws InterruptedException {
        return MSG_QUEUE.take();
    }
}