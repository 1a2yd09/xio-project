package com.cat.service;

import com.cat.dao.SignalDao;
import com.cat.entity.bean.WorkOrder;
import com.cat.entity.signal.CuttingSignal;
import com.cat.entity.signal.ProcessControlSignal;
import com.cat.entity.signal.TakeBoardSignal;
import com.cat.enums.ControlSignalCategory;
import com.cat.enums.ForwardEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author CAT
 */
@Component
public class SignalService {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    SignalDao signalDao;

    /**
     * 接收新的下料信号。
     *
     * @param order 工单
     * @return 下料信号
     * @throws InterruptedException 接收过程被中断
     */
    public CuttingSignal receiveNewCuttingSignal(WorkOrder order) throws InterruptedException {
        // test:
        this.insertCuttingSignal(order.getCuttingSize(), ForwardEdge.SHORT, order.getId());
        while (true) {
            logger.info("等待下料信号...");
            CuttingSignal cuttingSignal = this.getLatestNotProcessedCuttingSignal();
            if (cuttingSignal != null) {
                logger.info("获取到新的下料信号...");
                return cuttingSignal;
            }
            TimeUnit.SECONDS.sleep(3);
        }
    }

    /**
     * 等待新的流程启动信号。
     *
     * @throws InterruptedException 等待过程被中断
     */
    public void waitingForNewProcessStartSignal() throws InterruptedException {
        // test:
        this.insertProcessControlSignal(ControlSignalCategory.START);
        while (!this.isReceivedNewProcessControlSignal(ControlSignalCategory.START)) {
            logger.info("等待流程启动信号...");
            TimeUnit.SECONDS.sleep(3);
        }
        logger.info("获取到新的流程启动信号...");
    }

    /**
     * 是否接收到新的流程控制信号。
     *
     * @param signalCategory 流程控制信号枚举类型
     * @return true 表示接收到新的控制信号，false 表示未接收到新的控制信号
     */
    public boolean isReceivedNewProcessControlSignal(ControlSignalCategory signalCategory) {
        ProcessControlSignal controlSignal = this.getLatestNotProcessedControlSignal(signalCategory);
        if (controlSignal != null) {
            controlSignal.setProcessed(true);
            this.signalDao.updateProcessControlSignalProcessed(controlSignal);
            return true;
        }
        return false;
    }

    /**
     * 根据控制信号类型查询最新未被处理的控制信号，不存在未被处理的控制信号时返回 null。
     *
     * @param signalCategory 控制信号枚举类型
     * @return 控制信号
     */
    public ProcessControlSignal getLatestNotProcessedControlSignal(ControlSignalCategory signalCategory) {
        return this.signalDao.getLatestNotProcessedControlSignal(signalCategory.value);
    }

    /**
     * 新增流程控制信号。
     *
     * @param signalCategory 控制信号枚举类型
     */
    public void insertProcessControlSignal(ControlSignalCategory signalCategory) {
        this.signalDao.insertProcessControlSignal(signalCategory.value);
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
    public void insertCuttingSignal(String cuttingSize, ForwardEdge forwardEdge, Integer orderId) {
        this.signalDao.insertCuttingSignal(cuttingSize, forwardEdge, orderId);
    }
}
