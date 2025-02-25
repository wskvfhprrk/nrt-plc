package com.jc.service.impl;

import com.jc.config.Result;
import com.jc.entity.MachineStatus;
import com.jc.service.MachineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class Reset {

    @Autowired
    private MachineService machineService; // 注入 MachineService

    public synchronized Result start() {
        // 获取当前设置
        MachineStatus currentSettings = machineService.getSettings();
        return Result.success(currentSettings); // 返回当前设置
    }
}

