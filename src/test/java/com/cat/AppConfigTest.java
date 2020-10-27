package com.cat;

import com.cat.service.MachineActionService;
import com.cat.service.MainService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppConfigTest {
    static ApplicationContext context;
    static MainService mainService;
    static MachineActionService actionService;

    @BeforeAll
    public static void init() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        mainService = context.getBean(MainService.class);
        actionService = context.getBean(MachineActionService.class);
    }

    @Test
    void testSomething() throws InterruptedException {
        mainService.startService();
        assertEquals(10898, actionService.getCompletedActionCount());
    }
}
