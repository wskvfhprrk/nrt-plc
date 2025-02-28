package com.jc.service;

import com.jc.config.Result;
import com.jc.entity.MachineStatus;
import java.util.Map;

public interface MachineService {
    void saveSettings(MachineStatus settings);
    MachineStatus getSettings();
    Map<String, Object> getMachineStatus();
    Result resetAlert(int id);
    Result clearAllAlerts();
    Result sendDataToPLC(String data);
}