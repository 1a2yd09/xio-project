package com.cat;

import com.cat.pojo.OrderErrorMsg;
import com.cat.pojo.WorkOrder;
import com.cat.service.MailService;
import com.cat.service.OrderService;
import com.cat.utils.BoardUtil;
import com.cat.utils.ThreadUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MailTest extends BaseTest {
    @Autowired
    MailService mailService;
    @Autowired
    OrderService orderService;

    @Disabled("The mail function does not require repeated tests.")
    @Test
    void testSendMail() {
        // product: 2.5×309×1016
        WorkOrder order = orderService.getOrderById(3098528);
        String fakeCuttingSize = "2.5×300×1000";
        ExecutorService es = ThreadUtil.EMAIL_POOL;
        if (!BoardUtil.isFirstSpecGeSecondSpec(fakeCuttingSize, order.getProductSpecification())) {
            OrderErrorMsg msg = OrderErrorMsg.getInstance(order.getId(), fakeCuttingSize, order.getProductSpecification());
            es.submit(() -> mailService.sendOrderErrorMail(msg));
        }
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        order = orderService.getOrderById(3098528);
        assertNotNull(order);
    }
}
