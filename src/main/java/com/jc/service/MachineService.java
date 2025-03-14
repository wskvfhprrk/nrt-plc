package com.jc.service;

import com.jc.config.Result;
import com.jc.entity.MachineSettings;
import com.jc.entity.MachineStatus;
import com.jc.entity.Order;
import com.jc.entity.PlcOrder;
import java.util.Map;

public interface MachineService {
    Result saveSettings(MachineSettings settings);
    Map<String, Object> getMachineStatus();
    Result resetAlert(int id);
    Result clearAllAlerts();
    MachineSettings getMachineSettings();
    MachineSettings reset();
    Result addNewOrder(Order order);
    Result sendPlcOrder(PlcOrder plcOrder);
    Result resetAlarmViaPlc();
}