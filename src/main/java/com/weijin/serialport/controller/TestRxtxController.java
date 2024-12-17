package com.weijin.serialport.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.weijin.serialport.serial.SerialService;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/rxtx")
public class TestRxtxController {
	
	@Autowired
	private SerialService serialService;

	private static final Logger logger = LoggerFactory.getLogger(TestRxtxController.class);

	@ApiOperation(value = "获取串口信息", notes = "获取串口信息")
	@RequestMapping(value = "/SerialPorts", method = RequestMethod.GET)
	public String getSerialPorts() {
		List<String> datas = serialService.findPortName();
		return JSONObject.toJSONString(datas);
	}

	@ApiOperation(value = "给串口发送信息", notes = "给串口发送信息")
	@RequestMapping(value = "/SerialPorts/message", method = RequestMethod.POST)
	public String sendMessage(@RequestParam(value = "name",required = true) String name, @RequestBody(required = true) String data) {
		serialService.sendToPort(name, data);
		return "OK";
	}
}
