package com.cat;

import com.cat.entity.param.OperatingParameter;
import com.cat.service.ParameterService;
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
}
