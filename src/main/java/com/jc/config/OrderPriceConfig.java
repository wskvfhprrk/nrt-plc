package com.jc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单价格配置类，用于读取application.yml中的价格配置
 */
@Component
@ConfigurationProperties(prefix = "order")
public class OrderPriceConfig {
    
    private Map<String, Integer> prices = new HashMap<>();
    
    public Map<String, Integer> getPrices() {
        return prices;
    }
    
    public void setPrices(Map<String, Integer> prices) {
        this.prices = prices;
    }
    
    /**
     * 根据价格值获取价格等级
     * @param price 价格值
     * @return 价格等级（1-5），如果没有匹配则返回默认值1
     */
    public int getPriceLevelByValue(Integer price) {
        if (price == null) {
            return 1; // 默认值
        }
        
        for (int i = 1; i <= 5; i++) {
            Integer configPrice = prices.get("type" + i);
            if (configPrice != null && configPrice.equals(price)) {
                return i;
            }
        }
        
        return 1; // 默认值
    }
} 