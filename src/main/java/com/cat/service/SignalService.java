package com.cat.service;

import com.cat.dao.SignalDao;
import com.cat.entity.CuttingSignal;
import com.cat.entity.StartSignal;
import com.cat.entity.TakeBoardSignal;
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
        StartSignal startSignal = this.getLatestStartSignal();
        if (startSignal != null && !startSignal.getProcessed()) {
            startSignal.setProcessed(true);
            this.signalDao.processedStartSignal(startSignal);
            return true;
        }
        return false;
    }

    public StartSignal getLatestStartSignal() {
        return this.signalDao.getLatestStartSignal();
    }

    public void insertStartSignal() {
        this.signalDao.insertStartSignal();
    }

    public TakeBoardSignal getLatestTakeBoardSignal() {
        return this.signalDao.getLatestTakeBoardSignal();
    }

    public void insertTakeBoardSignal(Integer orderId) {
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

    public void insertCuttingSignal(String cuttingSize, Boolean isLongToward, Integer orderId) {
        this.signalDao.insertCuttingSignal(cuttingSize, isLongToward, orderId);
    }
}
