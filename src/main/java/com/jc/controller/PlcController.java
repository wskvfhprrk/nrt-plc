package com.jc.controller;

import com.jc.config.Result;
import com.jc.service.impl.PlcServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/plc")
public class PlcController {

    @Autowired
    private PlcServiceImpl plcServiceImpl;

    @GetMapping("/sendData")
    public Result sendData(String data) {
        plcServiceImpl.sendDataToPlc(data);
        return Result.success("数据已发送到PLC");
    }

    @GetMapping("/getSentData")
    public Result getSentData() {
        String sentData = plcServiceImpl.readSentData();
        return Result.success(sentData);
    }
} 