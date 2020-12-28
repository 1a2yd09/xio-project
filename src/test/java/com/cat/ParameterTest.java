package com.cat;

import com.cat.entity.param.OperatingParameter;
import com.cat.enums.OrderModule;
import com.cat.enums.OrderSortPattern;
import com.cat.service.ParameterService;
import com.cat.utils.ParamUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ParameterTest extends BaseTest {
    @Autowired
    ParameterService parameterService;

    @Test
    void testGetParameter() {
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        assertNotNull(op);
        System.out.println(op);
    }

    @Test
    void testInsertParameter() {
        parameterService.insertOperatingParameter(ParamUtils.getCommonParameter(OrderSortPattern.BY_SPEC, OrderModule.STRAIGHT_WEIGHT));
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        assertNotNull(op);
        System.out.println(op);
    }
}
