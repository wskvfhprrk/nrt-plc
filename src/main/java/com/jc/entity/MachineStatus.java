package com.jc.entity;

import lombok.Data;

@Data
public class MachineStatus {
    private int weight;             // 菜的重量
    private String status;         // 机器状态
    private String soupVolume;        // 汤的温度
    private int runTime;       // 机器运行时间
    private boolean autoClean;     // 自动清洗
    private boolean nightMode;     // 夜间模式
    private String currentTemperature; // 当前温度
    private String robotStatus;    // 机器人状态
    private String robotMode;      // 机器人模式（自动/手动）
    private String robotEmergencyStop; // 机器人急停状态
    private String currentProgram;  // 当前程序
    private String electricalBoxTemp; // 电箱温度
    private String electricalBoxHumidity; // 电箱湿度
    private int electricalBoxStatus; // 电箱状态

    // 添加设置电箱状态的方法
    public void setElectricalBoxStatus(int status) {
        this.electricalBoxStatus = status;
    }

    // 构造函数，赋予初始值（模拟数值）
    public MachineStatus() {        
        this.weight = 120;
        this.soupVolume = "350.0";
        this.runTime = 4;
        this.autoClean = true;
        this.nightMode = false;
        this.status = "running";
        this.currentTemperature = "80"; // 假设的初始温度
        this.robotStatus = "stopped"; // 假设的初始机器人状态
        this.robotMode = "自动"; // 初始化机器人模式
        this.robotEmergencyStop = "正常工作"; // 初始化急停状态
        this.currentProgram = "stopped"; // 假设的初始程序
        this.electricalBoxTemp = "25"; // 假设的电箱温度
        this.electricalBoxHumidity = "50"; // 假设的电箱湿度
        this.electricalBoxStatus = 0; // 初始化电箱状态
    }
} 