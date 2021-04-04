package com.cat.service;

import com.cat.enums.ControlSignalCategory;
import com.cat.enums.ForwardEdge;
import com.cat.mapper.SignalMapper;
import com.cat.pojo.CuttingSignal;
import com.cat.pojo.TakeBoardSignal;
import com.cat.pojo.WorkOrder;
import com.cat.utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author CAT
 */
@Slf4j
@Service
public class SignalService {
    private final SignalMapper signalMapper;

    public SignalService(SignalMapper signalMapper) {
        this.signalMapper = signalMapper;
    }

    /**
     * 接收新的下料信号。
     *
     * @param order 工单
     * @return 下料信号
     */
    public CuttingSignal receiveNewCuttingSignal(WorkOrder order) {
        // test:
        this.insertCuttingSignal(order.getCuttingSize(), ForwardEdge.SHORT, order.getId());
        log.info("等待下料信号...");
        CuttingSignal signal;
        while (true) {
            try {
                signal = ThreadUtil.getCuttingMessageQueue().take();
                break;
            } catch (Exception e) {
                log.warn("interrupted!", e);
            }
        }
        log.info("获取到新的下料信号...");
        return signal;
    }

    /**
     * 等待新的流程启动信号。
     */
    public void waitingForNewProcessStartSignal() {
        // test:
        this.insertProcessControlSignal(ControlSignalCategory.START);
        log.info("等待流程启动信号...");
        while (true) {
            try {
                ThreadUtil.getStartControlMessageQueue().take();
                break;
            } catch (Exception e) {
                log.warn("interrupted!", e);
            }
        }
        log.info("获取到新的流程启动信号...");
    }

    /**
     * 是否有新的流程停止信号。
     *
     * @return true 表示有新的流程停止信号
     */
    public boolean checkingForNewProcessStopSignal() {
        log.info("检查流程停止信号...");
        Integer flag = ThreadUtil.getStopControlMessageQueue().poll();
        return flag != null;
    }

    /**
     * 新增流程控制信号。
     *
     * @param signalCategory 控制信号枚举类型
     */
    public void insertProcessControlSignal(ControlSignalCategory signalCategory) {
        this.signalMapper.insertProcessControlSignal(signalCategory.value);
    }

    /**
     * 查询最新的取板信号，数据表为空时返回 null。
     *
     * @return 取板信号
     */
    public TakeBoardSignal getLatestTakeBoardSignal() {
        return this.signalMapper.getLatestTakeBoardSignal();
    }

    /**
     * 新增取板信号。
     *
     * @param orderId 工单 ID
     */
    public void insertTakeBoardSignal(Integer orderId) {
        this.signalMapper.insertTakeBoardSignal(orderId);
    }

    /**
     * 获取最新未被处理的下料信号，不存在未被处理的下料信号时将返回 null。
     *
     * @return 下料信号
     */
    public CuttingSignal getLatestNotProcessedCuttingSignal() {
        CuttingSignal cuttingSignal = this.signalMapper.getLatestNotProcessedCuttingSignal();
        if (cuttingSignal != null) {
            cuttingSignal.setProcessed(true);
            this.signalMapper.updateCuttingSignal(cuttingSignal);
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
        this.signalMapper.insertCuttingSignal(cuttingSize, forwardEdge.code, orderId);
    }
}
