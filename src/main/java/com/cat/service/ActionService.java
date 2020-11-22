package com.cat.service;

import com.cat.dao.ActionDao;
import com.cat.entity.MachineAction;
import com.cat.entity.enums.ActionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ActionService implements Clearable {
    @Autowired
    ActionDao actionDao;

    public boolean isAllMachineActionsCompleted() {
        return !ActionState.NOT_FINISHED.value.equals(this.actionDao.getFinalMachineAction().getState());
    }

    public Integer getMachineActionCount() {
        return this.actionDao.getMachineActionCount();
    }

    public Integer getCompletedActionCount() {
        return this.actionDao.getCompletedActionCount();
    }

    public List<MachineAction> getAllMachineActions() {
        return this.actionDao.getAllMachineActions();
    }

    public void completedAllMachineActions() {
        this.actionDao.completedAllMachineActions();
    }

    public void completedMachineActionById(Integer id) {
        this.actionDao.completedMachineActionById(id);
    }

    public void transferAllMachineActions() {
        this.actionDao.transferAllMachineActions();
    }

    public void truncateMachineAction() {
        this.actionDao.truncateMachineAction();
    }

    @Override
    public void clearTable() {
        this.actionDao.truncateMachineAction();
        this.actionDao.truncateCompletedAction();
    }
}
