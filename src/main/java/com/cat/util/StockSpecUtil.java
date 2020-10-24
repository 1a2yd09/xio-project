package com.cat.util;

import com.cat.entity.StockSpecification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockSpecUtil {
    private StockSpecUtil() {

    }

    public static StockSpecification getEmptyStockSpec() {
        return new StockSpecification(-1, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, LocalDateTime.now());
    }
}
