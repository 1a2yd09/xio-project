package com.cat.service;

import com.cat.dao.SignalDao;
import com.cat.entity.Signal;
import com.cat.entity.enums.SignalCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SignalService implements Clearable {
    @Autowired
    SignalDao signalDao;

    public boolean isReceivedNewSignal(SignalCategory category) {
        Signal signal = this.signalDao.getLatestSignal(category.value);
        if (signal != null && !signal.getProcessed()) {
            this.signalDao.processedSignal(signal.getId());
            return true;
        }
        return false;
    }

    public void addNewSignal(SignalCategory category) {
        this.signalDao.insertSignal(category.value);
    }

    public void clearSignalTable() {
        this.signalDao.truncateTable();
    }

    @Override
    public void clearTable() {
        this.clearSignalTable();
    }
}
