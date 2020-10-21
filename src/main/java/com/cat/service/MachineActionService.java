package com.cat.service;

import com.cat.entity.Board;
import com.cat.entity.MachineAction;
import com.cat.entity.enums.ActionCategory;
import com.cat.entity.enums.BoardCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MachineActionService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    WorkOrderService orderService;

    @Autowired
    InventoryService inventoryService;

    RowMapper<MachineAction> actionM = new BeanPropertyRowMapper<>(MachineAction.class);

    public void processingFinishedAction() {
        List<MachineAction> actions = this.getAllActions();
        for (MachineAction action : actions) {
            // TODO: 可能需要判断动作是否已完成。
            String boardCategory = action.getBoardCategory();
            if (boardCategory.equals(BoardCategory.PRODUCT.value)) {
                // TODO: 可以改成统一记录，最后一次写入，现在是每有一个成品操作就写入一次数据表。
                this.orderService.addOrderCompletedAmount(action.getWorkOrderId(), 1);
            } else if (boardCategory.equals(BoardCategory.SEMI_PRODUCT.value)) {
                this.inventoryService.addInventory(action.getBoardSpecification(), action.getBoardMaterial(), 1, BoardCategory.SEMI_PRODUCT.value);
            } else if (boardCategory.equals(BoardCategory.STOCK.value)) {
                this.inventoryService.addInventory(action.getBoardSpecification(), action.getBoardMaterial(), 1, BoardCategory.STOCK.value);
            }
        }
    }

    public void clearAllAction() {
        this.jdbcTemplate.update("TRUNCATE TABLE machine_action");
    }

    public Integer getActionCount() {
        return this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM machine_action", Integer.class);
    }

    public void doneAllAction() {
        this.jdbcTemplate.update("UPDATE machine_action SET completed = 1 WHERE completed = 0");
    }

    public List<MachineAction> getAllActions() {
        return this.jdbcTemplate.query("SELECT * FROM machine_action ORDER BY created_at", this.actionM);
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
