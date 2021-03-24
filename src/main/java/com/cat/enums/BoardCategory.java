package com.cat.enums;

/**
 * @author CAT
 */

public enum BoardCategory {
    /**
     * 下料板
     */
    CUTTING("下料板"),
    /**
     * 成品板
     */
    PRODUCT("成品板"),
    /**
     * 半成品
     */
    SEMI_PRODUCT("半成品"),
    /**
     * 库存件
     */
    STOCK("库存件"),
    /**
     * 余料板
     */
    REMAINING("余料板"),
    /**
     * 废料板
     */
    WASTE("废料板");

    public final String value;

    BoardCategory(String value) {
        this.value = value;
    }
}
