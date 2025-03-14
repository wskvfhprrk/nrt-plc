package com.jc.controller;

import com.jc.config.Result;
import com.jc.entity.Order;
import com.jc.entity.PlcOrder;
import com.jc.service.impl.RedisQueueService;
import com.jc.service.impl.MachineServiceImpl;
import com.jc.service.impl.PlcServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;


/**
 * 任务中心管理器
 */
@Service
@Slf4j
public class TaskCoordinator {

    @Autowired
    private RedisTemplate redisTemplate;
    private final PlcServiceImpl plcService;
    private final RedisQueueService redisQueueService;
    private final MachineServiceImpl machineService;
    private int lastPlcStatus = -1; // 初始化为-1，确保第一次一定会处理

    public TaskCoordinator( PlcServiceImpl plcService, RedisQueueService redisQueueService, MachineServiceImpl machineService) {
        this.plcService = plcService;
        this.redisQueueService = redisQueueService;
        this.machineService = machineService;
    }

    /**
     * 执行订单处理
     */
    public Result executeOrder() {
        // 检查PLC状态
        String plcData = (String) redisTemplate.opsForValue().get(PlcServiceImpl.PLC_DATA_KEY);
        if (plcData != null) {
            String[] dataArray = plcData.split(" ");
            // 假设VB25存储在特定位置，需要根据实际情况调整
            int plcStatus = Integer.parseInt(dataArray[25], 16);
            
            // 当状态为0或者与上一次状态不同时才处理
            if (plcStatus == 0 || plcStatus != lastPlcStatus) {
                lastPlcStatus=plcStatus;
                // 根据PLC状态处理
                if (plcStatus == 2) { // 完成状态
                    // 订单完成，移至已完成状态
                    redisQueueService.moveOrderToCompleted();
                    log.info("订单已完成，准备处理下一个订单");
                    
                    // 处理下一个订单
                    if (redisQueueService.getQueueSize() > 0) {
                        Order nextOrder = redisQueueService.moveOrderToProcessing();
                        if (nextOrder != null) {
                            // 将Order转换为PlcOrder
                            PlcOrder plcOrder = PlcOrder.fromOrder(nextOrder);
                            plcOrder.setIsNewOrder(true);
                            return machineService.sendPlcOrder(plcOrder);
                        }
                    }
                } else if (plcStatus == 0) { // 无订单状态
                    log.info("PLC当前无订单，可以添加新订单");
                    
                    // 如果有待处理订单，则开始处理
                    if (redisQueueService.getQueueSize() > 0) {
                        Order order = redisQueueService.moveOrderToProcessing();
                        if (order != null) {
                            // 将Order转换为PlcOrder
                            PlcOrder plcOrder = PlcOrder.fromOrder(order);
                            plcOrder.setIsNewOrder(true);
                            return machineService.sendPlcOrder(plcOrder);
                        }
                    }
                } else if (plcStatus == 3) { // 错误状态
                    log.error("PLC报告错误，需要人工干预");
                    // 处理错误逻辑
                    return Result.error("PLC报告错误，需要人工干预");
                } else if (plcStatus == 1) { // 运行中状态
                    // PLC正在处理订单，不做任何操作
                }
                
                // 更新上一次的状态值
                lastPlcStatus = plcStatus;
            }
        }
        
        return Result.success("订单处理中");
    }
}
