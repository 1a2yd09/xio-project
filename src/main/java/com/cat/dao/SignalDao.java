package com.cat.dao;

import com.cat.entity.Signal;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SignalDao extends AbstractDao {
    RowMapper<Signal> sigM = new BeanPropertyRowMapper<>(Signal.class);

    public Signal getLatestSignal(String category) {
        List<Signal> signals = this.jdbcTemplate.query("SELECT TOP 1 * FROM tb_signal WHERE category = ? ORDER BY id DESC", this.sigM, category);
        return signals.isEmpty() ? null : signals.get(0);
    }

    public void processedSignal(Long id) {
        this.jdbcTemplate.update("UPDATE tb_signal SET processed = 1 WHERE id = ?", id);
    }

    public void insertSignal(String category) {
        this.jdbcTemplate.update("INSERT INTO tb_signal(category) VALUES (?)", category);
    }

    public void truncateTable() {
        this.jdbcTemplate.update("TRUNCATE TABLE tb_signal");
    }
}
