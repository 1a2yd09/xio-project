package com.cat.dao;

import com.cat.entity.param.OperatingParameter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ParameterDao extends BaseDao {
    public OperatingParameter getLatestOperatingParameter() {
        List<OperatingParameter> list = jdbcTemplate.query("SELECT TOP 1 * FROM tb_operating_parameter ORDER BY id DESC", new BeanPropertyRowMapper<>(OperatingParameter.class));
        return list.isEmpty() ? null : list.get(0);
    }
}
