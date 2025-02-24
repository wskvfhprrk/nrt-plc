package com.jc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InputPoint {
    private String name; // 名称
    private String pointName; // 点位
    private String status; // 状态值

}