package com.cat.enums;

/**
 * @author CAT
 */

public enum ActionState {
    /**
     * 未完成
     */
    INCOMPLETE("未完成"),
    /**
     * 已完成
     */
    COMPLETED("已完成"),
    /**
     * 已报废
     */
    SCRAPPED("已报废");

    public final String value;

    ActionState(String value) {
        this.value = value;
    }
}
