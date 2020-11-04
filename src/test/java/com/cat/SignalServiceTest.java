package com.cat;

import com.cat.entity.enums.SignalCategoryEnum;
import com.cat.service.SignalService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SignalServiceTest {
    static ApplicationContext context;
    static SignalService signalService;

    @BeforeAll
    public static void init() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        signalService = context.getBean(SignalService.class);
    }

    @Test
    void testReceiveNewSignal() {
        boolean flag = signalService.isReceivedNewSignal(SignalCategoryEnum.ACTION);
        assertFalse(flag);
        signalService.addNewSignal(SignalCategoryEnum.ACTION);
        flag = signalService.isReceivedNewSignal(SignalCategoryEnum.ACTION);
        assertTrue(flag);
    }
}
