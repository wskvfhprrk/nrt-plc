package com.jc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "ip")
@Data
public class IpConfig {
    /**
     * 本机ip
     */
    private String local;
    /**
     * 本机tcp端口
     */
    private int nettyPort;
    /**
     * plc
     */
    private String plc;

}
