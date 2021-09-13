package com.weijin.serialport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class JavaSerialPortRxtxDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(JavaSerialPortRxtxDemoApplication.class, args);
		SystemInitConfig.initListener();
	}
}