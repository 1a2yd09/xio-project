package com.cat.entity.enums;

public enum SignalCategoryEnum {
    /**
     * 动作信号。
     */
    ACTION("动作"),
    /**
     * 开工信号。
     */
    START_WORK("开工");

    public final String value;

    SignalCategoryEnum(String value) {
        this.value = value;
    }
}
