package com.cat.service;

import com.cat.dao.ActionDao;
import com.cat.entity.bean.MachineAction;
import com.cat.enums.ActionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author CAT
 */
@Component
public class ActionService {
    @Autowired
    ActionDao actionDao;

    /**
     * 查看当前机器动作表中的全部动作是否都被处理
     *
     * @return 结果
     */
    public boolean isAllMachineActionsCompleted() {
        // 如果机器动作表中的最后一个动作状态不为“未完成”，则表示全部机器动作都被处理完毕
        return !ActionState.NOT_FINISHED.value.equals(this.actionDao.getFinalMachineAction().getState());
    }

    /**
     * 查询机器动作表的记录数量
     *
     * @return 结果
     */
    public Integer getMachineActionCount() {
        return this.actionDao.getMachineActionCount();
    }

    /**
     * 查询完成动作表的记录数量
     *
     * @return 结果
     */
    public Integer getCompletedActionCount() {
        return this.actionDao.getCompletedActionCount();
    }

    /**
     * 查询全部机器动作
     *
     * @return 机器动作集合
     */
    public List<MachineAction> getAllMachineActions() {
        return this.actionDao.getAllMachineActions();
    }

    /**
     * 将全部机器动作的状态字段置为已完成
     */
    public void completedAllMachineActions() {
        this.actionDao.completedAllMachineActions();
    }

    /**
     * 将指定 ID 的机器动作状态置为已完成
     *
     * @param id 机器动作 ID
     */
    public void completedMachineActionById(Integer id) {
        this.actionDao.completedMachineActionById(id);
    }

    /**
     * 将机器动作表中的所有数据转移到完成动作表当中
     */
    public void transferAllMachineActions() {
        this.actionDao.transferAllMachineActions();
    }

    /**
     * 清空机器动作表
     */
    public void truncateMachineAction() {
        this.actionDao.truncateMachineAction();
    }
}
