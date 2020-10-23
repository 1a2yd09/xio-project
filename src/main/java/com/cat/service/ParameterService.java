package com.cat.service;

import com.cat.entity.OperatingParameter;
import com.cat.entity.enums.BottomSortPattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
public class ParameterService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    RowMapper<OperatingParameter> opM = new BeanPropertyRowMapper<>(OperatingParameter.class);

    public OperatingParameter getOperatingParameter() {
        OperatingParameter op = this.getLatestOperatingParameter();
        return Objects.requireNonNullElseGet(op, () -> new OperatingParameter(-1, LocalDate.now(), BigDecimal.ZERO, BigDecimal.ZERO, BottomSortPattern.SEQ.value, LocalDateTime.now()));
    }

    public OperatingParameter getLatestOperatingParameter() {
        List<OperatingParameter> list = jdbcTemplate.query("SELECT TOP 1 * FROM tb_operating_parameter ORDER BY id DESC", this.opM);
        return list.isEmpty() ? null : list.get(0);
    }
}
