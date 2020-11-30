package com.cat.entity.signal;

/**
 * @author CAT
 */
public class CuttingSignal extends BaseSignal {
    private String cuttingSize;
    private Integer forwardEdge;
    private Integer orderId;

    public CuttingSignal() {
    }

    public CuttingSignal(String cuttingSize, Integer forwardEdge, Integer orderId) {
        this.cuttingSize = cuttingSize;
        this.forwardEdge = forwardEdge;
        this.orderId = orderId;
    }

    public String getCuttingSize() {
        return cuttingSize;
    }

    public void setCuttingSize(String cuttingSize) {
        this.cuttingSize = cuttingSize;
    }

    public Integer getForwardEdge() {
        return forwardEdge;
    }

    public void setForwardEdge(Integer forwardEdge) {
        this.forwardEdge = forwardEdge;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return super.toString() +
                "CuttingSignal{" +
                "cuttingSize='" + cuttingSize + '\'' +
                ", forwardEdge=" + forwardEdge +
                ", orderId=" + orderId +
                '}';
    }
}
