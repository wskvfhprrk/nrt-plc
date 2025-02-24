package com.jc.entity;

import lombok.Data;

@Data
public class MachineStatus {
    private int minTemp;          // 最小温度
    private int maxTemp;          // 最大温度
    private int noodleWeight;      // 面条重量
    private int soupVolume;        // 汤的体积
    private int cleaningInterval;   // 清洗间隔
    private int cookingTime;       // 烹饪时间
    private boolean autoClean;     // 自动清洗
    private boolean nightMode;     // 夜间模式
    private String status;         // 机器状态
    private int currentTemperature; // 当前温度
    private String robotStatus;    // 机器人状态
    private String currentProgram;  // 当前程序
    private int electricalBoxTemp; // 电箱温度
    private int electricalBoxHumidity; // 电箱湿度

    // 构造函数，赋予初始值（模拟数值）
    public MachineStatus() {
        this.minTemp = 75;
        this.maxTemp = 90;
        this.noodleWeight = 120;
        this.soupVolume = 350;
        this.cleaningInterval = 45;
        this.cookingTime = 4;
        this.autoClean = true;
        this.nightMode = false;
        this.status = "正在运行";
        this.currentTemperature = 80; // 假设的初始温度
        this.robotStatus = "停止"; // 假设的初始机器人状态
        this.currentProgram = "停止"; // 假设的初始程序
        this.electricalBoxTemp = 25; // 假设的电箱温度
        this.electricalBoxHumidity = 50; // 假设的电箱湿度
    }
} 