package com.cat.mapper;

import com.cat.pojo.CuttingSignal;
import com.cat.pojo.ProcessControlSignal;
import com.cat.pojo.TakeBoardSignal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * @author CAT
 */
@Mapper
public interface SignalMapper {
    /**
     * 获取最新未被处理的流程控制信号。
     *
     * @return 信号对象
     */
    ProcessControlSignal getLatestUnProcessedControlSignal();

    /**
     * 获取最新的流程控制信号。
     *
     * @return 信号对象
     */
    ProcessControlSignal getLatestControlSignal();

    /**
     * 更新指定流程控制信号记录。
     *
     * @param controlSignal 流程控制信号对象
     */
    void updateControlSignal(ProcessControlSignal controlSignal);

    /**
     * 新增流程控制信号记录。
     *
     * @param category 信号类型
     */
    void insertProcessControlSignal(@Param("category") Integer category);

    /**
     * 获取最新的取板信号。
     *
     * @return 取板信号对象
     */
    TakeBoardSignal getLatestTakeBoardSignal();

    /**
     * 新增取板信号记录。
     *
     * @param orderId 工单 ID
     */
    void insertTakeBoardSignal(@Param("orderId") Integer orderId);

    /**
     * 获取最新未被处理的下料信号记录，不存在将返回 null。
     *
     * @return 下料信号对象
     */
    CuttingSignal getLatestUnProcessedCuttingSignal();

    /**
     * 获取最新的下料信号记录，不存在下料信号记录将返回 null。
     *
     * @return 下料信号对象
     */
    CuttingSignal getLatestCuttingSignal();

    /**
     * 更新指定下料信号记录。
     *
     * @param cuttingSignal 下料信号对象
     */
    void updateCuttingSignal(CuttingSignal cuttingSignal);

    /**
     * 新增下料信号记录。
     *
     * @param cuttingSize  原料板规格
     * @param forwardEdge  原料板朝向
     * @param longEdgeTrim 长边修边值
     * @param orderId      工单 ID
     */
    void insertCuttingSignal(@Param("cuttingSize") String cuttingSize,
                             @Param("forwardEdge") Integer forwardEdge,
                             @Param("longEdgeTrim") BigDecimal longEdgeTrim,
                             @Param("orderId") Integer orderId);
}
