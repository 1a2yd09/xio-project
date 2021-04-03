package com.cat.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author CAT
 */
@AllArgsConstructor
@Getter
public enum OrderSortPattern {
    /**
     * 按批次号排序后再按顺序号排序
     */
    PCH_SEQ("批次顺序"),
    /**
     * 按批次号排序后再按成品规格排序
     */
    PCH_SPEC("批次规格"),
    /**
     * 按顺序号排序
     */
    SEQ("顺序"),
    /**
     * 按成品规格排序
     */
    SPEC("规格");

    private final String name;

    private static final Map<String, OrderSortPattern> LOOKUP = new HashMap<>(6);

    static {
        for (OrderSortPattern sortPattern : OrderSortPattern.values()) {
            LOOKUP.put(sortPattern.name, sortPattern);
        }
    }

    public static OrderSortPattern get(String name) {
        return LOOKUP.get(name);
    }
}
