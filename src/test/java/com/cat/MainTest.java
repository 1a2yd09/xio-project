package com.cat;

import com.cat.service.MachineActionService;
import com.cat.service.MainService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@Rollback
class MainTest extends BaseTest {
    @Autowired
    MainService mainService;
    @Autowired
    MachineActionService actionService;

    @Test
    void testSomething() throws InterruptedException {
        mainService.startService();
        assertEquals(10898, actionService.getCompletedActionCount());
    }
}
