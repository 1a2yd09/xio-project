package com.cat.entity.signal;

public class CuttingSignal extends BaseSignal {
    private String cuttingSize;
    private Boolean towardEdge;
    private Integer orderId;

    public String getCuttingSize() {
        return cuttingSize;
    }

    public void setCuttingSize(String cuttingSize) {
        this.cuttingSize = cuttingSize;
    }

    public Boolean getTowardEdge() {
        return towardEdge;
    }

    public void setTowardEdge(Boolean towardEdge) {
        this.towardEdge = towardEdge;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "CuttingSignal{" +
                "cuttingSize='" + cuttingSize + '\'' +
                ", towardEdge=" + towardEdge +
                ", orderId=" + orderId +
                '}';
    }
}
