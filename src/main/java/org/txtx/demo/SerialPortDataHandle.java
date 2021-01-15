package org.txtx.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author steel
 * datetime 2021/1/15 11:18
 */
public class SerialPortDataHandle implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SerialPortDataHandle.class);
    private final RXTX rxtx;
    // 线程控制标识
    private volatile boolean flag = true;

    public SerialPortDataHandle(RXTX rxtx) {
        this.rxtx = rxtx;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("串口线程已运行");
            while (flag) {
                // 如果堵塞队列中存在数据就将其输出
                // take() 取走BlockingQueue里排在首位的对象
                // 若BlockingQueue为空，阻断进入等待状态直到Blocking有新的对象被加入为止
                LOGGER.info(rxtx.take());
            }
        } catch (InterruptedException e) {
            LOGGER.error("线程执行异常", e);
        }
    }

    public void stop() {
        this.flag = false;
    }


}
