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
        log.info("机器运行程序1: {}", hexToBinary(data[13]));
        log.info("机器运行程序2: {}", hexToBinary(data[14]));
        log.info("机器人运行状态1: {}", hexToBinary(data[15]));
        log.info("机器人运行状态2: {}", hexToBinary(data[16]));
        log.info("当前汤的温度: {}", data[17] + data[18]);
        log.info("当前电箱的湿度: {}", data[18] + data[19]);
        log.info("当前电箱的温度: {}", data[20] + data[21]);
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

    /**
     * 读取运行状态参数
     * @return 运行状态参数的字符串表示
     */
    public String readRunStatusParameters() {
        // 假设从 PLC 读取到的状态参数
        String runStatusData = redisTemplate.opsForValue().get("plc:runStatus");
        if (runStatusData == null) {
            log.warn("未能从Redis获取运行状态参数");
            return "未获取到运行状态参数";
        }

        // 解析状态参数
        String[] statusData = runStatusData.split(" ");
        StringBuilder statusBuilder = new StringBuilder("运行状态参数：\n");
        statusBuilder.append("运行状态: ").append(statusData[0]).append("\n");
        statusBuilder.append("自动清洗: ").append(statusData[1]).append("\n");
        statusBuilder.append("夜间模式: ").append(statusData[2]).append("\n");
        statusBuilder.append("开门锁通电时间: ").append(statusData[3]).append("\n");
        statusBuilder.append("汤数量: ").append(statusData[4]).append("\n");
        statusBuilder.append("汤加热温度: ").append(statusData[5]).append("\n");
        statusBuilder.append("汤保温温度: ").append(statusData[6]).append("\n");
        statusBuilder.append("电柜风扇通风的温度值: ").append(statusData[7]).append("\n");
        statusBuilder.append("电柜风扇通风的湿度值: ").append(statusData[8]).append("\n");
        statusBuilder.append("排油烟风扇通风的时间: ").append(statusData[9]).append("\n");
        statusBuilder.append("价格对应的牛肉量: ").append(statusData[10]).append("\n");
        statusBuilder.append("配料对应的重量: ").append(statusData[11]).append("\n");

        return statusBuilder.toString();
    }

    /**
     * 存储PLC数据到Redis
     * @param plcData PLC数据字符串
     * @return 是否存储成功
     */
    public boolean storeData(String plcData) {
        try {
            // 验证数据格式
            if (!validatePlcData(plcData)) {
                log.error("PLC数据格式无效，无法存储: {}", plcData);
                return false;
            }

            // 获取当前时间戳
            String timestamp = String.valueOf(System.currentTimeMillis());

            // 存储原始数据
            redisTemplate.opsForValue().set("plc:raw_data", plcData);
            // 存储带时间戳的数据
            redisTemplate.opsForValue().set("plc:data:" + timestamp, plcData);
            
            // 解析并存储各个部分的数据
            String[] data = plcData.split(" ");
            
            // 存储输入信号
            redisTemplate.opsForValue().set("plc:input:IB1", hexToBinary(data[1]));
            redisTemplate.opsForValue().set("plc:input:IB2", hexToBinary(data[2]));
            redisTemplate.opsForValue().set("plc:input:IB3", hexToBinary(data[3]));
            redisTemplate.opsForValue().set("plc:input:IB4", hexToBinary(data[4]));
            
            // 存储输出信号
            redisTemplate.opsForValue().set("plc:output:QB1", hexToBinary(data[6]));
            redisTemplate.opsForValue().set("plc:output:QB2", hexToBinary(data[7]));
            redisTemplate.opsForValue().set("plc:output:QB3", hexToBinary(data[8]));
            redisTemplate.opsForValue().set("plc:output:QB4", hexToBinary(data[9]));
            redisTemplate.opsForValue().set("plc:output:QB8", hexToBinary(data[10]));
            redisTemplate.opsForValue().set("plc:output:QB9", hexToBinary(data[11]));
            
            // 存储运行状态
            redisTemplate.opsForValue().set("plc:status:program1", hexToBinary(data[13]));
            redisTemplate.opsForValue().set("plc:status:program2", hexToBinary(data[14]));
            redisTemplate.opsForValue().set("plc:status:robot1", hexToBinary(data[15]));
            redisTemplate.opsForValue().set("plc:status:robot2", hexToBinary(data[16]));
            
            // 存储温度和湿度数据
            redisTemplate.opsForValue().set("plc:temp:soup", data[17] + data[18]);
            redisTemplate.opsForValue().set("plc:humidity:box", data[18] + data[19]);
            redisTemplate.opsForValue().set("plc:temp:box", data[20] + data[21]);
            
            // 存储故障码
            redisTemplate.opsForValue().set("plc:error_code", data[49]);

            log.info("PLC数据已成功存储到Redis，时间戳: {}", timestamp);
            return true;
            
        } catch (Exception e) {
            log.error("存储PLC数据时发生错误: ", e);
            return false;
        }
    }
}
