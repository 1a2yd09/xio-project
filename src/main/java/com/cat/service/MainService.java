package com.cat.service;

import com.cat.enums.OrderModule;
import com.cat.pojo.OperatingParameter;
import com.cat.utils.ModuleFactory;
import com.cat.utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author CAT
 */
@Slf4j
@Service
public class MainService {
    private final ParameterService parameterService;
    private final SignalService signalService;
    private final ModuleFactory moduleFactory;

    public MainService(ParameterService parameterService, SignalService signalService, ModuleFactory moduleFactory) {
        this.parameterService = parameterService;
        this.signalService = signalService;
        this.moduleFactory = moduleFactory;
    }

    public void start() {
        ThreadUtil.WORK_THREAD_RUNNING.set(true);
        try {
            this.signalService.waitingForNewProcessStartSignal();
            OperatingParameter param = this.parameterService.getLatestOperatingParameter();
            OrderModule orderModule = OrderModule.get(param.getOrderModule());
            this.moduleFactory.getModuleService(orderModule.name()).processOrderList(param);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ThreadUtil.WORK_THREAD_RUNNING.set(false);
        }
    }
}
