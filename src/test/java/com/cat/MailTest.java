package com.cat;

import com.cat.pojo.OrderErrorMessage;
import com.cat.pojo.WorkOrder;
import com.cat.service.MailService;
import com.cat.service.OrderService;
import com.cat.utils.BoardUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("TODO")
class MailTest extends BaseTest {
    @Autowired
    MailService mailService;
    @Autowired
    OrderService orderService;

    @Test
    void testSendMail() {
        // product: 2.5×309×1016
        WorkOrder order = orderService.getOrderById(3098528);
        String fakeCuttingSize = "2.5×300×1000";
        ExecutorService es = new ThreadPoolExecutor(5, 10, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(5), r -> new Thread(r, "EmailPool-" + r.hashCode()));
        if (!BoardUtil.isFirstSpecGeSecondSpec(fakeCuttingSize, order.getProductSpecification())) {
            OrderErrorMessage msg = OrderErrorMessage.getInstance(order.getId(), fakeCuttingSize, order.getProductSpecification());
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
