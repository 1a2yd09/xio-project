package com.cat;

import com.cat.entity.bean.WorkOrder;
import com.cat.entity.message.OrderErrorMsg;
import com.cat.enums.OrderState;
import com.cat.service.MailService;
import com.cat.service.OrderService;
import com.cat.utils.BoardUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MailTest extends BaseTest {
    @Autowired
    MailService mailService;
    @Autowired
    OrderService orderService;

    @Test
    @Transactional
    @Rollback
    void testSendMail() {
        // product: 2.5×309×1016
        WorkOrder order = orderService.getOrderById(3098528);
        String fakeCuttingSize = "2.5×300×1000";
        if (!BoardUtils.isFirstSpecGeSecondSpec(fakeCuttingSize, order.getProductSpecification())) {
            OrderErrorMsg msg = OrderErrorMsg.getInstance(order.getId(), fakeCuttingSize, order.getProductSpecification());
            mailService.sendOrderErrorMail(msg);
            orderService.updateOrderState(order, OrderState.INTERRUPTED);
        }
        order = orderService.getOrderById(3098528);
        assertEquals(OrderState.INTERRUPTED.value, order.getOperationState());
    }
}
