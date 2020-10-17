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

    RowMapper<Signal> signalRowMapper = new BeanPropertyRowMapper<>(Signal.class);

    public boolean isReceivedNewSignal(SignalCategory category) {
        Signal signal = this.getLatestSignal(category);
        if (signal != null && !signal.getProcessed()) {
            this.processedSignal(signal.getId());
            return true;
        }
        return false;
    }

    public Signal getLatestSignal(SignalCategory category) {
        List<Signal> signals = this.jdbcTemplate.query("SELECT TOP 1 * FROM signal WHERE category = ? ORDER BY created_at DESC", this.signalRowMapper, category.value);
        return signals.isEmpty() ? null : signals.get(0);
    }

    public void processedSignal(Integer id) {
        this.jdbcTemplate.update("UPDATE signal SET processed = 1 WHERE id = ?", id);
    }
}
