package com.cat.dao;

import com.cat.entity.bean.MachineAction;
import com.cat.entity.board.BaseBoard;
import com.cat.enums.ActionCategory;
import com.cat.enums.ActionState;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author CAT
 */
@Component
public class ActionDao extends BaseDao {
    RowMapper<MachineAction> actionM = new BeanPropertyRowMapper<>(MachineAction.class);

    /**
     * 新增机器动作
     *
     * @param actionCategory 动作类型
     * @param dis            进刀距离
     * @param baseBoard      板材
     * @param orderId        工单 ID
     */
    public void insertMachineAction(ActionCategory actionCategory, BigDecimal dis, BaseBoard baseBoard, Integer orderId) {
        this.jdbcTemplate.update("INSERT INTO tb_machine_action (action_category, cut_distance, board_category, board_specification, board_material, work_order_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)", actionCategory.value, dis, baseBoard.getCategory().value, baseBoard.getSpecStr(), baseBoard.getMaterial(), orderId);
    }

    /**
     * 清空机器动作表
     */
    public void truncateMachineAction() {
        this.jdbcTemplate.update("TRUNCATE TABLE tb_machine_action");
    }

    /**
     * 查询机器动作表的记录数量
     *
     * @return 结果
     */
    public Integer getMachineActionCount() {
        return this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_machine_action", Integer.class);
    }

    /**
     * 查询完成动作表的记录数量
     *
     * @return 结果
     */
    public Integer getCompletedActionCount() {
        return this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_completed_action", Integer.class);
    }

    /**
     * 查询全部机器动作
     *
     * @return 机器动作集合
     */
    public List<MachineAction> getAllMachineActions() {
        return this.jdbcTemplate.query("SELECT * FROM tb_machine_action ORDER BY id", this.actionM);
    }

    /**
     * 查询机器动作表中的最后一个机器动作状态，不存在机器动作时将返回”未完成“
     *
     * @return 状态
     */
    public String getFinalMachineActionState() {
        try {
            return this.jdbcTemplate.queryForObject("SELECT TOP 1 state FROM tb_machine_action ORDER BY id DESC", String.class);
        } catch (EmptyResultDataAccessException e) {
            return ActionState.NOT_FINISHED.value;
        }
    }

    /**
     * 将全部机器动作的状态字段置为已完成
     */
    public void completedAllMachineActions() {
        this.jdbcTemplate.update("UPDATE tb_machine_action SET state = ? WHERE state = ?", ActionState.FINISHED.value, ActionState.NOT_FINISHED.value);
    }

    /**
     * 将指定 ID 的机器动作状态置为已完成
     *
     * @param id 机器动作 ID
     */
    public void completedMachineActionById(Integer id) {
        this.jdbcTemplate.update("UPDATE tb_machine_action SET state = ? WHERE id = ?", ActionState.FINISHED.value, id);
    }

    /**
     * 将机器动作表中的所有数据转移到完成动作表当中
     */
    public void transferAllMachineActions() {
        this.jdbcTemplate.update("INSERT INTO tb_completed_action " +
                "SELECT id, state, action_category, cut_distance, board_category, board_specification, board_material, work_order_id " +
                "FROM tb_machine_action");
    }
}
