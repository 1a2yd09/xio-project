package com.cat;

import com.cat.enums.OrderModule;
import com.cat.enums.OrderSortPattern;
import com.cat.service.ActionService;
import com.cat.service.MainService;
import com.cat.service.OrderService;
import com.cat.service.ParameterService;
import com.cat.utils.ParamUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest extends BaseTest {
    @Autowired
    MainService mainService;
    @Autowired
    ActionService actionService;
    @Autowired
    ParameterService parameterService;
    @Autowired
    OrderService orderService;

    @Transactional
    @Rollback
    @Test
    void testStart() throws InterruptedException {
        assertEquals(759, orderService.getAllProductionOrders().size());
        parameterService.insertOperatingParameter(ParamUtil.getCommonParameter(OrderSortPattern.BY_SEQ, OrderModule.BOTTOM_PLATFORM));
        mainService.start(OrderModule.BOTTOM_PLATFORM);
        parameterService.insertOperatingParameter(ParamUtil.getCommonParameter(OrderSortPattern.BY_SEQ, OrderModule.STRAIGHT_WEIGHT));
        mainService.start(OrderModule.STRAIGHT_WEIGHT);
        assertEquals(5353, actionService.getProcessedActionCount());
        assertEquals(0, orderService.getAllProductionOrders().size());
        assertEquals(759, orderService.getCompletedOrderCount());
    }

    @Disabled("Not for now")
    @Test
    void testNoRollbackStart() throws InterruptedException {
        assertEquals(759, orderService.getAllProductionOrders().size());
        parameterService.insertOperatingParameter(ParamUtil.getCommonParameter(OrderSortPattern.BY_SEQ, OrderModule.BOTTOM_PLATFORM));
        mainService.start(OrderModule.BOTTOM_PLATFORM);
        parameterService.insertOperatingParameter(ParamUtil.getCommonParameter(OrderSortPattern.BY_SEQ, OrderModule.STRAIGHT_WEIGHT));
        mainService.start(OrderModule.STRAIGHT_WEIGHT);
        assertEquals(5353, actionService.getProcessedActionCount());
        assertEquals(0, orderService.getAllProductionOrders().size());
        assertEquals(759, orderService.getCompletedOrderCount());
    }
}
