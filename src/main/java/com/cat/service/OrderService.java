package com.cat.service;

import com.cat.dao.InventoryDao;
import com.cat.dao.OrderDao;
import com.cat.entity.bean.Inventory;
import com.cat.entity.bean.WorkOrder;
import com.cat.enums.BoardCategory;
import com.cat.enums.OrderSortPattern;
import com.cat.enums.OrderState;
import com.cat.utils.BoardUtils;
import com.cat.utils.OrderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OrderService {
    @Autowired
    OrderDao orderDao;
    @Autowired
    InventoryDao inventoryDao;

    public void addOrderCompletedAmount(WorkOrder order, int amount) {
        order.setCompletedAmount(OrderUtils.addAmountPropWithInt(order.getCompletedAmount(), amount));
        this.orderDao.updateOrderCompletedAmount(order);

        if (order.getUnfinishedAmount() == 0) {
            this.updateOrderState(order, OrderState.COMPLETED);
        }
    }

    public List<WorkOrder> getPreprocessNotBottomOrders(LocalDate date) {
        List<WorkOrder> orders = this.getNotBottomOrders(date);
        Map<String, Inventory> inventoryMap = this.inventoryDao.getInventories(BoardCategory.STOCK.value)
                .stream()
                .collect(Collectors.toMap(Inventory::getSpecStr, Function.identity()));

        for (WorkOrder order : orders) {
            // 使用标准规格格式来获取指定的存货对象:
            Inventory inventory = inventoryMap.get(BoardUtils.getStandardSpecStr(order.getSpecification()));
            if (inventory != null && inventory.getMaterial().equals(order.getMaterial()) && inventory.getAmount() > 0) {
                // 此次用作成品的库存件数量是工单未完成数量和库存件数量当中的最小值:
                int usedInventoryNum = Math.min(order.getUnfinishedAmount(), inventory.getAmount());
                // 使用库存件作为成品属于工单开工的行为之一:
                this.updateOrderState(order, OrderState.ALREADY_STARTED);
                this.addOrderCompletedAmount(order, usedInventoryNum);
                // 直接在这里就将库存件数目写回数据表的理由是，不是每种库存件都会被获取，另外获取之后全部被用作成品，数目归零后不会再进入该逻辑:
                inventory.setAmount(inventory.getAmount() - usedInventoryNum);
                this.inventoryDao.updateInventoryAmount(inventory);
            }
        }

        return orders.stream().filter(order -> order.getUnfinishedAmount() > 0).collect(Collectors.toList());
    }

    public List<WorkOrder> getBottomOrders(String sortPattern, LocalDate date) {
        List<WorkOrder> orders = this.orderDao.getBottomOrders(date);
        if (OrderSortPattern.BY_SPEC.value.equals(sortPattern)) {
            // 如果要求按照规格排序，指的是”依次“按照成品的厚度、宽度、长度降序排序，三者都相同则按工单ID升序排序:
            orders.sort((o1, o2) -> {
                int retVal = BoardUtils.compareTwoSpecStr(o1.getSpecification(), o2.getSpecification());
                return retVal != 0 ? retVal : o1.getId() - o2.getId();
            });
        }
        return orders;
    }

    public List<WorkOrder> getNotBottomOrders(LocalDate date) {
        return this.orderDao.getNotBottomOrders(date);
    }

    public WorkOrder getOrderById(Integer id) {
        return this.orderDao.getOrderById(id);
    }

    public void updateOrderState(WorkOrder order, OrderState state) {
        order.setOperationState(state.value);
        this.orderDao.updateOrderState(order);
    }

    public Integer getOrderCount() {
        return this.orderDao.getOrderCount();
    }
}
