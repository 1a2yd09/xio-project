package com.cat;

import com.cat.service.ActionService;
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
    ActionService actionService;

    @Test
    void testSomething() throws InterruptedException {
        mainService.start();
        assertEquals(7381, actionService.getProcessedActionCount());
    }
}
