package com.cat.enums;

public enum ActionCategory {
    /**
     * 旋转
     */
    ROTATE("旋转"),
    /**
     * 进刀
     */
    CUT("进刀"),
    /**
     * 送板
     */
    SEND("送板");

    public final String value;

    ActionCategory(String value) {
        this.value = value;
    }
}
