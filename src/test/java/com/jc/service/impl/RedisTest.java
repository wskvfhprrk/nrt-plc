package com.jc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testReadAndWriteRedis() {
        int index = 16;
        String newValue = "01";
        // 读取值
        String value = (String) redisTemplate.opsForValue().get("plc:data");
        System.out.println("读取到的值: " + value);
        String[] parts = value.split(" ");
        parts[index] = newValue;
        String newString = String.join(" ", parts);
        System.out.println("新修改的字符串为：" + newString);
        // 写入值到另一个键
        redisTemplate.opsForValue().set("plc:data:copy", value);
        redisTemplate.opsForValue().set("plc:data", newString);
        System.out.println("已将值写入到 plc:data:copy");
    }

}