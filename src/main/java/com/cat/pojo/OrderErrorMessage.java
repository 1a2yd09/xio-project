package com.cat.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author CAT
 */
@Data
public class OrderErrorMessage {
    private Integer orderId;
    private String cuttingSize;
    private String productSpecification;
    private LocalDateTime createdAt;

    /**
     * 实例化工单错误消息对象。
     *
     * @param orderId              工单 ID
     * @param cuttingSize          下料尺寸
     * @param productSpecification 成品规格
     * @return 消息对象
     */
    public static OrderErrorMessage getInstance(Integer orderId, String cuttingSize, String productSpecification) {
        OrderErrorMessage msg = new OrderErrorMessage();
        msg.setOrderId(orderId);
        msg.setCuttingSize(cuttingSize);
        msg.setProductSpecification(productSpecification);
        msg.setCreatedAt(LocalDateTime.now());
        return msg;
    }
}