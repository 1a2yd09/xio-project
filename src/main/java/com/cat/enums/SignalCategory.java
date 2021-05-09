package com.cat.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author CAT
 */
@AllArgsConstructor
@Getter
public enum SignalCategory {
    /**
     * 启动
     */
    START("启动"),
    /**
     * 取板
     */
    TAKE_BOARD("取板"),
    /**
     * 下料
     */
    CUTTING("下料"),
    /**
     * 旋转
     */
    ROTATE("旋转"),
    /**
     * 动作
     */
    ACTION("动作"),
    /**
     * 停止
     */
    STOP("停止");

    private final String name;
}
