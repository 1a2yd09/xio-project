package com.cat.service;

import com.cat.dao.StockSpecDao;
import com.cat.entity.StockSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class StockSpecService implements Clearable {
    @Autowired
    StockSpecDao stockSpecDao;

    public List<StockSpecification> getGroupStockSpecs() {
        return this.stockSpecDao.getGroupStockSpecs();
    }

    public void insertStockSpec(BigDecimal height, BigDecimal width, BigDecimal length) {
        this.stockSpecDao.insertStockSpec(height, width, length);
    }

    @Override
    public void clearTable() {
        this.stockSpecDao.truncateStockSpec();
    }
}
