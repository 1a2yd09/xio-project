package com.cat;

import com.cat.entity.MachineAction;
import com.cat.service.MachineActionService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ActionServiceTest {
    static ApplicationContext context;
    static MachineActionService machineActionService;

    @BeforeAll
    public static void init() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        machineActionService = context.getBean(MachineActionService.class);
    }

    @Test
    public void testDoneAllAction() {
        machineActionService.doneAllAction();
        List<MachineAction> list = machineActionService.getAllActions();
        for (MachineAction ma : list) {
            assertEquals(ma.getCompleted(), true);
        }
    }
}
