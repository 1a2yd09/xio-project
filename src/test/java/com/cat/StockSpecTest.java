package com.cat;

import com.cat.pojo.StockSpecification;
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
        // 4、4.0、4.00写入到 decimal(6,2) 类型的字段时，将被统一为4.00:
        stockSpecService.insertStockSpec(BigDecimal.valueOf(4.0), BigDecimal.valueOf(192L), BigDecimal.valueOf(2300L));
        stockSpecService.insertStockSpec(BigDecimal.valueOf(4.00), BigDecimal.valueOf(192L), BigDecimal.valueOf(2400L));
        stockSpecService.insertStockSpec(BigDecimal.valueOf(4.10), BigDecimal.valueOf(192L), BigDecimal.valueOf(2400L));
        stockSpecService.insertStockSpec(BigDecimal.valueOf(5L), BigDecimal.valueOf(192L), BigDecimal.valueOf(2500L));
        stockSpecService.insertStockSpec(BigDecimal.valueOf(5L), BigDecimal.valueOf(192L), BigDecimal.valueOf(2600L));
        List<StockSpecification> specs = stockSpecService.getGroupStockSpecs();
        assertNotNull(specs);
        specs.forEach(System.out::println);
    }
}
