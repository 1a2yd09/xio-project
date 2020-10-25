package com.cat.service;

import com.cat.entity.Board;
import com.cat.entity.MachineAction;
import com.cat.entity.WorkOrder;
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

    public void processCompletedAction(WorkOrder order) {
        int productCount = 0;
        Board semiProduct = null;
        int semiCount = 0;
        Board stock = null;
        int stockCount = 0;

        for (MachineAction action : this.getAllActions()) {
            String boardCategory = action.getBoardCategory();
            // 记录数目，最后统一写入，理由是一次机器动作中，成品、非成品各自的规格和材质都是相同的:
            if (BoardCategory.PRODUCT.value.equals(boardCategory)) {
                productCount++;
            } else if (BoardCategory.SEMI_PRODUCT.value.equals(boardCategory)) {
                if (semiProduct == null) {
                    semiProduct = new Board(action.getBoardSpecification(), action.getBoardMaterial(), BoardCategory.SEMI_PRODUCT);
                }
                semiCount++;
            } else if (BoardCategory.STOCK.value.equals(boardCategory)) {
                if (stock == null) {
                    stock = new Board(action.getBoardSpecification(), action.getBoardMaterial(), BoardCategory.STOCK);
                }
                stockCount++;
            }
        }


        this.orderService.addOrderCompletedAmount(order, productCount);

        if (semiProduct != null) {
            this.inventoryService.addInventoryAmount(semiProduct, semiCount);
        }
        if (stock != null) {
            this.inventoryService.addInventoryAmount(stock, stockCount);
        }

        this.transferAllActions();
        this.truncateAction();
    }

    public void addAction(ActionCategory category, BigDecimal dis, Board board, Integer orderId, String orderModule) {
        // 注意位置参数的类型是否与数据库类型可以相互转换:
        this.jdbcTemplate.update("INSERT INTO tb_machine_action (action_category, cut_distance, board_category, board_specification, board_material, work_order_id, work_order_module) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)", category.value, dis, board.getCategory().value, board.getSpecification(), board.getMaterial(), orderId, orderModule);
    }

    public void truncateAction() {
        this.jdbcTemplate.update("TRUNCATE TABLE tb_machine_action");
    }

    public void truncateCompletedAction() {
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

    public void transferAllActions() {
        this.jdbcTemplate.update("INSERT INTO tb_completed_action SELECT * FROM tb_machine_action");
    }
}
