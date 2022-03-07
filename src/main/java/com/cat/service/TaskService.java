package com.cat.service;

import com.cat.mapper.SignalMapper;
import com.cat.pojo.ProcessControlSignal;
import com.cat.utils.SynUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.sql.SQLTimeoutException;

/**
 * @author CAT
 */
@Slf4j
@Service
public class TaskService {
    private final SignalMapper signalMapper;
    private final MainService mainService;
    private final ThreadPoolTaskExecutor executor;

    public TaskService(SignalMapper signalMapper, MainService mainService, @Qualifier("serviceTaskExecutor") ThreadPoolTaskExecutor executor) {
        this.signalMapper = signalMapper;
        this.mainService = mainService;
        this.executor = executor;
    }

    /**
     *  该任务主要是向数据库获取开始信号
     */
    @Scheduled(initialDelay = 1_000, fixedDelay = 1_000)
    public void checkStartControl() {
        try {
            // 从数据表tb_process_control_signal获取最新的process=0 And category = 1的记录
            ProcessControlSignal signal = this.signalMapper.getUnProcessedStartSignal();
            if (signal != null) {
                log.info("检测到新的流程控制信号到达...");
                // 成功获取到之后修改process=1
                signal.setProcessed(Boolean.TRUE);
                this.signalMapper.updateControlSignal(signal);
                // 开始信号队列START_SIGNAL_QUEUE放入开始信号，业务线程service-executor-1开始处理
                SynUtil.START_SIGNAL_QUEUE.put(1);
            }
        } catch (SQLTimeoutException | InterruptedException e) {
            log.error("检测流程控制信号异常...");
            Thread.currentThread().interrupt();
        }
    }

    @Scheduled(initialDelay = 1_000, fixedDelay = 3_000)
    public void checkServiceThreadState() {
        boolean isRunning = SynUtil.WORK_THREAD_RUNNING.get();
        if (!isRunning) {
            log.info("提交新的业务作业至业务线程池...");
            this.executor.execute(this.mainService::start);
        }
    }
}
