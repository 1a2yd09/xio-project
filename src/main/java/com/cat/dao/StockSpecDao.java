package com.cat.dao;

import com.cat.entity.StockSpecification;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class StockSpecDao extends BaseDao {
    public List<StockSpecification> getGroupSpecs() {
        return this.jdbcTemplate.query("SELECT id, height, width, length, created_at " +
                "FROM (SELECT id, height, width, length, created_at, ROW_NUMBER() OVER (PARTITION BY height ORDER BY id DESC) AS row_number " +
                "FROM tb_stock_specification) AS S " +
                "WHERE row_number = 1", new BeanPropertyRowMapper<>(StockSpecification.class));
    }

    public void insertStockSpec(BigDecimal height, BigDecimal width, BigDecimal length) {
        this.jdbcTemplate.update("INSERT INTO tb_stock_specification(height, width, length) VALUES (?, ?, ?)", height, width, length);
    }

    public void truncateTable() {
        this.jdbcTemplate.update("TRUNCATE TABLE tb_stock_specification");
    }
}
