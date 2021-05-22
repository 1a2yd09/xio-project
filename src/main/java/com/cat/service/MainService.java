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

    public void start() {
        SynUtil.WORK_THREAD_RUNNING.set(true);
        try {
            // test:
            this.signalService.insertProcessControlSignal(ControlSignalCategory.START);
            this.signalService.checkStartSignal();
            this.moduleServiceFactory.getModuleService(OrderModule.STRAIGHT_WEIGHT.name()).process();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            SynUtil.WORK_THREAD_RUNNING.set(false);
        }
    }
}
