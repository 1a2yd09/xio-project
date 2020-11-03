package com.cat.dao;

import com.cat.entity.MachineAction;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MachineActionDao extends AbstractDao {
    RowMapper<MachineAction> actionM = new BeanPropertyRowMapper<>(MachineAction.class);

    public void addAction(String actionCategory, BigDecimal dis, String boardCategory, String boardSpec, String boardMaterial, Integer orderId, String orderModule) {
        this.jdbcTemplate.update("INSERT INTO tb_machine_action (action_category, cut_distance, board_category, board_specification, board_material, work_order_id, work_order_module) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)", actionCategory, dis, boardCategory, boardSpec, boardMaterial, orderId, orderModule);
    }

    public void truncateActionTable() {
        this.jdbcTemplate.update("TRUNCATE TABLE tb_machine_action");
    }

    public void truncateCompletedActionTable() {
        this.jdbcTemplate.update("TRUNCATE TABLE tb_completed_action");
    }

    public Integer getActionCount() {
        return this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_machine_action", Integer.class);
    }

    public Integer getCompletedActionCount() {
        return this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_completed_action", Integer.class);
    }

    public List<MachineAction> getAllActions() {
        return this.jdbcTemplate.query("SELECT * FROM tb_machine_action ORDER BY id", this.actionM);
    }

    public void completedAllActions() {
        this.jdbcTemplate.update("UPDATE tb_machine_action SET completed = 1 WHERE completed = 0");
    }

    public void completedAction(Integer id) {
        this.jdbcTemplate.update("UPDATE tb_machine_action SET completed = 1 WHERE id = ?", id);
    }

    public void transferAllActions() {
        this.jdbcTemplate.update("INSERT INTO tb_completed_action " +
                "SELECT id, action_category, cut_distance, board_category, board_specification, board_material, work_order_id, work_order_module " +
                "FROM tb_machine_action");
    }
}
