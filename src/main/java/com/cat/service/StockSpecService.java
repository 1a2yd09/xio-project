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

    public List<StockSpecification> getGroupSpecs() {
        return this.stockSpecDao.getGroupSpecs();
    }

    public void addStockSpec(BigDecimal height, BigDecimal width, BigDecimal length) {
        this.stockSpecDao.insertStockSpec(height, width, length);
    }

    public void clearStockSpecTable() {
        this.stockSpecDao.truncateTable();
    }

    @Override
    public void clearTable() {
        this.clearStockSpecTable();
    }
}
