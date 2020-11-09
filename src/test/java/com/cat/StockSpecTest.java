package com.cat;

import com.cat.entity.NormalBoard;
import com.cat.entity.StockSpecification;
import com.cat.service.BoardService;
import com.cat.service.StockSpecificationService;
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
    StockSpecificationService stockSpecificationService;
    @Autowired
    BoardService boardService;

    @Test
    void testSpecification() {
        stockSpecificationService.addStockSpecification(BigDecimal.valueOf(3L), BigDecimal.valueOf(192L), BigDecimal.valueOf(2000L));
        stockSpecificationService.addStockSpecification(BigDecimal.valueOf(3L), BigDecimal.valueOf(192L), BigDecimal.valueOf(2100L));
        stockSpecificationService.addStockSpecification(BigDecimal.valueOf(4L), BigDecimal.valueOf(192L), BigDecimal.valueOf(2200L));
        stockSpecificationService.addStockSpecification(BigDecimal.valueOf(5L), BigDecimal.valueOf(192L), BigDecimal.valueOf(2500L));
        stockSpecificationService.addStockSpecification(BigDecimal.valueOf(5L), BigDecimal.valueOf(192L), BigDecimal.valueOf(2400L));
        List<StockSpecification> specs = stockSpecificationService.getGroupSpecification();
        assertNotNull(specs);
        specs.forEach(System.out::println);

        NormalBoard board = boardService.getMatchStockBoard(specs, BigDecimal.valueOf(3L), "热板");
        assertNotNull(board);
        System.out.println(board);

        board = boardService.getMatchStockBoard(specs, BigDecimal.valueOf(6L), "热板");
        assertNotNull(board);
        System.out.println(board);
    }
}
