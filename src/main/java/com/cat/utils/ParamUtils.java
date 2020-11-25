package com.cat.utils;

import com.cat.entity.param.OperatingParameter;
import com.cat.entity.param.StockSpecification;
import com.cat.enums.OrderSortPattern;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ParamUtils {
    private ParamUtils() {
    }

    public static OperatingParameter getDefaultParameter() {
        return new OperatingParameter(-1L, LocalDate.now(), BigDecimal.ZERO, BigDecimal.ZERO, OrderSortPattern.BY_SEQ.value, LocalDateTime.now());
    }

    public static StockSpecification getDefaultStockSpec() {
        return new StockSpecification(-1L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, LocalDateTime.now());
    }
}
