package com.cat.entity.signal;

/**
 * @author CAT
 */
public class TakeBoardSignal extends BaseSignal {
    private Integer orderId;

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return super.toString() +
                "TakeBoardSignal{" +
                "orderId=" + orderId +
                '}';
    }
}
