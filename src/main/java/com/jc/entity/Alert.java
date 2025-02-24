package com.jc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Alert {
    private String time; // 时间
    private String level; // 报警级别
    private String message; // 报警信息
    private boolean resolved; // 状态

}