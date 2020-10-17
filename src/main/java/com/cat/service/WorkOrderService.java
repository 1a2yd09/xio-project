package com.cat.service;

import com.cat.entity.WorkOrder;
import com.cat.entity.enums.BottomSortPattern;
import com.cat.entity.enums.WorkOrderModule;
import com.cat.util.BoardUtil;
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

    public List<WorkOrder> getBottomOrders(String sortPattern, LocalDate date) {
        List<WorkOrder> orders = this.getBottomOrders(date);
        if (sortPattern.equals(BottomSortPattern.SPEC.value)) {
            // 如果要求按照成品规格排序，则按照成品规格厚度、宽度、长度从大到小排序工单，规格相同则按ID升序排序:
            orders.sort((o1, o2) -> {
                int retVal = BoardUtil.compareTwoSpecification(o1.getSpecification(), o2.getSpecification());
                return retVal != 0 ? retVal : o1.getId() - o2.getId();
            });
        }
        return orders;
    }

    public List<WorkOrder> getBottomOrders(LocalDate date) {
        return this.jdbcTemplate.query("SELECT * FROM remote_work_order WHERE site_module = ? AND completion_date = ? ORDER BY CAST(sequence_number AS INT), id", this.workOrderRowMapper, WorkOrderModule.BOTTOM.value, date);
    }

    public List<WorkOrder> getNotBottomOrders(LocalDate date) {
        return this.jdbcTemplate.query("SELECT * FROM remote_work_order WHERE site_module != ? AND completion_date = ? ORDER BY CAST(sequence_number AS INT), id", this.workOrderRowMapper, WorkOrderModule.BOTTOM.value, date);
    }

    public WorkOrder getWorkOrderById(Integer id) {
        return this.jdbcTemplate.queryForObject("SELECT * FROM remote_work_order WHERE id = ?", this.workOrderRowMapper, id);
    }
}
