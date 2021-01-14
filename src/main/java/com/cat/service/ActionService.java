package com.cat.service;

import com.cat.dao.ActionDao;
import com.cat.entity.bean.MachineAction;
import com.cat.enums.ActionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author CAT
 */
@Component
public class ActionService {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    ActionDao actionDao;

    /**
     * 等待所有机器动作都被处理完毕。
     *
     * @throws InterruptedException 等待过程被中断
     */
    public void waitingForAllMachineActionsCompleted() throws InterruptedException {
        // test:
        this.completedAllMachineActions();
        while (!this.isAllMachineActionsProcessed()) {
            logger.info("等待动作处理...");
            TimeUnit.SECONDS.sleep(3);
        }
        logger.info("动作处理完毕...");
    }

    /**
     * 查看当前机器动作表中的全部动作是否都被处理。
     *
     * @return true 表示都被处理，false 表示未都被处理
     */
    public boolean isAllMachineActionsProcessed() {
        // 如果机器动作表中的最后一个动作状态不为“未完成”，则表示全部机器动作都被处理完毕:
        return !ActionState.INCOMPLETE.value.equals(this.actionDao.getFinalMachineActionState());
    }

    /**
     * 查询机器动作表记录数量。
     *
     * @return 记录数量
     */
    public Integer getMachineActionCount() {
        return this.actionDao.getMachineActionCount();
    }

    /**
     * 查询已处理动作表记录数量。
     *
     * @return 记录数量
     */
    public Integer getProcessedActionCount() {
        return this.actionDao.getProcessedActionCount();
    }

    /**
     * 按照 ID 顺序获取当前机器动作表中的所有动作。
     *
     * @return 机器动作集合
     */
    public List<MachineAction> getAllMachineActions() {
        return this.actionDao.getAllMachineActions();
    }

    /**
     * 将当前机器动作表中的所有机器动作状态置为已完成。
     */
    public void completedAllMachineActions() {
        this.actionDao.completedAllMachineActions();
    }

    /**
     * 将指定 ID 的机器动作状态置为已完成。
     *
     * @param id 动作 ID
     */
    public void completedMachineActionById(Integer id) {
        this.actionDao.completedMachineActionById(id);
    }

    /**
     * 将机器动作表中的所有记录转移到已处理动作表。
     */
    public void transferAllMachineActions() {
        this.actionDao.transferAllMachineActions();
    }

    /**
     * 清空机器动作表。
     */
    public void truncateMachineAction() {
        this.actionDao.truncateMachineAction();
    }
}
