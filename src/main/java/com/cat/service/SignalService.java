package com.cat.service;

import com.cat.entity.Signal;
import com.cat.entity.enums.SignalCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SignalService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    RowMapper<Signal> sigM = new BeanPropertyRowMapper<>(Signal.class);

    public boolean isReceivedNewSignal(SignalCategory category) {
        Signal signal = this.getLatestSignal(category);
        if (signal != null && !signal.getProcessed()) {
            signal.setProcessed(true);
            this.processedSignal(signal.getProcessed(), signal.getId());
            return true;
        }
        return false;
    }

    public Signal getLatestSignal(SignalCategory category) {
        // 在单测的时候发现使用时间进行排序会无法判断两个相同时间的先后，原因是时间精度不够高，因此选择同样能够分辨数据先后顺序的ID字段，在实际环境中，两个信号的时间几乎不可能相同:
        List<Signal> signals = this.jdbcTemplate.query("SELECT TOP 1 * FROM tb_signal WHERE category = ? ORDER BY id DESC", this.sigM, category.value);
        return signals.isEmpty() ? null : signals.get(0);
    }

    public void processedSignal(Boolean processed, Integer id) {
        this.jdbcTemplate.update("UPDATE tb_signal SET processed = ? WHERE id = ?", processed, id);
    }

    public void addNewSignal(SignalCategory category) {
        this.jdbcTemplate.update("INSERT INTO tb_signal(category) VALUES (?)", category.value);
    }

    public void truncateSignal() {
        this.jdbcTemplate.update("TRUNCATE TABLE tb_signal");
    }
}
