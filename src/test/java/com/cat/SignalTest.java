package com.cat;

import com.cat.entity.signal.CuttingSignal;
import com.cat.entity.signal.StartSignal;
import com.cat.entity.signal.TakeBoardSignal;
import com.cat.service.SignalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@Rollback
class SignalTest extends BaseTest {
    @Autowired
    SignalService signalService;

    @Test
    void testReceiveNewSignal() {
        boolean flag = signalService.isReceivedNewStartSignal();
        assertFalse(flag);
        signalService.insertStartSignal();
        StartSignal startSignal = signalService.getLatestNotProcessedStartSignal();
        System.out.println(startSignal);
        flag = signalService.isReceivedNewStartSignal();
        assertTrue(flag);
    }

    @Test
    void testTakeBoardSignal() {
        signalService.insertTakeBoardSignal(3098528);
        TakeBoardSignal tbs = signalService.getLatestTakeBoardSignal();
        assertNotNull(tbs);
        System.out.println(tbs);
    }

    @Test
    void testCuttingSignal() {
        CuttingSignal cs = signalService.getLatestNotProcessedCuttingSignal();
        assertNull(cs);

        signalService.insertCuttingSignal("2.5×1250×1589", 0, 3098528);
        cs = signalService.getLatestNotProcessedCuttingSignal();
        assertNotNull(cs);
        System.out.println(cs);

        cs = signalService.getLatestNotProcessedCuttingSignal();
        assertNull(cs);
    }
}
