package com.cat;

import com.cat.entity.enums.SignalCategory;
import com.cat.service.SignalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@Rollback
class SignalTest extends BaseTest {
    @Autowired
    SignalService signalService;

    @Test
    void testReceiveNewSignal() {
        boolean flag = signalService.isReceivedNewSignal(SignalCategory.ACTION);
        assertFalse(flag);
        signalService.addNewSignal(SignalCategory.ACTION);
        flag = signalService.isReceivedNewSignal(SignalCategory.ACTION);
        assertTrue(flag);
    }
}
