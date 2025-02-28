package com.jc.netty.server;

import com.jc.config.IpConfig;
import com.jc.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 分类处理服务
 * 用于根据客户端IP地址将消息分类处理到对应的设备处理器
 */
@Service
@Slf4j
public class FicationProcessing {

    @Autowired
    private PlcServiceImpl plcServiceImpl; // 信号设备处理器
    @Autowired
    private IpConfig ipConfig;

    /**
     * 分类处理方法
     *
     * @param clientIp 客户端IP地址
     * @param flag     是否为16进制消息
     * @param message  消息内容
     */
    public void classificationProcessing(String clientIp, boolean flag, String message) {
        // 根据客户端IP地址分类处理消息到对应的设备处理器
        if (clientIp.equals(ipConfig.getPlc())) {
            plcServiceImpl.handle(message, flag);
        }
    }
}
