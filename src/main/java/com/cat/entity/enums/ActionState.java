package com.cat.entity.enums;

public enum ActionState {
    /**
     * 动作状态。
     */
    NOT_FINISHED("未完成"), FINISHED("已完成"), FAILED("已报废");

    public final String value;

    ActionState(String value) {
        this.value = value;
    }
}
