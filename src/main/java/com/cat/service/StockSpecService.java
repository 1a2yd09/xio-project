package com.cat.service;

import com.cat.dao.StockSpecDao;
import com.cat.entity.param.StockSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author CAT
 */
@Component
public class StockSpecService {
    @Autowired
    StockSpecDao stockSpecDao;

    /**
     * 根据厚度对库存规格进行分组，每个分组中仅有对应厚度中最新被写入的规格。
     *
     * @return 库存件规格集合
     */
    public List<StockSpecification> getGroupStockSpecs() {
        return this.stockSpecDao.getGroupStockSpecs();
    }

    /**
     * 新增库存件规格。
     *
     * @param height 厚度
     * @param width  宽度
     * @param length 长度
     */
    public void insertStockSpec(BigDecimal height, BigDecimal width, BigDecimal length) {
        this.stockSpecDao.insertStockSpec(height, width, length);
    }
}
