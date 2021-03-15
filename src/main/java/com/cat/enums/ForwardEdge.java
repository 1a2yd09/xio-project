package com.cat.enums;

/**
 * @author CAT
 */

public enum ForwardEdge {
    /**
     * 朝前边为较短边
     */
    SHORT(0, "短边"),
    /**
     * 朝前边为较长边
     */
    LONG(1, "长边");

    public final Integer code;
    public final String value;

    ForwardEdge(Integer code, String value) {
        this.code = code;
        this.value = value;
    }
}
