package com.cat.util;

import com.cat.entity.OperatingParameter;
import com.cat.entity.enums.BottomSortPatternEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ParameterUtil {
    private ParameterUtil() {
    }

    public static OperatingParameter getDefaultParameter() {
        return new OperatingParameter(-1, LocalDate.now(), BigDecimal.ZERO, BigDecimal.ZERO, BottomSortPatternEnum.SEQ.value, LocalDateTime.now());
    }
}
