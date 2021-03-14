package com.cat.mapper;

import com.cat.entity.bean.MachineAction;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ActionMapper {
    /**
     * 新增机器动作，默认状态为未完成。
     *
     * @param action 机器动作
     */
    void insertMachineAction(MachineAction action);

    /**
     * 清空机器动作表。
     */
    void truncateMachineAction();

    /**
     * 查询机器动作表记录数量。
     *
     * @return 记录数量
     */
    int getMachineActionCount();

    /**
     * 查询已处理动作表记录数量。
     *
     * @return 记录数量
     */
    int getProcessedActionCount();

    /**
     * 按照 ID 升序获取当前机器动作表中的所有动作。
     *
     * @return 机器动作集合
     */
    List<MachineAction> getAllMachineActions();

    /**
     * 查询当前机器动作表中最后一个机器动作的状态，不存在机器动作时将返回 null。
     *
     * @return 状态
     */
    String getFinalMachineActionState();

    /**
     * 将当前机器动作表中的所有机器动作状态置为已完成。
     */
    void completedAllMachineActions();

    /**
     * 将指定 ID 的机器动作状态置为已完成。
     *
     * @param id 动作 ID
     */
    void completedMachineActionById(Integer id);

    /**
     * 将机器动作表中的所有记录转移到已处理动作表。
     */
    void transferAllMachineActions();
}
