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
            // 从tb_operating_parameter中查出参数，因为针对不同的工单可能会有不同的参数
            OperatingParameter param = this.parameterService.getLatestOperatingParameter();
            // 从tb_new_local_work_order中根据当前日期查出GDMK（工单模块）是"直梁工地模块"和"对重架工地模块"的记录，并按照参数中的sortPattern进行排序
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
                // test: 假设硬件设备已获取到原料板，斯科奇将原料版数据写入tb_cutting_signal数据表
                this.signalService.insertCuttingSignal("4.0×1500×3600", ForwardEdge.SHORT, BigDecimal.TEN, order.getId());

                // 我们从tb_cutting_signal数据库中获取记录，如果没有则阻塞等待
                this.signalService.waitingForSignal(SignalCategory.CUTTING, this.signalService::isReceivedNewCuttingSignal);
                // 成功获取到信号
                CuttingSignal signal = this.signalService.getLatestCuttingSignal();
                log.info("下料信号: {}", signal);
                orders.forEach(System.out::println);
                // 尝试报警，直接结束流程(交给前台去做):
                Integer nextOrderId = this.processOrder(param, signal, orders);
                // test:
                this.actionService.completedAllMachineActions();

                // 当所有旋转信号被处理了之后，吸盘就可以空闲出来去做取板任务
                this.signalService.waitingForSignal(SignalCategory.ROTATE, this.actionService::isAllRotateActionsCompleted);
                // 向数据表tb_take_board_signal中写入取板信号，让机械吸盘去取板
                this.signalService.sendTakeBoardSignal(nextOrderId);
                // 等待所有动作完成
                this.signalService.waitingForSignal(SignalCategory.ACTION, this.actionService::isAllMachineActionsProcessed);
                // 根据动作表中已完成的动作统计产出的板的种类、数量
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
