package com.weijin.serialport.serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author steel
 * datetime 2021/1/15 11:18
 */
public class SerialPortDataHandle implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(SerialPortDataHandle.class);

    private final RXTXSerialPortEventListener listener;
    // 线程控制标识
    private volatile boolean flag = true;

    public SerialPortDataHandle(RXTXSerialPortEventListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("串口线程已运行");
            while (flag) {
                // 如果堵塞队列中存在数据就将其输出
                // take() 取走BlockingQueue里排在首位的对象
                // 若BlockingQueue为空，阻断进入等待状态直到Blocking有新的对象被加入为止
            	byte[] message = take();
				LOGGER.info("处理信息：" + bytesToHexString(message));
            }
        } catch (InterruptedException e) {
            LOGGER.error("线程执行异常", e);
        }
    }

    public void stop() {
        this.flag = false;
    }

	public byte[] take() throws InterruptedException {
		return listener.MSG_QUEUE.take();
	}

	/**
	 * 16进制字符串转十进制字节数组 这是常用的方法，如某些硬件的通信指令就是提供的16进制字符串，发送时需要转为字节数组再进行发送
	 *
	 * @param strSource 16进制字符串，如 "455A432F5600"，每两位对应字节数组中的一个10进制元素
	 *                  默认会去除参数字符串中的空格，所以参数 "45 5A 43 2F 56 00" 也是可以的
	 * @return 十进制字节数组, 如 [69, 90, 67, 47, 86, 0]
	 */
	private static byte[] hexString2Bytes(String strSource) {
		if (strSource == null || "".equals(strSource.trim())) {
			System.out.println("hexString2Bytes 参数为空，放弃转换.");
			return null;
		}
		strSource = strSource.replace(" ", "");
		int l = strSource.length() / 2;
		byte[] ret = new byte[l];
		for (int i = 0; i < l; i++) {
			ret[i] = Integer.valueOf(strSource.substring(i * 2, i * 2 + 2), 16).byteValue();
		}
		return ret;
	}

	/**
	 * Hex字符串转byte
	 *
	 * @param inHex 待转换的Hex字符串
	 * @return 转换后的byte
	 */
	public static byte hexToByte(String inHex) {
		return (byte) Integer.parseInt(inHex, 16);
	}

	/**
	 * hex字符串转byte数组
	 *
	 * @param inHex 待转换的Hex字符串
	 * @return 转换后的byte数组结果
	 */
	public static byte[] hexToByteArray(String inHex) {
		int hexlen = inHex.length();
		byte[] result;
		if (hexlen % 2 == 1) {
			// 奇数
			hexlen++;
			result = new byte[(hexlen / 2)];
			inHex = "0" + inHex;
		} else {
			// 偶数
			result = new byte[(hexlen / 2)];
		}
		int j = 0;
		for (int i = 0; i < hexlen; i += 2) {
			result[j] = hexToByte(inHex.substring(i, i + 2));
			j++;
		}
		return result;
	}

	/**
	 * 数组转换成十六进制字符串
	 *
	 * @param bArray
	 * @return HexString
	 */
	public static final String bytesToHexString(byte[] bArray) {
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2) {
				sb.append(0);
			}
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}

}
