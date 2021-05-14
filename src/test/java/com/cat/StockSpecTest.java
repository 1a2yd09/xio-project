package com.cat;

import com.cat.pojo.StockSpecification;
import com.cat.service.ProcessBoardService;
import com.cat.service.StockSpecService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StockSpecTest extends BaseTest {
    @Autowired
    StockSpecService stockSpecService;
    @Autowired
    ProcessBoardService processBoardService;

    /**
     * 插入不同厚度的库存件，取出库存件集合时将按照厚度进行分组，每组取最新的记录作为该厚度下的库存件规格。
     */
    @Test
    void testSpecification() {
        stockSpecService.insertStockSpec(BigDecimal.valueOf(3L), BigDecimal.valueOf(192L), BigDecimal.valueOf(2000L));
        stockSpecService.insertStockSpec(BigDecimal.valueOf(3L), BigDecimal.valueOf(192L), BigDecimal.valueOf(2200L));
        stockSpecService.insertStockSpec(BigDecimal.valueOf(3L), BigDecimal.valueOf(192L), BigDecimal.valueOf(2100L));
        // 4、4.0、4.00写入到 decimal(6,2) 类型的字段时，将被统一为4.00:
        stockSpecService.insertStockSpec(BigDecimal.valueOf(4L), BigDecimal.valueOf(192L), BigDecimal.valueOf(2200L));
        stockSpecService.insertStockSpec(BigDecimal.valueOf(4.0), BigDecimal.valueOf(192L), BigDecimal.valueOf(2300L));
        stockSpecService.insertStockSpec(BigDecimal.valueOf(4.00), BigDecimal.valueOf(192L), BigDecimal.valueOf(2400L));
        stockSpecService.insertStockSpec(BigDecimal.valueOf(4.10), BigDecimal.valueOf(192L), BigDecimal.valueOf(2400L));
        stockSpecService.insertStockSpec(BigDecimal.valueOf(5L), BigDecimal.valueOf(192L), BigDecimal.valueOf(2500L));
        stockSpecService.insertStockSpec(BigDecimal.valueOf(5L), BigDecimal.valueOf(192L), BigDecimal.valueOf(2600L));
        stockSpecService.insertStockSpec(BigDecimal.valueOf(5L), BigDecimal.valueOf(192L), BigDecimal.valueOf(2400L));
        List<StockSpecification> specs = stockSpecService.getGroupStockSpecs();
        assertEquals(4, specs.size());
        specs.forEach(System.out::println);
    }
}
