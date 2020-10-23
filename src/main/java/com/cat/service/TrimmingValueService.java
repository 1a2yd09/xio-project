package com.cat.service;

import com.cat.entity.TrimmingValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class TrimmingValueService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    RowMapper<TrimmingValue> trimM = new BeanPropertyRowMapper<>(TrimmingValue.class);

    public List<BigDecimal> getTrimValues() {
        TrimmingValue tp = this.getLatestTrimmingParameter();
        if (tp != null) {
            return List.of(tp.getTrimTop(), tp.getTrimLeft(), tp.getTrimBottom(), tp.getTrimRight());
        } else {
            return List.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }

    public TrimmingValue getLatestTrimmingParameter() {
        List<TrimmingValue> list = this.jdbcTemplate.query("SELECT TOP 1 * FROM tb_trimming_value ORDER BY id DESC", this.trimM);
        return list.isEmpty() ? null : list.get(0);
    }
}
