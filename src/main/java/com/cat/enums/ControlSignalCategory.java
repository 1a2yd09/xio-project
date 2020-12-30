package com.cat.enums;

/**
 * @author CAT
 */
public enum ControlSignalCategory {
    /**
     * 启动信号值
     */
    START(1),
    /**
     * 暂停信号值
     */
    STOP(0);

    public final Integer value;

    ControlSignalCategory(Integer value) {
        this.value = value;
    }
}
