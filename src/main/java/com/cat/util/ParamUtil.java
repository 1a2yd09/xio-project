package com.cat.util;

import com.cat.entity.OperatingParameter;
import com.cat.entity.StockSpecification;
import com.cat.entity.enums.OrderSortPattern;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ParamUtil {
    private ParamUtil() {
    }

    public static OperatingParameter getDefaultParameter() {
        return new OperatingParameter(-1L, LocalDate.now(), BigDecimal.ZERO, BigDecimal.ZERO, OrderSortPattern.BY_SEQ.value, LocalDateTime.now());
    }

    public static StockSpecification getDefaultStockSpec() {
        return new StockSpecification(-1L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, LocalDateTime.now());
    }
}
