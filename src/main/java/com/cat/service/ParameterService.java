package com.cat.service;

import com.cat.entity.OperatingParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class ParameterService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    RowMapper<OperatingParameter> opM = new BeanPropertyRowMapper<>(OperatingParameter.class);

    public OperatingParameter getLatestOperatingParameter() {
        return jdbcTemplate.queryForObject("SELECT TOP 1 * FROM tb_operating_parameter ORDER BY id DESC", this.opM);
    }
}
