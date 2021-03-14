package com.cat.service;

import com.cat.entity.signal.CuttingSignal;
import com.cat.entity.signal.ProcessControlSignal;
import com.cat.enums.ActionState;
import com.cat.enums.ControlSignalCategory;
import com.cat.mapper.ActionMapper;
import com.cat.mapper.SignalMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Service
public class TaskService {
    final Logger logger = LoggerFactory.getLogger(getClass());

    public static BlockingQueue<ProcessControlSignal> START_CONTROL_MESSAGE_QUEUE = new ArrayBlockingQueue<>(1);
    public static BlockingQueue<ProcessControlSignal> STOP_CONTROL_MESSAGE_QUEUE = new ArrayBlockingQueue<>(1);
    public static BlockingQueue<CuttingSignal> CUTTING_MESSAGE_QUEUE = new ArrayBlockingQueue<>(1);
    public static BlockingQueue<Integer> ACTION_DONE_MESSAGE_QUEUE = new ArrayBlockingQueue<>(1);

    @Autowired
    SignalMapper signalMapper;
    @Autowired
    ActionMapper actionMapper;

    @Scheduled(initialDelay = 1_000, fixedDelay = 1_000)
    public void checkNewControlMessage() {
        ProcessControlSignal signal = this.signalMapper.getLatestNotProcessedControlSignal();
        if (signal != null) {
            logger.info("检测到新的控制信号写入...");
            signal.setProcessed(Boolean.TRUE);
            this.signalMapper.updateControlSignal(signal);
            if (ControlSignalCategory.START.value.equals(signal.getCategory())) {
                START_CONTROL_MESSAGE_QUEUE.offer(signal);
            } else {
                STOP_CONTROL_MESSAGE_QUEUE.offer(signal);
            }
        }
    }

    @Scheduled(initialDelay = 1_000, fixedDelay = 1_000)
    public void checkNewCuttingMessage() {
        CuttingSignal signal = this.signalMapper.getLatestNotProcessedCuttingSignal();
        if (signal != null) {
            signal.setProcessed(Boolean.TRUE);
            this.signalMapper.updateCuttingSignal(signal);
            logger.info("检测到新的下料信号写入...");
            CUTTING_MESSAGE_QUEUE.offer(signal);
        }
    }

    @Scheduled(initialDelay = 1_000, fixedDelay = 1_000)
    public void checkNewActionDoneMessage() {
        String state = actionMapper.getFinalMachineActionState();
        if (ActionState.COMPLETED.value.equals(state)) {
            logger.info("检测到新的动作完成信号写入...");
            ACTION_DONE_MESSAGE_QUEUE.offer(1);
        }
    }
}
