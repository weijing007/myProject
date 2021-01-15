package org.txtx.demo;

import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import org.apache.commons.codec.DecoderException;

import java.io.IOException;
import java.util.Scanner;
import java.util.TooManyListenersException;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws PortInUseException, UnsupportedCommOperationException,
            TooManyListenersException, IOException, DecoderException {
        RXTX rxtx = new RXTX();
        rxtx.start();
        SerialPortDataHandle serialPortDataHandle = new SerialPortDataHandle(rxtx);
        Thread thread = new Thread(serialPortDataHandle);
        thread.start();
        Scanner scanner = new Scanner(System.in);
        String data;
        while ((data = scanner.next()) != null) {
            rxtx.sendData(data);
        }
    }
}
