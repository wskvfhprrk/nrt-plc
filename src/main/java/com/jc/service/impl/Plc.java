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
    private NettyServerHandler nettyServerHandler;
    @Autowired
    private IpConfig ipConfig;
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
        
        // 解析PLC数据
        parsePlcData(cleanMessage);

        // 将解析后的数据存入Redis
        redisTemplate.opsForValue().set("plc:data", cleanMessage);
        log.info("PLC数据已存入Redis缓存");

        // 去除前3和后3个字符后再发送回去
        String trimmedMessage = cleanMessage.substring(3, cleanMessage.length() - 3);

        //00 01 02 03 04 05 06 07 08 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 01 00 01 00 64 08 00 00 00 B3 15 B3 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF
        String str="01 02 03 04 05 06 07 08 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 01 00 01 00 64 08 00 00 00 B3 15 B3 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";

        // 发送plc数据
        nettyServerHandler.sendMessageToClient(ipConfig.getPlc(), "00 " + str + " FF", isHex);
    }

    /**
     * 解析数据
     * @param cleanMessage
     */
    private void parsePlcData(String cleanMessage) {
        String[] data = cleanMessage.split(" ");

        log.info("PLC信号状态解析结果：");
        log.info("输入信号：");
        log.info("IB1: {}", hexToBinary(data[1]));
        log.info("IB2: {}", hexToBinary(data[2]));
        log.info("IB3: {}", hexToBinary(data[3]));
        log.info("IB4: {}", hexToBinary(data[4]));
        log.info("备用: {}", hexToBinary(data[5]));
        log.info("输出信号：");
        log.info("QB1: {}", hexToBinary(data[6]));
        log.info("QB2: {}", hexToBinary(data[7]));
        log.info("QB3: {}", hexToBinary(data[8]));
        log.info("QB4: {}", hexToBinary(data[9]));
        log.info("QB8: {}", hexToBinary(data[10]));
        log.info("QB9: {}", hexToBinary(data[11]));
        log.info("备用: {}", hexToBinary(data[12]));
        log.info("机器运行程序1: {}",hexToBinary(data[13]));
        log.info("机器运行程序2: {}",hexToBinary(data[14]));
        log.info("机器人运行状态1: {}",hexToBinary(data[15]));
        log.info("机器人运行状态2: {}",hexToBinary(data[16]));
        log.info("当前汤的温度: {}", data[17]+data[18]);
        log.info("当前电箱的湿度: {}", data[18]+data[19]);
        log.info("当前电箱的温度: {}", data[20]+data[21]);
        log.info("机器故障码: {}", data[49]);
    }

    /**
     * 16进制字符串转二进制字符串
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
}
