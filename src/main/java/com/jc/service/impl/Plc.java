package com.jc.service.impl;

import com.jc.config.IpConfig;
import com.jc.netty.server.NettyServerHandler;
import com.jc.service.DeviceHandler;
import lombok.Data;
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

    /**
     * 处理消息
     *
     * @param message 消息内容
     * @param isHex   是否为16进制消息
     */
    /**
     * 验证PLC数据格式
     *
     * @param message PLC数据
     * @return 是否有效
     */
    private boolean validatePlcData(String message) {
        // 验证长度
        if (message.length() != 299) {
            log.warn("无效的PLC数据长度：{}，应为299字符（100字节）", message.length());
            return false;
        }

        // 验证开头和结尾
        if (!message.startsWith("00") || !message.endsWith("FF")) {
            log.warn("无效的PLC数据格式：开头应为00，结尾应为FF");
            return false;
        }

        return true;
    }

    @Override
    public void handle(String message, boolean isHex) {
        // 去除空格
        String cleanMessage = message.trim();
        
        // 验证数据格式
        if (!validatePlcData(cleanMessage)) {
            return;
        }
        
        log.info("收到有效的PLC数据：{}", message);
        
        // 将解析后的数据存入Redis
        redisTemplate.opsForValue().set("plc:data", cleanMessage);
        log.info("PLC数据已存入Redis缓存");
        
    }

    /**
     * 获取PLC输出点的状态
     * @param pointId PLC点位ID（例如：V7.0, V7.1等）
     * @return 返回"打开"或"关闭"
     */
    public String getOutputStatus(String pointId) {
        // 从Redis获取PLC数据
        String plcData = (String) redisTemplate.opsForValue().get("plc:data");
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

}
