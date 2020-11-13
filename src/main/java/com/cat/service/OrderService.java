package com.cat.service;

import com.cat.dao.OrderDao;
import com.cat.entity.Inventory;
import com.cat.entity.WorkOrder;
import com.cat.entity.enums.BottomSortPattern;
import com.cat.entity.enums.OrderState;
import com.cat.util.BoardUtil;
import com.cat.util.OrderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderService {
    @Autowired
    OrderDao orderDao;
    @Autowired
    InventoryService inventoryService;

    public void addOrderCompletedAmount(WorkOrder order, int amount) {
        order.setCompletedAmount(OrderUtil.addAmountPropWithInt(order.getCompletedAmount(), amount));
        this.orderDao.updateOrderCompletedAmount(order.getCompletedAmount(), order.getId());

        if (order.getUnfinishedAmount() == 0) {
            this.updateOrderState(order, OrderState.COMPLETED);
        }
    }

    public List<WorkOrder> getPreprocessNotBottomOrders(LocalDate date) {
        List<WorkOrder> orders = this.getNotBottomOrders(date);
        // 获取直梁工单集合后，尝试使用当前已有的库存件作为成品:
        Map<String, Inventory> inventoryMap = this.inventoryService.getStockMap();

        for (WorkOrder order : orders) {
            // 使用标准规格格式来获取指定的存货对象:
            Inventory inventory = inventoryMap.get(BoardUtil.getStandardSpecStr(order.getSpecStr()));
            if (inventory != null && inventory.getMaterial().equals(order.getMaterial()) && inventory.getAmount() > 0) {
                // 此次用作成品的库存件数量是工单未完成数量和库存件数量当中的最小值:
                int usedInventoryNum = Math.min(order.getUnfinishedAmount(), inventory.getAmount());
                // 使用库存件作为成品属于工单开工的行为之一:
                this.updateOrderState(order, OrderState.ALREADY_STARTED);
                this.addOrderCompletedAmount(order, usedInventoryNum);
                // 直接在这里就将库存件数目写回数据表的理由是，不是每批库存件都会被获取，另外获取之后全部被用作成品，数目归零后不会再进入该逻辑:
                inventory.setAmount(inventory.getAmount() - usedInventoryNum);
                this.inventoryService.updateInventoryAmount(inventory.getAmount(), inventory.getId());
            }
        }

        return orders.stream().filter(order -> order.getUnfinishedAmount() > 0).collect(Collectors.toList());
    }

    public List<WorkOrder> getBottomOrders(String sortPattern, LocalDate date) {
        List<WorkOrder> orders = this.orderDao.getBottomOrders(date);
        if (BottomSortPattern.SPEC.value.equals(sortPattern)) {
            // 如果要求按照规格排序，指的是”依次“按照成品的厚度、宽度、长度降序排序，三者都相同则按工单ID升序排序:
            orders.sort((o1, o2) -> {
                int retVal = BoardUtil.compareTwoSpecStr(o1.getSpecStr(), o2.getSpecStr());
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
        this.orderDao.updateOrderState(order.getOperationState(), order.getId());
    }

    public void clearOrderTable() {
        this.orderDao.truncateTable();
    }

    public void copyRemoteOrderToLocal(LocalDate date) {
        this.orderDao.copyRemoteOrderToLocal(date);
    }

    public Integer getOrderCount() {
        return this.orderDao.getAllOrderCount();
    }
}
