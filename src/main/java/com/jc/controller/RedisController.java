package com.jc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    //访问地址：http://localhost:8080/readAndWriteRedis?index=22&newValue=FF
    @GetMapping("readAndWriteRedis")
    public void readAndWriteRedis(int index, int newValue) {
        // 读取值
        String value = (String) redisTemplate.opsForValue().get("plc:data");
        System.out.println("读取到的值: " + value);
        String[] parts = null;
        if (value != null) {
            parts = value.split(" ");
        }
        // 限制值的范围，大于255时设置为255
        int limitedValue = Math.min(newValue, 255);
        // 将整数转换为两位十六进制字符串
        String hexValue = String.format("%02X", limitedValue);
        if (parts != null) {
            parts[index] = hexValue;
        }
        String newString = String.join(" ", parts);
        System.out.println("新修改的字符串为：" + newString);
        // 写入值到另一个键
        redisTemplate.opsForValue().set("plc:data:copy", value);
        redisTemplate.opsForValue().set("plc:data", newString);
        System.out.println("已将值写入到 plc:data:copy");
    }
}
