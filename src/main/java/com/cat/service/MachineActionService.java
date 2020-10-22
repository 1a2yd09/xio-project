package com.cat.service;

import com.cat.entity.Board;
import com.cat.entity.MachineAction;
import com.cat.entity.enums.ActionCategory;
import com.cat.entity.enums.BoardCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MachineActionService {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    WorkOrderService orderService;

    @Autowired
    InventoryService inventoryService;

    RowMapper<MachineAction> actionM = new BeanPropertyRowMapper<>(MachineAction.class);

    public int processingFinishedAction() {
        int orderId = -1;
        int productCount = 0;
        Board semiProduct = null;
        int semiCount = 0;
        Board stock = null;
        int stockCount = 0;

        List<MachineAction> actions = this.getAllActions();
        for (MachineAction action : actions) {
            // TODO: 可能需要判断动作是否已完成。
            String boardCategory = action.getBoardCategory();
            // 记录数目，最后统一写入，理由是一次机器动作中，成品、非成品各自的规格和材质都是相同的:
            if (boardCategory.equals(BoardCategory.PRODUCT.value)) {
                if (orderId == -1) {
                    orderId = action.getWorkOrderId();
                }
                productCount++;
            } else if (boardCategory.equals(BoardCategory.SEMI_PRODUCT.value)) {
                if (semiProduct == null) {
                    semiProduct = new Board(action.getBoardSpecification(), action.getBoardMaterial(), BoardCategory.SEMI_PRODUCT);
                }
                semiCount++;
            } else if (boardCategory.equals(BoardCategory.STOCK.value)) {
                if (stock == null) {
                    stock = new Board(action.getBoardSpecification(), action.getBoardMaterial(), BoardCategory.STOCK);
                }
                stockCount++;
            }
        }

        if (orderId != -1) {
            this.orderService.addOrderCompletedAmount(orderId, productCount);
        }
        if (semiProduct != null) {
            this.inventoryService.addInventory(semiProduct, semiCount);
        }
        if (stock != null) {
            this.inventoryService.addInventory(stock, stockCount);
        }

        this.transferAction();
        this.clearAllAction();

        return productCount;
    }

    public void clearAllAction() {
        this.jdbcTemplate.update("TRUNCATE TABLE tb_machine_action");
    }

    public Integer getActionCount() {
        return this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_machine_action", Integer.class);
    }

    public Integer getDoneActionCount() {
        return this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_completed_action", Integer.class);
    }

    public void doneAllAction() {
        this.jdbcTemplate.update("UPDATE tb_machine_action SET completed = 1 WHERE completed = 0");
    }

    public List<MachineAction> getAllActions() {
        return this.jdbcTemplate.query("SELECT * FROM tb_machine_action ORDER BY id", this.actionM);
    }

    public void addPickAction(Board board, Integer orderId, String orderModule) {
        // 注意位置参数的类型是否与数据库类型可以相互转换:
        this.jdbcTemplate.update("INSERT INTO tb_machine_action (action_category, board_category, board_specification, board_material, work_order_id, work_order_module) " +
                "VALUES (?, ?, ?, ?, ?, ?)", ActionCategory.PICK.value, board.getCategory().value, board.getSpecification(), board.getMaterial(), orderId, orderModule);
    }

    public void addRotateAction(Board board, Integer orderId, String orderModule) {
        this.jdbcTemplate.update("INSERT INTO tb_machine_action (action_category, board_category, board_specification, board_material, work_order_id, work_order_module) " +
                "VALUES (?, ?, ?, ?, ?, ?)", ActionCategory.ROTATE.value, board.getCategory().value, board.getSpecification(), board.getMaterial(), orderId, orderModule);
    }

    public void addCuttingAction(Board board, Integer orderId, String orderModule) {
        this.jdbcTemplate.update("INSERT INTO tb_machine_action (action_category, cut_distance, board_category, board_specification, board_material, work_order_id, work_order_module) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)", ActionCategory.CUT.value, board.getWidth(), board.getCategory().value, board.getSpecification(), board.getMaterial(), orderId, orderModule);
    }

    public void addCutAction(BigDecimal distance, Board board, Integer orderId, String orderModule) {
        this.jdbcTemplate.update("INSERT INTO tb_machine_action (action_category, cut_distance, board_category, board_specification, board_material, work_order_id, work_order_module) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)", ActionCategory.CUT.value, distance, board.getCategory().value, board.getSpecification(), board.getMaterial(), orderId, orderModule);
    }

    public void addSendingAction(Board board, Integer orderId, String orderModule) {
        this.jdbcTemplate.update("INSERT INTO tb_machine_action (action_category, board_category, board_specification, board_material, work_order_id, work_order_module) " +
                "VALUES (?, ?, ?, ?, ?, ?)", ActionCategory.SEND.value, board.getCategory().value, board.getSpecification(), board.getMaterial(), orderId, orderModule);
    }

    public void truncateDoneAction() {
        this.jdbcTemplate.update("TRUNCATE TABLE tb_completed_action");
    }

    public void transferAction() {
        this.jdbcTemplate.update("INSERT INTO tb_completed_action SELECT * FROM tb_machine_action");
    }
}
