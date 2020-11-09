package com.cat;

import com.cat.entity.OperatingParameter;
import com.cat.entity.TrimmingValue;
import com.cat.service.ParameterService;
import com.cat.service.TrimmingValueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ParameterTest extends BaseTest {
    @Autowired
    ParameterService parameterService;
    @Autowired
    TrimmingValueService trimmingValueService;

    @Test
    void testGetParameter() {
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        assertNotNull(op);
        System.out.println(op);
    }

    @Test
    void testGetTrimmingValue() {
        TrimmingValue tv = trimmingValueService.getLatestTrimmingValue();
        assertNotNull(tv);
        System.out.println(tv);
    }
}
