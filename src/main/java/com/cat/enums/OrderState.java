package com.cat.enums;

public enum OrderState {
    /**
     * 未开工。
     */
    NOT_YET_STARTED("未开工"),
    /**
     * 已开工。
     */
    ALREADY_STARTED("已开工"),
    /**
     * 已完工。
     */
    COMPLETED("已完工");

    public final String value;

    OrderState(String value) {
        this.value = value;
    }
}
