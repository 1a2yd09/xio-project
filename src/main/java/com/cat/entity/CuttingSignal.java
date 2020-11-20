package com.cat.entity;

public class CuttingSignal extends BaseSignal {
    private String specification;
    private Boolean towardEdge;
    private Integer orderId;

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
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
        return super.toString() +
                "CuttingSignal{" +
                "specification='" + specification + '\'' +
                ", towardEdge=" + towardEdge +
                ", orderId=" + orderId +
                '}';
    }
}
