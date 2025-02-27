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
import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service // 标记这是一个服务类
@Slf4j  // 启用日志功能
public class MachineServiceImpl implements MachineService {
    private MachineStatus currentSettings; // 存储当前机器设置
    private List<Alert> alerts = new ArrayList<>(); // 存储报警信息列表

    @Autowired
    private Plc plc; // 注入PLC控制器
    @Autowired
    private NettyServerHandler nettyServerHandler; // 注入Netty服务处理器
    @Autowired
    private IpConfig ipConfig; // 注入IP配置
    @Autowired
    private RedisTemplate redisTemplate; // 注入Redis模板

    // 构造函数：初始化默认设置
    public MachineServiceImpl() {
        currentSettings = new MachineStatus();
        currentSettings.setWeight(120); // 设置面条重量
        currentSettings.setSoupVolume("35");   // 设置汤量
        currentSettings.setRunTime(4);    // 设置烹饪时间
        currentSettings.setAutoClean(true);   // 启用自动清洗
        currentSettings.setNightMode(false);  // 关闭夜间模式
        currentSettings.setStatus("running"); // 设置运行状态

        // 初始化报警信息
        Alert alert1 = new Alert(1, "2023-10-01 10:00", "一级", "温度过高", false);
        alerts.add(alert1);

        Alert alert2 = new Alert(2, "2023-10-01 10:05", "二级", "水位低", true);
        alerts.add(alert2);
    }

    @Override
    public void saveSettings(MachineStatus settings) {
        try {
            validateSettings(settings); // 验证设置参数
            this.currentSettings = settings; // 保存设置
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // 验证设置参数的方法
    private void validateSettings(MachineStatus settings) {

        if (settings.getWeight() < 50 || settings.getWeight() > 200) {
            throw new IllegalArgumentException("面条重量超出范围");
        }



        if (settings.getRunTime() < 1 || settings.getRunTime() > 10) {
            throw new IllegalArgumentException("制作时间超出范围");
        }

        if (!isValidStatus(settings.getStatus())) {
            throw new IllegalArgumentException("无效的运行状态");
        }
    }

    @Override
    public MachineStatus getSettings() {
        // 如果没有当前设置，返回默认设置
        if (currentSettings == null) {
            return getDefaultSettings();
        }
        return currentSettings;
    }

    // 验证运行状态是否有效
    private boolean isValidStatus(String status) {
        return status != null && (
                status.equals("running") ||    // 运行中
                status.equals("standby") ||    // 待机
                status.equals("stopped")       // 停止
        );
    }

    // 获取默认设置
    private MachineStatus getDefaultSettings() {
        MachineStatus defaultSettings = new MachineStatus();
        defaultSettings.setWeight(100);  // 默认面条重量
        defaultSettings.setSoupVolume("30");    // 默认汤量
        defaultSettings.setRunTime(3);     // 默认烹饪时间
        defaultSettings.setAutoClean(true);    // 默认启用自动清洗
        defaultSettings.setNightMode(false);   // 默认关闭夜间模式
        defaultSettings.setStatus("running");  // 默认运行状态
        return defaultSettings;
    }

    @Override
    public Map<String, Object> getMachineStatus() {
        // 从Redis中获取当前机器状态数据
        Map<String, Object> status = new HashMap<>();
        Object o = redisTemplate.opsForValue().get("plc:data");
        if (o == null) {
            return null;
        }
        String plcData = (String) o;

        if (plcData == null) {
            log.warn("未能从Redis获取PLC数据");
            return status;
        }
        // 解析PLC数据并更新当前设置
        parsePlcData(plcData);

        // 获取当前设置
        MachineStatus settings = getSettings(); // 使用更新后的设置

        // 将当前设置的相关信息添加到状态中
        status.put("machineStatus", settings.getStatus());
        status.put("temperature", settings.getCurrentTemperature()); // 使用当前温度
        status.put("weight", settings.getWeight()); // 重量
        status.put("runtime", settings.getRunTime());
        status.put("robotStatus", settings.getRobotStatus()); // 使用机器人状态
        status.put("currentProgram", settings.getCurrentProgram()); // 使用当前程序
        status.put("electricalBoxTemp", settings.getElectricalBoxTemp()); // 使用电箱温度
        status.put("electricalBoxHumidity", settings.getElectricalBoxHumidity()); // 使用电箱湿度
        status.put("electricalBoxStatus", settings.getElectricalBoxStatus()); // 使用电箱状态
        status.put("autoClean", settings.isAutoClean()); // 自动清洗
        status.put("nightMode", settings.isNightMode()); // 夜间模式

        // 添加设备输入点、输出点和报警信息
        status.put("inputPoints", getInputPoints());
        status.put("outputPoints", getOutputPoints());
        status.put("alerts", getAlerts());

        return status;
    }

    /**
     * 解析数据
     *
     * @param cleanMessage
     */
    private void parsePlcData(String cleanMessage) {
        String[] data = cleanMessage.split(" "); // 假设数据以空格分隔

        // 验证数据包格式
        if (!data[0].equals("00") || !data[data.length - 1].equals("FF")) {
            log.error("数据包格式错误: 开头必须为00，结尾必须为FF");
            return;
        }

        // 创建新的 MachineStatus 对象
        MachineStatus settings = new MachineStatus();

        try {
            // 1. 解析温度相关数据
            int soupTemp = Integer.parseInt(data[18] + data[19], 16); // 获取原始十六进制值
            if (soupTemp > 0x7FFF) { // 判断是否为负数（最高位为1）
                soupTemp = -(~soupTemp & 0xFFFF) - 1; // 转换为有符号整数
            }
            settings.setCurrentTemperature(String.valueOf(soupTemp / 10.0)); // 设置当前温度
            settings.setSoupVolume(String.valueOf(soupTemp)); // 设置汤温度

            // 2. 解析电箱温湿度 (VB20-23)
            String boxHumidity = String.valueOf(Integer.parseInt(data[20] + data[21], 16));
            int boxTemp = Integer.parseInt(data[22] + data[23], 16);
            if (boxTemp > 0x7FFF) { // 判断是否为负数（最高位为1）
                boxTemp = -(~boxTemp & 0xFFFF) - 1; // 转换为有符号整数
            }
            settings.setElectricalBoxHumidity(String.valueOf(boxTemp / 10.0));
            settings.setElectricalBoxTemp(boxHumidity);
            
            // 3. 解析重量数据 (VB26)
            int weight = Integer.parseInt(data[26], 16);
            settings.setWeight(weight);
            
            // 4. 解析机器人状态 (VB16)
            int robotStatusBinary = Integer.parseInt(data[16], 16);
            settings.setRobotStatus(parseRobotStatus(robotStatusBinary));

            // 5. 解析当前机器人运行程序状态 (VB14)
            int programNumber = Integer.parseInt(data[14], 16);
            String currentProgram = "";
            switch (programNumber) {
                case 1:
                    currentProgram = "粉丝仓1";
                    break;
                case 2:
                    currentProgram = "粉丝仓2";
                    break;
                case 3:
                    currentProgram = "粉丝仓3";
                    break;
                case 4:
                    currentProgram = "粉丝仓4";
                    break;
                case 5:
                    currentProgram = "粉丝仓5";
                    break;
                case 6:
                    currentProgram = "粉丝仓6";
                    break;
                case 7:
                    currentProgram = "放碗子加热";
                    break;
                case 8:
                    currentProgram = "取碗";
                    break;
                case 9:
                    currentProgram = "取汤";
                    break;
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                    currentProgram = "备用" + (programNumber - 9);
                    break;
                default:
                    currentProgram = "未知程序";
            }
            settings.setCurrentProgram(currentProgram);
            settings.setRunTime(Integer.parseInt(data[32]+data[33], 16)); // 机器运行时间
            // 6. 运行状态
            int runningStatus = Integer.parseInt(data[15], 16);
            switch (runningStatus) {
                case 1:
                    settings.setStatus("正常运行");    // 正常运行
                    break;
                case 2:
                    settings.setStatus("待机模式");    // 待机模式
                    break;
                case 0:
                    settings.setStatus("停止运行");    // 停止运行
                    break;
                default:
                    log.warn("未知的运行状态值: {}", runningStatus);
                    settings.setStatus("未知的运行状态值");
            }
            
            // 7. 解析电箱状态 (VB17)
            int electricalBoxStatus = Integer.parseInt(data[17], 16);
            settings.setElectricalBoxStatus(electricalBoxStatus); // 假设你在 MachineStatus 中有这个方法
            
            // 8. 设置清洗和夜间模式
            boolean autoClean = Integer.parseInt(data[52], 16)==0?false:true;
            boolean nightMode = Integer.parseInt(data[53], 16)==0?false:true;
            settings.setAutoClean(autoClean);
            settings.setNightMode(nightMode);
            
            // 9. 检查故障码并记录报警 (VB50)
            int errorCode = Integer.parseInt(data[50], 16);
            if (errorCode != 0) {
                Alert alert = new Alert(
                    alerts.size() + 1,
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                    "一级",
                    "故障码: " + errorCode,
                    false
                );
                alerts.add(alert);
            }

        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            log.error("解析PLC数据失败: {}", e.getMessage());
            return;
        }

        // 更新当前设置
        this.currentSettings = settings;
    }

    // 辅助方法：解析机器人状态
    private String parseRobotStatus(int binaryStatus) {

                // 返回对应的状态
                switch (binaryStatus) {
                    case 0: return "未上电"; // V16.0
                    case 1: return "未使能"; // V16.1
                    case 2: return "空闲";   // V16.2
                    case 3: return "程序运行状态"; // V16.3
                    case 4: return "暂停状态"; // V16.4
                    case 5: return "程序结束"; // V16.5
                    case 6: return "故障";   // V16.6
                    case 7: return "机器人运动中"; // V16.7
                }
        return "机器人未执行命令";
    }

    /**
     * 16进制字符串转二进制字符串
     *
     * @param hex 16进制字符串
     * @return 8位二进制字符串
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


    public List<InputPoint> getInputPoints() {
        List<InputPoint> inputPoints = new ArrayList<>();
        // 初始化所有输入点，动态获取状态
        inputPoints.add(new InputPoint("粉丝到位传感器", "V1.0", getPlcStatus("V1.0")));
        inputPoints.add(new InputPoint("粉丝气缸1", "V1.1", getPlcStatus("V1.1")));
        inputPoints.add(new InputPoint("粉丝气缸2", "V1.2", getPlcStatus("V1.2")));
        inputPoints.add(new InputPoint("粉丝气缸3", "V1.3", getPlcStatus("V1.3")));
        inputPoints.add(new InputPoint("粉丝气缸4", "V1.4", getPlcStatus("V1.4")));
        inputPoints.add(new InputPoint("粉丝气缸5", "V1.5", getPlcStatus("V1.5")));
        inputPoints.add(new InputPoint("粉丝气缸6", "V1.6", getPlcStatus("V1.6")));
        inputPoints.add(new InputPoint("出碗检测", "V1.7", getPlcStatus("V1.7")));
        inputPoints.add(new InputPoint("是否有碗", "V2.0", getPlcStatus("V2.0")));
        inputPoints.add(new InputPoint("碗报警", "V2.1", getPlcStatus("V2.1")));
        inputPoints.add(new InputPoint("出碗电机", "V2.2", getPlcStatus("V2.2")));
        inputPoints.add(new InputPoint("做汤机气缸", "V2.3", getPlcStatus("V2.3")));
        inputPoints.add(new InputPoint("流量计", "V2.4", getPlcStatus("V2.4")));
        inputPoints.add(new InputPoint("出餐口气缸", "V2.5", getPlcStatus("V2.5")));
        inputPoints.add(new InputPoint("推碗气缸", "V2.6", getPlcStatus("V2.6")));
        inputPoints.add(new InputPoint("门锁1", "V2.7", getPlcStatus("V2.7")));
        inputPoints.add(new InputPoint("门锁2", "V3.0", getPlcStatus("V3.0")));
        inputPoints.add(new InputPoint("门锁3", "V3.1", getPlcStatus("V3.1")));
        inputPoints.add(new InputPoint("门锁4", "V3.2", getPlcStatus("V3.2")));
        inputPoints.add(new InputPoint("门锁5", "V3.3", getPlcStatus("V3.3")));
        inputPoints.add(new InputPoint("门锁6", "V3.4", getPlcStatus("V3.4")));
        inputPoints.add(new InputPoint("门锁7", "V3.5", getPlcStatus("V3.5")));
        inputPoints.add(new InputPoint("夹手气缸", "V3.6", getPlcStatus("V3.6")));
        inputPoints.add(new InputPoint("旋转气缸", "V3.7", getPlcStatus("V3.7")));
        inputPoints.add(new InputPoint("切网机数量", "V4.0", getPlcStatus("V4.0")));
        inputPoints.add(new InputPoint("切肉机是否有肉", "V4.1", getPlcStatus("V4.1")));
        inputPoints.add(new InputPoint("切肉机报警", "V4.2", getPlcStatus("V4.2")));
        inputPoints.add(new InputPoint("汤桶报警", "V4.3", getPlcStatus("V4.3")));
        inputPoints.add(new InputPoint("汤桶最低位", "V4.4", getPlcStatus("V4.4")));
        inputPoints.add(new InputPoint("水桶报警", "V4.5", getPlcStatus("V4.5")));
        inputPoints.add(new InputPoint("水桶最低位", "V4.6", getPlcStatus("V4.6")));
        inputPoints.add(new InputPoint("废水桶满", "V4.7", getPlcStatus("V4.7")));
        inputPoints.add(new InputPoint("称重气缸", "V5.1", getPlcStatus("V5.1")));
        inputPoints.add(new InputPoint("气压开关", "V5.2", getPlcStatus("V5.2")));
        inputPoints.add(new InputPoint("备用1", "V5.3", getPlcStatus("V5.3")));
        inputPoints.add(new InputPoint("备用2", "V5.4", getPlcStatus("V5.4")));
        return inputPoints;
    }

    public List<OutputPoint> getOutputPoints() {
        List<OutputPoint> outputPoints = new ArrayList<>();
        // 初始化所有输出点，动态获取状态
        outputPoints.add(new OutputPoint("粉丝仓气缸1", "V7.0", getPlcStatus("V7.0")));
        outputPoints.add(new OutputPoint("粉丝仓气缸2", "V7.1", getPlcStatus("V7.1")));
        outputPoints.add(new OutputPoint("粉丝仓气缸3", "V7.2", getPlcStatus("V7.2")));
        outputPoints.add(new OutputPoint("粉丝仓气缸4", "V7.3", getPlcStatus("V7.3")));
        outputPoints.add(new OutputPoint("粉丝仓气缸5", "V7.4", getPlcStatus("V7.4")));
        outputPoints.add(new OutputPoint("粉丝仓气缸6", "V7.5", getPlcStatus("V7.5")));
        outputPoints.add(new OutputPoint("做汤机气缸", "V7.6", getPlcStatus("V7.6")));
        outputPoints.add(new OutputPoint("出餐口气缸", "V7.7", getPlcStatus("V7.7")));
        outputPoints.add(new OutputPoint("推碗气缸", "V8.0", getPlcStatus("V8.0")));
        outputPoints.add(new OutputPoint("夹手气缸", "V8.1", getPlcStatus("V8.1")));
        outputPoints.add(new OutputPoint("旋转气缸", "V8.2", getPlcStatus("V8.2")));
        outputPoints.add(new OutputPoint("切网机气缸", "V8.3", getPlcStatus("V8.3")));
        outputPoints.add(new OutputPoint("切肉机气缸", "V8.4", getPlcStatus("V8.4")));
        outputPoints.add(new OutputPoint("汤桶加热蒸汽阀", "V8.5", getPlcStatus("V8.5")));
        outputPoints.add(new OutputPoint("出汤电磁阀", "V8.6", getPlcStatus("V8.6")));
        outputPoints.add(new OutputPoint("消毒蒸汽阀", "V8.7", getPlcStatus("V8.7")));
        outputPoints.add(new OutputPoint("备用3", "V9.0", getPlcStatus("V9.0")));
        outputPoints.add(new OutputPoint("备用4", "V9.1", getPlcStatus("V9.1")));
        outputPoints.add(new OutputPoint("备用5", "V9.2", getPlcStatus("V9.2")));
        outputPoints.add(new OutputPoint("备用6", "V9.3", getPlcStatus("V9.3")));
        outputPoints.add(new OutputPoint("备用7", "V9.4", getPlcStatus("V9.4")));
        outputPoints.add(new OutputPoint("备用8", "V9.5", getPlcStatus("V9.5")));
        outputPoints.add(new OutputPoint("备用9", "V9.6", getPlcStatus("V9.6")));
        outputPoints.add(new OutputPoint("备用10", "V9.7", getPlcStatus("V9.7")));
        outputPoints.add(new OutputPoint("门锁1", "V10.0", getPlcStatus("V10.0")));
        outputPoints.add(new OutputPoint("门锁2", "V10.1", getPlcStatus("V10.1")));
        outputPoints.add(new OutputPoint("门锁3", "V10.2", getPlcStatus("V10.2")));
        outputPoints.add(new OutputPoint("门锁4", "V10.3", getPlcStatus("V10.3")));
        outputPoints.add(new OutputPoint("门锁5", "V10.4", getPlcStatus("V10.4")));
        outputPoints.add(new OutputPoint("门锁6", "V10.5", getPlcStatus("V10.5")));
        outputPoints.add(new OutputPoint("门锁7", "V10.6", getPlcStatus("V10.6")));
        outputPoints.add(new OutputPoint("备用11", "V10.7", getPlcStatus("V10.7"))); // 将重复的"备用4"改为"备用11"

        return outputPoints;
    }

    // 新增获取PLC输出状态的方法
    private String getPlcStatus(String pointId) {
        // 这里实现获取PLC状态的逻辑，返回"打开"或"关闭"
        // 例如：根据点位ID从PLC获取状态
        // 这只是一个示例，具体实现需要根据实际情况来
        return plc.getOutputStatus(pointId); // 假设plc有这个方法
    }

    // 修改获取报警信息的方法
    public List<Alert> getAlerts() {
        return new ArrayList<>(alerts); // 返回副本保证数据安全
    }

    @Override
    public Result resetAlert(int id) {
        // 首先检查内存中的alerts
        for (Alert alert : alerts) {
            if (alert.getId() != null && alert.getId() == id) {
                alert.setResolved(true);
                break;
            }
        }
        return Result.success();
    }

    @Override
    @Transactional
    public Result clearAllAlerts() {
        try {
            // 清除数据库中的报警记录
            return Result.success();
        } catch (Exception e) {
            log.error("清除报警信息失败", e);
            return Result.error(e.getMessage());
        }
    }

    public void sendDataToPLC(String data) {
        // 确保数据以 00 开头，以 FF 结尾
        if (!data.startsWith("00") || !data.endsWith("FF")) {
            throw new IllegalArgumentException("数据包格式不正确");
        }
        // 发送数据到 PLC 的逻辑
        nettyServerHandler.sendMessageToClient(ipConfig.getPlc(), data, true);
    }

}