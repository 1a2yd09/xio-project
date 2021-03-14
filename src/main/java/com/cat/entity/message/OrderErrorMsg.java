package com.cat.entity.message;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author CAT
 */
@Data
public class OrderErrorMsg {
    private Integer orderId;
    private String cuttingSize;
    private String productSpecification;
    private LocalDateTime createdAt;

    /**
     * 获取一个消息对象。
     *
     * @param orderId              工单 ID
     * @param cuttingSize          下料尺寸
     * @param productSpecification 成品规格
     * @return 消息对象
     */
    public static OrderErrorMsg getInstance(Integer orderId, String cuttingSize, String productSpecification) {
        OrderErrorMsg msg = new OrderErrorMsg();
        msg.setOrderId(orderId);
        msg.setCuttingSize(cuttingSize);
        msg.setProductSpecification(productSpecification);
        msg.setCreatedAt(LocalDateTime.now());
        return msg;
    }
}
