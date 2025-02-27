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


}
