package com.cat.service;

import com.cat.mapper.StockSpecMapper;
import com.cat.pojo.StockSpecification;
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
