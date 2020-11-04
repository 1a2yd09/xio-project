package com.cat.entity.enums;

public enum BottomSortPatternEnum {
    /**
     * 按顺序号排序。
     */
    SEQ("顺序"),
    /**
     * 按成品规格排序。
     */
    SPEC("规格");

    public final String value;

    BottomSortPatternEnum(String value) {
        this.value = value;
    }
}
