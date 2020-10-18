package com.cat.service;

import com.cat.entity.Board;
import com.cat.entity.MachineAction;
import com.cat.entity.enums.ActionCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MachineActionService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    RowMapper<MachineAction> actionM = new BeanPropertyRowMapper<>(MachineAction.class);

    public void clearAllAction() {
        this.jdbcTemplate.update("TRUNCATE TABLE machine_action");
    }

    public Integer getActionCount() {
        return this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM machine_action", Integer.class);
    }

    public void addPickAction(Board board, Integer orderId, String orderModule) {
        // 注意位置参数的类型是否与数据库类型可以相互转换:
        this.jdbcTemplate.update("INSERT INTO machine_action (action_category, board_category, board_specification, board_material, work_order_id, work_order_module) " +
                "VALUES (?, ?, ?, ?, ?, ?)", ActionCategory.PICK.value, board.getCategory().value, board.getSpecification(), board.getMaterial(), orderId, orderModule);
    }

    public void addRotateAction(Board board, Integer orderId, String orderModule) {
        this.jdbcTemplate.update("INSERT INTO machine_action (action_category, board_category, board_specification, board_material, work_order_id, work_order_module) " +
                "VALUES (?, ?, ?, ?, ?, ?)", ActionCategory.ROTATE.value, board.getCategory().value, board.getSpecification(), board.getMaterial(), orderId, orderModule);
    }

    public void addCuttingAction(Board board, Integer orderId, String orderModule) {
        this.jdbcTemplate.update("INSERT INTO machine_action (action_category, cut_distance, board_category, board_specification, board_material, work_order_id, work_order_module) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)", ActionCategory.CUT.value, board.getWidth(), board.getCategory().value, board.getSpecification(), board.getMaterial(), orderId, orderModule);
    }

    public void addCutAction(BigDecimal distance, Board board, Integer orderId, String orderModule) {
        this.jdbcTemplate.update("INSERT INTO machine_action (action_category, cut_distance, board_category, board_specification, board_material, work_order_id, work_order_module) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)", ActionCategory.CUT.value, distance, board.getCategory().value, board.getSpecification(), board.getMaterial(), orderId, orderModule);
    }

    public void addSendingAction(Board board, Integer orderId, String orderModule) {
        this.jdbcTemplate.update("INSERT INTO machine_action (action_category, board_category, board_specification, board_material, work_order_id, work_order_module) " +
                "VALUES (?, ?, ?, ?, ?, ?)", ActionCategory.SEND.value, board.getCategory().value, board.getSpecification(), board.getMaterial(), orderId, orderModule);
    }
}
