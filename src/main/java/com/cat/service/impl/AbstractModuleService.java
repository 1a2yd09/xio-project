package com.cat.service.impl;

import com.cat.enums.OrderSortPattern;
import com.cat.enums.SignalCategory;
import com.cat.pojo.CuttingSignal;
import com.cat.pojo.OperatingParameter;
import com.cat.pojo.WorkOrder;
import com.cat.pojo.message.OrderMessage;
import com.cat.service.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Deque;

/**
 * @author CAT
 */
@Slf4j
@Getter
public abstract class AbstractModuleService implements ModuleService {
    private final SignalService signalService;
    private final ActionService actionService;
    private final OrderService orderService;
    private final ParameterService parameterService;
    private final ProcessBoardService processBoardService;

    protected AbstractModuleService(SignalService signalService, ActionService actionService, OrderService orderService, ParameterService parameterService, ProcessBoardService processBoardService) {
        this.signalService = signalService;
        this.actionService = actionService;
        this.orderService = orderService;
        this.parameterService = parameterService;
        this.processBoardService = processBoardService;
    }

    @Override
    public void process(OperatingParameter param) {
        Deque<WorkOrder> orderDeque = this.getOrderDeque(OrderSortPattern.get(param.getSortPattern()), param.getOrderDate());
        log.info("工单数量: {}", orderDeque.size());
        this.signalService.sendTakeBoardSignal(orderDeque.peekFirst());
        while (!orderDeque.isEmpty()) {
            WorkOrder order = orderDeque.peekFirst();
            log.info("当前工单: {}", order);
            this.signalService.waitingForSignal(SignalCategory.CUTTING, this.signalService::isReceivedNewCuttingSignal);
            CuttingSignal signal = this.signalService.getLatestCuttingSignal();
            MainService.RUNNING_ORDER.set(OrderMessage.of(order, signal));
            log.info("下料信号: {}", signal);
            OperatingParameter op = this.parameterService.getLatestOperatingParameter();
            orderDeque.addAll(this.getOrderDeque(OrderSortPattern.get(op.getSortPattern()), op.getOrderDate()));
            WorkOrder[] processOrders = this.getProcessOrders(orderDeque);
            this.processOrder(op, signal, processOrders);
            this.actionService.processAction(orderDeque, processOrders);
            this.signalService.waitingForSignal(SignalCategory.ROTATE, this.actionService::isAllRotateActionsCompleted);
            this.signalService.sendTakeBoardSignal(orderDeque.peekFirst());
            this.signalService.waitingForSignal(SignalCategory.ACTION, this.actionService::isAllMachineActionsProcessed);
            this.actionService.transferAllActions();
            if (this.signalService.checkStopSignal()) {
                log.info("收到流程停止信号...");
                return;
            }
        }
    }

    /**
     * 获取工单队列。
     *
     * @param sortPattern 排序方式
     * @param date        工单日期
     * @return 工单队列
     */
    protected abstract Deque<WorkOrder> getOrderDeque(OrderSortPattern sortPattern, LocalDate date);

    /**
     * 处理当前工单。
     *
     * @param parameter     运行参数
     * @param cuttingSignal 下料信号
     * @param orders        处理当前工单需要的工单对象数组
     */
    protected abstract void processOrder(OperatingParameter parameter, CuttingSignal cuttingSignal, WorkOrder... orders);

    /**
     * 获取处理当前工单需要的工单对象数组
     *
     * @param orderDeque 工单队列
     * @return 工单对象数组
     */
    protected abstract WorkOrder[] getProcessOrders(Deque<WorkOrder> orderDeque);
}
