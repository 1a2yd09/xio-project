package com.cat.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

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

    private static final Map<String, BoardCategory> LOOKUP = new HashMap<>(6);
    public final String value;

    BoardCategory(String value) {
        this.value = value;
    }

    static {
        for (BoardCategory bc : EnumSet.allOf(BoardCategory.class)) {
            LOOKUP.put(bc.value, bc);
        }
    }

    public static BoardCategory get(String value) {
        return LOOKUP.get(value);
    }
}
