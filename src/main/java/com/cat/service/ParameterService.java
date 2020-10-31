package com.cat.service;

import com.cat.entity.OperatingParameter;
import com.cat.util.ParameterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ParameterService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    RowMapper<OperatingParameter> opM = new BeanPropertyRowMapper<>(OperatingParameter.class);

    public OperatingParameter getLatestOperatingParameter() {
        return Objects.requireNonNullElseGet(this.getOperatingParameter(), ParameterUtil::getDefaultParameter);
    }

    private OperatingParameter getOperatingParameter() {
        List<OperatingParameter> list = jdbcTemplate.query("SELECT TOP 1 * FROM tb_operating_parameter ORDER BY id DESC", this.opM);
        return list.isEmpty() ? null : list.get(0);
    }
}
