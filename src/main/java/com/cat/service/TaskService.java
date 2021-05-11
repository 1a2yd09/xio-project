package com.cat.service;

import com.cat.enums.ControlSignalCategory;
import com.cat.mapper.SignalMapper;
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
    private final MainService mainService;

    public TaskService(SignalMapper signalMapper, MainService mainService) {
        this.signalMapper = signalMapper;
        this.mainService = mainService;
    }

    @Scheduled(initialDelay = 1_000, fixedDelay = 3_000)
    public void checkNewControlMessage() throws InterruptedException {
        ProcessControlSignal signal = this.signalMapper.getLatestUnProcessedControlSignal();
        if (signal != null) {
            log.info("检测到新的流程控制信号到达...");
            signal.setProcessed(Boolean.TRUE);
            this.signalMapper.updateControlSignal(signal);
            Integer category = signal.getCategory();
            if (ControlSignalCategory.START.value.equals(category)) {
                ThreadUtil.getStartControlMessageQueue().put(category);
            } else {
                ThreadUtil.getStopControlMessageQueue().put(category);
            }
        }
    }

    @Scheduled(initialDelay = 1_000, fixedDelay = 60_000)
    public void checkMainThreadState() {
        boolean isRunning = ThreadUtil.WORK_THREAD_RUNNING.get();
        log.info("工作流程是否正常: {}", isRunning);
        if (!isRunning) {
            log.info("提交新的工作任务至业务线程池...");
            ThreadPoolFactory.getServiceThreadPool().execute(this.mainService::start);
        }
    }
}
