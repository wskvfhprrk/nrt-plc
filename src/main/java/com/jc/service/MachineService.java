package com.jc.service;

import com.jc.config.Result;
import com.jc.entity.MachineSettings;
import com.jc.entity.MachineStatus;
import java.util.Map;

public interface MachineService {
    Result saveSettings(MachineSettings settings);
    Map<String, Object> getMachineStatus();
    Result resetAlert(int id);
    Result clearAllAlerts();
    MachineStatus getMachineSettings();
}