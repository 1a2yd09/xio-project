package com.cat.service;

import com.cat.enums.OrderModule;
import com.cat.pojo.OperatingParameter;
import com.cat.pojo.message.OrderMessage;
import com.cat.utils.ModuleServiceFactory;
import com.cat.utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author CAT
 */
@Slf4j
@Service
public class MainService {
    public static final AtomicReference<OrderMessage> RUNNING_ORDER = new AtomicReference<>(null);

    private final ParameterService parameterService;
    private final SignalService signalService;
    private final ModuleServiceFactory moduleServiceFactory;
    private final MailService mailService;

    public MainService(ParameterService parameterService, SignalService signalService, ModuleServiceFactory moduleServiceFactory, MailService mailService) {
        this.parameterService = parameterService;
        this.signalService = signalService;
        this.moduleServiceFactory = moduleServiceFactory;
        this.mailService = mailService;
    }

    public void start() {
        ThreadUtil.WORK_THREAD_RUNNING.set(true);
        try {
            this.signalService.checkStartSignal();
            OperatingParameter param = this.parameterService.getLatestOperatingParameter();
            OrderModule orderModule = OrderModule.get(param.getOrderModule());
            this.moduleServiceFactory.getModuleService(orderModule.name()).processOrderList(param);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            this.mailService.sendWorkErrorMail(RUNNING_ORDER.get());
            ThreadUtil.WORK_THREAD_RUNNING.set(false);
        }
    }
}
