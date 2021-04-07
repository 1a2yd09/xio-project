package com.cat;

import com.cat.mapper.OrderMapper;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public abstract class BaseTest {
    @Autowired
    OrderMapper orderMapper;

    @AfterEach
    void restoreDatabase() {
        this.orderMapper.restoreDatabase();
    }
}
