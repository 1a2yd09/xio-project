package com.cat.service;

import com.cat.dao.SignalDao;
import com.cat.entity.CuttingSignal;
import com.cat.entity.Signal;
import com.cat.entity.StartSignal;
import com.cat.entity.enums.SignalCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SignalService implements Clearable {
    @Autowired
    SignalDao signalDao;

    public boolean isReceivedNewSignal(SignalCategory category) {
        Signal signal = this.getLatestSignal(category);
        if (signal != null && !signal.getProcessed()) {
            this.signalDao.processedSignal(signal.getId());
            return true;
        }
        return false;
    }

    public Signal getLatestSignal(SignalCategory category) {
        return this.signalDao.getLatestSignal(category.value);
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

    public boolean isReceivedNewStartSignal() {
        StartSignal startSignal = this.signalDao.getLatestStartSignal();
        if (startSignal != null && !startSignal.getProcessed()) {
            this.signalDao.processedStartSignal(startSignal.getId());
            return true;
        }
        return false;
    }

    public StartSignal getLatestStartSignal() {
        return this.signalDao.getLatestStartSignal();
    }

    public void addNewStartSignal() {
        this.signalDao.insertStartSignal();
    }

    public void addNewTakeBoardSignal(Integer orderId) {
        this.signalDao.insertTakeBoardSignal(orderId);
    }

    public CuttingSignal getLatestNotProcessedCuttingSignal() {
        CuttingSignal cuttingSignal = this.signalDao.getLatestCuttingSignal();
        if (cuttingSignal != null && !cuttingSignal.getProcessed()) {
            this.signalDao.processedCuttingSignal(cuttingSignal.getId());
            return cuttingSignal;
        }
        return null;
    }
}
