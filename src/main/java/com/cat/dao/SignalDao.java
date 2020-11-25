package com.cat.dao;

import com.cat.entity.signal.CuttingSignal;
import com.cat.entity.signal.StartSignal;
import com.cat.entity.signal.TakeBoardSignal;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author CAT
 */
@Component
public class SignalDao extends BaseDao {
    /**
     * 查询最新未被处理的开工信号，不存在未被处理的开工信号时返回 null
     *
     * @return 开工信号
     */
    public StartSignal getLatestNotProcessedStartSignal() {
        List<StartSignal> list = this.jdbcTemplate.query("SELECT TOP 1 * FROM tb_start_signal WHERE processed = 0 ORDER BY id DESC", new BeanPropertyRowMapper<>(StartSignal.class));
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 更新开工信号的状态
     *
     * @param startSignal 开工信号
     */
    public void processedStartSignal(StartSignal startSignal) {
        this.jdbcTemplate.update("UPDATE tb_start_signal SET processed = ? WHERE id = ?", startSignal.getProcessed(), startSignal.getId());
    }

    /**
     * 新增开工信号
     */
    public void insertStartSignal() {
        this.jdbcTemplate.update("INSERT INTO tb_start_signal(processed) VALUES (DEFAULT)");
    }

    /**
     * 查询最新的取板信号，数据表为空时返回 null
     *
     * @return 取板信号
     */
    public TakeBoardSignal getLatestTakeBoardSignal() {
        List<TakeBoardSignal> list = this.jdbcTemplate.query("SELECT TOP 1 * FROM tb_take_board_signal ORDER BY id DESC", new BeanPropertyRowMapper<>(TakeBoardSignal.class));
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 新增取板信号
     *
     * @param orderId 取板信号中的工单 ID 字段
     */
    public void insertTakeBoardSignal(Integer orderId) {
        this.jdbcTemplate.update("INSERT INTO tb_take_board_signal(order_id) VALUES (?)", orderId);
    }

    /**
     * 查询最新未被处理的下料信号，不存在未被处理的下料信号时将返回 null
     *
     * @return 下料信号
     */
    public CuttingSignal getLatestNotProcessedCuttingSignal() {
        List<CuttingSignal> list = this.jdbcTemplate.query("SELECT TOP 1 * FROM tb_cutting_signal WHERE processed = 0 ORDER BY id DESC", new BeanPropertyRowMapper<>(CuttingSignal.class));
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 更新下料信号的状态
     *
     * @param cuttingSignal 下料信号
     */
    public void processedCuttingSignal(CuttingSignal cuttingSignal) {
        this.jdbcTemplate.update("UPDATE tb_cutting_signal SET processed = ? WHERE id = ?", cuttingSignal.getProcessed(), cuttingSignal.getId());
    }

    /**
     * 新增下料信号
     *
     * @param cuttingSize  准确的下料板尺寸
     * @param isLongToward 下料板是否为较长边朝前
     * @param orderId      工单 ID
     */
    public void insertCuttingSignal(String cuttingSize, Boolean isLongToward, Integer orderId) {
        this.jdbcTemplate.update("INSERT INTO tb_cutting_signal(cutting_size, toward_edge, order_id) VALUES (?, ?, ?)", cuttingSize, isLongToward, orderId);
    }
}
