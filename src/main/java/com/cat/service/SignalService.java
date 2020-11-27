package com.cat.service;

import com.cat.dao.SignalDao;
import com.cat.entity.signal.CuttingSignal;
import com.cat.entity.signal.StartSignal;
import com.cat.entity.signal.TakeBoardSignal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author CAT
 */
@Component
public class SignalService {
    @Autowired
    SignalDao signalDao;

    /**
     * 是否接收到新的开工信号。
     *
     * @return true 表示接收到新的开工信号，false 表示未接收到新的开工信号
     */
    public boolean isReceivedNewStartSignal() {
        StartSignal startSignal = this.getLatestNotProcessedStartSignal();
        if (startSignal != null) {
            startSignal.setProcessed(true);
            this.signalDao.updateStartSignalProcessed(startSignal);
            return true;
        }
        return false;
    }

    /**
     * 查询最新未被处理的开工信号，不存在未被处理的开工信号时返回 null。
     *
     * @return 开工信号
     */
    public StartSignal getLatestNotProcessedStartSignal() {
        return this.signalDao.getLatestNotProcessedStartSignal();
    }

    /**
     * 新增开工信号。
     */
    public void insertStartSignal() {
        this.signalDao.insertStartSignal();
    }

    /**
     * 查询最新的取板信号，数据表为空时返回 null。
     *
     * @return 取板信号
     */
    public TakeBoardSignal getLatestTakeBoardSignal() {
        return this.signalDao.getLatestTakeBoardSignal();
    }

    /**
     * 新增取板信号。
     *
     * @param orderId 工单 ID
     */
    public void insertTakeBoardSignal(Integer orderId) {
        this.signalDao.insertTakeBoardSignal(orderId);
    }

    /**
     * 获取最新未被处理的下料信号，不存在未被处理的下料信号时将返回 null。
     *
     * @return 下料信号
     */
    public CuttingSignal getLatestNotProcessedCuttingSignal() {
        CuttingSignal cuttingSignal = this.signalDao.getLatestNotProcessedCuttingSignal();
        if (cuttingSignal != null) {
            cuttingSignal.setProcessed(true);
            this.signalDao.updateCuttingSignalProcessed(cuttingSignal);
            return cuttingSignal;
        }
        return null;
    }

    /**
     * 新增下料信号。
     *
     * @param cuttingSize 下料板尺寸
     * @param forwardEdge 下料板朝向，0表示短边朝前，1表示长边朝前
     * @param orderId     工单 ID
     */
    public void insertCuttingSignal(String cuttingSize, Integer forwardEdge, Integer orderId) {
        this.signalDao.insertCuttingSignal(cuttingSize, forwardEdge, orderId);
    }
}
