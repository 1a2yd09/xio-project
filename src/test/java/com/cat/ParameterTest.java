package com.cat;

import com.cat.entity.OperatingParameter;
import com.cat.entity.TrimmingValue;
import com.cat.service.ParameterService;
import com.cat.service.TrimmingValueService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ParameterTest {
    static ApplicationContext context;
    static ParameterService parameterService;
    static TrimmingValueService trimmingValueService;

    @BeforeAll
    static void init() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        parameterService = context.getBean(ParameterService.class);
        trimmingValueService = context.getBean(TrimmingValueService.class);
    }

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
