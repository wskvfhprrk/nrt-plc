package com.jc.service.impl;

import com.jc.config.IpConfig;
import com.jc.config.Result;
import com.jc.netty.client.NettyClientHandler;
import com.jc.netty.server.NettyServerHandler;
import com.jc.service.DeviceHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * IO设备处理类——根据传感器自动控制设备
 * 实现了DeviceHandler接口，提供了处理IO设备消息的功能
 */
@Service
@Slf4j
public class PlcServiceImpl implements DeviceHandler {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    public Boolean sendOrderStatus = false;

    private static final String PLC_DATA_START = "00";
    private static final String PLC_DATA_END = "FF";
    public static final String PLC_DATA_KEY = "plc:data";
    public static final String PLC_SEND_DATA_KEY = "plc:send:data";
    public static final String VB150_KEY = "VB150";
    @Autowired
    private NettyServerHandler nettyServerHandler;
    @Autowired
    private IpConfig ipConfig;

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

//        log.info("收到有效的PLC数据：{}", message);
        storePlcData(cleanMessage);
        // todo 进行同步配置参数
//        syncVB50ToVB150(cleanMessage);
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
//        log.info("PLC数据已存入Redis缓存");
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






    // 同步VB50到VB150
    private void syncVB50ToVB150(String message) {
        // 获取VB50的值
        String vb50Value = extractVBValue(message, 50);
        
        // 从缓存中获取要发送到PLC的数据
        String plcSendData = redisTemplate.opsForValue().get(PLC_SEND_DATA_KEY);
        if (!isEmpty(plcSendData)) {
            // 从PLC发送数据字符串中截取前51个字节(VB0-VB50)的数据，以空格分隔
            String[] parts = plcSendData.split(" ");
            // 截取前51个字节(VB0-VB50)的数据，以空格分隔
            String dataUpToVB50 = String.join(" ", Arrays.copyOf(parts, 51));
            // 组合数据
            String updatedData = dataUpToVB50 + " " + vb50Value;
            // 更新发送数据
            redisTemplate.opsForValue().set(PLC_SEND_DATA_KEY, updatedData);
//            log.info("已将VB50的值同步到VB150存进redis中: {}", updatedData);
        } else {
            log.warn("已将VB50的值同步到VB150到redis中失败");
        }
        
        // 保持原有的VB150更新逻辑
        redisTemplate.opsForValue().set(VB150_KEY, vb50Value);
//        log.info("已将VB50的值同步到VB150: {}", vb50Value);
    }


    /**
     * 从PLC消息中提取指定位置开始的值
     * @param message PLC完整消息数据
     * @param index 开始提取的索引位置
     * @return 从指定索引开始到结束的所有值，以空格连接
     */
    private String extractVBValue(String message, int index) {
        String[] parts = message.split(" ");
        if (index >= parts.length) {
            log.warn("索引超出数组范围：index={}, length={}", index, parts.length);
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = index; i < parts.length; i++) {
            result.append(parts[i]);
            if (i < parts.length - 1) {
                result.append(" ");
            }
        }
        return result.toString();
    }



    // 检查字符串是否为空
    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    /**
     * 发送数据到PLC
     */
    public Result sendDataToPlc() {
        // 从Redis获取要发送的数据
        String dataToSend = redisTemplate.opsForValue().get(PLC_SEND_DATA_KEY);
        if (dataToSend == null || dataToSend.isEmpty()) {
            log.warn("没有找到要发送的PLC数据");
            return Result.error("没有找到要发送的PLC数据");
        }

        // 使用Netty发送数据
        nettyServerHandler.sendMessageToClient(ipConfig.getPlc(), dataToSend, true);
        log.info("数据已成功发送到PLC: {}", dataToSend);
        return Result.success();
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
        return sentData;
    }

    public String readPlcData() {
        String sentData = redisTemplate.opsForValue().get(PLC_DATA_KEY);
        if (isEmpty(sentData)) {
            log.warn("没有找到PLC数据");
            return null;
        }
        return sentData;
    }
}

