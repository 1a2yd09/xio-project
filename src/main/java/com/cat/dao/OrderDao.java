package com.cat.dao;

import com.cat.entity.bean.WorkOrder;
import com.cat.enums.OrderModule;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class OrderDao extends BaseDao {
    RowMapper<WorkOrder> orderM = new BeanPropertyRowMapper<>(WorkOrder.class);

    public Integer getOrderCount() {
        return this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_local_work_order", Integer.class);
    }

    public void updateOrderState(WorkOrder order) {
        this.jdbcTemplate.update("UPDATE tb_local_work_order SET ZT = ? WHERE bid = ?", order.getOperationState(), order.getId());
    }

    public void updateOrderCompletedAmount(WorkOrder order) {
        if (order.getCompletedAmount() == null) {
            // 注意 update() 方法后续的参数数组不支持非空对象，其方法签名上可以看到 @Nullable 注解:
            this.jdbcTemplate.update("UPDATE tb_local_work_order SET YWGSL = null WHERE bid = ?", order.getId());
        } else {
            this.jdbcTemplate.update("UPDATE tb_local_work_order SET YWGSL = ? WHERE bid = ?", order.getCompletedAmount(), order.getId());
        }
    }

    public WorkOrder getOrderById(Integer id) {
        return this.jdbcTemplate.queryForObject("SELECT * FROM vi_local_work_order WHERE id = ?", this.orderM, id);
    }

    public List<WorkOrder> getNotBottomOrders(LocalDate date) {
        return this.jdbcTemplate.query("SELECT * FROM vi_local_work_order " +
                "WHERE site_module != ? AND CAST(completion_date AS DATE) = ? " +
                "ORDER BY CAST(sequence_number AS INT), id", this.orderM, OrderModule.BOTTOM.value, date);
    }

    public List<WorkOrder> getBottomOrders(LocalDate date) {
        return this.jdbcTemplate.query("SELECT * FROM vi_local_work_order " +
                "WHERE site_module = ? AND CAST(completion_date AS DATE) = ? " +
                "ORDER BY CAST(sequence_number AS INT), id", this.orderM, OrderModule.BOTTOM.value, date);
    }
}
