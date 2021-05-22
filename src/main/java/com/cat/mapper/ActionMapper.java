package com.cat.mapper;

import com.cat.pojo.MachineAction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.SQLTimeoutException;
import java.util.List;

/**
 * @author CAT
 */
@Mapper
public interface ActionMapper {
    /**
     * 新增机器动作记录。
     *
     * @param action 机器动作
     */
    void insertMachineAction(MachineAction action);

    /**
     * 清空机器动作表。
     */
    void truncateMachineAction();

    /**
     * 统计机器动作表记录数量。
     *
     * @return 记录数量
     */
    int getMachineActionCount();

    /**
     * 统计已处理动作表记录数量。
     *
     * @return 记录数量
     */
    int getProcessedActionCount();

    /**
     * 按照 ID 升序获取机器动作表中的所有动作记录。
     *
     * @return 机器动作集合
     */
    List<MachineAction> getAllMachineActions();

    /**
     * 查询机器动作表中最后一个机器动作的状态，不存在机器动作时将返回 null。
     *
     * @return 状态
     * @throws SQLTimeoutException 查询超时
     */
    String getFinalMachineActionState() throws SQLTimeoutException;

    /**
     * 将机器动作表中的所有机器动作记录状态置为已完成。
     */
    void completedAllMachineActions();

    /**
     * 将指定 ID 的机器动作状态置为已完成。
     *
     * @param id 动作 ID
     */
    void completedMachineActionById(@Param("id") Integer id);

    /**
     * 将机器动作表中的所有记录转移到已处理动作表。
     */
    void transferAllMachineActions();

    /**
     * 返回机器动作表中未完成的旋转动作个数。
     *
     * @return 未完成旋转动作个数
     */
    int getIncompleteRotateActionCount();
}
