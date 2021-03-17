package com.cat.service;

import com.cat.enums.ActionState;
import com.cat.enums.ControlSignalCategory;
import com.cat.mapper.ActionMapper;
import com.cat.mapper.SignalMapper;
import com.cat.pojo.CuttingSignal;
import com.cat.pojo.ProcessControlSignal;
import com.cat.utils.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    SignalMapper signalMapper;
    @Autowired
    ActionMapper actionMapper;

    @Scheduled(initialDelay = 1_000, fixedDelay = 1_000)
    public void checkNewControlMessage() throws InterruptedException {
        ProcessControlSignal signal = this.signalMapper.getLatestNotProcessedControlSignal();
        if (signal != null) {
            logger.info("检测到新的控制信号写入...");
            signal.setProcessed(Boolean.TRUE);
            this.signalMapper.updateControlSignal(signal);
            if (ControlSignalCategory.START.value.equals(signal.getCategory())) {
                ThreadUtil.getStartControlMessageQueue().put(signal.getCategory());
            } else {
                ThreadUtil.getStopControlMessageQueue().put(signal.getCategory());
            }
        }
    }

    @Scheduled(initialDelay = 1_000, fixedDelay = 1_000)
    public void checkNewCuttingMessage() throws InterruptedException {
        CuttingSignal signal = this.signalMapper.getLatestNotProcessedCuttingSignal();
        if (signal != null) {
            signal.setProcessed(Boolean.TRUE);
            this.signalMapper.updateCuttingSignal(signal);
            logger.info("检测到新的下料信号写入...");
            ThreadUtil.getCuttingMessageQueue().put(signal);
        }
    }

    @Scheduled(initialDelay = 1_000, fixedDelay = 1_000)
    public void checkNewActionDoneMessage() throws InterruptedException {
        String state = actionMapper.getFinalMachineActionState();
        if (state != null && !ActionState.INCOMPLETE.value.equals(state)) {
            logger.info("监控到所有动作都被处理完毕...");
            ThreadUtil.getActionProcessedMessageQueue().put(state);
        }
    }
}
