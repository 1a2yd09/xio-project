package com.cat;

import com.cat.entity.NormalBoard;
import com.cat.entity.StockSpecification;
import com.cat.service.BoardService;
import com.cat.service.StockSpecService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
@Rollback
class StockSpecTest extends BaseTest {
    @Autowired
    StockSpecService stockSpecService;
    @Autowired
    BoardService boardService;

    @Test
    void testSpecification() {
        stockSpecService.insertStockSpec(BigDecimal.valueOf(3L), BigDecimal.valueOf(192L), BigDecimal.valueOf(2000L));
        stockSpecService.insertStockSpec(BigDecimal.valueOf(3L), BigDecimal.valueOf(192L), BigDecimal.valueOf(2100L));
        stockSpecService.insertStockSpec(BigDecimal.valueOf(4L), BigDecimal.valueOf(192L), BigDecimal.valueOf(2200L));
        stockSpecService.insertStockSpec(BigDecimal.valueOf(5L), BigDecimal.valueOf(192L), BigDecimal.valueOf(2500L));
        stockSpecService.insertStockSpec(BigDecimal.valueOf(5L), BigDecimal.valueOf(192L), BigDecimal.valueOf(2400L));
        List<StockSpecification> specs = stockSpecService.getGroupStockSpecs();
        assertNotNull(specs);
        specs.forEach(System.out::println);
    }
}
