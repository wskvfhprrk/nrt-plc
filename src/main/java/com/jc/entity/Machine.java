package com.jc.entity;

import lombok.Data;

@Data
public class Machine {
    private int minTemp;
    private int maxTemp;
    private int noodleWeight;
    private int soupVolume;
    private int cleaningInterval;
    private int cookingTime;
    private boolean autoClean;
    private boolean nightMode;
    private String status;
} 