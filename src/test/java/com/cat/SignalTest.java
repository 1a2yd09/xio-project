package com.cat;

import com.cat.enums.ControlSignalCategory;
import com.cat.enums.ForwardEdge;
import com.cat.pojo.CuttingSignal;
import com.cat.pojo.ProcessControlSignal;
import com.cat.pojo.TakeBoardSignal;
import com.cat.service.SignalService;
import com.cat.utils.SynUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SignalTest extends BaseTest {
    @Autowired
    SignalService signalService;

    /**
     * 新增取板信号，状态为未处理。
     */
    @Test
    void testTakeBoardSignal() {
        signalService.insertTakeBoardSignal(20000000);
        TakeBoardSignal tbs = signalService.getLatestTakeBoardSignal();
        assertNotNull(tbs);
        System.out.println(tbs);
    }

    /**
     * 插入下料信号，获取最新未被处理的下料信号，获取后下料信号状态变为已处理。
     */
    @Test
    void testCuttingSignal() {
        CuttingSignal cs = signalService.getLatestUnProcessedCuttingSignal();
        assertNull(cs);

        signalService.insertCuttingSignal("4×1500×3600", ForwardEdge.SHORT, BigDecimal.ZERO, 20000000);
        cs = signalService.getLatestUnProcessedCuttingSignal();
        assertNotNull(cs);
        assertEquals(Boolean.TRUE, cs.getProcessed());
        System.out.println(cs);

        cs = signalService.getLatestUnProcessedCuttingSignal();
        assertNull(cs);
    }

    /**
     * 未收到流程启动信号前，从同步队列获取将获取到空对象，收到流程启动信号后，从同步队列获取将获取到启动信号对象。
     */
    @Test
    void testStartSignal() throws InterruptedException {
        Integer state = SynUtil.getStartControlMessageQueue().poll();
        assertNull(state);
        signalService.insertProcessControlSignal(ControlSignalCategory.START);
        state = SynUtil.getStartControlMessageQueue().take();
        assertNotNull(state);
        ProcessControlSignal signal = signalService.getLatestControlSignal();
        assertEquals(Boolean.TRUE, signal.getProcessed());
        System.out.println(signal);
    }

    /**
     * 未收到流程停止信号前，从同步队列获取将获取到空对象，收到流程停止信号后，从同步队列获取将获取到停止信号对象。
     */
    @Test
    void testStopSignal() throws InterruptedException {
        Integer state = SynUtil.getStopControlMessageQueue().poll();
        assertNull(state);
        signalService.insertProcessControlSignal(ControlSignalCategory.STOP);
        state = SynUtil.getStopControlMessageQueue().take();
        assertNotNull(state);
        ProcessControlSignal signal = signalService.getLatestControlSignal();
        assertEquals(Boolean.TRUE, signal.getProcessed());
        System.out.println(signal);
    }
}
