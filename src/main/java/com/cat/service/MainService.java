package com.cat.service;

import com.cat.enums.ControlSignalCategory;
import com.cat.enums.OrderModule;
import com.cat.utils.ModuleServiceFactory;
import com.cat.utils.SynUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author CAT
 */
@Slf4j
@Service
public class MainService {
    private final SignalService signalService;
    private final ModuleServiceFactory moduleServiceFactory;

    public MainService(SignalService signalService, ModuleServiceFactory moduleServiceFactory) {
        this.signalService = signalService;
        this.moduleServiceFactory = moduleServiceFactory;
    }

    /**
     *
     */
    public void start() {
        SynUtil.WORK_THREAD_RUNNING.set(true);
        try {
            // 测试用，假定斯科奇已放入数据到数据库
            this.signalService.insertProcessControlSignal(ControlSignalCategory.START);
            // 获取START_SIGNAL_QUEUE的信号，没有则阻塞
            this.signalService.checkStartSignal();
            // 开始处理
            this.moduleServiceFactory.getModuleService(OrderModule.STRAIGHT_WEIGHT.name()).process();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            SynUtil.WORK_THREAD_RUNNING.set(false);
        }
    }
}
