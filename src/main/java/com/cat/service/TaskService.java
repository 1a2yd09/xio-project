package com.cat.service;

import com.cat.enums.ActionState;
import com.cat.enums.ControlSignalCategory;
import com.cat.mapper.ActionMapper;
import com.cat.mapper.SignalMapper;
import com.cat.pojo.CuttingSignal;
import com.cat.pojo.ProcessControlSignal;
import com.cat.utils.ThreadPoolFactory;
import com.cat.utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author CAT
 */
@Slf4j
@Service
public class TaskService {
    private final SignalMapper signalMapper;
    private final ActionMapper actionMapper;

    private final MainService mainService;

    public TaskService(SignalMapper signalMapper, ActionMapper actionMapper, MainService mainService) {
        this.signalMapper = signalMapper;
        this.actionMapper = actionMapper;
        this.mainService = mainService;
    }

    @Scheduled(initialDelay = 1_000, fixedDelay = 1_000)
    public void checkNewControlMessage() throws InterruptedException {
        ProcessControlSignal signal = this.signalMapper.getLatestNotProcessedControlSignal();
        if (signal != null) {
            log.info("检测到新的流程控制信号到达...");
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
            log.info("检测到新的下料信号到达...");
            signal.setProcessed(Boolean.TRUE);
            this.signalMapper.updateCuttingSignal(signal);
            ThreadUtil.getCuttingMessageQueue().put(signal);
        }
    }

    @Scheduled(initialDelay = 1_000, fixedDelay = 1_000)
    public void checkNewActionDoneMessage() throws InterruptedException {
        String state = actionMapper.getFinalMachineActionState();
        if (state != null && !ActionState.INCOMPLETE.value.equals(state)) {
            log.info("检测到所有动作都被处理完毕...");
            ThreadUtil.getActionProcessedMessageQueue().put(state);
        }
    }

    @Scheduled(initialDelay = 7_000, fixedDelay = 3_000)
    public void checkMainThreadState() {
        boolean workThreadRunning = ThreadUtil.WORK_THREAD_RUNNING.get();
        log.info("工作流程是否正常: {}", workThreadRunning);
        if (!workThreadRunning) {
            log.info("提交新的工作任务至线程池");
            ThreadPoolFactory.getDefaultThreadPool().execute(mainService::start);
        }
    }
}
