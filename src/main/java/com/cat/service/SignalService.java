package com.cat.service;

import com.cat.enums.ControlSignalCategory;
import com.cat.enums.ForwardEdge;
import com.cat.enums.SignalCategory;
import com.cat.mapper.SignalMapper;
import com.cat.pojo.CuttingSignal;
import com.cat.pojo.ProcessControlSignal;
import com.cat.pojo.TakeBoardSignal;
import com.cat.pojo.WorkOrder;
import com.cat.utils.BoardUtil;
import com.cat.utils.SynUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.function.BooleanSupplier;

/**
 * @author CAT
 */
@Slf4j
@Service
public class SignalService {
    private final SignalMapper signalMapper;
    private final ThreadPoolTaskScheduler scheduler;

    public SignalService(SignalMapper signalMapper, @Qualifier("serviceTaskScheduler") ThreadPoolTaskScheduler scheduler) {
        this.signalMapper = signalMapper;
        this.scheduler = scheduler;
    }

    /**
     * 根据函数式接口事件，等待特定的信号到达。
     *
     * @param supplier 无参且返回布尔值的函数
     */
    public void waitingForSignal(SignalCategory sc, BooleanSupplier supplier) {
        log.info("等待{}信号到达...", sc.getName());
        CountDownLatch cdl = new CountDownLatch(1);
        ScheduledFuture<?> sf = this.scheduler.scheduleWithFixedDelay(() -> {
            if (supplier.getAsBoolean()) {
                cdl.countDown();
            }
        }, 1000);
        try {
            cdl.await();
            sf.cancel(false);
            log.info("{}信号到达...", sc.getName());
        } catch (InterruptedException e) {
            log.warn("等待信号过程中出现异常: ", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 等待新的流程启动信号。
     */
    public void checkStartSignal() {
        log.info("等待流程启动信号...");
        while (true) {
            try {
                SynUtil.getStartControlMessageQueue().take();
                break;
            } catch (Exception e) {
                log.warn("等待流程启动信号过程中出现异常: ", e);
                Thread.currentThread().interrupt();
            }
        }
        log.info("获取到新的流程启动信号...");
    }

    /**
     * 是否有新的流程停止信号。
     *
     * @return true 表示有新的流程停止信号
     */
    public boolean checkStopSignal() {
        log.info("检查流程停止信号...");
        Integer flag = SynUtil.getStopControlMessageQueue().poll();
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
     * 获取最新的流程控制信号对象。
     *
     * @return 信号对象
     */
    public ProcessControlSignal getLatestControlSignal() {
        return this.signalMapper.getLatestControlSignal();
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
     * 发送取板信号。
     *
     * @param order 工单对象
     */
    public void sendTakeBoardSignal(WorkOrder order) {
        if (order != null) {
            this.signalMapper.insertTakeBoardSignal(order.getId());
        }
    }

    /**
     * 获取最新未被处理的下料信号，不存在未被处理的下料信号时将返回 null。
     *
     * @return 下料信号
     */
    public CuttingSignal getLatestUnProcessedCuttingSignal() {
        CuttingSignal cuttingSignal = this.signalMapper.getLatestUnProcessedCuttingSignal();
        if (cuttingSignal != null) {
            cuttingSignal.setProcessed(true);
            this.signalMapper.updateCuttingSignal(cuttingSignal);
            return cuttingSignal;
        }
        return null;
    }

    /**
     * 查看是否收到有效的下料新信号。
     *
     * @return true 表示收到有效的下料新信号，否则表示未接收到有效的下料新信号
     */
    public boolean isReceivedNewCuttingSignal() {
        CuttingSignal signal = this.signalMapper.getLatestUnProcessedCuttingSignal();
        if (signal != null) {
            signal.setProcessed(true);
            this.signalMapper.updateCuttingSignal(signal);
            return BoardUtil.isValidSpec(signal.getCuttingSize());
        } else {
            return false;
        }
    }

    /**
     * 获取最新的下料信号。
     *
     * @return 下料信号对象
     */
    public CuttingSignal getLatestCuttingSignal() {
        return this.signalMapper.getLatestCuttingSignal();
    }

    /**
     * 新增下料信号。
     *
     * @param cuttingSize 下料板尺寸
     * @param forwardEdge 下料板朝向，0表示短边朝前，1表示长边朝前
     * @param orderId     工单 ID
     */
    public void insertCuttingSignal(String cuttingSize, ForwardEdge forwardEdge, BigDecimal longEdgeTrim, Integer orderId) {
        this.signalMapper.insertCuttingSignal(cuttingSize, forwardEdge.code, longEdgeTrim, orderId);
    }
}
