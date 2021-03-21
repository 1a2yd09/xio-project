package com.cat.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @author CAT
 */

public enum OrderModule {
    /**
     * 轿底吊顶工地模块
     */
    BOTTOM("轿底吊顶工地模块"),
    /**
     * 直梁工地模块
     */
    STRAIGHT("直梁工地模块"),
    /**
     * 对重架工地模块
     */
    WEIGHT("对重架工地模块"),
    /**
     * 轿底平台工单集合，包含轿底吊顶工地模块
     */
    BOTTOM_PLATFORM("轿底平台"),
    /**
     * 直梁对重工单集合，包含直梁工地模块和对重架工地模块
     */
    STRAIGHT_WEIGHT("直梁对重");

    private static final Map<String, OrderModule> LOOKUP = new HashMap<>(2);
    public final String value;

    OrderModule(String value) {
        this.value = value;
    }

    static {
        LOOKUP.put(BOTTOM_PLATFORM.value, BOTTOM_PLATFORM);
        LOOKUP.put(STRAIGHT_WEIGHT.value, STRAIGHT_WEIGHT);
    }

    public static OrderModule get(String value) {
        return LOOKUP.get(value);
    }
}
