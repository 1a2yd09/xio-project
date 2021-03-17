package com.cat;

import com.cat.enums.ForwardEdge;
import com.cat.pojo.CuttingSignal;
import com.cat.pojo.TakeBoardSignal;
import com.cat.service.SignalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SignalTest extends BaseTest {
    @Autowired
    SignalService signalService;

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

        signalService.insertCuttingSignal("2.5×1250×1589", ForwardEdge.SHORT, 3098528);
        cs = signalService.getLatestNotProcessedCuttingSignal();
        assertNotNull(cs);
        System.out.println(cs);

        cs = signalService.getLatestNotProcessedCuttingSignal();
        assertNull(cs);
    }
}
