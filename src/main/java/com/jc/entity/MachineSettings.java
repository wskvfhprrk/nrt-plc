package com.jc.entity;

import lombok.Data;

@Data
public class MachineSettings {
    // 基本设置
    private Boolean autoClean;     // 自动清洗
    private Boolean nightMode;     // 夜间模式
    
    // 开门锁设置
    private Integer openLockTime;  // 开门锁通电时间(毫秒)
    
    // 汤温度设置
    private Integer soupMaxTemperature;  // 出汤温度
    private Integer soupMinTemperature;  // 汤保温温度
    private Integer soupQuantity;  // 汤数量(脉冲)
    
    // 风扇设置
    private Integer fanVentilationTime;  // 排油烟风扇通风时间(秒)
    private Integer electricalBoxFanTemp;  // 电柜风扇通风温度值
    private Integer electricalBoxFanHumidity;  // 电柜风扇通风湿度值
    
    // 价格设置
    private Integer price1;  // 价格1
    private Integer price2;  // 价格2
    private Integer price3;  // 价格3
    private Integer price4;  // 价格4
    private Integer price5;  // 价格5
    
    // 配料重量设置
    private Integer ingredient1Weight;  // 配料1重量
    private Integer ingredient2Weight;  // 配料2重量
    private Integer ingredient3Weight;  // 配料3重量
    private Integer ingredient4Weight;  // 配料4重量
    private Integer ingredient5Weight;  // 配料5重量
    
    // 机器人设置
    private Integer beefSoupTime;      // 汤牛肉时间设置(秒)
    private Boolean robotAutoMode;     // 机器人模式(true:自动,false:手动)
    private Boolean robotEmergencyStop; // 机器人急停开关(true:开启,false:关闭)

}