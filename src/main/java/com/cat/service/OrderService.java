package com.cat.service;

import com.cat.enums.BoardCategory;
import com.cat.enums.OrderModule;
import com.cat.enums.OrderSortPattern;
import com.cat.enums.OrderState;
import com.cat.mapper.InventoryMapper;
import com.cat.mapper.OrderMapper;
import com.cat.pojo.Inventory;
import com.cat.pojo.OperatingParameter;
import com.cat.pojo.WorkOrder;
import com.cat.utils.BoardUtil;
import com.cat.utils.OrderUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * 增加工单的已完工数目并更新工单状态，如果更新后工单状态为已完工，则将工单从生产工单表迁移至完工工单表。
     *
     * @param order    工单
     * @param quantity 新完工的成品数目
     */
    public void addOrderCompletedQuantity(WorkOrder order, int quantity) {
        order.setCompletedQuantity(OrderUtil.addQuantityPropWithInt(order.getCompletedQuantity(), quantity));
        this.orderMapper.updateOrderCompletedQuantity(order);
        this.updateOrderState(order, order.getIncompleteQuantity() == 0 ? OrderState.COMPLETED : OrderState.STARTED);
        if (OrderState.COMPLETED.value.equals(order.getOperationState())) {
            this.transferWorkOrderToCompleted(order.getId());
            this.deleteOrderById(order.getId());
        }
    }

    /**
     * 根据计划完工日期获取经过预处理的对重直梁工单集合。
     *
     * @param date 计划完工日期
     * @return 预处理的对重直梁工单集合
     */
    public List<WorkOrder> getPreprocessNotBottomOrders(LocalDate date) {
        List<WorkOrder> orders = this.getNotBottomOrders(date);
        Map<String, Inventory> stockMap = this.inventoryMapper.getInventories(BoardCategory.STOCK.value)
                .stream()
                .collect(Collectors.toMap(stock -> BoardUtil.getStandardSpecStr(stock.getSpecification()), Function.identity()));

        for (WorkOrder order : orders) {
            Inventory stock = stockMap.get(BoardUtil.getStandardSpecStr(order.getProductSpecification()));
            if (stock != null && stock.getMaterial().equals(order.getMaterial()) && stock.getQuantity() > 0) {
                int usedStockQuantity = Math.min(order.getIncompleteQuantity(), stock.getQuantity());
                // 使用库存件作为成品属于工单开工的行为之一:
                this.addOrderCompletedQuantity(order, usedStockQuantity);
                stock.setQuantity(stock.getQuantity() - usedStockQuantity);
                this.inventoryMapper.updateInventoryQuantity(stock);
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
        Map<String, String> paramMap = new HashMap<>(2);
        paramMap.put("module1", OrderModule.BOTTOM.value);
        paramMap.put("date", date.toString());
        List<WorkOrder> orders = this.orderMapper.getBottomOrders(paramMap);
        if (OrderSortPattern.BY_SPEC.value.equals(sortPattern)) {
            // 如果要求按照规格排序，指的是”依次“按照成品的厚度、宽度、长度降序排序，三者都相同则按工单 ID 升序排序:
            orders.sort((o1, o2) -> {
                int retVal = BoardUtil.compareTwoSpecStr(o1.getProductSpecification(), o2.getProductSpecification());
                return retVal != 0 ? -retVal : o1.getId() - o2.getId();
            });
        } else {
            orders.sort((o1, o2) -> {
                Integer sn1 = Integer.parseInt(o1.getSequenceNumber());
                Integer sn2 = Integer.parseInt(o2.getSequenceNumber());
                if (!sn1.equals(sn2)) {
                    return sn1.compareTo(sn2);
                } else {
                    return o1.getId() - o2.getId();
                }
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
        Map<String, String> paramMap = new HashMap<>(3);
        paramMap.put("module1", OrderModule.STRAIGHT.value);
        paramMap.put("module2", OrderModule.WEIGHT.value);
        paramMap.put("date", date.toString());
        List<WorkOrder> orders = this.orderMapper.getNotBottomOrders(paramMap);
        orders.sort((o1, o2) -> {
            Integer sn1 = Integer.parseInt(o1.getSequenceNumber());
            Integer sn2 = Integer.parseInt(o2.getSequenceNumber());
            if (!sn1.equals(sn2)) {
                return sn1.compareTo(sn2);
            } else {
                return o1.getId() - o2.getId();
            }
        });
        return orders;
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
     * 更新工单运行状态。
     *
     * @param order 工单
     * @param state 状态
     */
    public void updateOrderState(WorkOrder order, OrderState state) {
        order.setOperationState(state.value);
        this.orderMapper.updateOrderState(order);
    }

    /**
     * 获取当前生产工单表中的全体工单。
     *
     * @return 全体生产工单
     */
    public List<WorkOrder> getAllProductionOrders() {
        return this.orderMapper.getAllProductionOrders();
    }

    /**
     * 根据工单模块和运行参数获取指定的生产工单。
     *
     * @param orderModule 工单模块
     * @param param       运行参数
     * @return 生产工单列表
     */
    public List<WorkOrder> getProductionOrders(OrderModule orderModule, OperatingParameter param) {
        if (OrderModule.BOTTOM_PLATFORM == orderModule) {
            return this.getBottomOrders(param.getSortPattern(), param.getOrderDate());
        } else {
            return this.getPreprocessNotBottomOrders(param.getOrderDate());
        }
    }

    /**
     * 根据工单 ID 从工单表中删除对应工单。
     *
     * @param id 工单 ID。
     */
    public void deleteOrderById(Integer id) {
        this.orderMapper.deleteOrderById(id);
    }

    /**
     * 根据工单 ID 将已完工工单迁移至完工工单表中。
     *
     * @param id 工单 ID
     */
    public void transferWorkOrderToCompleted(Integer id) {
        this.orderMapper.transferWorkOrderToCompleted(id);
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
