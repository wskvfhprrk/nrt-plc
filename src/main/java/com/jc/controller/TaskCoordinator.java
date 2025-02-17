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
        return Result.success();
    }
}
