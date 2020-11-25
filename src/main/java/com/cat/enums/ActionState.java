package com.cat.enums;

public enum ActionState {
    /**
     * 未完成
     */
    NOT_FINISHED("未完成"),
    /**
     * 已完成
     */
    FINISHED("已完成"),
    /**
     * 已报废
     */
    FAILED("已报废");

    public final String value;

    ActionState(String value) {
        this.value = value;
    }
}
