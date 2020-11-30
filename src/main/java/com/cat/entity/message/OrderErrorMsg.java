package com.cat.entity.message;

import java.time.LocalDateTime;

/**
 * @author CAT
 */
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

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getCuttingSize() {
        return cuttingSize;
    }

    public void setCuttingSize(String cuttingSize) {
        this.cuttingSize = cuttingSize;
    }

    public String getProductSpecification() {
        return productSpecification;
    }

    public void setProductSpecification(String productSpecification) {
        this.productSpecification = productSpecification;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "OrderErrorMsg{" +
                "orderId=" + orderId +
                ", cuttingSize='" + cuttingSize + '\'' +
                ", productSpecification='" + productSpecification + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
