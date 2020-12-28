package com.cat.dao;

import com.cat.entity.param.OperatingParameter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author CAT
 */
@Component
public class ParameterDao extends BaseDao {
    /**
     * 查询最新的运行参数，数据表为空时将返回 null。
     *
     * @return 运行参数
     */
    public OperatingParameter getLatestOperatingParameter() {
        try {
            return this.jdbcTemplate.queryForObject("SELECT TOP 1 * FROM tb_operating_parameter ORDER BY id DESC", new BeanPropertyRowMapper<>(OperatingParameter.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 新增运行参数。
     *
     * @param orderDate      工单日期
     * @param fixedWidth     固定宽度
     * @param wasteThreshold 废料阈值
     * @param sortPattern    排序方式
     * @param orderModule    工单模块
     */
    public void insertOperatingParameter(LocalDate orderDate, BigDecimal fixedWidth, BigDecimal wasteThreshold, String sortPattern, String orderModule) {
        this.jdbcTemplate.update("INSERT INTO tb_operating_parameter (order_date, fixed_width, waste_threshold, sort_pattern, order_module) " +
                "VALUES (?, ?, ?, ?, ?)", orderDate, fixedWidth, wasteThreshold, sortPattern, orderModule);
    }
}
