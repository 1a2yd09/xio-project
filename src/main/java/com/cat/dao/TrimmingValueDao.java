package com.cat.dao;

import com.cat.entity.TrimmingValue;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TrimmingValueDao extends AbstractDao {
    public TrimmingValue getTrimmingValue() {
        List<TrimmingValue> list = this.jdbcTemplate.query("SELECT TOP 1 * FROM tb_trimming_value ORDER BY id DESC", new BeanPropertyRowMapper<>(TrimmingValue.class));
        return list.isEmpty() ? null : list.get(0);
    }
}
