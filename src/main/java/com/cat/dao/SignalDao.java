package com.cat.dao;

import com.cat.entity.CuttingSignal;
import com.cat.entity.Signal;
import com.cat.entity.StartSignal;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SignalDao extends BaseDao {
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

    public StartSignal getLatestStartSignal() {
        List<StartSignal> list = this.jdbcTemplate.query("SELECT TOP 1 * FROM tb_start_signal ORDER BY id DESC", new BeanPropertyRowMapper<>(StartSignal.class));
        return list.isEmpty() ? null : list.get(0);
    }

    public void processedStartSignal(Long id) {
        this.jdbcTemplate.update("UPDATE tb_start_signal SET processed = 1 WHERE id = ?", id);
    }

    public void insertStartSignal() {
        this.jdbcTemplate.update("INSERT INTO tb_start_signal(processed) VALUES (DEFAULT)");
    }

    public void insertTakeBoardSignal(Integer orderId) {
        this.jdbcTemplate.update("INSERT INTO tb_take_board_signal(order_id) VALUES (?)", orderId);
    }

    public CuttingSignal getLatestCuttingSignal() {
        List<CuttingSignal> list = this.jdbcTemplate.query("SELECT TOP 1 * FROM tb_cutting_signal ORDER BY id DESC", new BeanPropertyRowMapper<>(CuttingSignal.class));
        return list.isEmpty() ? null : list.get(0);
    }

    public void processedCuttingSignal(Long id) {
        this.jdbcTemplate.update("UPDATE tb_cutting_signal SET processed = 1 WHERE id = ?", id);
    }
}
