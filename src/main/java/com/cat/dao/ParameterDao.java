package com.cat.dao;

import com.cat.entity.param.OperatingParameter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

/**
 * @author CAT
 */
@Component
public class ParameterDao extends BaseDao {
    /**
     * 查询最新的运行参数，数据表为空时将返回 null
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
}
