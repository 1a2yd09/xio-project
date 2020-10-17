package com.cat.entity.enums;

public enum BoardCategory {
    /**
     * 下料板。
     */
    CUTTING("下料"),
    /**
     * 成品板。
     */
    PRODUCT("成品"),
    /**
     * 半成品。
     */
    SEMI_PRODUCT("半成品"),
    /**
     * 库存件。
     */
    STOCK("库存件"),
    /**
     * 余料。
     */
    REMAINING("余料"),
    /**
     * 废料。
     */
    WASTED("废料");

    public final String value;

    BoardCategory(String value) {
        this.value = value;
    }
}
