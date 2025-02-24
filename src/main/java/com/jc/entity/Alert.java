package com.jc.entity;

import lombok.Data;

@Data
public class Alert {
    private Long id;  // 改为 Long 类型
    private String timestamp;  // 改回String类型
    private String level;
    private String message;
    private boolean resolved;

    public Alert(String timestamp, String level, String message, boolean resolved) {
        this.timestamp = timestamp;  // 直接使用字符串时间
        this.level = level;
        this.message = message;
        this.resolved = resolved;
    }

    // 添加无参构造函数用于JSON反序列化
    public Alert() {
    }
}