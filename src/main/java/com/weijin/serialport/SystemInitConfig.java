package com.weijin.serialport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.weijin.serialport.serial.SerialService;

@Service
public class SystemInitConfig implements CommandLineRunner, EnvironmentAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemInitConfig.class);

	@Autowired
	private SerialService serialService;

	@Override
	public void setEnvironment(Environment environment) {
		// TODO Auto-generated method stub
	}

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		serialService.start();
	}
}
