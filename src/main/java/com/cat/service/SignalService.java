package com.cat.service;

import com.cat.dao.SignalDao;
import com.cat.entity.CuttingSignal;
import com.cat.entity.StartSignal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SignalService implements Clearable {
    @Autowired
    SignalDao signalDao;

    @Override
    public void clearTable() {
        this.signalDao.truncateStartSignal();
        this.signalDao.truncateTakeBoardSignal();
        this.signalDao.truncateCuttingSignal();
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

    public void insertCuttingSignal(String specification, Boolean longToward, int orderId) {
        this.signalDao.insertCuttingSignal(specification, longToward, orderId);
    }
}
