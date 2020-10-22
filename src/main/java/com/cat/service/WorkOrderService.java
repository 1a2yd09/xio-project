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

@Component
public class WorkOrderService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    InventoryService inventoryService;

    RowMapper<WorkOrder> workOrderRowMapper = new BeanPropertyRowMapper<>(WorkOrder.class);

    public void preprocessNotBottomOrder(List<WorkOrder> orders) {
        // 获取全体库存件:
        Map<String, Inventory> inventoryMap = this.inventoryService.getStockMap();

        for (WorkOrder order : orders) {
            // 根据成品规格获取指定库存件:
            Inventory inventory = inventoryMap.get(BoardUtil.getStandardSpecStr(order.getSpecification()));
            if (inventory != null && order.getMaterial().equals(inventory.getMaterial()) && inventory.getAmount() > 0) {
                // 存在规格相同的库存件，继续判断材质是否相同，最后判断库存件数目是否大于零，因为可能存在某个库存件被前面的工单全部当成成品使用:

                // 工单未完成数目和库存件数目两者中的最小值就是此次作为成品的数目:
                int usedInventoryNum = Math.min(order.getUnfinishedAmount(), inventory.getAmount());
                // 工单的已完工数目加上该数目:
                this.addOrderCompletedAmount(order.getId(), usedInventoryNum);
                // 库存件的数目减去该数目:
                inventory.setAmount(inventory.getAmount() - usedInventoryNum);
                // 直接在这里将库存件数目写回数据表，不放在最后一起处理的理由是——某个库存件可能不被获取或只获取一次，以及获取之后全部被用作成品:
                this.inventoryService.updateInventoryAmount(inventory);
            }
        }
    }

    public void addOrderCompletedAmount(Integer orderId, int amount) {
        WorkOrder order = this.getWorkOrderById(orderId);
        int newCompletedAmount = OrderUtil.amountPropertyStrToInt(order.getCompletedAmount()) + amount;
        order.setCompletedAmount(String.valueOf(newCompletedAmount));
        // 更新数据表尽量以对象为参数，防止在需要返回对象的逻辑时没有和数据表同步:
        this.updateOrderCompletedAmount(order);
        // 如果工单已完工数量等于需求量，将工单状态置为已完工:
        if (order.getAmount().equals(order.getCompletedAmount())) {
            order.setOperationState(OrderState.COMPLETED.value);
            this.updateOrderState(order);
        }
    }

    public List<WorkOrder> getBottomOrders(String sortPattern, LocalDate date) {
        List<WorkOrder> orders = this.getBottomOrders(date);
        if (sortPattern.equals(BottomSortPattern.SPEC.value)) {
            // 如果要求按照成品规格(此处规格指的是依次比较规格的三个度量)排序，则按照成品规格厚度、宽度、长度降序排序工单，都相同则按ID升序排序:
            orders.sort((o1, o2) -> {
                int retVal = BoardUtil.sortTwoSpecStr(o1.getSpecification(), o2.getSpecification());
                return retVal != 0 ? retVal : o1.getId() - o2.getId();
            });
        }
        return orders;
    }

    public List<WorkOrder> getBottomOrders(LocalDate date) {
        return this.jdbcTemplate.query("SELECT * FROM vi_local_work_order " +
                "WHERE site_module = ? AND operation_state != ? AND CAST(completion_date AS DATE) = ? " +
                "ORDER BY CAST(sequence_number AS INT), id", this.workOrderRowMapper, WorkOrderModule.BOTTOM.value, OrderState.COMPLETED.value, date);
    }

    public List<WorkOrder> getNotBottomOrders(LocalDate date) {
        return this.jdbcTemplate.query("SELECT * FROM vi_local_work_order " +
                "WHERE site_module != ? AND operation_state != ? AND CAST(completion_date AS DATE) = ? " +
                "ORDER BY CAST(sequence_number AS INT), id", this.workOrderRowMapper, WorkOrderModule.BOTTOM.value, OrderState.COMPLETED.value, date);
    }

    public WorkOrder getWorkOrderById(Integer id) {
        return this.jdbcTemplate.queryForObject("SELECT * FROM vi_local_work_order WHERE id = ?", this.workOrderRowMapper, id);
    }

    public void updateOrderCompletedAmount(WorkOrder order) {
        // 查询是从视图中查询，更新是更新回原数据表:
        if (order.getCompletedAmount() == null) {
            // 注意 update() 方法后续的参数数组不支持非空对象，在其方法签名上可以看到 @Nullable 注解:
            this.jdbcTemplate.update("UPDATE tb_local_work_order SET YWGSL = null WHERE bid = ?", order.getId());
        } else {
            this.jdbcTemplate.update("UPDATE tb_local_work_order SET YWGSL = ? WHERE bid = ?", order.getCompletedAmount(), order.getId());
        }
    }

    public void updateOrderState(WorkOrder order) {
        this.jdbcTemplate.update("UPDATE tb_local_work_order SET ZT = ? WHERE bid = ?", order.getOperationState(), order.getId());
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
        return this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vi_local_work_order", Integer.class);
    }
}
