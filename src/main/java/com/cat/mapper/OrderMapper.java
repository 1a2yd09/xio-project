package com.cat.mapper;

import com.cat.pojo.WorkOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author CAT
 */
@Mapper
public interface OrderMapper {
    /**
     * 更新指定工单的运行状态。
     *
     * @param order 工单对象
     */
    void updateOrderState(WorkOrder order);

    /**
     * 更新指定工单的已完工数量。
     *
     * @param order 工单对象
     */
    void updateOrderCompletedQuantity(WorkOrder order);

    /**
     * 根据工单 ID 获取指定工单，不存在指定 ID 工单时将返回 null。
     *
     * @param id 工单 ID
     * @return 工单对象
     */
    WorkOrder getOrderById(@Param("id") Integer id);

    /**
     * 根据工单 ID 获取指定完工工单，不存在指定 ID 工单时将返回 null。
     *
     * @param id 工单 ID
     * @return 工单对象
     */
    WorkOrder getCompletedOrderById(@Param("id") Integer id);

    /**
     * 根据工单模块和工单日期获取未开工的对重直梁工单集合。
     *
     * @param paramMap 参数集合
     * @return 工单集合
     */
    List<WorkOrder> getStraightOrders(Map<String, String> paramMap);

    /**
     * 根据工单模块和工单日期获取未开工的轿底工单集合。
     *
     * @param paramMap 参数集合
     * @return 工单集合
     */
    List<WorkOrder> getBottomOrders(Map<String, String> paramMap);

    /**
     * 获取本地工单表中的全体工单集合。
     *
     * @return 工单集合
     */
    List<WorkOrder> getAllLocalOrders();

    /**
     * 根据工单 ID 从远程工单表中删除对应工单。
     *
     * @param id 工单 ID。
     */
    void deleteRemoteOrderById(@Param("id") Integer id);

    /**
     * 根据工单 ID 从本地工单表中删除对应工单。
     *
     * @param id 工单 ID。
     */
    void deleteOrderById(@Param("id") Integer id);

    /**
     * 根据工单 ID 修改远程工单表中对应工单的下料板规格。
     *
     * @param cuttingSize 下料板规格
     * @param id          工单 ID
     */
    void updateRemoteOrderCuttingSize(@Param("cuttingSize") String cuttingSize, @Param("id") Integer id);

    /**
     * 根据工单 ID 将已完工工单迁移至完工工单表中。
     *
     * @param id 工单 ID
     */
    void transferWorkOrderToCompleted(@Param("id") Integer id);

    /**
     * 获取完工工单表当中的工单个数。
     *
     * @return 工单个数
     */
    int getCompletedOrderCount();

    /**
     * 还原数据库。
     */
    void restoreDatabase();

    /**
     * 根据具体日期获取当日完成工单数量。
     *
     * @param date 日期
     * @return 数量
     */
    int getCompletedOrderCountByDate(@Param("date") LocalDate date);

    /**
     * 获取日期区间内，每个日期的工单完成数量集合。
     *
     * @param start 起始日期
     * @param end   结束日期
     * @return 完成数量集合
     */
    List<Integer> getCompletedOrderCountByRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    /**
     * 更新一组工单集合的状态为已开工。
     *
     * @param orders 工单集合
     */
    void batchUpdateOrderState(List<WorkOrder> orders);

    /**
     * 新增一条工单记录。
     *
     * @param order 工单对象
     */
    void insertOrder(WorkOrder order);
}
