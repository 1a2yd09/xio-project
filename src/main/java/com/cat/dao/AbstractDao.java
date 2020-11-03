package com.cat.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class AbstractDao {
    @Autowired
    JdbcTemplate jdbcTemplate;
}
