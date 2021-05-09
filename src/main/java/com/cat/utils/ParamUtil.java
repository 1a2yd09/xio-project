package com.cat.utils;

import com.cat.enums.OrderModule;
import com.cat.enums.OrderSortPattern;
import com.cat.pojo.OperatingParameter;
import com.cat.pojo.StockSpecification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author CAT
 */
public class ParamUtil {
    private static final OperatingParameter DEFAULT_PARAM = new OperatingParameter(-1L, LocalDate.now(), BigDecimal.ZERO, BigDecimal.ZERO, OrderSortPattern.SEQ.getName(), OrderModule.BOTTOM_PLATFORM.getName(), LocalDateTime.now());
    private static final StockSpecification DEFAULT_STOCK_SPEC = new StockSpecification(-1L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, LocalDateTime.now());

    private ParamUtil() {
    }

    /**
     * 获取一个默认的运行参数。
     *
     * @return 运行参数
     */
    public static OperatingParameter getDefaultParameter() {
        return DEFAULT_PARAM;
    }

    public static OperatingParameter getCommonParameter(OrderSortPattern orderSortPattern, OrderModule orderModule) {
        return new OperatingParameter(LocalDate.of(2019, 11, 13), new BigDecimal(192), new BigDecimal(100), orderSortPattern.getName(), orderModule.getName());
    }

    /**
     * 获取一个默认的库存件规格。
     *
     * @return 库存件规格
     */
    public static StockSpecification getDefaultStockSpec() {
        return DEFAULT_STOCK_SPEC;
    }
}
