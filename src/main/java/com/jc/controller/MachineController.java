package com.jc.controller;

import com.jc.config.Result;
import com.jc.entity.MachineStatus;
import com.jc.service.MachineService; // 导入服务接口
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/api/machine/status")
    public Map<String, Object> getMachineStatusFromApi() {
        return machineService.getMachineStatus();
    }

    // 添加新的报警复位端点
    @PostMapping("/alerts/reset")
    public Result resetAlert(@RequestBody Map<String, String> request) {
        String alertTime = request.get("time");
        machineService.resetAlert(alertTime);
        return Result.success("报警已复位");
    }
} 