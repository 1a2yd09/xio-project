package com.cat.service;

import com.cat.entity.Inventory;
import com.cat.entity.WorkOrder;
import com.cat.entity.enums.BottomSortPattern;
import com.cat.entity.enums.OrderState;
import com.cat.entity.enums.WorkOrderModule;
import com.cat.util.BoardUtil;
import com.cat.util.OrderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WorkOrderService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    InventoryService inventoryService;

    RowMapper<WorkOrder> orderM = new BeanPropertyRowMapper<>(WorkOrder.class);

    public void startOrder(WorkOrder order) {
        order.setOperationState(OrderState.ALREADY_STARTED.value);
        this.updateOrderState(order.getOperationState(), order.getId());
    }

    public List<WorkOrder> getPreprocessNotBottomOrders(LocalDate date) {
        List<WorkOrder> orders = this.getNotBottomOrders(date);
        Map<String, Inventory> inventoryMap = this.inventoryService.getStockMap();

        for (WorkOrder order : orders) {
            // 使用输出到存货表的标准规格格式来获取指定的存货对象:
            Inventory inventory = inventoryMap.get(BoardUtil.getStandardSpecStr(order.getSpecification()));
            if (inventory != null && inventory.getMaterial().equals(order.getMaterial()) && inventory.getAmount() > 0) {
                int usedInventoryNum = Math.min(order.getUnfinishedAmount(), inventory.getAmount());
                this.addOrderCompletedAmount(order, usedInventoryNum);
                inventory.setAmount(inventory.getAmount() - usedInventoryNum);
                // 直接在这里就将库存件数目写回数据表，不放在最后一起处理的理由是，
                // 某批库存件可能不被获取或只获取一次，以及获取之后全部被用作成品，数目归零后不会再进入该判断逻辑:
                this.inventoryService.updateInventoryAmount(inventory.getAmount(), inventory.getId());
            }
        }

        return orders.stream().filter(order -> order.getUnfinishedAmount() > 0).collect(Collectors.toList());
    }

    public void addOrderCompletedAmount(WorkOrder order, int amount) {
        order.setCompletedAmount(OrderUtil.addAmountPropWithInt(order.getCompletedAmount(), amount));
        this.updateOrderCompletedAmount(order.getCompletedAmount(), order.getId());

        if (order.getUnfinishedAmount() == 0) {
            order.setOperationState(OrderState.COMPLETED.value);
            this.updateOrderState(order.getOperationState(), order.getId());
        }
    }

    public List<WorkOrder> getBottomOrders(String sortPattern, LocalDate date) {
        List<WorkOrder> orders = this.getBottomOrders(date);
        if (sortPattern.equals(BottomSortPattern.SPEC.value)) {
            // 如果要求按照规格排序，指的是”依次“根据成品的厚度、宽度、长度降序排序，三者都相同则按工单ID升序排序:
            orders.sort((o1, o2) -> {
                int retVal = BoardUtil.sortTwoSpecStr(o1.getSpecification(), o2.getSpecification());
                return retVal != 0 ? retVal : o1.getId() - o2.getId();
            });
        }
        return orders;
    }

    public List<WorkOrder> getBottomOrders(LocalDate date) {
        return this.jdbcTemplate.query("SELECT * FROM vi_local_work_order " +
                "WHERE site_module = ? AND CAST(completion_date AS DATE) = ? " +
                "ORDER BY CAST(sequence_number AS INT), id", this.orderM, WorkOrderModule.BOTTOM.value, date);
    }

    public List<WorkOrder> getNotBottomOrders(LocalDate date) {
        return this.jdbcTemplate.query("SELECT * FROM vi_local_work_order " +
                "WHERE site_module != ? AND CAST(completion_date AS DATE) = ? " +
                "ORDER BY CAST(sequence_number AS INT), id", this.orderM, WorkOrderModule.BOTTOM.value, date);
    }

    public WorkOrder getOrderById(Integer id) {
        return this.jdbcTemplate.queryForObject("SELECT * FROM vi_local_work_order WHERE id = ?", this.orderM, id);
    }

    public void updateOrderCompletedAmount(String amount, Integer id) {
        // 查询是从视图中查询，更新是更新回原数据表:
        if (amount == null) {
            // 注意 update() 方法后续的参数数组不支持非空对象，在其方法签名上可以看到 @Nullable 注解:
            this.jdbcTemplate.update("UPDATE tb_local_work_order SET YWGSL = null WHERE bid = ?", id);
        } else {
            this.jdbcTemplate.update("UPDATE tb_local_work_order SET YWGSL = ? WHERE bid = ?", amount, id);
        }
    }

    public void updateOrderState(String state, Integer id) {
        this.jdbcTemplate.update("UPDATE tb_local_work_order SET ZT = ? WHERE bid = ?", state, id);
    }

    public void truncateOrderTable() {
        this.jdbcTemplate.update("TRUNCATE TABLE tb_local_work_order");
    }

    public void copyRemoteOrderToLocal(LocalDate date) {
        this.jdbcTemplate.update("INSERT INTO tb_local_work_order " +
                "SELECT * FROM tb_remote_work_order " +
                "WHERE CAST(jhwgrq AS DATE) = ?", date);
    }

    public Integer getOrderCount() {
        return this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_local_work_order", Integer.class);
    }
}
