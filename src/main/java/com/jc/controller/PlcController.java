package com.jc.controller;

import com.jc.config.Result;
import com.jc.service.impl.Plc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/plc")
public class PlcController {

    @Autowired
    private Plc plc;

    @GetMapping("/sendData")
    public Result sendData(String data) {
        plc.sendDataToPlc(data);
        return Result.success("数据已发送到PLC");
    }

    @GetMapping("/getSentData")
    public Result getSentData() {
        String sentData = plc.readSentData();
        return Result.success(sentData);
    }
} 