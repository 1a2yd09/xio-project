package com.cat.pojo.message;

import com.cat.pojo.CuttingSignal;
import com.cat.pojo.WorkOrder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author CAT
 */
@Data
public class OrderMessage {
    private WorkOrder order;
    private CuttingSignal signal;
    private LocalDateTime createdAt;

    public static OrderMessage of(WorkOrder order, CuttingSignal signal) {
        OrderMessage msg = new OrderMessage();
        msg.setOrder(order);
        msg.setSignal(signal);
        msg.setCreatedAt(LocalDateTime.now());
        return msg;
    }
}
