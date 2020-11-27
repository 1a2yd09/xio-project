package com.cat.dao;

import com.cat.entity.signal.CuttingSignal;
import com.cat.entity.signal.StartSignal;
import com.cat.entity.signal.TakeBoardSignal;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

/**
 * @author CAT
 */
@Component
public class SignalDao extends BaseDao {
    /**
     * 查询最新未被处理的开工信号，不存在未被处理的开工信号时返回 null。
     *
     * @return 开工信号
     */
    public StartSignal getLatestNotProcessedStartSignal() {
        try {
            return this.jdbcTemplate.queryForObject("SELECT TOP 1 * FROM tb_start_signal WHERE processed = 0 ORDER BY id DESC", new BeanPropertyRowMapper<>(StartSignal.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 更新开工信号状态。
     *
     * @param startSignal 开工信号
     */
    public void updateStartSignalProcessed(StartSignal startSignal) {
        this.jdbcTemplate.update("UPDATE tb_start_signal SET processed = ? WHERE id = ?", startSignal.getProcessed(), startSignal.getId());
    }

    /**
     * 新增开工信号。
     */
    public void insertStartSignal() {
        this.jdbcTemplate.update("INSERT INTO tb_start_signal(processed) VALUES (DEFAULT)");
    }

    /**
     * 查询最新的取板信号，数据表为空时返回 null。
     *
     * @return 取板信号
     */
    public TakeBoardSignal getLatestTakeBoardSignal() {
        try {
            return this.jdbcTemplate.queryForObject("SELECT TOP 1 * FROM tb_take_board_signal ORDER BY id DESC", new BeanPropertyRowMapper<>(TakeBoardSignal.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 新增取板信号。
     *
     * @param orderId 工单 ID
     */
    public void insertTakeBoardSignal(Integer orderId) {
        this.jdbcTemplate.update("INSERT INTO tb_take_board_signal(order_id) VALUES (?)", orderId);
    }

    /**
     * 查询最新未被处理的下料信号，不存在未被处理的下料信号时将返回 null。
     *
     * @return 下料信号
     */
    public CuttingSignal getLatestNotProcessedCuttingSignal() {
        try {
            return this.jdbcTemplate.queryForObject("SELECT TOP 1 * FROM tb_cutting_signal WHERE processed = 0 ORDER BY id DESC", new BeanPropertyRowMapper<>(CuttingSignal.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 更新下料信号状态。
     *
     * @param cuttingSignal 下料信号
     */
    public void updateCuttingSignalProcessed(CuttingSignal cuttingSignal) {
        this.jdbcTemplate.update("UPDATE tb_cutting_signal SET processed = ? WHERE id = ?", cuttingSignal.getProcessed(), cuttingSignal.getId());
    }

    /**
     * 新增下料信号。
     *
     * @param cuttingSize 下料板尺寸
     * @param forwardEdge 下料板朝向
     * @param orderId     工单 ID
     */
    public void insertCuttingSignal(String cuttingSize, Integer forwardEdge, Integer orderId) {
        this.jdbcTemplate.update("INSERT INTO tb_cutting_signal(cutting_size, forward_edge, order_id) VALUES (?, ?, ?)", cuttingSize, forwardEdge, orderId);
    }
}
