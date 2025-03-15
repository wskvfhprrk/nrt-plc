package com.jc.service.impl;

import com.alibaba.fastjson.JSON;
import com.jc.constants.Constants;
import com.jc.entity.Order;
import com.jc.entity.PlcOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis队列服务，用于实现FIFO队列操作
 */
@Service
@Slf4j
public class RedisQueueService {

    @Autowired
    private RedisTemplate redisTemplate;
    // 使用Constants中定义的常量
    // 待做订单队列key
    // 正在制作订单key
    // 已完成订单队列key

    /**
     * 添加订单到待处理队列
     *
     * @param order 订单对象
     */
    public void addOrder(Order order) {
        redisTemplate.opsForList().rightPush(Constants.PENDING_ORDER_REDIS_PRIMARY_KEY, JSON.toJSONString(order));
        log.info("订单已添加到待处理队列: {}", order.getOrderId());
    }

    /**
     * 将Order对象添加到待处理队列
     *
     * @param order 订单对象
     */
    public void enqueue(Order order) {
        // 添加到队列
        addOrder(order);
        log.info("订单已添加到待处理队列: {}, 价格等级: {}", order.getOrderId(), order.getSelectedPrice());
    }

    /**
     * 获取队列中待处理订单数量
     *
     * @return 队列大小
     */
    public long getQueueSize() {
        Long size = redisTemplate.opsForList().size(Constants.PENDING_ORDER_REDIS_PRIMARY_KEY);
        return size != null ? size : 0;
    }

    /**
     * 获取下一个待处理订单
     *
     * @return 待处理的订单，如果队列为空则返回null
     */
    public Order getNextOrder() {
        String orderJson = (String) redisTemplate.opsForList().leftPop(Constants.PENDING_ORDER_REDIS_PRIMARY_KEY);
        if (orderJson != null) {
            return JSON.parseObject(orderJson, Order.class);
        }
        return null;
    }

    /**
     * 将订单从待做状态移至正在制作状态
     *
     * @return 待处理的订单，如果队列为空则返回null
     */
    public Order moveOrderToProcessing() {
        Order order = getNextOrder();
        if (order != null) {
            redisTemplate.opsForValue().set(Constants.ORDER_REDIS_PRIMARY_KEY_IN_PROGRESS, JSON.toJSONString(order));
            log.info("订单已移至正在制作状态: {}", order.getOrderId());
        }
        return order;
    }

    /**
     * 将订单从正在制作状态移至已完成状态
     */
    public void moveOrderToCompleted() {
        String orderJson = (String) redisTemplate.opsForValue().get(Constants.ORDER_REDIS_PRIMARY_KEY_IN_PROGRESS);
        if (orderJson != null) {
            redisTemplate.opsForList().rightPush(Constants.COMPLETED_ORDER_REDIS_PRIMARY_KEY, orderJson);
            // 删除正在制作状态的订单缓存
            redisTemplate.delete(Constants.ORDER_REDIS_PRIMARY_KEY_IN_PROGRESS);
            // 设置已完成订单队列的过期时间为1分钟
            redisTemplate.expire(Constants.COMPLETED_ORDER_REDIS_PRIMARY_KEY, 60, java.util.concurrent.TimeUnit.SECONDS);
            Order order = JSON.parseObject(orderJson, Order.class);
            log.info("订单已移至已完成状态: {}, 缓存将在1分钟后失效", order.getOrderId());
        } else {
//            log.warn("没有正在制作的订单，无法移至已完成状态");
        }
    }

    /**
     * 获取所有已完成订单
     *
     * @return 已完成订单列表
     */
    public List<Order> getCompletedOrders() {
        List<Object> orderJsonList = redisTemplate.opsForList().range(Constants.COMPLETED_ORDER_REDIS_PRIMARY_KEY, 0, -1);
        List<Order> orders = new ArrayList<>();
        if (orderJsonList != null) {
            for (Object orderJson : orderJsonList) {
                orders.add(JSON.parseObject((String) orderJson, Order.class));
            }
        }
        return orders;
    }

    /**
     * 清空已完成订单队列
     */
    public void clearCompletedOrders() {
        redisTemplate.delete(Constants.COMPLETED_ORDER_REDIS_PRIMARY_KEY);
        log.info("已清空已完成订单队列");
    }

    /**
     * 获取当前正在处理的订单
     *
     * @return 正在处理的订单，如果没有则返回null
     */
    public Order getProcessingOrder() {
        String orderJson = (String) redisTemplate.opsForValue().get(Constants.ORDER_REDIS_PRIMARY_KEY_IN_PROGRESS);
        if (orderJson != null) {
            return JSON.parseObject(orderJson, Order.class);
        }
        return null;
    }
}
