package com.cat;

import com.cat.entity.StartSignal;
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
        boolean flag = signalService.isReceivedNewStartSignal();
        assertFalse(flag);
        signalService.addNewStartSignal();
        StartSignal startSignal = signalService.getLatestStartSignal();
        System.out.println(startSignal);
        flag = signalService.isReceivedNewStartSignal();
        assertTrue(flag);
    }
}
