package com.cat.service;

import com.cat.entity.WorkOrder;
import com.cat.entity.enums.BottomSortPattern;
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

@Component
public class WorkOrderService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    RowMapper<WorkOrder> workOrderRowMapper = new BeanPropertyRowMapper<>(WorkOrder.class);

    public void addOrderCompletedAmount(Integer orderId, int amount) {
        WorkOrder order = this.getWorkOrderById(orderId);
        int newCompletedAmount = OrderUtil.amountPropertyStrToInt(order.getCompletedAmount()) + amount;
        order.setCompletedAmount(String.valueOf(newCompletedAmount));
        // 更新数据表尽量以对象为参数，防止在需要返回对象的逻辑时没有和数据表同步:
        this.updateOrderCompletedAmount(order);
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
        return this.jdbcTemplate.query("SELECT * FROM v_local_work_order WHERE site_module = ? AND CAST(completion_date AS DATE) = ? ORDER BY CAST(sequence_number AS INT), id", this.workOrderRowMapper, WorkOrderModule.BOTTOM.value, date);
    }

    public List<WorkOrder> getNotBottomOrders(LocalDate date) {
        return this.jdbcTemplate.query("SELECT * FROM v_local_work_order WHERE site_module != ? AND CAST(completion_date AS DATE) = ? ORDER BY CAST(sequence_number AS INT), id", this.workOrderRowMapper, WorkOrderModule.BOTTOM.value, date);
    }

    public WorkOrder getWorkOrderById(Integer id) {
        return this.jdbcTemplate.queryForObject("SELECT * FROM v_local_work_order WHERE id = ?", this.workOrderRowMapper, id);
    }

    public void updateOrderCompletedAmount(WorkOrder order) {
        // 查询是从视图中查询，更新是更新回原数据表:
        if (order.getCompletedAmount() == null) {
            // 注意 update() 方法后续的参数数组不支持非空对象，在其方法签名上可以看到 @Nullable 注解:
            this.jdbcTemplate.update("UPDATE local_work_order SET YWGSL = null WHERE bid = ?", order.getId());
        } else {
            this.jdbcTemplate.update("UPDATE local_work_order SET YWGSL = ? WHERE bid = ?", order.getCompletedAmount(), order.getId());
        }
    }
}
