package com.cat.service;

import com.cat.enums.BoardCategory;
import com.cat.enums.OrderModule;
import com.cat.enums.OrderSortPattern;
import com.cat.enums.OrderState;
import com.cat.mapper.OrderMapper;
import com.cat.pojo.CutBoard;
import com.cat.pojo.CuttingSignal;
import com.cat.pojo.NormalBoard;
import com.cat.pojo.WorkOrder;
import com.cat.pojo.dto.OrderCount;
import com.cat.utils.BoardUtil;
import com.cat.utils.OrderComparator;
import com.cat.utils.OrderUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author CAT
 */
@Service
public class OrderService {
    private final OrderMapper orderMapper;

    public OrderService(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    /**
     * 修改工单的已完工数目，如果工单未完成数目为零，则修改工单状态为已完工，并迁移至完工表中。
     *
     * @param order    工单对象
     * @param quantity 新完工的成品数目
     */
    public void addOrderCompletedQuantity(WorkOrder order, int quantity) {
        order.setCompletedQuantity(OrderUtil.addQuantityPropWithInt(order.getCompletedQuantity(), quantity));
        this.orderMapper.updateOrderCompletedQuantity(order);
        order.setOperationState(order.getIncompleteQuantity() == 0 ? OrderState.COMPLETED.value : OrderState.STARTED.value);
        this.orderMapper.updateOrderState(order);
        if (order.getIncompleteQuantity() == 0) {
            this.transferWorkOrderToCompleted(order.getId());
        }
    }

    public void addOrderCompletedQuantity(Integer orderId, int quantity) {
        WorkOrder order = this.orderMapper.getOrderById(orderId);
        order.setCompletedQuantity(OrderUtil.addQuantityPropWithInt(order.getCompletedQuantity(), quantity));
        this.orderMapper.updateOrderCompletedQuantity(order);
        order.setOperationState(order.getIncompleteQuantity() == 0 ? OrderState.COMPLETED.value : OrderState.STARTED.value);
        this.orderMapper.updateOrderState(order);
        if (order.getIncompleteQuantity() == 0) {
            this.transferWorkOrderToCompleted(order.getId());
        }
    }

    /**
     * 根据排序方式以及计划完工日期获取有效并排序的对重直梁工单集合。
     *
     * @param date 计划完工日期
     * @return 对重直梁工单集合
     */
    public List<WorkOrder> getStraightOrders(OrderSortPattern sortPattern, LocalDate date) {
        return this.orderMapper.getStraightOrders(Map.of(
                "module1", OrderModule.STRAIGHT.getName(),
                "module2", OrderModule.WEIGHT.getName(),
                "date", date.toString()
        )).stream()
                .filter(OrderUtil::validateOrder)
                .filter(order -> order.getIncompleteQuantity() > 0)
                .sorted((o1, o2) -> OrderComparator.getComparator(sortPattern.name()).compare(o1, o2))
                .collect(Collectors.toList());
    }

    /**
     * 根据排序方式以及计划完工日期获取有效、排序、预处理的对重直梁工单队列。
     *
     * @param sortPattern 工单排序方式
     * @param date        计划完工日期
     * @return 对重直梁工单队列
     */
    public Deque<WorkOrder> getPreprocessStraightDeque(OrderSortPattern sortPattern, LocalDate date) {
        return new LinkedList<>(this.getStraightOrders(sortPattern, date));
    }

    /**
     * 根据排序方式以及计划完工日期获取有效并排序的轿底工单集合。
     *
     * @param sortPattern 工单排序方式
     * @param date        计划完工日期
     * @return 轿底工单集合
     */
    public List<WorkOrder> getBottomOrders(OrderSortPattern sortPattern, LocalDate date) {
        return this.orderMapper.getBottomOrders(Map.of(
                "module1", OrderModule.BOTTOM.getName(),
                "date", date.toString()
        )).stream()
                .filter(OrderUtil::validateOrder)
                .filter(order -> order.getIncompleteQuantity() > 0)
                .sorted((o1, o2) -> OrderComparator.getComparator(sortPattern.name()).compare(o1, o2))
                .collect(Collectors.toList());
    }

    /**
     * 根据排序方式以及计划完工日期获取有效并排序的轿底工单队列。
     *
     * @param sortPattern 工单排序方式
     * @param date        计划完工日期
     * @return 轿底工单队列
     */
    public Deque<WorkOrder> getBottomDeque(OrderSortPattern sortPattern, LocalDate date) {
        return new LinkedList<>(this.getBottomOrders(sortPattern, date));
    }

    /**
     * 根据日期范围获取完工工单的数量情况。
     *
     * @param range 范围
     * @return 范围内每个日期的工单完成数量
     */
    public List<OrderCount> getCompletedOrderCountByRange(int range) {
        LocalDate now = LocalDate.now();
        List<OrderCount> result = new ArrayList<>(range);
        for (int i = 0; i < range; i++) {
            LocalDate date = now.minusDays(i);
            result.add(new OrderCount(date.toString(), this.orderMapper.getCompletedOrderCountByDate(date)));
        }
        return result;
    }

    /**
     * 根据工单 ID 获取指定工单，不存在指定 ID 工单时将返回 null。
     *
     * @param id 工单 ID
     * @return 工单
     */
    public WorkOrder getOrderById(Integer id) {
        return this.orderMapper.getOrderById(id);
    }

    /**
     * 根据工单 ID 获取指定完成工单，不存在指定 ID 工单时将返回 null。
     *
     * @param id 工单 ID
     * @return 工单
     */
    public WorkOrder getCompletedOrderById(Integer id) {
        return this.orderMapper.getCompletedOrderById(id);
    }

    /**
     * 获取本地工单表中的全体工单。
     *
     * @return 全体工单
     */
    public List<WorkOrder> getAllLocalOrders() {
        return this.orderMapper.getAllLocalOrders();
    }

    /**
     * 根据工单 ID 将已完工工单迁移至完工工单表中。
     *
     * @param id 工单 ID
     */
    public void transferWorkOrderToCompleted(Integer id) {
        this.orderMapper.transferWorkOrderToCompleted(id);
        this.orderMapper.deleteOrderById(id);
    }

    /**
     * 获取完工工单表当中的工单个数。
     *
     * @return 工单个数
     */
    public Integer getCompletedOrderCount() {
        return this.orderMapper.getCompletedOrderCount();
    }

    /**
     * 新增一条工单记录。
     *
     * @param order 工单对象
     */
    public void insertOrder(WorkOrder order) {
        this.orderMapper.insertOrder(order);
    }

    public List<NormalBoard> getBoardList(CuttingSignal signal, List<WorkOrder> workOrders, BigDecimal wasteThreshold) {
        ArrayList<NormalBoard> normalBoards = new ArrayList<>();
        if (workOrders.isEmpty()) {
            WorkOrder preOrder = this.getOrderById(signal.getOrderId());
            CutBoard srcBoard = BoardUtil.getCutBoard(signal.getCuttingSize(), preOrder.getMaterial(), signal.getForwardEdge(), preOrder.getId());
            if (srcBoard.getWidth().compareTo(BigDecimal.ZERO) <= 0) {
                return new ArrayList<>();
            }
            NormalBoard remainBoard = new NormalBoard(srcBoard.getHeight(), srcBoard.getWidth(), srcBoard.getLength(), srcBoard.getMaterial(), BoardUtil.calBoardCategory(srcBoard.getWidth(), srcBoard.getLength(), wasteThreshold), preOrder.getId());
            remainBoard.setCutTimes(1);
            normalBoards.add(remainBoard);
            return normalBoards;
        }
        // 根据order、signal创建原料板和目标板对象，方便后续操作

        WorkOrder order = workOrders.remove(0);

        int incompleteQuantity = order.getIncompleteQuantity();
        CutBoard srcBoard = BoardUtil.getCutBoard(signal.getCuttingSize(), order.getMaterial(), signal.getForwardEdge(), order.getId());
        NormalBoard destBoard = BoardUtil.getStandardProduct(order.getProductSpecification(), order.getMaterial(), incompleteQuantity, order.getId());

        if (srcBoard.getWidth().compareTo(BigDecimal.ZERO) <= 0) {
            return new ArrayList<>();
        }
        BigDecimal remainWidth = srcBoard.getWidth();


        // 如果原料板不能剪出一块目标板，直接结束
        if (srcBoard.getLength().compareTo(destBoard.getLength()) < 0 || srcBoard.getWidth().compareTo(destBoard.getWidth()) < 0) {
            NormalBoard remainBoard = new NormalBoard(srcBoard.getHeight(), srcBoard.getWidth(), srcBoard.getLength(), srcBoard.getMaterial(), BoardUtil.calBoardCategory(srcBoard.getWidth(), srcBoard.getLength(), wasteThreshold), order.getId());
            remainBoard.setCutTimes(1);
            normalBoards.add(remainBoard);
            return normalBoards;
        }
        // 1、考虑长度
        // 如果原材料的宽度大于夹钳宽度（支持修边），且原材料的长度大于目标板材的长度（需要修边），则进行修边操作
        if (srcBoard.getWidth().compareTo(BoardUtil.CLAMP_WIDTH) < 0 && srcBoard.getLength().compareTo(destBoard.getLength()) > 0) {
            NormalBoard remainBoard = new NormalBoard(srcBoard.getHeight(), srcBoard.getWidth(), srcBoard.getLength(), srcBoard.getMaterial(), BoardUtil.calBoardCategory(srcBoard.getWidth(), srcBoard.getLength(), wasteThreshold), order.getId());
            remainBoard.setCutTimes(1);
            normalBoards.add(remainBoard);
            return normalBoards;
        }
        // 修边需要减去的长度

        // 2、考虑宽度
        int couldProduct = incompleteQuantity;

        // 计算一块原料板最多能剪几块目标板
        while (destBoard.getWidth().multiply(new BigDecimal(couldProduct)).compareTo(srcBoard.getWidth().subtract(BoardUtil.CLAMP_DEPTH)) > 0) {
            couldProduct--;
        }

        incompleteQuantity -= couldProduct;

        remainWidth = remainWidth.subtract(destBoard.getWidth().multiply(new BigDecimal(couldProduct)));

        destBoard.setCutTimes(couldProduct);
        normalBoards.add(destBoard);

        // 1、如果couldProduct < requireNum，意味着剩余的原料板还得生产当前工单的成品板
        // 反之，当前工单的成品板数量以满足要求，剩余板材用来生产后续工单的成品板
        // 2、剩余板材宽度不足以生产当前工单的成品板
        if (incompleteQuantity > 0 && destBoard.getWidth().compareTo(remainWidth) <= 0) {
            if (destBoard.getWidth().compareTo(BoardUtil.CLAMP_DEPTH) >= 0) {
                BigDecimal widthCut = remainWidth.subtract(destBoard.getWidth());
                // 余料板
                NormalBoard remainBoard = new NormalBoard(srcBoard.getHeight(), widthCut, destBoard.getLength(), srcBoard.getMaterial(), BoardUtil.calBoardCategory(widthCut, destBoard.getLength(), wasteThreshold), order.getId());
                remainBoard.setCutTimes(widthCut.compareTo(BigDecimal.ZERO) == 0 ? 0 : 1);
                normalBoards.add(remainBoard);
                remainWidth = remainWidth.subtract(widthCut);


                // 成品板
                NormalBoard extraProBoard = new NormalBoard(srcBoard.getHeight(), destBoard.getWidth(), destBoard.getLength(), destBoard.getMaterial(), BoardCategory.PRODUCT, order.getId());
                extraProBoard.setCutTimes(1);
                normalBoards.add(extraProBoard);
                // 减去成品板的总宽得到剩余宽度
                remainWidth = remainWidth.subtract(destBoard.getWidth());

            } else {
                NormalBoard extraBoard = new NormalBoard(srcBoard.getHeight(), remainWidth, destBoard.getLength(), destBoard.getMaterial(), BoardUtil.calBoardCategory(remainWidth, destBoard.getLength(), wasteThreshold), order.getId());
                extraBoard.setCutTimes(1);
                normalBoards.add(extraBoard);
                // 当前工单的成品板小于夹钳深度时，无法生产；且后续工单的板材宽度会更小，更不能剪。所以直接报废
                return normalBoards;
            }
        }

        // 创建一个虚拟的cuttingSignal用来处理余料
        String spec = destBoard.getHeight() + "×" + remainWidth + "×" + destBoard.getLength();
        CuttingSignal virtualSignal = new CuttingSignal(spec, 1, order.getId());

        List<NormalBoard> boardsFromRemain = getBoardList(virtualSignal, workOrders, wasteThreshold);
        if (boardsFromRemain != null) {
            normalBoards.addAll(boardsFromRemain);
        }
        return normalBoards;
    }
}
