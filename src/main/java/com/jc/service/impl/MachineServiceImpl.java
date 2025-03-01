package com.jc.service.impl;

import com.jc.config.Result;
import com.jc.entity.MachineStatus;
import com.jc.entity.InputPoint;
import com.jc.entity.OutputPoint;
import com.jc.entity.Alert;
import com.jc.entity.MachineSettings;
import com.jc.service.MachineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson.JSON;

@Service
@Slf4j
public class MachineServiceImpl implements MachineService {
    // 常量定义
    private static final String REDIS_PLC_DATA_KEY = "plc:data";
    private static final String REDIS_ALERTS_KEY = "plc:alerts";
    private static final String PLC_DATA_START = "00";
    private static final String PLC_DATA_END = "FF";
    private static final String STATUS_RUNNING = "running";
    private static final String STATUS_NORMAL = "正常运行";
    private static final String STATUS_STANDBY_MODE = "待机模式";
    private static final String STATUS_STOP = "停止运行";
    private static final String STATUS_UNKNOWN = "未知的运行状态值";

    // 字段定义
    private MachineStatus currentStatus;
    private MachineSettings currentSettings;
    private final List<Alert> alerts = new ArrayList<>();

    // 依赖注入
    @Autowired
    private PlcServiceImpl plcServiceImpl;
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
        currentStatus = new MachineStatus();
        currentStatus.setWeight(120);
        currentStatus.setSoupVolume("35");
        currentStatus.setRunTime(4);
        currentStatus.setAutoClean(true);
        currentStatus.setNightMode(false);
        currentStatus.setStatus(STATUS_RUNNING);

        currentSettings = new MachineSettings();
        currentSettings.setAutoClean(false);
        currentSettings.setNightMode(false);
        currentSettings.setOpenLockTime(50);
        currentSettings.setSoupMaxTemperature(90);
        currentSettings.setSoupMinTemperature(70);
        currentSettings.setSoupQuantity(10);
        currentSettings.setFanVentilationTime(60);
        currentSettings.setElectricalBoxFanTemp(40);
        currentSettings.setElectricalBoxFanHumidity(60);

        currentSettings.setPrice1(10);
        currentSettings.setPrice2(15);
        currentSettings.setPrice3(20);
        currentSettings.setPrice4(25);
        currentSettings.setPrice5(30);

        currentSettings.setIngredient1Weight(100);
        currentSettings.setIngredient2Weight(150);
        currentSettings.setIngredient3Weight(200);
        currentSettings.setIngredient4Weight(250);
        currentSettings.setIngredient5Weight(300);
    }

    /**
     * 初始化默认报警信息
     */
    private void initializeDefaultAlerts() {
        alerts.add(new Alert(1, "2023-10-01 10:00", "一级", "温度过高", false));
        alerts.add(new Alert(2, "2023-10-01 10:05", "二级", "水位低", true));
        // 将默认报警信息同步到Redis
        syncAlertsToRedis();
    }

    /**
     * 将报警信息同步到Redis
     */
    private void syncAlertsToRedis() {
        try {
            String alertsJson = JSON.toJSONString(alerts);
            redisTemplate.opsForValue().set(REDIS_ALERTS_KEY, alertsJson);
            log.debug("报警信息已同步到Redis");
        } catch (Exception e) {
            log.error("同步报警信息到Redis失败: {}", e.getMessage());
        }
    }

    /**
     * 从Redis加载报警信息
     */
    private void loadAlertsFromRedis() {
        try {
            String alertsJson = redisTemplate.opsForValue().get(REDIS_ALERTS_KEY);
            if (alertsJson != null) {
                alerts.clear();
                alerts.addAll(JSON.parseArray(alertsJson, Alert.class));
                log.debug("从Redis加载了{}条报警信息", alerts.size());
            }
        } catch (Exception e) {
            log.error("从Redis加载报警信息失败: {}", e.getMessage());
        }
    }

    // ==================== 设置管理相关方法 ====================

    @Override
    public Result saveSettings(MachineSettings settings) {
        try {
            // 验证设置参数的合法性
            validateSettings(settings);
            
            // 从redis中读取当前PLC数据
            String currentPlcData = plcServiceImpl.readSentData();
            if (currentPlcData == null || currentPlcData.isEmpty()) {
                log.error("无法从Redis获取PLC数据");
                return Result.error("无法获取PLC数据");
            }
            
            // 规范化数据格式：确保每个字节之间只有一个空格
            currentPlcData = currentPlcData.replaceAll("\\s+", " ").trim();
            
            // 分割数据
            String[] dataArray = currentPlcData.split(" ");
            log.info("从Redis读取到PLC数据，共{}字节", dataArray.length);
            
            // 检查数据格式
            if (dataArray.length < 2 || !dataArray[0].equals(PLC_DATA_START) || !dataArray[dataArray.length - 1].equals(PLC_DATA_END)) {
                log.error("PLC数据格式错误：开头必须为00，结尾必须为FF");
                return Result.error("PLC数据格式错误");
            }
            
            // 处理可能的多字节表示问题
            List<String> normalizedData = new ArrayList<>();
            for (String byteStr : dataArray) {
                // 如果字节字符串长度超过2，则可能是多字节表示
                if (byteStr.length() > 2) {
                    // 每两个字符分割成一个字节
                    for (int i = 0; i < byteStr.length(); i += 2) {
                        if (i + 2 <= byteStr.length()) {
                            normalizedData.add(byteStr.substring(i, i + 2));
                        } else {
                            normalizedData.add(byteStr.substring(i));
                        }
                    }
                } else {
                    normalizedData.add(byteStr);
                }
            }
            
            // 更新数据数组
            dataArray = normalizedData.toArray(new String[0]);
            log.info("规范化后的PLC数据，共{}字节", dataArray.length);
            
            // 保留前50位数据（VB0-VB49）
            if (dataArray.length < 50) {
                log.error("PLC数据格式错误，长度不足50字节");
                return Result.error("PLC数据格式错误");
            }
            String dataBeforeVB50 = String.join(" ", Arrays.copyOfRange(dataArray, 0, 50));
            
            // 将设置值转换为两位16进制字符串
            StringBuilder settingsData = new StringBuilder();
            
            // 添加基本设置数据（VB150-VB199）
            settingsData.append(String.format("%02X", settings.getAutoClean() ? 1 : 0)).append(" ")      // VB50: 自动清洗开关(1:开启,0:关闭)
                      .append(String.format("%02X", settings.getNightMode() ? 1 : 0)).append(" ")        // VB51: 夜间模式开关(1:开启,0:关闭)
                      .append(String.format("%02X", settings.getOpenLockTime())).append(" ")             // VB52: 开门锁通电时间(0-255秒)
                      .append(String.format("%02X", settings.getSoupMaxTemperature())).append(" ")       // VB53: 汤最高温度(0-255℃)
                      .append(String.format("%02X", settings.getSoupMinTemperature())).append(" ")       // VB54: 汤最低温度(0-255℃)
                      .append(String.format("%02X", settings.getSoupQuantity())).append(" ")             // VB55: 汤数量(0-255)
                      .append(String.format("%02X", settings.getFanVentilationTime())).append(" ")       // VB56: 排油烟风扇通风时间(0-255分钟)
                      .append(String.format("%02X", settings.getElectricalBoxFanTemp())).append(" ")     // VB57: 电柜风扇通风温度(0-255℃)
                      .append(String.format("%02X", settings.getElectricalBoxFanHumidity())).append(" "); // VB58: 电柜风扇通风湿度(0-255%)
            
            // 添加价格设置（VB57-VB61）
            settingsData.append(String.format("%02X", settings.getPrice1())).append(" ")                // VB59: 价格1(0-255元)
                      .append(String.format("%02X", settings.getPrice2())).append(" ")                  // VB60: 价格2(0-255元)
                      .append(String.format("%02X", settings.getPrice3())).append(" ")                  // VB61: 价格3(0-255元)
                      .append(String.format("%02X", settings.getPrice4())).append(" ")                  // VB62: 价格4(0-255元)
                      .append(String.format("%02X", settings.getPrice5())).append(" ");                 // VB63: 价格5(0-255元)
            
            // 添加配料重量设置（VB62-VB71，每个配料用2字节表示）
            appendTwoByteHex(settingsData, settings.getIngredient1Weight());                           // VB64-65: 配料1重量(0-255g)
            appendTwoByteHex(settingsData, settings.getIngredient2Weight());                           // VB66-67: 配料2重量(0-255g)
            appendTwoByteHex(settingsData, settings.getIngredient3Weight());                           // VB68-69: 配料3重量(0-255g)
            appendTwoByteHex(settingsData, settings.getIngredient4Weight());                           // VB70-71: 配料4重量(0-255g)
            appendTwoByteHex(settingsData, settings.getIngredient5Weight());                           // VB72-73: 配料5重量(0-255g)
            
            // 计算已添加的字节数
            int currentBytes = settingsData.toString().split(" ").length;
            
            // 如果原始数据长度超过100（即有VB100及以后的数据）
            if (dataArray.length > 100) {
                // 补充剩余字节直到VB99（共50个字节，VB50-VB99）
                int bytesToAdd = 50 - currentBytes;
                
                for (int i = 0; i < bytesToAdd; i++) {
                    settingsData.append("00").append(" ");
                }
                
                // 保留VB100及以后的数据（不包括最后的FF）
                String dataAfterVB99 = String.join(" ", Arrays.copyOfRange(dataArray, 100, dataArray.length - 1));
                
                // 合并数据：前50字节 + 中间50字节 + 后面的字节 + 结束标记FF
                String finalPlcData = dataBeforeVB50 + " " + settingsData.toString().trim() + " " + dataAfterVB99 + " " + PLC_DATA_END;
                log.info("准备发送到PLC的数据共{}字节", finalPlcData.split(" ").length);
                
                // 存入Redis并发送到PLC
                redisTemplate.opsForValue().set(PlcServiceImpl.PLC_SEND_DATA_KEY, finalPlcData);
                plcServiceImpl.sendDataToPlc();
            } else {
                // 如果原始数据长度不超过100，则只需要补充到VB99
                int bytesToAdd = 50 - currentBytes;
                
                for (int i = 0; i < bytesToAdd; i++) {
                    settingsData.append("00").append(" ");
                }
                
                // 添加结束标记FF
                settingsData.append(PLC_DATA_END);
                
                // 合并数据
                String finalPlcData = dataBeforeVB50 + " " + settingsData.toString().trim();
                log.info("准备发送到PLC的数据共{}字节", finalPlcData.split(" ").length);
                
                // 存入Redis并发送到PLC
                redisTemplate.opsForValue().set(PlcServiceImpl.PLC_SEND_DATA_KEY, finalPlcData);
                plcServiceImpl.sendDataToPlc();
            }
            
            // 更新当前设置
            currentSettings = settings;
            
            return Result.success(settings);
        } catch (Exception e) {
            log.error("保存设置失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 验证设置参数的合法性
     */
    private void validateSettings(MachineSettings settings) {
        // 验证所有数值是否在0-255范围内
        validateRange(settings.getOpenLockTime(), "开门锁通电时间");
        validateRange(settings.getSoupMaxTemperature(), "汤最高温度");
        validateRange(settings.getSoupMinTemperature(), "汤最低温度");
        validateRange(settings.getSoupQuantity(), "汤数量");
        validateRange(settings.getFanVentilationTime(), "排油烟风扇通风时间");
        validateRange(settings.getElectricalBoxFanTemp(), "电柜风扇通风温度");
        validateRange(settings.getElectricalBoxFanHumidity(), "电柜风扇通风湿度");
        
        // 验证价格设置
        validateRange(settings.getPrice1(), "价格1");
        validateRange(settings.getPrice2(), "价格2");
        validateRange(settings.getPrice3(), "价格3");
        validateRange(settings.getPrice4(), "价格4");
        validateRange(settings.getPrice5(), "价格5");
        
        // 验证配料重量设置
        validateRange(settings.getIngredient1Weight(), "配料1重量");
        validateRange(settings.getIngredient2Weight(), "配料2重量");
        validateRange(settings.getIngredient3Weight(), "配料3重量");
        validateRange(settings.getIngredient4Weight(), "配料4重量");
        validateRange(settings.getIngredient5Weight(), "配料5重量");

        // 验证温度逻辑
        if (settings.getSoupMaxTemperature() < settings.getSoupMinTemperature()) {
            throw new IllegalArgumentException("汤最高温度不能低于最低温度");
        }
    }

    /**
     * 验证数值是否在0-255范围内
     */
    private void validateRange(int value, String fieldName) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException(fieldName + "必须在0-255范围内");
        }
    }

    /**
     * 将整数值转换为两个字节的16进制字符串并添加到StringBuilder
     */
    private void appendTwoByteHex(StringBuilder sb, int value) {
        // 高字节
        sb.append(String.format("%02X", (value >> 8) & 0xFF)).append(" ");
        // 低字节
        sb.append(String.format("%02X", value & 0xFF)).append(" ");
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


        // 填充状态信息
        populateStatusMap(status, currentStatus);

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
    private void populateStatusMap(Map<String, Object> status, MachineStatus machineStatus) {
        // 基本状态信息
        status.put("machineStatus", machineStatus.getStatus());
        status.put("temperature", machineStatus.getCurrentTemperature());
        status.put("weight", machineStatus.getWeight());
        status.put("runtime", machineStatus.getRunTime());
        status.put("robotStatus", machineStatus.getRobotStatus());
        status.put("robotMode", machineStatus.getRobotMode());
        status.put("robotEmergencyStop", machineStatus.getRobotEmergencyStop());
        status.put("currentProgram", machineStatus.getCurrentProgram());
        status.put("electricalBoxTemp", machineStatus.getElectricalBoxTemp());
        status.put("electricalBoxHumidity", machineStatus.getElectricalBoxHumidity());
        status.put("electricalBoxStatus", machineStatus.getElectricalBoxStatus());
        status.put("autoClean", machineStatus.isAutoClean());
        status.put("nightMode", machineStatus.isNightMode());

        // 设置信息
        status.put("openLockTime", currentSettings.getOpenLockTime());
        status.put("soupMaxTemperature", currentSettings.getSoupMaxTemperature());
        status.put("soupMinTemperature", currentSettings.getSoupMinTemperature());
        status.put("soupQuantity", currentSettings.getSoupQuantity());
        status.put("fanVentilationTime", currentSettings.getFanVentilationTime());
        status.put("electricalBoxFanTemp", currentSettings.getElectricalBoxFanTemp());
        status.put("electricalBoxFanHumidity", currentSettings.getElectricalBoxFanHumidity());

        // 价格信息
        status.put("price1", currentSettings.getPrice1());
        status.put("price2", currentSettings.getPrice2());
        status.put("price3", currentSettings.getPrice3());
        status.put("price4", currentSettings.getPrice4());
        status.put("price5", currentSettings.getPrice5());

        // 配料重量信息
        status.put("ingredient1Weight", currentSettings.getIngredient1Weight());
        status.put("ingredient2Weight", currentSettings.getIngredient2Weight());
        status.put("ingredient3Weight", currentSettings.getIngredient3Weight());
        status.put("ingredient4Weight", currentSettings.getIngredient4Weight());
        status.put("ingredient5Weight", currentSettings.getIngredient5Weight());

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
            parseRobotModeAndEmergencyStop(data, settings);
            parseProgramStatus(data, settings);
            parseRunningStatus(data, settings);
            parseElectricalBoxStatus(data, settings);
            parseCleanAndNightMode(data, settings);
            checkErrorCode(data);

            // 更新当前设置
            this.currentStatus = settings;
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
        // 同步到Redis
        syncAlertsToRedis();
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
            case 0:
                return "未上电";
            case 1:
                return "未使能";
            case 2:
                return "空闲";
            case 3:
                return "程序运行状态";
            case 4:
                return "暂停状态";
            case 5:
                return "程序结束";
            case 6:
                return "故障";
            case 7:
                return "机器人运动中";
            default:
                return "机器人未执行命令";
        }
    }

    /**
     * 获取程序描述
     */
    private String getProgramDescription(int programNumber) {
        switch (programNumber) {
            case 1:
                return "粉丝仓1";
            case 2:
                return "粉丝仓2";
            case 3:
                return "粉丝仓3";
            case 4:
                return "粉丝仓4";
            case 5:
                return "粉丝仓5";
            case 6:
                return "粉丝仓6";
            case 7:
                return "放碗子加热";
            case 8:
                return "取碗";
            case 9:
                return "取汤";
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
        StringBuilder binary = new StringBuilder(Integer.toBinaryString(decimal));
        // 补齐8位
        while (binary.length() < 8) {
            binary.insert(0, "0");
        }
        return binary.toString();
    }

    /**
     * 解析机器人模式和急停状态
     */
    private void parseRobotModeAndEmergencyStop(String[] data, MachineStatus settings) {
        if (data.length <= 10) return;

        // 假设V10.6是机器人模式，V10.7是机器人急停
        String robotModeBit = String.valueOf(hexToBinary(data[10]).charAt(6));
        String emergencyStopBit = String.valueOf(hexToBinary(data[10]).charAt(7));

        // 设置机器人模式
        settings.setRobotMode(robotModeBit.equals("1") ? "自动" : "手动");

        // 设置急停状态
        settings.setRobotEmergencyStop(emergencyStopBit.equals("1") ? "急停" : "正常工作");

        log.debug("机器人模式: {}, 急停状态: {}", settings.getRobotMode(), settings.getRobotEmergencyStop());
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

                InputPoint point = new InputPoint(name, pointId, status);
                inputPoints.add(point);
                log.debug("添加输入点: {}, 状态: {}", name, status);
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

        // V3.x 系列
        map.put("V3.0", "门锁2");
        map.put("V3.1", "门锁3");
        map.put("V3.2", "门锁4");
        map.put("V3.3", "门锁5");
        map.put("V3.4", "门锁6");
        map.put("V3.5", "门锁7");
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
        map.put("V5.0", "称重气缸");
        map.put("V5.1", "气压开关");
        map.put("V5.2", "备用1");
        map.put("V5.3", "备用2");
        map.put("V5.4", "备用3");
        map.put("V5.5", "备用4");
        map.put("V5.6", "备用5");
        map.put("V5.7", "备用6");
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

                OutputPoint point = new OutputPoint(name, pointId, status);
                outputPoints.add(point);
                log.debug("添加输出点: {}, 状态: {}", name, status);
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
        map.put("V7.1", "机器人开机");
        map.put("V7.2", "称重盒方向");
        map.put("V7.3", "称重盒开关");
        map.put("V7.4", "抽汤机");
        map.put("V7.5", "抽水泵");
        map.put("V7.6", "配电箱风扇");
        map.put("V7.7", "玻璃窗");

        // V8.x 系列
        map.put("V8.0", "旋转气缸");
        map.put("V8.1", "放碗电机");
        map.put("V8.2", "震动盘");
        map.put("V8.3", "柜灯继电器1");
        map.put("V8.4", "柜灯继电器2");
        map.put("V8.5", "柜灯继电器3");
        map.put("V8.6", "抽油烟排气");
        map.put("V8.7", "粉丝仓气缸1");

        // V9.x 系列
        map.put("V9.0", "粉丝仓气缸2");
        map.put("V9.1", "粉丝仓气缸3");
        map.put("V9.2", "粉丝仓气缸4");
        map.put("V9.3", "粉丝仓气缸5");
        map.put("V9.4", "粉丝仓气缸6");
        map.put("V9.5", "做汤机气缸");
        map.put("V9.6", "出餐口气缸");
        map.put("V9.7", "推碗气缸");

        // V10.x 系列
        map.put("V10.0", "夹手气缸");
        map.put("V10.1", "推肉气缸");
        map.put("V10.2", "称重气缸");
        map.put("V10.3", "汤桶加热蒸汽阀");
        map.put("V10.4", "出汤电磁阀");
        map.put("V10.5", "消毒蒸汽阀");
        map.put("V10.6", "机器人模式切换");
        map.put("V10.7", "机器人急停");

        // V11.x 系列
        map.put("V11.0", "备用");
        map.put("V11.1", "门锁1");
        map.put("V11.2", "门锁2");
        map.put("V11.3", "门锁3");
        map.put("V11.4", "门锁4");
        map.put("V11.5", "门锁5");
        map.put("V11.6", "门锁6");
        map.put("V11.7", "门锁7");

        return map;
    }

    /**
     * 获取PLC输出状态
     */
    private String getPlcStatus(String pointId) {
        try {
            String status = plcServiceImpl.getOutputStatus(pointId);
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
            // 先从Redis加载最新的报警信息
            loadAlertsFromRedis();

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
            // 先从Redis加载最新的报警信息
            loadAlertsFromRedis();

            boolean found = false;
            for (Alert alert : alerts) {
                if (alert.getId() != null && alert.getId() == id) {
                    alert.setResolved(true);
                    found = true;
                    log.info("已重置报警ID: {}", id);
                    break;
                }
            }

            if (found) {
                // 同步更新后的状态到Redis
                syncAlertsToRedis();
                return Result.success("报警已重置");
            } else {
                return Result.error("未找到指定ID的报警信息");
            }
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
            // 同步到Redis（删除Redis中的记录）
            redisTemplate.delete(REDIS_ALERTS_KEY);
            log.info("已清除所有报警信息");
            return Result.success("所有报警信息已清除");
        } catch (Exception e) {
            log.error("清除报警信息失败", e);
            return Result.error("清除报警信息失败: " + e.getMessage());
        }
    }

    @Override
    public MachineSettings getMachineSettings() {
        try {
            // 从PlcServiceImpl读取已发送的数据
            String plcData = plcServiceImpl.readSentData();
            if (plcData == null || plcData.isEmpty()) {
                log.warn("未能从PLC缓存中读取设置数据，返回当前设置");
                return currentSettings;
            }

            // 将数据字符串分割成字节数组
            String[] data = plcData.split(" ");
            
            // 验证数据格式
            if (data.length < 2 || !data[0].equals(PLC_DATA_START) || !data[data.length - 1].equals(PLC_DATA_END)) {
                log.error("PLC数据格式无效");
                return currentSettings;
            }

            MachineSettings settings = new MachineSettings();
            
            try {
                // 解析基本设置 (VB150-VB199)
                settings.setAutoClean(Integer.parseInt(data[50], 16) == 1);                // VB50: 自动清洗开关(1:开启,0:关闭)
                settings.setNightMode(Integer.parseInt(data[51], 16) == 1);                // VB51: 夜间模式开关(1:开启,0:关闭)
                settings.setOpenLockTime(Integer.parseInt(data[52], 16));                  // VB52: 开门锁通电时间(0-255秒)
                settings.setSoupQuantity(Integer.parseInt(data[53], 16));                  // VB53: 汤数量(0-255)
                settings.setSoupMaxTemperature(Integer.parseInt(data[54], 16));            // VB54: 汤最高温度(0-255℃)
                settings.setSoupMinTemperature(Integer.parseInt(data[55], 16));            // VB55: 汤最低温度(0-255℃)
                settings.setFanVentilationTime(Integer.parseInt(data[56], 16));            // VB56: 排油烟风扇通风时间(0-255分钟)
                settings.setElectricalBoxFanTemp(Integer.parseInt(data[57], 16));          // VB57: 电柜风扇通风温度(0-255℃)
                settings.setElectricalBoxFanHumidity(Integer.parseInt(data[58], 16));      // VB58: 电柜风扇通风湿度(0-255%)

                // 解析价格设置 (VB57-VB61)
                settings.setPrice1(Integer.parseInt(data[59], 16));                        // VB59: 价格1(0-255元)
                settings.setPrice2(Integer.parseInt(data[60], 16));                        // VB60: 价格2(0-255元)
                settings.setPrice3(Integer.parseInt(data[61], 16));                        // VB61: 价格3(0-255元)
                settings.setPrice4(Integer.parseInt(data[62], 16));                        // VB62: 价格4(0-255元)
                settings.setPrice5(Integer.parseInt(data[63], 16));                        // VB63: 价格5(0-255元)

                // 解析配料重量设置 (VB62-VB71，每个配料2字节)
                settings.setIngredient1Weight(Integer.parseInt(data[64], 16));            // VB64-65: 配料1重量(0-255g)
                settings.setIngredient2Weight(Integer.parseInt(data[65], 16));            // VB66-67: 配料2重量(0-255g)
                settings.setIngredient3Weight(Integer.parseInt(data[66], 16));            // VB68-69: 配料3重量(0-255g)
                settings.setIngredient4Weight(Integer.parseInt(data[67], 16));            // VB70-71: 配料4重量(0-255g)
                settings.setIngredient5Weight(Integer.parseInt(data[68], 16));            // VB72-73: 配料5重量(0-255g)

                log.debug("成功解析机器设置数据");
                return settings;
                
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                log.error("解析PLC数据时出错: {}", e.getMessage());
                return currentSettings;
            }
            
        } catch (Exception e) {
            log.error("获取机器设置失败: {}", e.getMessage());
            return currentSettings;
        }
    }

}