package com.cat.enums;

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
    WEIGHT("对重架工地模块");

    public final String value;

    OrderModule(String value) {
        this.value = value;
    }
}
