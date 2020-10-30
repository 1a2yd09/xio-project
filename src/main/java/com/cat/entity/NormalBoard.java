package com.cat.entity;

import com.cat.entity.enums.BoardCategory;

import java.math.BigDecimal;

public class NormalBoard extends AbstractBoard {
    public NormalBoard() {
    }

    public NormalBoard(BigDecimal height, BigDecimal width, BigDecimal length, String material, BoardCategory category) {
        super(height, width, length, material, category);
    }

    public NormalBoard(String specification, String material, BoardCategory category) {
        super(specification, material, category);
    }
}
