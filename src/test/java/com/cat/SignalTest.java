package com.cat;

import com.cat.pojo.CuttingSignal;
import com.cat.pojo.TakeBoardSignal;
import com.cat.enums.ForwardEdge;
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

//    @Test
//    void testProcessStopSignal() {
//        assertFalse(signalService.isReceivedNewProcessControlSignal(ControlSignalCategory.STOP));
//        signalService.insertProcessControlSignal(ControlSignalCategory.STOP);
//        ProcessControlSignal controlSignal = signalService.getLatestNotProcessedControlSignal(ControlSignalCategory.STOP);
//        System.out.println(controlSignal);
//        assertTrue(signalService.isReceivedNewProcessControlSignal(ControlSignalCategory.STOP));
//    }
//
//    @Test
//    void testProcessStartSignal() {
//        assertFalse(signalService.isReceivedNewProcessControlSignal(ControlSignalCategory.START));
//        signalService.insertProcessControlSignal(ControlSignalCategory.START);
//        ProcessControlSignal controlSignal = signalService.getLatestNotProcessedControlSignal(ControlSignalCategory.START);
//        System.out.println(controlSignal);
//        assertTrue(signalService.isReceivedNewProcessControlSignal(ControlSignalCategory.START));
//    }

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
