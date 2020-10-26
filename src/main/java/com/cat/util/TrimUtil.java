package com.cat.util;

import com.cat.entity.TrimmingValue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TrimUtil {
    private TrimUtil() {
    }

    public static TrimmingValue getDefaultValue() {
        return new TrimmingValue(-1, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, LocalDateTime.now());
    }
}
