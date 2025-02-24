package com.jc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Alert {
    private Integer id;
    private String timestamp;
    private String level;
    private String message;
    private boolean resolved;

}