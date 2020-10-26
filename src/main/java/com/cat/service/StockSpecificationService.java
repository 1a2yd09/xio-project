package com.cat.service;

import com.cat.entity.StockSpecification;
import com.cat.util.StockSpecUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class StockSpecificationService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    RowMapper<StockSpecification> specM = new BeanPropertyRowMapper<>(StockSpecification.class);

    public StockSpecification getMatchSpecification(BigDecimal height) {
        return this.getGroupSpecification().stream().filter(spec -> spec.getHeight().compareTo(height) == 0).findFirst().orElse(StockSpecUtil.getDefaultStockSpec());
    }

    public List<StockSpecification> getGroupSpecification() {
        return this.jdbcTemplate.query("SELECT id, height, width, length, created_at " +
                "FROM (SELECT id, height, width, length, created_at, ROW_NUMBER() OVER (PARTITION BY height ORDER BY id DESC) AS row_number " +
                "FROM tb_stock_specification) AS S " +
                "WHERE row_number = 1", this.specM);
    }

    public void addStockSpecification(BigDecimal height, BigDecimal width, BigDecimal length) {
        this.jdbcTemplate.update("INSERT INTO tb_stock_specification(height, width, length) VALUES (?, ?, ?)", height, width, length);
    }
}
