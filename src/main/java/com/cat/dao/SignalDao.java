package com.cat.dao;

import com.cat.entity.CuttingSignal;
import com.cat.entity.StartSignal;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SignalDao extends BaseDao {
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

    public void truncateStartSignal() {
        this.jdbcTemplate.update("TRUNCATE TABLE tb_start_signal");
    }

    public void insertTakeBoardSignal(Integer orderId) {
        this.jdbcTemplate.update("INSERT INTO tb_take_board_signal(order_id) VALUES (?)", orderId);
    }

    public void truncateTakeBoardSignal() {
        this.jdbcTemplate.update("TRUNCATE TABLE tb_take_board_signal");
    }

    public CuttingSignal getLatestCuttingSignal() {
        List<CuttingSignal> list = this.jdbcTemplate.query("SELECT TOP 1 * FROM tb_cutting_signal ORDER BY id DESC", new BeanPropertyRowMapper<>(CuttingSignal.class));
        return list.isEmpty() ? null : list.get(0);
    }

    public void processedCuttingSignal(Long id) {
        this.jdbcTemplate.update("UPDATE tb_cutting_signal SET processed = 1 WHERE id = ?", id);
    }

    public void insertCuttingSignal(String specification, Boolean longToward, int orderId) {
        this.jdbcTemplate.update("INSERT INTO tb_cutting_signal(specification, toward_edge, order_id) VALUES (?, ?, ?)", specification, longToward, orderId);
    }

    public void truncateCuttingSignal() {
        this.jdbcTemplate.update("TRUNCATE TABLE tb_cutting_signal");
    }
}
