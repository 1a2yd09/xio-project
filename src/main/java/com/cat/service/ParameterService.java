package com.cat.service;

import com.cat.entity.OperatingParameter;
import com.cat.entity.TrimmingParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ParameterService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    RowMapper<OperatingParameter> operatorM = new BeanPropertyRowMapper<>(OperatingParameter.class);
    RowMapper<TrimmingParameter> trimM = new BeanPropertyRowMapper<>(TrimmingParameter.class);

    public List<BigDecimal> getTrimValues() {
        TrimmingParameter tp = this.getLatestTrimmingParameter();
        if (tp != null) {
            return List.of(tp.getTrimTop(), tp.getTrimLeft(), tp.getTrimBottom(), tp.getTrimRight());
        } else {
            return List.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }

    public TrimmingParameter getLatestTrimmingParameter() {
        List<TrimmingParameter> list = this.jdbcTemplate.query("SELECT TOP 1 * FROM tb_trimming_value ORDER BY created_at DESC", this.trimM);
        return list.isEmpty() ? null : list.get(0);
    }

    public OperatingParameter getLatestOperatingParameter() {
        List<OperatingParameter> list = this.jdbcTemplate.query("SELECT TOP 1 * FROM tb_operating_parameter ORDER BY created_at DESC", this.operatorM);
        return list.isEmpty() ? null : list.get(0);
    }
}
