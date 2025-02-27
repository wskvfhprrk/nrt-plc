package com.jc.service.impl;

import com.jc.config.IpConfig;
import com.jc.config.Result;
import com.jc.entity.MachineStatus;
import com.jc.entity.InputPoint;
import com.jc.entity.OutputPoint;
import com.jc.entity.Alert;
import com.jc.netty.server.NettyServerHandler;
import com.jc.service.MachineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class MachineServiceImpl implements MachineService {
    // 常量定义
    private static final String REDIS_PLC_DATA_KEY = "plc:data";
    private static final String PLC_DATA_START = "00";
    private static final String PLC_DATA_END = "FF";
    private static final int MIN_WEIGHT = 50;
    private static final int MAX_WEIGHT = 200;
    private static final int MIN_RUN_TIME = 1;
    private static final int MAX_RUN_TIME = 10;
    private static final String STATUS_RUNNING = "running";
    private static final String STATUS_STANDBY = "standby";
    private static final String STATUS_STOPPED = "stopped";
    private static final String STATUS_NORMAL = "正常运行";
    private static final String STATUS_STANDBY_MODE = "待机模式";
    private static final String STATUS_STOP = "停止运行";
    private static final String STATUS_UNKNOWN = "未知的运行状态值";
    
    // 字段定义
    private MachineStatus currentSettings;
    private final List<Alert> alerts = new ArrayList<>();

    // 依赖注入
    @Autowired
    private Plc plc;
    @Autowired
    private NettyServerHandler nettyServerHandler;
    @Autowired
    private IpConfig ipConfig;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 构造函数：初始化默认设置和报警信息
     */
    public MachineServiceImpl() {
        initializeDefaultSettings();
        initializeDefaultAlerts();
    }

    /**
     * 初始化默认设置
     */
    private void initializeDefaultSettings() {
        currentSettings = new MachineStatus();
        currentSettings.setWeight(120);
        currentSettings.setSoupVolume("35");
        currentSettings.setRunTime(4);
        currentSettings.setAutoClean(true);
        currentSettings.setNightMode(false);
        currentSettings.setStatus(STATUS_RUNNING);
    }

    /**
     * 初始化默认报警信息
     */
    private void initializeDefaultAlerts() {
        alerts.add(new Alert(1, "2023-10-01 10:00", "一级", "温度过高", false));
        alerts.add(new Alert(2, "2023-10-01 10:05", "二级", "水位低", true));
    }

    // ==================== 设置管理相关方法 ====================

    @Override
    public void saveSettings(MachineStatus settings) {
        try {
            validateSettings(settings);
            this.currentSettings = settings;
        } catch (Exception e) {
            log.error("保存设置失败: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 验证设置参数的合法性
     */
    private void validateSettings(MachineStatus settings) {
        if (settings.getWeight() < MIN_WEIGHT || settings.getWeight() > MAX_WEIGHT) {
            throw new IllegalArgumentException("面条重量超出范围");
        }

        if (settings.getRunTime() < MIN_RUN_TIME || settings.getRunTime() > MAX_RUN_TIME) {
            throw new IllegalArgumentException("制作时间超出范围");
        }

        if (!isValidStatus(settings.getStatus())) {
            throw new IllegalArgumentException("无效的运行状态");
        }
    }

    /**
     * 验证运行状态是否有效
     */
    private boolean isValidStatus(String status) {
        return status != null && (
                status.equals(STATUS_RUNNING) ||
                status.equals(STATUS_STANDBY) ||
                status.equals(STATUS_STOPPED)
        );
    }

    @Override
    public MachineStatus getSettings() {
        return currentSettings != null ? currentSettings : getDefaultSettings();
    }

    /**
     * 获取默认设置
     */
    private MachineStatus getDefaultSettings() {
        MachineStatus defaultSettings = new MachineStatus();
        defaultSettings.setWeight(100);
        defaultSettings.setSoupVolume("30");
        defaultSettings.setRunTime(3);
        defaultSettings.setAutoClean(true);
        defaultSettings.setNightMode(false);
        defaultSettings.setStatus(STATUS_RUNNING);
        return defaultSettings;
    }

    // ==================== 机器状态相关方法 ====================

    @Override
    public Map<String, Object> getMachineStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // 从Redis获取PLC数据
        String plcData = getPlcDataFromRedis();
        if (plcData == null) {
            log.warn("未能从Redis获取PLC数据");
            return status;
        }
        
        // 解析PLC数据并更新当前设置
        parsePlcData(plcData);
        
        // 获取当前设置
        MachineStatus settings = getSettings();
        
        // 填充状态信息
        populateStatusMap(status, settings);
        
        return status;
    }
    
    /**
     * 从Redis获取PLC数据
     */
    private String getPlcDataFromRedis() {
        return redisTemplate.opsForValue().get(REDIS_PLC_DATA_KEY);
    }
    
    /**
     * 填充状态Map
     */
    private void populateStatusMap(Map<String, Object> status, MachineStatus settings) {
        // 基本状态信息
        status.put("machineStatus", settings.getStatus());
        status.put("temperature", settings.getCurrentTemperature());
        status.put("weight", settings.getWeight());
        status.put("runtime", settings.getRunTime());
        status.put("robotStatus", settings.getRobotStatus());
        status.put("currentProgram", settings.getCurrentProgram());
        status.put("electricalBoxTemp", settings.getElectricalBoxTemp());
        status.put("electricalBoxHumidity", settings.getElectricalBoxHumidity());
        status.put("electricalBoxStatus", settings.getElectricalBoxStatus());
        status.put("autoClean", settings.isAutoClean());
        status.put("nightMode", settings.isNightMode());
        
        // 获取并添加设备输入点、输出点和报警信息
        List<InputPoint> inputPoints = getInputPoints();
        List<OutputPoint> outputPoints = getOutputPoints();
        List<Alert> currentAlerts = getAlerts();
        
        // 记录日志
        log.debug("获取到输入点数量: {}", inputPoints.size());
        log.debug("获取到输出点数量: {}", outputPoints.size());
        log.debug("获取到报警数量: {}", currentAlerts.size());
        
        // 添加到状态Map
        status.put("inputPoints", inputPoints);
        status.put("outputPoints", outputPoints);
        status.put("alerts", currentAlerts);
    }

    // ==================== PLC数据解析相关方法 ====================

    /**
     * 解析PLC数据
     */
    private void parsePlcData(String cleanMessage) {
        String[] data = cleanMessage.split(" ");
        
        // 验证数据包格式
        if (!validatePlcDataFormat(data)) {
            return;
        }
        
        MachineStatus settings = new MachineStatus();
        
        try {
            parseTemperatureData(data, settings);
            parseElectricalBoxData(data, settings);
            parseWeightData(data, settings);
            parseRobotStatus(data, settings);
            parseProgramStatus(data, settings);
            parseRunningStatus(data, settings);
            parseElectricalBoxStatus(data, settings);
            parseCleanAndNightMode(data, settings);
            checkErrorCode(data);
            
            // 更新当前设置
            this.currentSettings = settings;
        } catch (Exception e) {
            log.error("解析PLC数据失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 验证PLC数据格式
     */
    private boolean validatePlcDataFormat(String[] data) {
        if (data.length < 2 || !data[0].equals(PLC_DATA_START) || !data[data.length - 1].equals(PLC_DATA_END)) {
            log.error("数据包格式错误: 开头必须为00，结尾必须为FF");
            return false;
        }
        return true;
    }
    
    /**
     * 解析温度相关数据
     */
    private void parseTemperatureData(String[] data, MachineStatus settings) {
        if (data.length <= 19) return;
        
        int soupTemp = parseSignedHexValue(data[18] + data[19]);
        settings.setCurrentTemperature(String.valueOf(soupTemp / 10.0));
        settings.setSoupVolume(String.valueOf(soupTemp));
    }
    
    /**
     * 解析电箱温湿度数据
     */
    private void parseElectricalBoxData(String[] data, MachineStatus settings) {
        if (data.length <= 23) return;
        
        String boxHumidity = String.valueOf(Integer.parseInt(data[20] + data[21], 16));
        int boxTemp = parseSignedHexValue(data[22] + data[23]);
        
        settings.setElectricalBoxHumidity(String.valueOf(boxTemp / 10.0));
        settings.setElectricalBoxTemp(boxHumidity);
    }
    
    /**
     * 解析重量数据
     */
    private void parseWeightData(String[] data, MachineStatus settings) {
        if (data.length <= 26) return;
        
        int weight = Integer.parseInt(data[26], 16);
        settings.setWeight(weight);
    }
    
    /**
     * 解析机器人状态
     */
    private void parseRobotStatus(String[] data, MachineStatus settings) {
        if (data.length <= 16) return;
        
        int robotStatusBinary = Integer.parseInt(data[16], 16);
        settings.setRobotStatus(getRobotStatusDescription(robotStatusBinary));
    }
    
    /**
     * 解析程序状态
     */
    private void parseProgramStatus(String[] data, MachineStatus settings) {
        if (data.length <= 33) return;
        
        int programNumber = Integer.parseInt(data[14], 16);
        settings.setCurrentProgram(getProgramDescription(programNumber));
        settings.setRunTime(Integer.parseInt(data[32] + data[33], 16));
    }
    
    /**
     * 解析运行状态
     */
    private void parseRunningStatus(String[] data, MachineStatus settings) {
        if (data.length <= 15) return;
        
        int runningStatus = Integer.parseInt(data[15], 16);
        switch (runningStatus) {
            case 1:
                settings.setStatus(STATUS_NORMAL);
                break;
            case 2:
                settings.setStatus(STATUS_STANDBY_MODE);
                break;
            case 0:
                settings.setStatus(STATUS_STOP);
                break;
            default:
                log.warn("未知的运行状态值: {}", runningStatus);
                settings.setStatus(STATUS_UNKNOWN);
        }
    }
    
    /**
     * 解析电箱状态
     */
    private void parseElectricalBoxStatus(String[] data, MachineStatus settings) {
        if (data.length <= 17) return;
        
        int electricalBoxStatus = Integer.parseInt(data[17], 16);
        settings.setElectricalBoxStatus(electricalBoxStatus);
    }
    
    /**
     * 解析清洗和夜间模式
     */
    private void parseCleanAndNightMode(String[] data, MachineStatus settings) {
        if (data.length <= 53) return;
        
        boolean autoClean = Integer.parseInt(data[52], 16) != 0;
        boolean nightMode = Integer.parseInt(data[53], 16) != 0;
        settings.setAutoClean(autoClean);
        settings.setNightMode(nightMode);
    }
    
    /**
     * 检查错误代码
     */
    private void checkErrorCode(String[] data) {
        if (data.length <= 50) return;
        
        int errorCode = Integer.parseInt(data[50], 16);
        if (errorCode != 0) {
            addErrorAlert(errorCode);
        }
    }
    
    /**
     * 添加错误报警
     */
    private void addErrorAlert(int errorCode) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Alert alert = new Alert(
            alerts.size() + 1,
            sdf.format(new Date()),
            "一级",
            "故障码: " + errorCode,
            false
        );
        alerts.add(alert);
    }
    
    /**
     * 解析有符号十六进制值
     */
    private int parseSignedHexValue(String hexValue) {
        int value = Integer.parseInt(hexValue, 16);
        if (value > 0x7FFF) {
            value = -(~value & 0xFFFF) - 1;
        }
        return value;
    }
    
    /**
     * 获取机器人状态描述
     */
    private String getRobotStatusDescription(int status) {
        switch (status) {
            case 0: return "未上电";
            case 1: return "未使能";
            case 2: return "空闲";
            case 3: return "程序运行状态";
            case 4: return "暂停状态";
            case 5: return "程序结束";
            case 6: return "故障";
            case 7: return "机器人运动中";
            default: return "机器人未执行命令";
        }
    }
    
    /**
     * 获取程序描述
     */
    private String getProgramDescription(int programNumber) {
        switch (programNumber) {
            case 1: return "粉丝仓1";
            case 2: return "粉丝仓2";
            case 3: return "粉丝仓3";
            case 4: return "粉丝仓4";
            case 5: return "粉丝仓5";
            case 6: return "粉丝仓6";
            case 7: return "放碗子加热";
            case 8: return "取碗";
            case 9: return "取汤";
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
                return "备用" + (programNumber - 9);
            default:
                return "未知程序";
        }
    }
    
    /**
     * 16进制字符串转二进制字符串
     */
    private String hexToBinary(String hex) {
        int decimal = Integer.parseInt(hex, 16);
        String binary = Integer.toBinaryString(decimal);
        // 补齐8位
        while (binary.length() < 8) {
            binary = "0" + binary;
        }
        return binary;
    }

    // ==================== 输入/输出点相关方法 ====================

    /**
     * 获取输入点列表
     */
    public List<InputPoint> getInputPoints() {
        List<InputPoint> inputPoints = new ArrayList<>();
        Map<String, String> inputPointMap = getInputPointMap();
        
        try {
            for (Map.Entry<String, String> entry : inputPointMap.entrySet()) {
                String pointId = entry.getKey();
                String name = entry.getValue();
                String status = getPlcStatus(pointId);
                
                if (status != null) {
                    InputPoint point = new InputPoint(name, pointId, status);
                    inputPoints.add(point);
                    log.debug("添加输入点: {}, 状态: {}", name, status);
                } else {
                    log.warn("无法获取输入点状态: {}", pointId);
                }
            }
        } catch (Exception e) {
            log.error("获取输入点列表失败: {}", e.getMessage(), e);
        }
        
        return inputPoints;
    }
    
    /**
     * 获取输入点映射表
     */
    private Map<String, String> getInputPointMap() {
        Map<String, String> map = new LinkedHashMap<>();
        
        // V1.x 系列
        map.put("V1.0", "粉丝到位传感器");
        map.put("V1.1", "粉丝气缸1");
        map.put("V1.2", "粉丝气缸2");
        map.put("V1.3", "粉丝气缸3");
        map.put("V1.4", "粉丝气缸4");
        map.put("V1.5", "粉丝气缸5");
        map.put("V1.6", "粉丝气缸6");
        map.put("V1.7", "出碗检测");
        
        // V2.x 系列
        map.put("V2.0", "是否有碗");
        map.put("V2.1", "碗报警");
        map.put("V2.2", "出碗电机");
        map.put("V2.3", "做汤机气缸");
        map.put("V2.4", "流量计");
        map.put("V2.5", "出餐口气缸");
        map.put("V2.6", "推碗气缸");
        map.put("V2.7", "门锁1");
        map.put("V2.8", "门锁2");
        
        // V3.x 系列
        map.put("V3.0", "门锁3");
        map.put("V3.1", "门锁4");
        map.put("V3.2", "门锁5");
        map.put("V3.3", "门锁6");
        map.put("V3.4", "门锁7");
        map.put("V3.6", "夹手气缸");
        map.put("V3.7", "旋转气缸");
        
        // V4.x 系列
        map.put("V4.0", "切网机数量");
        map.put("V4.1", "切肉机是否有肉");
        map.put("V4.2", "切肉机报警");
        map.put("V4.3", "汤桶报警");
        map.put("V4.4", "汤桶最低位");
        map.put("V4.5", "水桶报警");
        map.put("V4.6", "水桶最低位");
        map.put("V4.7", "废水桶满");
        
        // V5.x 系列
        map.put("V5.1", "称重气缸");
        map.put("V5.2", "气压开关");
        map.put("V5.3", "备用1");
        map.put("V5.4", "备用2");
        
        return map;
    }

    /**
     * 获取输出点列表
     */
    public List<OutputPoint> getOutputPoints() {
        List<OutputPoint> outputPoints = new ArrayList<>();
        Map<String, String> outputPointMap = getOutputPointMap();
        
        try {
            for (Map.Entry<String, String> entry : outputPointMap.entrySet()) {
                String pointId = entry.getKey();
                String name = entry.getValue();
                String status = getPlcStatus(pointId);
                
                if (status != null) {
                    OutputPoint point = new OutputPoint(name, pointId, status);
                    outputPoints.add(point);
                    log.debug("添加输出点: {}, 状态: {}", name, status);
                } else {
                    log.warn("无法获取输出点状态: {}", pointId);
                }
            }
        } catch (Exception e) {
            log.error("获取输出点列表失败: {}", e.getMessage(), e);
        }
        
        return outputPoints;
    }
    
    /**
     * 获取输出点映射表
     */
    private Map<String, String> getOutputPointMap() {
        Map<String, String> map = new LinkedHashMap<>();
        
        // V7.x 系列
        map.put("V7.0", "粉丝仓气缸1");
        map.put("V7.1", "粉丝仓气缸2");
        map.put("V7.2", "粉丝仓气缸3");
        map.put("V7.3", "粉丝仓气缸4");
        map.put("V7.4", "粉丝仓气缸5");
        map.put("V7.5", "粉丝仓气缸6");
        map.put("V7.6", "做汤机气缸");
        map.put("V7.7", "出餐口气缸");
        
        // V8.x 系列
        map.put("V8.0", "推碗气缸");
        map.put("V8.1", "夹手气缸");
        map.put("V8.2", "旋转气缸");
        map.put("V8.3", "切网机气缸");
        map.put("V8.4", "切肉机气缸");
        map.put("V8.5", "汤桶加热蒸汽阀");
        map.put("V8.6", "出汤电磁阀");
        map.put("V8.7", "消毒蒸汽阀");
        
        // V9.x 系列
        map.put("V9.0", "备用3");
        map.put("V9.1", "备用4");
        map.put("V9.2", "备用5");
        map.put("V9.3", "备用6");
        map.put("V9.4", "备用7");
        map.put("V9.5", "备用8");
        map.put("V9.6", "备用9");
        map.put("V9.7", "备用10");
        
        // V10.x 系列
        map.put("V10.0", "门锁1");
        map.put("V10.1", "门锁2");
        map.put("V10.2", "门锁3");
        map.put("V10.3", "门锁4");
        map.put("V10.4", "门锁5");
        map.put("V10.5", "门锁6");
        map.put("V10.6", "门锁7");
        map.put("V10.7", "备用11");
        
        return map;
    }

    /**
     * 获取PLC输出状态
     */
    private String getPlcStatus(String pointId) {
        try {
            String status = plc.getOutputStatus(pointId);
            if (status == null || status.isEmpty()) {
                log.warn("PLC点位 {} 返回空状态", pointId);
                return "未知";
            }
            return status;
        } catch (Exception e) {
            log.error("获取PLC点位 {} 状态失败: {}", pointId, e.getMessage());
            return "错误";
        }
    }

    // ==================== 报警相关方法 ====================

    /**
     * 获取报警信息列表
     */
    public List<Alert> getAlerts() {
        try {
            if (alerts.isEmpty()) {
                log.debug("当前无报警信息");
            } else {
                log.debug("当前报警数量: {}", alerts.size());
                for (Alert alert : alerts) {
                    log.debug("报警信息: ID={}, 内容={}, 是否已解决={}", 
                        alert.getId(), alert.getMessage(), alert.isResolved());
                }
            }
            return new ArrayList<>(alerts);
        } catch (Exception e) {
            log.error("获取报警信息失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public Result resetAlert(int id) {
        try {
            for (Alert alert : alerts) {
                if (alert.getId() != null && alert.getId() == id) {
                    alert.setResolved(true);
                    log.info("已重置报警ID: {}", id);
                    break;
                }
            }
            return Result.success();
        } catch (Exception e) {
            log.error("重置报警失败: {}", e.getMessage());
            return Result.error("重置报警失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result clearAllAlerts() {
        try {
            // 清除内存中的报警记录
            alerts.clear();
            log.info("已清除所有报警信息");
            return Result.success();
        } catch (Exception e) {
            log.error("清除报警信息失败", e);
            return Result.error(e.getMessage());
        }
    }

    // ==================== PLC通信相关方法 ====================

    @Override
    public void sendDataToPLC(String data) {
        if (!data.startsWith(PLC_DATA_START) || !data.endsWith(PLC_DATA_END)) {
            log.error("数据包格式不正确: {}", data);
            throw new IllegalArgumentException("数据包格式不正确");
        }
        
        try {
            nettyServerHandler.sendMessageToClient(ipConfig.getPlc(), data, true);
            log.info("已发送数据到PLC: {}", data);
        } catch (Exception e) {
            log.error("发送数据到PLC失败: {}", e.getMessage());
            throw new RuntimeException("发送数据到PLC失败: " + e.getMessage());
        }
    }
}