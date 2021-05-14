package com.cat.service;

import com.cat.enums.BoardCategory;
import com.cat.enums.OrderModule;
import com.cat.enums.OrderSortPattern;
import com.cat.enums.OrderState;
import com.cat.mapper.InventoryMapper;
import com.cat.mapper.OrderMapper;
import com.cat.pojo.Inventory;
import com.cat.pojo.WorkOrder;
import com.cat.pojo.dto.OrderCount;
import com.cat.utils.BoardUtil;
import com.cat.utils.OrderComparator;
import com.cat.utils.OrderUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author CAT
 */
@Service
public class OrderService {
    private final InventoryMapper inventoryMapper;
    private final OrderMapper orderMapper;

    public OrderService(InventoryMapper inventoryMapper, OrderMapper orderMapper) {
        this.inventoryMapper = inventoryMapper;
        this.orderMapper = orderMapper;
    }

    /**
     * 修改工单的已完工数目，如果工单未完成数目为零，则修改工单状态为已完工，并迁移至完工表中。
     *
     * @param order    工单对象
     * @param quantity 新完工的成品数目
     */
    public void addOrderCompletedQuantity(WorkOrder order, int quantity) {
        order.setCompletedQuantity(OrderUtil.addQuantityPropWithInt(order.getCompletedQuantity(), quantity));
        this.orderMapper.updateOrderCompletedQuantity(order);
        if (order.getIncompleteQuantity() == 0) {
            order.setOperationState(OrderState.COMPLETED.value);
            this.orderMapper.updateOrderState(order);
            this.transferWorkOrderToCompleted(order.getId());
        }
    }

    /**
     * 根据排序方式以及计划完工日期获取有效并排序的对重直梁工单集合。
     *
     * @param date 计划完工日期
     * @return 对重直梁工单集合
     */
    public List<WorkOrder> getStraightOrders(OrderSortPattern sortPattern, LocalDate date) {
        List<WorkOrder> orderList = this.orderMapper.getStraightOrders(Map.of(
                "module1", OrderModule.STRAIGHT.getName(),
                "module2", OrderModule.WEIGHT.getName(),
                "date", date.toString()
        )).stream()
                .filter(OrderUtil::validateOrder)
                .sorted((o1, o2) -> OrderComparator.getComparator(sortPattern.name()).compare(o1, o2))
                .collect(Collectors.toList());
        if (!orderList.isEmpty()) {
            orderList.forEach(order -> order.setOperationState(OrderState.STARTED.value));
            this.orderMapper.batchUpdateOrderState(orderList);
        }
        return orderList;
    }

    /**
     * 根据排序方式以及计划完工日期获取有效、排序、预处理的对重直梁工单队列。
     *
     * @param sortPattern 工单排序方式
     * @param date        计划完工日期
     * @return 对重直梁工单队列
     */
    public Deque<WorkOrder> getPreprocessStraightDeque(OrderSortPattern sortPattern, LocalDate date) {
        List<WorkOrder> orderList = this.getStraightOrders(sortPattern, date);

        Map<String, Inventory> stockMap = this.inventoryMapper.getInventories(BoardCategory.STOCK.value)
                .stream()
                .collect(Collectors.toMap(stock -> BoardUtil.getStandardSpecStr(stock.getSpecification()), Function.identity()));

        for (WorkOrder order : orderList) {
            Inventory stock = stockMap.get(BoardUtil.getStandardSpecStr(order.getProductSpecification()));
            if (stock != null && stock.getMaterial().equals(order.getMaterial()) && stock.getQuantity() > 0) {
                int usedStockQuantity = Math.min(order.getIncompleteQuantity(), stock.getQuantity());
                this.addOrderCompletedQuantity(order, usedStockQuantity);
                stock.setQuantity(stock.getQuantity() - usedStockQuantity);
                this.inventoryMapper.updateInventoryQuantity(stock);
            }
        }

        return new LinkedList<>(orderList);
    }

    /**
     * 根据排序方式以及计划完工日期获取有效并排序的轿底工单集合。
     *
     * @param sortPattern 工单排序方式
     * @param date        计划完工日期
     * @return 轿底工单集合
     */
    public List<WorkOrder> getBottomOrders(OrderSortPattern sortPattern, LocalDate date) {
        List<WorkOrder> orderList = this.orderMapper.getBottomOrders(Map.of(
                "module1", OrderModule.BOTTOM.getName(),
                "date", date.toString()
        )).stream()
                .filter(OrderUtil::validateOrder)
                .sorted((o1, o2) -> OrderComparator.getComparator(sortPattern.name()).compare(o1, o2))
                .collect(Collectors.toList());
        if (!orderList.isEmpty()) {
            orderList.forEach(order -> order.setOperationState(OrderState.STARTED.value));
            this.orderMapper.batchUpdateOrderState(orderList);
        }
        return orderList;
    }

    /**
     * 根据排序方式以及计划完工日期获取有效并排序的轿底工单队列。
     *
     * @param sortPattern 工单排序方式
     * @param date        计划完工日期
     * @return 轿底工单队列
     */
    public Deque<WorkOrder> getBottomDeque(OrderSortPattern sortPattern, LocalDate date) {
        return new LinkedList<>(this.getBottomOrders(sortPattern, date));
    }

    /**
     * 根据日期范围获取完工工单的数量情况。
     *
     * @param range 范围
     * @return 范围内每个日期的工单完成数量
     */
    public List<OrderCount> getCompletedOrderCountByRange(int range) {
        LocalDate now = LocalDate.now();
        List<OrderCount> result = new ArrayList<>(range);
        for (int i = 0; i < range; i++) {
            LocalDate date = now.minusDays(i);
            result.add(new OrderCount(date.toString(), this.orderMapper.getCompletedOrderCountByDate(date)));
        }
        return result;
    }

    /**
     * 根据工单 ID 获取指定工单，不存在指定 ID 工单时将返回 null。
     *
     * @param id 工单 ID
     * @return 工单
     */
    public WorkOrder getOrderById(Integer id) {
        return this.orderMapper.getOrderById(id);
    }

    /**
     * 根据工单 ID 获取指定完成工单，不存在指定 ID 工单时将返回 null。
     *
     * @param id 工单 ID
     * @return 工单
     */
    public WorkOrder getCompletedOrderById(Integer id) {
        return this.orderMapper.getCompletedOrderById(id);
    }

    /**
     * 获取本地工单表中的全体工单。
     *
     * @return 全体工单
     */
    public List<WorkOrder> getAllLocalOrders() {
        return this.orderMapper.getAllLocalOrders();
    }

    /**
     * 根据工单 ID 将已完工工单迁移至完工工单表中。
     *
     * @param id 工单 ID
     */
    public void transferWorkOrderToCompleted(Integer id) {
        this.orderMapper.transferWorkOrderToCompleted(id);
        this.orderMapper.deleteOrderById(id);
    }

    /**
     * 获取完工工单表当中的工单个数。
     *
     * @return 工单个数
     */
    public Integer getCompletedOrderCount() {
        return this.orderMapper.getCompletedOrderCount();
    }
}
