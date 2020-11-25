package com.cat.dao;

import com.cat.entity.board.BaseBoard;
import com.cat.entity.bean.MachineAction;
import com.cat.enums.ActionCategory;
import com.cat.enums.ActionState;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ActionDao extends BaseDao {
    RowMapper<MachineAction> actionM = new BeanPropertyRowMapper<>(MachineAction.class);

    public void insertMachineAction(ActionCategory actionCategory, BigDecimal dis, BaseBoard baseBoard, Integer orderId) {
        this.jdbcTemplate.update("INSERT INTO tb_machine_action (action_category, cut_distance, board_category, board_specification, board_material, work_order_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)", actionCategory.value, dis, baseBoard.getCategory().value, baseBoard.getSpecStr(), baseBoard.getMaterial(), orderId);
    }

    public void truncateMachineAction() {
        this.jdbcTemplate.update("TRUNCATE TABLE tb_machine_action");
    }

    public Integer getMachineActionCount() {
        return this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_machine_action", Integer.class);
    }

    public Integer getCompletedActionCount() {
        return this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_completed_action", Integer.class);
    }

    public List<MachineAction> getAllMachineActions() {
        return this.jdbcTemplate.query("SELECT * FROM tb_machine_action ORDER BY id", this.actionM);
    }

    public MachineAction getFinalMachineAction() {
        List<MachineAction> list = this.jdbcTemplate.query("SELECT * FROM tb_machine_action ORDER BY id DESC", this.actionM);
        return list.isEmpty() ? null : list.get(0);
    }

    public void completedAllMachineActions() {
        this.jdbcTemplate.update("UPDATE tb_machine_action SET state = ? WHERE state = ?", ActionState.FINISHED.value, ActionState.NOT_FINISHED.value);
    }

    public void completedMachineActionById(Integer id) {
        this.jdbcTemplate.update("UPDATE tb_machine_action SET state = ? WHERE id = ?", ActionState.FINISHED.value, id);
    }

    public void transferAllMachineActions() {
        this.jdbcTemplate.update("INSERT INTO tb_completed_action " +
                "SELECT id, state, action_category, cut_distance, board_category, board_specification, board_material, work_order_id " +
                "FROM tb_machine_action");
    }
}
