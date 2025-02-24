package com.jc.controller;

import com.jc.config.Result;
import com.jc.entity.MachineStatus;
import com.jc.service.MachineService; // 导入服务接口
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("machines")
public class MachineController {

    @Autowired
    private MachineService machineService; // 注入服务

    @PostMapping("/save")
    public Result saveSettings(@RequestBody MachineStatus settings) {
        machineService.saveSettings(settings); // 使用服务保存设置
        return Result.success("设置已保存");
    }

    @GetMapping("/get")
    public Result getSettings() {
        MachineStatus settings = machineService.getSettings(); // 使用服务获取设置
        return Result.success(settings);
    }

    // 新增方法以处理前端请求
    @GetMapping("/status") // 处理 /machines/status 请求
    public Result getMachineStatus() {
        Map<String, Object> status = machineService.getMachineStatus();
        return Result.success(status);
    }


    // 添加新的报警复位端点
    @PutMapping("/alerts/reset")
    public Result resetAlert(@RequestParam int alertId) {
        return Result.success();
    }

}