package com.cat.dao;

import com.cat.entity.WorkOrder;
import com.cat.entity.enums.WorkOrderModule;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class WorkOrderDao extends AbstractDao {
    RowMapper<WorkOrder> orderM = new BeanPropertyRowMapper<>(WorkOrder.class);

    public Integer getAllOrderCount() {
        return this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_local_work_order", Integer.class);
    }

    public void copyRemoteOrderToLocal(LocalDate date) {
        this.jdbcTemplate.update("INSERT INTO tb_local_work_order " +
                "SELECT * FROM tb_remote_work_order " +
                "WHERE CAST(jhwgrq AS DATE) = ?", date);
    }

    public void truncateTable() {
        this.jdbcTemplate.update("TRUNCATE TABLE tb_local_work_order");
    }

    public void updateOrderState(String state, Integer id) {
        this.jdbcTemplate.update("UPDATE tb_local_work_order SET ZT = ? WHERE bid = ?", state, id);
    }

    public void updateOrderCompletedAmount(String amount, Integer id) {
        if (amount == null) {
            // 注意 update() 方法后续的参数数组不支持非空对象，其方法签名上可以看到 @Nullable 注解:
            this.jdbcTemplate.update("UPDATE tb_local_work_order SET YWGSL = null WHERE bid = ?", id);
        } else {
            this.jdbcTemplate.update("UPDATE tb_local_work_order SET YWGSL = ? WHERE bid = ?", amount, id);
        }
    }

    public WorkOrder getOrderById(Integer id) {
        return this.jdbcTemplate.queryForObject("SELECT * FROM vi_local_work_order WHERE id = ?", this.orderM, id);
    }

    public List<WorkOrder> getNotBottomOrders(LocalDate date) {
        return this.jdbcTemplate.query("SELECT * FROM vi_local_work_order " +
                "WHERE site_module != ? AND CAST(completion_date AS DATE) = ? " +
                "ORDER BY CAST(sequence_number AS INT), id", this.orderM, WorkOrderModule.BOTTOM.value, date);
    }

    public List<WorkOrder> getBottomOrders(LocalDate date) {
        return this.jdbcTemplate.query("SELECT * FROM vi_local_work_order " +
                "WHERE site_module = ? AND CAST(completion_date AS DATE) = ? " +
                "ORDER BY CAST(sequence_number AS INT), id", this.orderM, WorkOrderModule.BOTTOM.value, date);
    }
}
