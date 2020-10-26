package com.cat.service;

import com.cat.entity.TrimmingValue;
import com.cat.util.TrimUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class TrimmingValueService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    RowMapper<TrimmingValue> trimM = new BeanPropertyRowMapper<>(TrimmingValue.class);

    public TrimmingValue getTrimmingValue() {
        return Objects.requireNonNullElseGet(this.getLatestTrimmingValue(), TrimUtil::getDefaultValue);
    }

    public TrimmingValue getLatestTrimmingValue() {
        List<TrimmingValue> list = this.jdbcTemplate.query("SELECT TOP 1 * FROM tb_trimming_value ORDER BY id DESC", this.trimM);
        return list.isEmpty() ? null : list.get(0);
    }
}
