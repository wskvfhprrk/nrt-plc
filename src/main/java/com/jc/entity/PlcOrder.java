package com.jc.entity;

import com.jc.config.OrderPriceConfig;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * PlcOrder 实体类，专门用于向PLC发送订单数据
 */
@Data
@Component
public class PlcOrder implements Serializable {

    private static OrderPriceConfig orderPriceConfig;

    @Autowired
    public void setOrderPriceConfig(OrderPriceConfig orderPriceConfig) {
        PlcOrder.orderPriceConfig = orderPriceConfig;
    }

    /**
     * 订单ID
     */
    private String orderId;
    
    /**
     * 当前加工订单的价格值（1-5）
     */
    private Integer priceLevel;
    
    /**
     * 当前配料配方（0:无特殊配方, 1:辣, 2:微辣, 3:不辣）
     */
    private Integer recipeCode;
    
    /**
     * 种类（1:牛肉粉丝，2:牛肉面）
     */
    private Integer foodType;
    
    /**
     * 是否新订单（true为新订单）
     */
    private Boolean isNewOrder;
    
    /**
     * 默认构造函数
     */
    public PlcOrder() {
        this.isNewOrder = false;
    }
    
    /**
     * 带参数的构造函数
     */
    public PlcOrder(String orderId, Integer priceLevel, Integer recipeCode, Integer foodType) {
        this.orderId = orderId;
        this.priceLevel = priceLevel;
        this.recipeCode = recipeCode;
        this.foodType = foodType;
        this.isNewOrder = false;
    }
    
    /**
     * 从Order对象创建PlcOrder对象
     */
    public static PlcOrder fromOrder(Order order) {
        PlcOrder plcOrder = new PlcOrder();
        plcOrder.setOrderId(order.getOrderId());
        
        // 设置价格等级
        if (order.getSelectedPrice() != null) {
            // 使用配置类根据实际价格值映射到价格等级
            int priceLevel = orderPriceConfig != null 
                ? orderPriceConfig.getPriceLevelByValue(order.getSelectedPrice())
                : 1; // 如果配置类未初始化，则使用默认值
            
            plcOrder.setPriceLevel(priceLevel);
        } else {
            plcOrder.setPriceLevel(1); // 默认为1
        }
        
        // 设置食谱类型
        if (order.getSelectedRecipe() != null && order.getSelectedRecipe().contains("面")) {
            plcOrder.setFoodType(2); // 牛肉面
        } else {
            plcOrder.setFoodType(1); // 默认为牛肉粉丝
        }
        
        // 设置配料配方代码
        if (order.getSelectedSpice() != null) {
            String spiceType = order.getSelectedSpice();
            if (spiceType.contains("辣") && !spiceType.contains("微辣")) {
                plcOrder.setRecipeCode(1); // 辣
            } else if (spiceType.contains("微辣")) {
                plcOrder.setRecipeCode(2); // 微辣
            } else if (spiceType.contains("不辣")) {
                plcOrder.setRecipeCode(3); // 不辣
            } else {
                plcOrder.setRecipeCode(0); // 默认无特殊配方
            }
        } else {
            plcOrder.setRecipeCode(0); // 默认无特殊配方
        }
        
        plcOrder.setIsNewOrder(false);
        
        return plcOrder;
    }
} 