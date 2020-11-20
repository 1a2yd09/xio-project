package com.cat.util;

import com.cat.entity.OperatingParameter;
import com.cat.entity.StockSpecification;
import com.cat.entity.enums.OrderSortPattern;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ParamUtil {
    private ParamUtil() {
    }

    public static OperatingParameter getDefaultParameter() {
        return new OperatingParameter(LocalDate.now(), BigDecimal.ZERO, BigDecimal.ZERO, OrderSortPattern.BY_SEQ.value);
    }

    public static StockSpecification getDefaultStockSpec() {
        return new StockSpecification(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
