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

    public void addNewSignal(SignalCategory category) {
        this.insertSignal(category.value);
    }

    public boolean isReceivedNewSignal(SignalCategory category) {
        Signal signal = this.getLatestSignal(category);
        if (signal != null && !signal.getProcessed()) {
            signal.setProcessed(true);
            this.processedSignal(signal);
            return true;
        }
        return false;
    }

    private Signal getLatestSignal(SignalCategory category) {
        List<Signal> signals = this.jdbcTemplate.query("SELECT TOP 1 * FROM signal WHERE category = ? ORDER BY created_at DESC", this.signalRowMapper, category.value);
        return signals.isEmpty() ? null : signals.get(0);
    }

    private void processedSignal(Signal signal) {
        this.jdbcTemplate.update("UPDATE signal SET processed = ? WHERE id = ?", signal.getProcessed(), signal.getId());
    }

    private void insertSignal(String category) {
        this.jdbcTemplate.update("INSERT INTO signal(category) VALUES (?)", category);
    }
}
