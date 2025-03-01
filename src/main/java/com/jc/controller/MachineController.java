package com.jc.controller;

import com.jc.config.Result;
import com.jc.entity.MachineSettings;
import com.jc.entity.MachineStatus;
import com.jc.service.MachineService; // 导入服务接口
import com.jc.service.impl.PlcServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("machines")
public class MachineController {

    @Autowired
    private MachineService machineService; // 注入服务
    @Autowired
    private PlcServiceImpl plcService;


    @PostMapping("/save")
    public Result saveSettings(@RequestBody MachineSettings settings) {
        try {
            machineService.saveSettings(settings); // 使用服务保存设置
            return Result.success("设置成功");
        } catch (Exception e) {
            return Result.error("保存设置失败: " + e.getMessage());
        }
    }

    @GetMapping("/get")
    public Result<MachineSettings> getSettings() {
        try {
            MachineSettings settings = machineService.getMachineSettings();
            if (settings == null) {
                return Result.error("获取设置失败：未找到设置信息");
            }
            return Result.success(settings);
        } catch (Exception e) {
            return Result.error("获取设置失败: " + e.getMessage());
        }
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
        try {
            // 获取默认设置并保存
            MachineSettings defaultSettings = new MachineSettings();
            // 设置默认值
            defaultSettings.setAutoClean(false);
            defaultSettings.setNightMode(false);
            defaultSettings.setOpenLockTime(500);  // 默认500毫秒
            defaultSettings.setSoupMaxTemperature(85);  // 默认85度
            defaultSettings.setSoupMinTemperature(65);  // 默认65度
            defaultSettings.setSoupQuantity(100);  // 默认100脉冲
            defaultSettings.setFanVentilationTime(300);  // 默认300秒
            defaultSettings.setElectricalBoxFanTemp(40);  // 默认40度
            defaultSettings.setElectricalBoxFanHumidity(80);  // 默认80%
            
            // 设置默认价格
            defaultSettings.setPrice1(10);
            defaultSettings.setPrice2(15);
            defaultSettings.setPrice3(20);
            defaultSettings.setPrice4(25);
            defaultSettings.setPrice5(30);
            
            // 设置默认配料重量
            defaultSettings.setIngredient1Weight(100);
            defaultSettings.setIngredient2Weight(100);
            defaultSettings.setIngredient3Weight(100);
            defaultSettings.setIngredient4Weight(100);
            defaultSettings.setIngredient5Weight(100);

            // 保存默认设置
            machineService.saveSettings(defaultSettings);
            
            return Result.success("重置成功");
        } catch (Exception e) {
            return Result.error("重置失败: " + e.getMessage());
        }
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
    public Result writeData() {
        try {
            return plcService.sendDataToPlc();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}