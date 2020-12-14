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

/**
 * @author CAT
 */
@Component
public class OrderService {
    @Autowired
    OrderDao orderDao;
    @Autowired
    InventoryDao inventoryDao;

    /**
     * 增加工单的已完工数目并更新工单状态。
     *
     * @param order    工单
     * @param quantity 新完工的成品数目
     */
    public void addOrderCompletedQuantity(WorkOrder order, int quantity) {
        order.setCompletedQuantity(OrderUtils.addQuantityPropWithInt(order.getCompletedQuantity(), quantity));
        this.orderDao.updateOrderCompletedQuantity(order);
        this.updateOrderState(order, order.getIncompleteQuantity() == 0 ? OrderState.COMPLETED : OrderState.STARTED);
    }

    /**
     * 根据计划完工日期获取经过预处理的对重直梁工单集合。
     *
     * @param date 计划完工日期
     * @return 预处理的对重直梁工单集合
     */
    public List<WorkOrder> getPreprocessNotBottomOrders(LocalDate date) {
        List<WorkOrder> orders = this.getNotBottomOrders(date);
        Map<String, Inventory> stockMap = this.inventoryDao.getInventories(BoardCategory.STOCK.value)
                .stream()
                .collect(Collectors.toMap(stock -> BoardUtils.getStandardSpecStr(stock.getSpecification()), Function.identity()));

        for (WorkOrder order : orders) {
            Inventory stock = stockMap.get(BoardUtils.getStandardSpecStr(order.getProductSpecification()));
            if (stock != null && stock.getMaterial().equals(order.getMaterial()) && stock.getQuantity() > 0) {
                int usedStockQuantity = Math.min(order.getIncompleteQuantity(), stock.getQuantity());
                // 使用库存件作为成品属于工单开工的行为之一:
                this.addOrderCompletedQuantity(order, usedStockQuantity);
                stock.setQuantity(stock.getQuantity() - usedStockQuantity);
                this.inventoryDao.updateInventoryQuantity(stock);
            }
        }

        return orders.stream().filter(order -> order.getIncompleteQuantity() > 0).collect(Collectors.toList());
    }

    /**
     * 根据排序方式以及计划完工日期获取轿底工单集合。
     *
     * @param sortPattern 工单排序方式
     * @param date        计划完工日期
     * @return 轿底工单集合
     */
    public List<WorkOrder> getBottomOrders(String sortPattern, LocalDate date) {
        List<WorkOrder> orders = this.orderDao.getBottomOrders(date);
        if (OrderSortPattern.BY_SPEC.value.equals(sortPattern)) {
            // 如果要求按照规格排序，指的是”依次“按照成品的厚度、宽度、长度降序排序，三者都相同则按工单 ID 升序排序:
            orders.sort((o1, o2) -> {
                int retVal = BoardUtils.compareTwoSpecStr(o1.getProductSpecification(), o2.getProductSpecification());
                return retVal != 0 ? -retVal : o1.getId() - o2.getId();
            });
        }
        return orders;
    }

    /**
     * 根据计划完工日期获取对重直梁工单集合（按顺序号升序排序）。
     *
     * @param date 计划完工日期
     * @return 对重直梁工单集合
     */
    public List<WorkOrder> getNotBottomOrders(LocalDate date) {
        return this.orderDao.getNotBottomOrders(date);
    }

    /**
     * 根据工单 ID 获取指定工单，不存在指定 ID 工单时将返回 null。
     *
     * @param id 工单 ID
     * @return 工单
     */
    public WorkOrder getOrderById(Integer id) {
        return this.orderDao.getOrderById(id);
    }

    /**
     * 更新工单运行状态。
     *
     * @param order 工单
     * @param state 状态
     */
    public void updateOrderState(WorkOrder order, OrderState state) {
        order.setOperationState(state.value);
        this.orderDao.updateOrderState(order);
    }
}
