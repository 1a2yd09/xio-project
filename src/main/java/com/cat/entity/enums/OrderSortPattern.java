package com.cat.entity.enums;

public enum OrderSortPattern {
    /**
     * 按顺序号排序。
     */
    BY_SEQ("顺序"),
    /**
     * 按成品规格排序。
     */
    BY_SPEC("规格");

    public final String value;

    OrderSortPattern(String value) {
        this.value = value;
    }
}
