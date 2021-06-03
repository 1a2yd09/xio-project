package com.cat.service;

import com.cat.mapper.StockSpecMapper;
import com.cat.pojo.StockSpecification;
import com.cat.pojo.WorkOrder;
import com.cat.utils.BoardUtil;
import com.cat.utils.ParamUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author CAT
 */
@Service
public class StockSpecService {
    private final StockSpecMapper stockSpecMapper;

    public StockSpecService(StockSpecMapper stockSpecMapper) {
        this.stockSpecMapper = stockSpecMapper;
    }

    /**
     * 根据厚度对库存规格进行分组，每个分组中仅有对应厚度中最新被写入的规格。
     *
     * @return 库存件规格集合
     */
    public List<StockSpecification> getGroupStockSpecs() {
        return this.stockSpecMapper.getGroupStockSpecs();
    }

    public WorkOrder getStockWorkOrder(WorkOrder productWorkOrder) {
        List<BigDecimal> list = BoardUtil.specStrToDecList(productWorkOrder.getProductSpecification());
        BigDecimal productHeight = list.get(0);
        StockSpecification ss = this.getGroupStockSpecs().stream()
                .filter(spec -> spec.getHeight().compareTo(productHeight) == 0)
                .findFirst()
                .orElse(ParamUtil.getDefaultStockSpec());
        WorkOrder stockOrder = new WorkOrder();
        stockOrder.setOperationState(productWorkOrder.getOperationState());
        stockOrder.setProductSpecification(BoardUtil.getStandardSpecStr(ss.getHeight(), ss.getWidth(), ss.getLength()));
        stockOrder.setMaterial(productWorkOrder.getMaterial());
        stockOrder.setProductQuantity("99");
        stockOrder.setCompletionDate(productWorkOrder.getCompletionDate());
        stockOrder.setId(-1);
        stockOrder.setBatchNumber(productWorkOrder.getBatchNumber());
        stockOrder.setSequenceNumber(productWorkOrder.getSequenceNumber());
        stockOrder.setCuttingSize(productWorkOrder.getCuttingSize());
        stockOrder.setSiteModule(productWorkOrder.getSiteModule());
        stockOrder.setCompletedQuantity(null);
        return stockOrder;
    }

    /**
     * 新增库存件规格。
     *
     * @param height 厚度
     * @param width  宽度
     * @param length 长度
     */
    public void insertStockSpec(BigDecimal height, BigDecimal width, BigDecimal length) {
        this.stockSpecMapper.insertStockSpec(height, width, length);
    }
}
