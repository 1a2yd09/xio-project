package com.cat;

import com.cat.enums.OrderModule;
import com.cat.enums.OrderSortPattern;
import com.cat.pojo.OperatingParameter;
import com.cat.service.ParameterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ParameterTest extends BaseTest {
    @Autowired
    ParameterService parameterService;

    /**
     * 如果参数表为空，将返回一个默认的参数对象，如果参数表不为空，则获取最新的参数对象。
     */
    @Test
    void testGetParameter() {
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        assertNotNull(op);
        System.out.println(op);
        parameterService.insertOperatingParameter(new OperatingParameter(LocalDate.now(), new BigDecimal("192"), new BigDecimal("100"), OrderSortPattern.PCH_SEQ.getName(), OrderModule.BOTTOM_PLATFORM.getName()));
        op = parameterService.getLatestOperatingParameter();
        assertNotNull(op);
        System.out.println(op);
    }
}
