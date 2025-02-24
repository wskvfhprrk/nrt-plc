package com.jc.service;

import com.jc.entity.MachineStatus;
import java.util.Map;

public interface MachineService {
    void saveSettings(MachineStatus settings);
    MachineStatus getSettings();
    Map<String, Object> getMachineStatus();
    void resetAlert(String alertTime);
} 