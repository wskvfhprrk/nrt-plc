package com.jc;

import com.jc.mqtt.MqttConsumerConfig;
import com.jc.mqtt.MqttProviderConfig;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.io.IOException;

@SpringBootApplication(scanBasePackages = "com.jc")
@EnableAsync
@EnableScheduling
@Slf4j
public class Application {

    @Autowired
    private ApplicationContext ctx;
    @Autowired
    private MqttProviderConfig mqttProviderConfig;
    @Autowired
    private MqttConsumerConfig mqttConsumerConfig;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void openBrowser() {
        //启动mqtt发送端连接
        mqttProviderConfig.connect();
        //启动mqtt接收端连接
        mqttConsumerConfig.connect();
        //打开windows浏览器脚本全屏显示
        try {
            String scriptPath = "C:\\scripts\\open_browser.ps1";
            String command = "powershell.exe -ExecutionPolicy Bypass -File \"" + scriptPath + "\"";
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @Bean
    public CommandLineRunner run() {
        return args -> {
            // 启动Netty服务器
            ChannelFuture serverFuture = ctx.getBean("serverBootstrap", ChannelFuture.class);
            serverFuture.sync();


            // 添加关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    serverFuture.channel().close();
                    serverFuture.channel().eventLoop().shutdownGracefully();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
            // 阻塞直到服务器通道关闭
            serverFuture.channel().closeFuture().sync();
        };
    }
}
