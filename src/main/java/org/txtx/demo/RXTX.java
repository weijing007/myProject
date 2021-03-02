package org.txtx.demo;

import gnu.io.*;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author steel
 * datetime 2021/1/15 11:18
 */
public class RXTX {
    private static final Logger LOGGER = LoggerFactory.getLogger(RXTX.class);
    // 堵塞队列：用来存放串口发送到服务端的数据
    private static final BlockingQueue<String> MSG_QUEUE = new ArrayBlockingQueue<>(1024);
    // 串口输入流引用
    private static BufferedInputStream inputStream;
    // 串口输出流引用
    private static BufferedOutputStream outputStream;
    // 串口对象引用
    private static SerialPort serialPort;

    private volatile boolean ready;

    public String take() throws InterruptedException {
        return MSG_QUEUE.take();
    }

    public void sendData(String data) throws DecoderException, IOException {
        LOGGER.info("send hex data is [{}]", data);
        if (!this.writeReady()) {
            return;
        }
        outputStream.write(Hex.decodeHex(data));
        outputStream.flush();
    }

    private boolean writeReady() {
        if (Objects.isNull(outputStream)) {
            LOGGER.error("out channel is not connected, will try init.");
            this.reset();
        }
        return Objects.nonNull(outputStream);
    }

    public void start() throws PortInUseException, UnsupportedCommOperationException, TooManyListenersException,
            IOException {
        this.init();
        this.heart();
    }

    public void init() throws PortInUseException, IOException, TooManyListenersException,
            UnsupportedCommOperationException {
        // 通过串口通信管理类获得当前连接上的端口列表
        //（获取一个枚举对象，该CommPortIdentifier对象包含系统中每个端口的对象集[串口、并口]）
        // 有效连接上的端口的枚举
        Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            // 获取相应串口对象
            // 通讯端口管理，控制对通信端口的访问的中心类
            CommPortIdentifier commPortIdentifier = (CommPortIdentifier) portList.nextElement();
            /*
             *  判断端口类型是否为串口
             *  PORT_SERIAL = 1; 【串口】
             *  PORT_PARALLEL = 2; 【并口】
             *  PORT_I2C = 3; 【I2C】
             *  PORT_RS485 = 4; 【RS485】
             *  PORT_RAW = 5; 【RAW】
             */
            if (commPortIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                LOGGER.info("串口设备名称：" + commPortIdentifier.getName());
                // 判断模拟COM4串口存在，就打开该串口
                if (commPortIdentifier.getName().equals("COM3")) {
                    LOGGER.info("测试串口设备名称：" + commPortIdentifier.getName());

                    if (Objects.isNull(serialPort)) {
                        // 打开串口，设置名字为COM_4(自定义),延迟阻塞时等待3000毫秒（赋值给预设的串口引用）
                        serialPort = (SerialPort) commPortIdentifier.open("COM3", 3000);
                        LOGGER.info("COM3");
                    }

                    // 在串口引用不为空时进行下述操作
                    if (Objects.isNull(serialPort)) {
                        continue;
                    }
                    // 1. 设置串口的输入输出流引用
                    inputStream = new BufferedInputStream(serialPort.getInputStream());
                    outputStream = new BufferedOutputStream(serialPort.getOutputStream());
                    // 2. 设置串口监听器
                    serialPort.addEventListener(new RXTXSerialPortEventListener());
                    // 设置监听器在有数据时通知生效
                    serialPort.notifyOnDataAvailable(true);
                    // 3. 设置串口相关读写参数
                    // 比特率、数据位、停止位、校验位
                    serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                            SerialPort.PARITY_EVEN);
                    ready = true;
                }
            }
        }
    }

    public void reset() {
        ready = false;
        serialPort.removeEventListener();
        serialPort.close();
        serialPort = null;
    }

    public void heart() {
        new Timer("rxtx-init-heart", true).schedule(new TimerTask() {
            @Override
            public void run() {
                if (ready) {
                    LOGGER.info("serial port heart beat normal");
                    return;
                }
                LOGGER.info("serial port heart beat error");
                try {
                    init();
                } catch (Throwable e) {
                    LOGGER.error("reset init rxtx error", e);
                }
            }
        }, 1000L, 30000L);
    }

    private class RXTXSerialPortEventListener implements SerialPortEventListener {

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
                    byte[] readBuffer = new byte[1024];
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    try {
                        // 存储待接收读取字节数大小
                        int numBytes;
                        while ((numBytes = inputStream.read(readBuffer)) != -1) {
                            // 数据接收缓冲容器清空初始化
                            byteArrayOutputStream.write(readBuffer, 0, numBytes);
                        }
                        MSG_QUEUE.add(" 收到的串口发送数据为：" + Hex.encodeHexString(byteArrayOutputStream.toByteArray()));
                    } catch (IOException e) {
                        LOGGER.error("IO异常", e);
                        reset();
                    }
                    break;
                default:
                    break;
            }
        }
    }

}
