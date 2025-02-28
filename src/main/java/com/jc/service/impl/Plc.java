package com.jc.service.impl;

import com.jc.service.DeviceHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * IO设备处理类——根据传感器自动控制设备
 * 实现了DeviceHandler接口，提供了处理IO设备消息的功能
 */
@Service
@Slf4j
public class Plc implements DeviceHandler {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    public Boolean sendOrderStatus = false;

    private static final String PLC_DATA_START = "00";
    private static final String PLC_DATA_END = "FF";
    private static final String PLC_DATA_KEY = "plc:data";
    private static final String PLC_SEND_DATA_KEY = "plc:send:data";
    private static final String VB150_KEY = "VB150";

    /**
     * 处理消息
     *
     * @param message 消息内容
     * @param isHex   是否为16进制消息
     */
    @Override
    public void handle(String message, boolean isHex) {
        String cleanMessage = message.trim();

        if (!isValidPlcData(cleanMessage)) {
            return;
        }

        log.info("收到有效的PLC数据：{}", message);
        storePlcData(cleanMessage);
        syncVB50ToVB150(cleanMessage);
    }

    /**
     * 验证PLC数据格式
     *
     * @param message PLC数据
     * @return 是否有效
     */
    private boolean isValidPlcData(String message) {
        if (message.length() != 299) {
            log.warn("无效的PLC数据长度：{}，应为299字符（100字节）", message.length());
            return false;
        }
        if (!message.startsWith(PLC_DATA_START) || !message.endsWith(PLC_DATA_END)) {
            log.warn("无效的PLC数据格式：开头应为00，结尾应为FF");
            return false;
        }
        return true;
    }

    // 存储PLC数据到Redis
    private void storePlcData(String data) {
        redisTemplate.opsForValue().set(PLC_DATA_KEY, data);
        log.info("PLC数据已存入Redis缓存");
    }

    /**
     * 获取PLC输出点的状态
     * @param pointId PLC点位ID（例如：V7.0, V7.1等）
     * @return 返回"打开"或"关闭"
     */
    public String getOutputStatus(String pointId) {
        // 从Redis获取PLC数据
        String plcData = redisTemplate.opsForValue().get(PLC_DATA_KEY);
        if (plcData == null) {
            log.warn("未能从Redis获取PLC数据");
            return "未知";
        }

        // 解析点位ID，例如"V7.0"
        String[] parts = pointId.substring(1).split("\\.");
        int byteIndex = Integer.parseInt(parts[0]);
        int bitPosition = Integer.parseInt(parts[1]);

        // 将PLC数据分割成字节数组
        String[] dataBytes = plcData.split(" ");
        
        // 检查索引是否有效
        if (byteIndex >= dataBytes.length) {
            log.warn("无效的字节索引：{}", byteIndex);
            return "未知";
        }

        try {
            // 将十六进制字符串转换为整数
            int value = Integer.parseInt(dataBytes[byteIndex], 16);
            // 检查特定位的状态
            boolean isOn = ((value >> bitPosition) & 1) == 1;
            return isOn ? "打开" : "关闭";
        } catch (NumberFormatException e) {
            log.error("解析PLC数据失败：{}", e.getMessage());
            return "未知";
        }
    }


    // 解析PLC数据
    private String parsePlcData(String plcData) {
        if (plcData.startsWith(PLC_DATA_START) && plcData.endsWith(PLC_DATA_END)) {
            return plcData.substring(2, plcData.length() - 2); // 去掉开头和结尾的检验位
        } else {
            log.warn("PLC数据格式不正确");
            return "未知";
        }
    }


    // 更新发送给PLC的数据
    private void updateSendData(String newData) {
        String existingData = redisTemplate.opsForValue().get(PLC_SEND_DATA_KEY);
        if (!isEmpty(existingData)) {
            String updatedData = replaceVB150AndAfter(existingData, newData);
            redisTemplate.opsForValue().set(PLC_SEND_DATA_KEY, updatedData);
            log.info("已更新plc:send:data: {}", updatedData);
        } else {
            redisTemplate.opsForValue().set(PLC_SEND_DATA_KEY, newData);
            log.info("已将数据发送到PLC: {}", newData);
        }
    }


    // 同步VB50到VB150
    private void syncVB50ToVB150(String message) {
        String vb50Value = extractVBValue(message, "VB50");
        redisTemplate.opsForValue().set(VB150_KEY, vb50Value);
        log.info("已将VB50的值同步到VB150: {}", vb50Value);
    }

    /**
     * 从PLC消息中提取指定VB变量的值
     * @param message PLC完整消息数据
     * @param vbName VB变量名（例如：VB50、VB150等）
     * @return 提取的VB值
     */
    private String extractVBValue(String message, String vbName) {
        // 从vbName中提取数字部分（例如从"VB50"提取出50）
        int vbNumber = Integer.parseInt(vbName.substring(2));
        
        // 计算在消息中的实际位置
        // 由于每个字节用两个十六进制字符表示，所以索引需要乘以2
        int startIndex = vbNumber * 2;
        int endIndex = startIndex + 2;  // 每个VB值占用2个字符
        
        // 检查索引是否越界
        if (endIndex > message.length()) {
            log.warn("提取VB值时索引越界：{}，消息长度：{}", endIndex, message.length());
            return "00";  // 返回默认值
        }
        
        return message.substring(startIndex, endIndex);
    }

    // 替换VB150以后的值
    private String replaceVB150AndAfter(String existingData, String newData) {
        String[] parts = existingData.split(" ");
        int vb150Index = findVB150Index(parts);

        if (vb150Index != -1) {
            for (int i = vb150Index + 1; i < parts.length; i++) {
                parts[i] = newData; // 替换为新的数据
            }
        }
        return String.join(" ", parts);
    }

    // 查找VB150的索引
    private int findVB150Index(String[] parts) {
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("VB150")) {
                return i;
            }
        }
        return -1;
    }

    // 检查字符串是否为空
    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    /**
     * 发送数据到PLC
     * @param data 要发送的数据
     */
    public void sendDataToPlc(String data) {
        String cleanData = data.trim();
        log.info("准备发送数据到PLC：{}", cleanData);
        
        // 更新发送数据
        updateSendData(cleanData);
        
        // 设置发送状态
        sendOrderStatus = true;
        
        log.info("数据已成功发送到PLC");
    }

    /**
     * 读取已发送到PLC的数据
     * @return 最近一次发送到PLC的数据，如果没有数据则返回null
     */
    public String readSentData() {
        String sentData = redisTemplate.opsForValue().get(PLC_SEND_DATA_KEY);
        if (isEmpty(sentData)) {
            log.warn("没有找到已发送的PLC数据");
            return null;
        }
        log.info("读取到已发送的PLC数据：{}", sentData);
        return sentData;
    }
}

