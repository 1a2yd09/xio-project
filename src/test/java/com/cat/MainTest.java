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

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled("TODO")
class MainTest extends BaseTest {
    @Autowired
    MainService mainService;
    @Autowired
    ActionService actionService;
    @Autowired
    ParameterService parameterService;
    @Autowired
    OrderService orderService;

    @Test
    void testStart() throws InterruptedException {
        assertEquals(759, orderService.getAllProductionOrders().size());
        parameterService.insertOperatingParameter(ParamUtil.getCommonParameter(OrderSortPattern.SEQ, OrderModule.BOTTOM_PLATFORM));
        mainService.start();
        parameterService.insertOperatingParameter(ParamUtil.getCommonParameter(OrderSortPattern.SEQ, OrderModule.STRAIGHT_WEIGHT));
        mainService.start();
        assertEquals(0, orderService.getAllProductionOrders().size());
        assertEquals(759, orderService.getCompletedOrderCount());
    }
}
