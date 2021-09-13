package com.weijin.serialport;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import gnu.io.CommPortIdentifier;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/rxtx")
public class TestRxtxController {
	
	private static final Logger logger = LoggerFactory.getLogger(TestRxtxController.class);

	@ApiOperation(value = "获取串口信息", notes = "获取串口信息")
	@RequestMapping(value = "/SerialPorts", method = RequestMethod.GET)
	public String getSerialPorts() {
		List<CommPortIdentifier> datas = SerialUtil.findPort();
		return JSONObject.toJSONString(datas);
	}

	@ApiOperation(value = "给串口发送信息", notes = "给串口发送信息")
	@RequestMapping(value = "/SerialPorts/message", method = RequestMethod.POST)
	public String sendMessage(@RequestBody(required = true) String data) {
		SerialUtil.sendToPort("", data.getBytes());
		return "OK";
	}
}
