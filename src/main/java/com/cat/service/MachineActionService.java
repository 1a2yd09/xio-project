package com.cat.service;

import com.cat.dao.MachineActionDao;
import com.cat.entity.BaseBoard;
import com.cat.entity.MachineAction;
import com.cat.entity.NormalBoard;
import com.cat.entity.WorkOrder;
import com.cat.entity.enums.ActionCategoryEnum;
import com.cat.entity.enums.BoardCategoryEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MachineActionService {
    @Autowired
    MachineActionDao actionDao;
    @Autowired
    WorkOrderService orderService;
    @Autowired
    InventoryService inventoryService;

    public boolean isAllActionsCompleted() {
        for (MachineAction action : this.getAllActions()) {
            if (Boolean.FALSE.equals(action.getCompleted())) {
                return false;
            }
        }
        return true;
    }

    public void processCompletedAction(WorkOrder order, BoardCategoryEnum inventoryCategory) {
        int productCount = 0;
        NormalBoard inventory = null;
        int inventoryCount = 0;

        for (MachineAction action : this.getAllActions()) {
            String boardCategory = action.getBoardCategory();
            // 记录数目，最后统一写入，理由是一次机器动作中，成品、存货各自的规格和材质都是相同的:
            if (BoardCategoryEnum.PRODUCT.value.equals(boardCategory)) {
                productCount++;
            } else if (inventoryCategory.value.equals(boardCategory)) {
                if (inventory == null) {
                    inventory = new NormalBoard(action.getBoardSpecification(), action.getBoardMaterial(), inventoryCategory);
                }
                inventoryCount++;
            }
        }

        this.orderService.addOrderCompletedAmount(order, productCount);
        if (inventory != null) {
            this.inventoryService.addInventoryAmount(inventory, inventoryCount);
        }

        this.transferAllActions();
        this.clearActionTable();
    }

    public void addAction(ActionCategoryEnum category, BigDecimal dis, BaseBoard board, Integer orderId, String orderModule) {
        this.actionDao.addAction(category.value, dis, board.getCategory().value, board.getSpecStr(), board.getMaterial(), orderId, orderModule);
    }

    public void clearActionTable() {
        this.actionDao.truncateActionTable();
    }

    public void clearCompletedActionTable() {
        this.actionDao.truncateCompletedActionTable();
    }

    public Integer getActionCount() {
        return this.actionDao.getActionCount();
    }

    public Integer getCompletedActionCount() {
        return this.actionDao.getCompletedActionCount();
    }

    public List<MachineAction> getAllActions() {
        return this.actionDao.getAllActions();
    }

    public void completedAllActions() {
        this.actionDao.completedAllActions();
    }

    public void completedAction(Integer id) {
        this.actionDao.completedAction(id);
    }

    public void transferAllActions() {
        this.actionDao.transferAllActions();
    }
}
