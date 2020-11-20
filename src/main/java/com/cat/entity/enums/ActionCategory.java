package com.cat.entity.enums;

public enum ActionCategory {
    /**
     * 动作类型。
     */
    ROTATE("旋转"), CUT("进刀"), SEND("送板");

    public final String value;

    ActionCategory(String value) {
        this.value = value;
    }
}
