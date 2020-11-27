package com.cat.enums;

/**
 * @author CAT
 */

public enum ForwardEdge {
    /**
     * 短边朝前。
     */
    SHORT(0, "短边"),
    /**
     * 长边朝前。
     */
    LONG(1, "长边");

    public final Integer code;
    public final String value;

    ForwardEdge(Integer code, String value) {
        this.code = code;
        this.value = value;
    }
}
