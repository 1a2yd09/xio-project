package com.cat.dao;

import com.cat.entity.bean.MachineAction;
import com.cat.entity.board.BaseBoard;
import com.cat.enums.ActionCategory;
import com.cat.enums.ActionState;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author CAT
 */
@Component
public class ActionDao extends BaseDao {
    /**
     * 新增机器动作，状态默认为未完成。
     *
     * @param actionCategory 动作类型
     * @param baseBoard      板材
     * @param orderId        工单 ID
     */
    public void insertMachineAction(ActionCategory actionCategory, BaseBoard baseBoard, Integer orderId) {
        this.jdbcTemplate.update("INSERT INTO tb_machine_action (action_category, board_category, board_specification, board_material, order_id) " +
                "VALUES (?, ?, ?, ?, ?)", actionCategory.value, baseBoard.getCategory().value, baseBoard.getStandardSpecStr(), baseBoard.getMaterial(), orderId);
    }

    /**
     * 新增机器动作，状态默认为未完成。
     *
     * @param actionCategory 动作类型
     * @param dis            进刀距离
     * @param baseBoard      板材
     * @param orderId        工单 ID
     */
    public void insertMachineAction(ActionCategory actionCategory, BigDecimal dis, BaseBoard baseBoard, Integer orderId) {
        this.jdbcTemplate.update("INSERT INTO tb_machine_action (action_category, cut_distance, board_category, board_specification, board_material, order_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)", actionCategory.value, dis, baseBoard.getCategory().value, baseBoard.getStandardSpecStr(), baseBoard.getMaterial(), orderId);
    }

    /**
     * 清空机器动作表。
     */
    public void truncateMachineAction() {
        this.jdbcTemplate.update("TRUNCATE TABLE tb_machine_action");
    }

    /**
     * 查询机器动作表记录数量。
     *
     * @return 记录数量
     */
    public Integer getMachineActionCount() {
        return this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_machine_action", Integer.class);
    }

    /**
     * 查询已处理动作表记录数量。
     *
     * @return 记录数量
     */
    public Integer getProcessedActionCount() {
        return this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_processed_action", Integer.class);
    }

    /**
     * 按照 ID 顺序获取当前机器动作表中的所有动作。
     *
     * @return 机器动作集合
     */
    public List<MachineAction> getAllMachineActions() {
        return this.jdbcTemplate.query("SELECT * FROM tb_machine_action ORDER BY id", new BeanPropertyRowMapper<>(MachineAction.class));
    }

    /**
     * 查询当前机器动作表中最后一个机器动作的状态，不存在机器动作时将返回”未完成“。
     *
     * @return 状态
     */
    public String getFinalMachineActionState() {
        try {
            return this.jdbcTemplate.queryForObject("SELECT TOP 1 state FROM tb_machine_action ORDER BY id DESC", String.class);
        } catch (EmptyResultDataAccessException e) {
            return ActionState.INCOMPLETE.value;
        }
    }

    /**
     * 将当前机器动作表中的所有机器动作状态置为已完成。
     */
    public void completedAllMachineActions() {
        this.jdbcTemplate.update("UPDATE tb_machine_action SET state = ? WHERE state != ?", ActionState.COMPLETED.value, ActionState.COMPLETED.value);
    }

    /**
     * 将指定 ID 的机器动作状态置为已完成。
     *
     * @param id 动作 ID
     */
    public void completedMachineActionById(Integer id) {
        this.jdbcTemplate.update("UPDATE tb_machine_action SET state = ? WHERE id = ?", ActionState.COMPLETED.value, id);
    }

    /**
     * 将机器动作表中的所有记录转移到已处理动作表。
     */
    public void transferAllMachineActions() {
        this.jdbcTemplate.update("INSERT INTO tb_processed_action " +
                "SELECT id, state, action_category, cut_distance, board_category, board_specification, board_material, order_id, created_at " +
                "FROM tb_machine_action");
    }
}
