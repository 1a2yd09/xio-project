package com.cat.dao;

import com.cat.entity.param.StockSpecification;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author CAT
 */
@Component
public class StockSpecDao extends BaseDao {
    /**
     * 根据厚度对库存规格进行分组，每个分组中仅有对应厚度中最新被写入的规格
     *
     * @return 库存件规格集合
     */
    public List<StockSpecification> getGroupStockSpecs() {
        return this.jdbcTemplate.query("SELECT id, height, width, length, created_at " +
                "FROM (SELECT id, height, width, length, created_at, ROW_NUMBER() OVER (PARTITION BY height ORDER BY id DESC) AS row_number " +
                "FROM tb_stock_specification) AS S " +
                "WHERE row_number = 1", new BeanPropertyRowMapper<>(StockSpecification.class));
    }

    /**
     * 新增库存件规格
     *
     * @param height 厚度
     * @param width  宽度
     * @param length 长度
     */
    public void insertStockSpec(BigDecimal height, BigDecimal width, BigDecimal length) {
        this.jdbcTemplate.update("INSERT INTO tb_stock_specification(height, width, length) VALUES (?, ?, ?)", height, width, length);
    }
}
