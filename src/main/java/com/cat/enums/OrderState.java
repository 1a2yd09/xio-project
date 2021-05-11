package com.cat.enums;

/**
 * @author CAT
 */

public enum OrderState {
    /**
     * 未开工
     */
    NOT_STARTED("未开工"),
    /**
     * 已开工
     */
    STARTED("已开工"),
    /**
     * 已完工
     */
    COMPLETED("已完工"),
    /**
     * 已暂停
     */
    PAUSED("已暂停");

    public final String value;

    OrderState(String value) {
        this.value = value;
    }
}
