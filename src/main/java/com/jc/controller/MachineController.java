package com.jc.controller;

import com.jc.config.Result;
import com.jc.entity.MachineSettings;
import com.jc.entity.Order;
import com.jc.entity.PlcOrder;
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
    
    // 添加通过PLC复位报警的端点
    @PostMapping("/alerts/reset-via-plc")
    public Result resetAlarmViaPlc() {
        return machineService.resetAlarmViaPlc();
    }

//    // 添加处理新订单的端点
//    @PostMapping("/order")
//    public Result processOrder(@RequestBody Order order) {
//        return machineService.addNewOrder(order);
//    }
    
    // 添加处理PLC订单的端点
    @PostMapping("/plc-order")
    public Result processPlcOrder(@RequestBody PlcOrder plcOrder) {
        return machineService.sendPlcOrder(plcOrder);
    }

    // 添加重置接口
    @PostMapping("/reset")
    public Result reset() {
        try {
            MachineSettings settings = machineService.reset();
            if (settings == null) {
                return Result.error("获取设置失败：未找到设置信息");
            }
            return Result.success(settings);
        } catch (Exception e) {
            return Result.error("获取设置失败: " + e.getMessage());
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