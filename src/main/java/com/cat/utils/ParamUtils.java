package com.cat.utils;

import com.cat.entity.param.OperatingParameter;
import com.cat.entity.param.StockSpecification;
import com.cat.enums.OrderModule;
import com.cat.enums.OrderSortPattern;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author CAT
 */
public class ParamUtils {
    private ParamUtils() {
    }

    /**
     * 获取一个默认的运行参数。
     *
     * @return 运行参数
     */
    public static OperatingParameter getDefaultParameter() {
        return new OperatingParameter(-1L, LocalDate.now(), BigDecimal.ZERO, BigDecimal.ZERO, OrderSortPattern.BY_SEQ.value, OrderModule.BOTTOM_PLATFORM.value, LocalDateTime.now());
    }

    public static OperatingParameter getCommonParameter(OrderSortPattern orderSortPattern, OrderModule orderModule) {
        return new OperatingParameter(LocalDate.of(2019, 11, 13), new BigDecimal(192), new BigDecimal(100), orderSortPattern.value, orderModule.value);
    }

    /**
     * 获取一个默认的库存件规格。
     *
     * @return 库存件规格
     */
    public static StockSpecification getDefaultStockSpec() {
        return new StockSpecification(-1L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, LocalDateTime.now());
    }
}
