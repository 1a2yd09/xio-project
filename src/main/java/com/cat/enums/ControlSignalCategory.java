package com.cat.enums;

/**
 * @author CAT
 */
public enum ControlSignalCategory {
    /**
     * 流程启动
     */
    START(1),
    /**
     * 流程暂停
     */
    STOP(0);

    public final Integer value;

    ControlSignalCategory(Integer value) {
        this.value = value;
    }
}
