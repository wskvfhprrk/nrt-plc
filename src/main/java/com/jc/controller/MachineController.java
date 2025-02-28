package com.jc.controller;

import com.jc.config.Result;
import com.jc.entity.MachineStatus;
import com.jc.service.MachineService; // 导入服务接口
import com.jc.service.impl.Reset; // 导入 Reset 类
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
        try {
            machineService.saveSettings(settings); // 使用服务保存设置
            return Result.success("设置已保存");
        } catch (Exception e) {
            return Result.error("保存设置失败: " + e.getMessage());
        }
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
        return machineService.resetAlert(alertId);
    }

    // 添加重置接口
    @PostMapping("/reset")
    public Result reset() {
        return getSettings();
    }

    @DeleteMapping("/alerts/clear")
    public Result clearAllAlerts() {
        try {
            return machineService.clearAllAlerts();
        } catch (Exception e) {
            return Result.error("清除报警信息失败: " + e.getMessage());
        }
    }

    // 处理上位机写入数据请求
    @PostMapping("/writeData")
    public Result writeData(@RequestBody String data) {
        try {
            return machineService.sendDataToPLC(data);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}