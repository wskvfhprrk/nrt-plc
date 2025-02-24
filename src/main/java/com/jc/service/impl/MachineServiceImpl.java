package com.jc.service.impl;

import com.jc.entity.MachineStatus;
import com.jc.entity.InputPoint;
import com.jc.entity.OutputPoint;
import com.jc.entity.Alert;
import com.jc.service.MachineService;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.List;
import java.util.ArrayList;

@Service
public class MachineServiceImpl implements MachineService {

    private MachineStatus currentSettings; // 存储当前设置
    private List<Alert> alerts = new ArrayList<>(); // 存储报警信息

    // 初始化默认设置
    public MachineServiceImpl() {
        // 创建默认的机器设置
        currentSettings = new MachineStatus();
        currentSettings.setMinTemp(75);
        currentSettings.setMaxTemp(90);
        currentSettings.setNoodleWeight(120);
        currentSettings.setSoupVolume(350);
        currentSettings.setCleaningInterval(45);
        currentSettings.setCookingTime(4);
        currentSettings.setAutoClean(true);
        currentSettings.setNightMode(false);
        currentSettings.setStatus("running");
        
        // 初始化报警信息
        alerts.add(new Alert("2023-10-01 10:00", "一级", "温度过高", false));
        alerts.add(new Alert("2023-10-01 10:05", "二级", "水位低", true));
    }

    @Override
    public void saveSettings(MachineStatus settings) {
        validateSettings(settings); // 调用新的验证方法
        // 保存设置
        this.currentSettings = settings;
    }

    // 新增验证设置的方法
    private void validateSettings(MachineStatus settings) {
        // 验证温度范围
        if (settings.getMinTemp() < 60 || settings.getMaxTemp() > 100) {
            throw new IllegalArgumentException("温度设置超出范围");
        }
        
        // 验证最低温度小于最高温度
        if (settings.getMinTemp() >= settings.getMaxTemp()) {
            throw new IllegalArgumentException("最低温度必须小于最高温度");
        }

        // 验证面条重量范围
        if (settings.getNoodleWeight() < 50 || settings.getNoodleWeight() > 200) {
            throw new IllegalArgumentException("面条重量超出范围");
        }

        // 验证汤量范围
        if (settings.getSoupVolume() < 200 || settings.getSoupVolume() > 500) {
            throw new IllegalArgumentException("汤量超出范围");
        }

        // 验证清洗间隔范围
        if (settings.getCleaningInterval() < 30 || settings.getCleaningInterval() > 240) {
            throw new IllegalArgumentException("清洗间隔时间超出范围");
        }

        // 验证制作时间范围
        if (settings.getCookingTime() < 1 || settings.getCookingTime() > 10) {
            throw new IllegalArgumentException("制作时间超出范围");
        }

        // 验证运行状态是否有效
        if (!isValidStatus(settings.getStatus())) {
            throw new IllegalArgumentException("无效的运行状态");
        }
    }

    @Override
    public MachineStatus getSettings() {
        // 如果没有当前设置，返回默认设置
        if (currentSettings == null) {
            return getDefaultSettings();
        }
        return currentSettings;
    }

    // 验证运行状态是否有效
    private boolean isValidStatus(String status) {
        return status != null && (
            status.equals("running") ||
            status.equals("standby") ||
            status.equals("stopped")
        );
    }

    // 获取默认设置
    private MachineStatus getDefaultSettings() {
        MachineStatus defaultSettings = new MachineStatus();
        defaultSettings.setMinTemp(80);
        defaultSettings.setMaxTemp(95);
        defaultSettings.setNoodleWeight(100);
        defaultSettings.setSoupVolume(300);
        defaultSettings.setCleaningInterval(60);
        defaultSettings.setCookingTime(3);
        defaultSettings.setAutoClean(true);
        defaultSettings.setNightMode(false);
        defaultSettings.setStatus("running");
        return defaultSettings;
    }

    @Override
    public Map<String, Object> getMachineStatus() {
        Map<String, Object> status = new HashMap<>();
        MachineStatus settings = new MachineStatus(); // 获取当前设置

        // 将当前设置的相关信息添加到状态中
        status.put("machineStatus", settings.getStatus());
        status.put("temperature", settings.getCurrentTemperature()); // 使用当前温度
        status.put("noodleWeight", settings.getNoodleWeight());
        status.put("cleaningInterval", settings.getCleaningInterval());
        status.put("robotStatus", settings.getRobotStatus()); // 使用机器人状态
        status.put("currentProgram", settings.getCurrentProgram()); // 使用当前程序
        status.put("electricalBoxTemp", settings.getElectricalBoxTemp()); // 使用电箱温度
        status.put("electricalBoxHumidity", settings.getElectricalBoxHumidity()); // 使用电箱湿度

        // 添加设备输入点、输出点和报警信息
        status.put("inputPoints", getInputPoints());
        status.put("outputPoints", getOutputPoints());
        status.put("alerts", getAlerts());

        return status;
    }
    
    public List<InputPoint> getInputPoints() {
        List<InputPoint> inputPoints = new ArrayList<>();
        // 根据 plc_points_cursor 的实际状态更新输入点
        inputPoints.add(new InputPoint("粉丝到位传感器", "V1.0", "打开"));
        inputPoints.add(new InputPoint("粉丝气缸1", "V1.1", "关闭"));
        inputPoints.add(new InputPoint("粉丝气缸2", "V1.2", "打开"));
        inputPoints.add(new InputPoint("粉丝气缸3", "V1.3", "关闭"));
        inputPoints.add(new InputPoint("粉丝气缸4", "V1.4", "打开"));
        inputPoints.add(new InputPoint("粉丝气缸5", "V1.5", "关闭"));
        inputPoints.add(new InputPoint("粉丝气缸6", "V1.6", "打开"));
        inputPoints.add(new InputPoint("出碗检测", "V1.7", "关闭"));
        inputPoints.add(new InputPoint("是否有碗", "V2.0", "打开"));
        inputPoints.add(new InputPoint("碗报警", "V2.1", "关闭"));
        inputPoints.add(new InputPoint("出碗电机", "V2.2", "打开"));
        inputPoints.add(new InputPoint("做汤机气缸", "V2.3", "关闭"));
        inputPoints.add(new InputPoint("流量计", "V2.4", "打开"));
        inputPoints.add(new InputPoint("出餐口气缸", "V2.5", "关闭"));
        inputPoints.add(new InputPoint("推碗气缸", "V2.6", "打开"));
        inputPoints.add(new InputPoint("门锁1", "V2.7", "关闭"));
        inputPoints.add(new InputPoint("门锁2", "V3.0", "打开"));
        inputPoints.add(new InputPoint("门锁3", "V3.1", "关闭"));
        inputPoints.add(new InputPoint("门锁4", "V3.2", "打开"));
        inputPoints.add(new InputPoint("门锁5", "V3.3", "关闭"));
        inputPoints.add(new InputPoint("门锁6", "V3.4", "打开"));
        inputPoints.add(new InputPoint("门锁7", "V3.5", "关闭"));
        inputPoints.add(new InputPoint("夹手气缸", "V3.6", "打开"));
        inputPoints.add(new InputPoint("旋转气缸", "V3.7", "关闭"));
        inputPoints.add(new InputPoint("切网机数量", "V4.0", "打开"));
        inputPoints.add(new InputPoint("切肉机是否有肉", "V4.1", "关闭"));
        inputPoints.add(new InputPoint("切肉机报警", "V4.2", "打开"));
        inputPoints.add(new InputPoint("汤桶报警", "V4.3", "关闭"));
        inputPoints.add(new InputPoint("汤桶最低位", "V4.4", "打开"));
        inputPoints.add(new InputPoint("水桶报警", "V4.5", "关闭"));
        inputPoints.add(new InputPoint("水桶最低位", "V4.6", "打开"));
        inputPoints.add(new InputPoint("废水桶满", "V4.7", "关闭"));
        inputPoints.add(new InputPoint("称重气缸", "V5.1", "打开"));
        inputPoints.add(new InputPoint("气压开关", "V5.2", "关闭"));
        inputPoints.add(new InputPoint("备用1", "V5.3", "打开"));
        inputPoints.add(new InputPoint("备用2", "V5.4", "关闭"));
        inputPoints.add(new InputPoint("备用3", "V5.5", "打开"));
        inputPoints.add(new InputPoint("备用4", "V5.6", "关闭"));
        inputPoints.add(new InputPoint("备用5", "V5.7", "打开"));
        inputPoints.add(new InputPoint("备用6", "V5.8", "关闭"));
        inputPoints.add(new InputPoint("备用7", "V6.0", "打开"));
        return inputPoints;
    }

    public List<OutputPoint> getOutputPoints() {
        List<OutputPoint> outputPoints = new ArrayList<>();
        // 根据 plc_points_cursor 的实际状态更新输出点
        outputPoints.add(new OutputPoint("粉丝仓气缸1", "V7.0", "关闭"));
        outputPoints.add(new OutputPoint("粉丝仓气缸2", "V7.1", "打开"));
        outputPoints.add(new OutputPoint("粉丝仓气缸3", "V7.2", "关闭"));
        outputPoints.add(new OutputPoint("粉丝仓气缸4", "V7.3", "打开"));
        outputPoints.add(new OutputPoint("粉丝仓气缸5", "V7.4", "关闭"));
        outputPoints.add(new OutputPoint("粉丝仓气缸6", "V7.5", "打开"));
        outputPoints.add(new OutputPoint("做汤机气缸", "V7.6", "关闭"));
        outputPoints.add(new OutputPoint("出餐口气缸", "V7.7", "打开"));
        outputPoints.add(new OutputPoint("推碗气缸", "V8.0", "关闭"));
        outputPoints.add(new OutputPoint("夹手气缸", "V8.1", "打开"));
        outputPoints.add(new OutputPoint("旋转气缸", "V8.2", "关闭"));
        outputPoints.add(new OutputPoint("切网机气缸", "V8.3", "打开"));
        outputPoints.add(new OutputPoint("切肉机气缸", "V8.4", "关闭"));
        outputPoints.add(new OutputPoint("汤桶加热蒸汽阀", "V8.5", "打开"));
        outputPoints.add(new OutputPoint("出汤电磁阀", "V8.6", "关闭"));
        outputPoints.add(new OutputPoint("消毒蒸汽阀", "V8.7", "打开"));
        outputPoints.add(new OutputPoint("备用3", "V9.0", "关闭"));
        outputPoints.add(new OutputPoint("备用4", "V9.1", "打开"));
        outputPoints.add(new OutputPoint("备用5", "V9.2", "关闭"));
        outputPoints.add(new OutputPoint("备用6", "V9.3", "打开"));
        outputPoints.add(new OutputPoint("备用7", "V9.4", "关闭"));
        outputPoints.add(new OutputPoint("备用8", "V9.5", "打开"));
        outputPoints.add(new OutputPoint("备用9", "V9.6", "关闭"));
        outputPoints.add(new OutputPoint("备用10", "V9.7", "打开"));
        outputPoints.add(new OutputPoint("门锁1", "V10.0", "关闭"));
        outputPoints.add(new OutputPoint("门锁2", "V10.1", "打开"));
        outputPoints.add(new OutputPoint("门锁3", "V10.2", "关闭"));
        outputPoints.add(new OutputPoint("门锁4", "V10.3", "打开"));
        outputPoints.add(new OutputPoint("门锁5", "V10.4", "关闭"));
        outputPoints.add(new OutputPoint("门锁6", "V10.5", "打开"));
        outputPoints.add(new OutputPoint("门锁7", "V10.6", "关闭"));
        outputPoints.add(new OutputPoint("备用4", "V10.7", "打开"));
        return outputPoints;
    }

    // 添加新的报警处理方法
    public void resetAlert(String alertTime) {
        for (Alert alert : alerts) {
            if (alert.getTime().equals(alertTime)) {
                alert.setResolved(true);
                break;
            }
        }
    }

    // 修改获取报警信息的方法
    public List<Alert> getAlerts() {
        return new ArrayList<>(alerts); // 返回副本保证数据安全
    }
} 