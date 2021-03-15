package com.cat.mapper;

import com.cat.pojo.WorkOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

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
     * 根据工单模块和工单日期获取对重直梁工单集合。
     *
     * @param siteModule 工单模块
     * @param date       工单日期
     * @return 工单集合
     */
    List<WorkOrder> getNotBottomOrders(@Param("siteModule") String siteModule, @Param("date") LocalDateTime date);

    /**
     * 根据工单模块和工单日期获取轿底工单集合。
     *
     * @param siteModule 工单模块
     * @param date       工单日期
     * @return 工单集合
     */
    List<WorkOrder> getBottomOrders(@Param("siteModule") String siteModule, @Param("date") LocalDateTime date);

    /**
     * 获取当前生产工单表中的全体工单集合。
     *
     * @return 工单集合
     */
    List<WorkOrder> getAllProductionOrders();

    /**
     * 根据工单 ID 从远程工单表中删除对应工单。
     *
     * @param id 工单 ID。
     */
    void deleteRemoteOrderById(@Param("id") Integer id);

    /**
     * 根据工单 ID 从工单表中删除对应工单。
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

    void restoreDatabase();
}
