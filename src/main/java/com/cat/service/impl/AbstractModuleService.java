package com.cat.service.impl;

import com.cat.enums.ForwardEdge;
import com.cat.enums.OrderSortPattern;
import com.cat.enums.SignalCategory;
import com.cat.pojo.CuttingSignal;
import com.cat.pojo.OperatingParameter;
import com.cat.pojo.WorkOrder;
import com.cat.service.*;
import com.cat.utils.OrderUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Deque;
import java.util.List;

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
    public void process() {
        boolean firstSend = true;
        while (true) {
            OperatingParameter param = this.parameterService.getLatestOperatingParameter();
            List<WorkOrder> orders = this.orderService.getStraightOrders(OrderSortPattern.get(param.getSortPattern()), param.getOrderDate());
            log.info("工单数量: {}", orders.size());
            if (!orders.isEmpty()) {
                WorkOrder order = orders.get(0);
                log.info("头部工单: {}", order);
                if (firstSend) {
                    System.out.println("流程刚启动，需要单独发送取板信号!");
                    this.signalService.sendTakeBoardSignal(order.getId());
                    firstSend = false;
                }
                // test:
                this.signalService.insertCuttingSignal("4.0×1485×3530", ForwardEdge.SHORT, new BigDecimal("15"), order.getId());
                this.signalService.waitingForSignal(SignalCategory.CUTTING, this.signalService::isReceivedNewCuttingSignal);
                CuttingSignal signal = this.signalService.getLatestCuttingSignal();
                log.info("下料信号: {}", signal);
                orders.forEach(System.out::println);
                orders = OrderUtil.filterOrderList(signal.getOrderId(), orders);
                order = orders.get(0);
                log.info("当前头部工单: {}", order);
                // 尝试报警，直接结束流程(交给前台去做):
                Integer nextOrderId = this.processOrder(param, signal, orders);
                // test:
                this.actionService.completedAllMachineActions();
                this.signalService.waitingForSignal(SignalCategory.ROTATE, this.actionService::isAllRotateActionsCompleted);
                this.signalService.sendTakeBoardSignal(nextOrderId);
                this.signalService.waitingForSignal(SignalCategory.ACTION, this.actionService::isAllMachineActionsProcessed);
                this.actionService.processAction();
            } else {
                break;
            }
        }
    }

    /**
     * 获取工单队列。
     *
     * @param orderDeque  运行时工单队列
     * @param sortPattern 排序方式
     * @param date        工单日期
     */
    protected abstract void getOrderDeque(Deque<WorkOrder> orderDeque, OrderSortPattern sortPattern, LocalDate date);

    /**
     * 处理当前工单。
     *
     * @param parameter     运行参数
     * @param cuttingSignal 下料信号
     * @param orders        处理当前工单需要的工单对象数组
     */
    protected abstract void processOrder(OperatingParameter parameter, CuttingSignal cuttingSignal, WorkOrder... orders);

    /**
     * 处理当前工单新逻辑。
     *
     * @param parameter     运行参数
     * @param cuttingSignal 下料信号
     * @param orderList     工单列表
     */
    protected abstract Integer processOrder(OperatingParameter parameter, CuttingSignal cuttingSignal, List<WorkOrder> orderList);

    /**
     * 获取处理当前工单需要的工单对象数组
     *
     * @param orderDeque 工单队列
     * @return 工单对象数组
     */
    protected abstract WorkOrder[] getProcessOrders(Deque<WorkOrder> orderDeque);
}
