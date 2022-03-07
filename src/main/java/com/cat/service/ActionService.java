package com.cat.service;

import com.cat.enums.ActionState;
import com.cat.enums.BoardCategory;
import com.cat.mapper.ActionMapper;
import com.cat.pojo.Inventory;
import com.cat.pojo.MachineAction;
import com.cat.pojo.WorkOrder;
import com.cat.utils.BoardUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.SQLTimeoutException;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author CAT
 */
@Slf4j
@Service
public class ActionService {
    private final ActionMapper actionMapper;
    private final OrderService orderService;
    private final InventoryService inventoryService;

    public ActionService(ActionMapper actionMapper, OrderService orderService, InventoryService inventoryService) {
        this.actionMapper = actionMapper;
        this.orderService = orderService;
        this.inventoryService = inventoryService;
    }

    /**
     * 处理一组将被机器完成的动作。
     *
     * @param orderDeque 工单队列
     * @param orders     动作列表中涉及的工单
     */
    public void processAction(Deque<WorkOrder> orderDeque, WorkOrder... orders) {
        Map<Integer, Integer> map = new HashMap<>(16);

        for (MachineAction action : this.getAllMachineActions()) {
            if (ActionState.COMPLETED.value.equals(action.getState()) && BoardCategory.PRODUCT.value.equals(action.getBoardCategory())) {
                map.put(action.getOrderId(), map.getOrDefault(action.getOrderId(), 0) + 1);
            }
        }

        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            this.orderService.addOrderCompletedQuantity(entry.getKey(), entry.getValue());
        }

        this.transferAllActions();
    }

    public void processAction() {
        Map<Integer, Integer> map = new HashMap<>(16);
        Inventory inventory = null;
        int inventoryCount = 0;

        // 根据已完成的动作可以得知产出的成品板和库存板的数量
        for (MachineAction action : this.getAllMachineActions()) {
            if (ActionState.COMPLETED.value.equals(action.getState())) {
                String category = action.getBoardCategory();
                if (BoardCategory.PRODUCT.value.equals(category)) {
                    map.put(action.getOrderId(), map.getOrDefault(action.getOrderId(), 0) + 1);
                } else if (BoardCategory.STOCK.value.equals(category)) {
                    if (inventory == null) {
                        inventory = new Inventory(BoardUtil.getStandardSpecStr(action.getBoardSpecification()), action.getBoardMaterial(), category);
                    }
                    inventoryCount++;
                }
            }
        }

        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            this.orderService.addOrderCompletedQuantity(entry.getKey(), entry.getValue());
        }
        if (inventory != null) {
            inventory.setQuantity(inventoryCount);
            this.inventoryService.updateInventoryQuantity(inventory);
        }

        this.transferAllActions();
    }

    /**
     * 查看当前机器动作表中的全部动作是否都被完成。
     *
     * @return true 表示都被完成，否则表示未都被完成
     */
    public boolean isAllMachineActionsProcessed() {
        try {
            return ActionState.COMPLETED.value.equals(this.actionMapper.getFinalMachineActionState());
        } catch (SQLTimeoutException e) {
            return false;
        }
    }

    /**
     * 转移当前动作表中的所有动作至已处理动作表中，清空当前动作表。
     */
    public void transferAllActions() {
        this.transferAllMachineActions();
        this.truncateMachineAction();
    }

    /**
     * 统计机器动作表记录数量。
     *
     * @return 记录数量
     */
    public Integer getMachineActionCount() {
        return this.actionMapper.getMachineActionCount();
    }

    /**
     * 统计已处理动作表记录数量。
     *
     * @return 记录数量
     */
    public Integer getProcessedActionCount() {
        return this.actionMapper.getProcessedActionCount();
    }

    /**
     * 按照 ID 升序获取机器动作表中的所有动作记录。
     *
     * @return 机器动作集合
     */
    public List<MachineAction> getAllMachineActions() {
        return this.actionMapper.getAllMachineActions();
    }

    /**
     * 将机器动作表中的所有机器动作记录状态置为已完成。
     */
    public void completedAllMachineActions() {
        this.actionMapper.completedAllMachineActions();
    }

    /**
     * 将指定 ID 的机器动作状态置为已完成。
     *
     * @param id 动作 ID
     */
    public void completedMachineActionById(Integer id) {
        this.actionMapper.completedMachineActionById(id);
    }

    /**
     * 将机器动作表中的所有记录转移到已处理动作表。
     */
    public void transferAllMachineActions() {
        this.actionMapper.transferAllMachineActions();
    }

    /**
     * 清空机器动作表。
     */
    public void truncateMachineAction() {
        this.actionMapper.truncateMachineAction();
    }

    /**
     * 确认剩余未完成动作是否包含旋转动作
     *
     * @return true 表示不包含旋转动作，否则包含旋转动作
     */
    public boolean isAllRotateActionsCompleted() {
        return this.actionMapper.getIncompleteRotateActionCount() == 0;
    }
}
