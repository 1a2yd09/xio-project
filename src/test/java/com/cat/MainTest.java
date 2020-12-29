package com.cat;

import com.cat.enums.OrderModule;
import com.cat.enums.OrderSortPattern;
import com.cat.service.ActionService;
import com.cat.service.MainService;
import com.cat.service.ParameterService;
import com.cat.utils.ParamUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@Rollback
class MainTest extends BaseTest {
    @Autowired
    MainService mainService;
    @Autowired
    ActionService actionService;
    @Autowired
    ParameterService parameterService;

    @Test
    void testStart() throws InterruptedException {
        parameterService.insertOperatingParameter(ParamUtils.getCommonParameter(OrderSortPattern.BY_SEQ, OrderModule.BOTTOM_PLATFORM));
        mainService.start(OrderModule.BOTTOM_PLATFORM);
        parameterService.insertOperatingParameter(ParamUtils.getCommonParameter(OrderSortPattern.BY_SEQ, OrderModule.STRAIGHT_WEIGHT));
        mainService.start(OrderModule.STRAIGHT_WEIGHT);
        assertEquals(5353, actionService.getProcessedActionCount());
    }
}
