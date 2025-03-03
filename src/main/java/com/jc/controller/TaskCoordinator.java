package com.jc.controller;

import com.jc.config.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * 任务中心管理器
 */
@Service
@Slf4j
public class TaskCoordinator {
    public Result executeOrder()  {
        /**
         * 当plc状态==2（完成订单）或0（停止中）时，表表示订单已经完成，如果==1表示工作中，==3表示故障，当为0和2时发送订单，
         *
         */
        return Result.success();
    }
}
