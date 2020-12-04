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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
        ExecutorService es = new ThreadPoolExecutor(4, 10, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), r -> new Thread(r, "mailThreadPool-Thread-" + r.hashCode()));
        if (!BoardUtils.isFirstSpecGeSecondSpec(fakeCuttingSize, order.getProductSpecification())) {
            OrderErrorMsg msg = OrderErrorMsg.getInstance(order.getId(), fakeCuttingSize, order.getProductSpecification());
            es.submit(() -> mailService.sendOrderErrorMail(msg));
            orderService.updateOrderState(order, OrderState.INTERRUPTED);
        }
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        order = orderService.getOrderById(3098528);
        assertEquals(OrderState.INTERRUPTED.value, order.getOperationState());
    }
}
