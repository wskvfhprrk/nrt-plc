package com.jc.service.impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jc.constants.Constants;
import com.jc.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Redis队列服务，用于实现FIFO队列操作
 */
@Service
@Slf4j
public class RedisQueueService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    // 将Order对象添加到队列末尾
    public void   enqueue(Order order) {
        String orderJson = JSON.toJSONString(order);
        if(order.getCustomerName()==null)return;
        redisTemplate.opsForList().rightPush(Constants.PENDING_ORDER_REDIS_PRIMARY_KEY, orderJson);
    }


    // 获取队列长度
    public long getQueueSize() {
        return redisTemplate.opsForList().size(Constants.PENDING_ORDER_REDIS_PRIMARY_KEY);
    }


}
