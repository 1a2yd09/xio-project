package com.cat;

import com.cat.entity.NormalBoard;
import com.cat.entity.StockSpecification;
import com.cat.service.BoardService;
import com.cat.service.StockSpecificationService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class StockSpecTest {
    static ApplicationContext context;
    static StockSpecificationService stockSpecificationService;
    static BoardService boardService;

    @BeforeAll
    static void init() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        stockSpecificationService = context.getBean(StockSpecificationService.class);
        boardService = context.getBean(BoardService.class);
    }

    @Test
    void testSpecification() {
        List<StockSpecification> specs = stockSpecificationService.getGroupSpecification();
        assertNotNull(specs);
        specs.forEach(System.out::println);
        NormalBoard board = boardService.getMatchStockBoard(specs, new BigDecimal(3), "热板");
        assertNotNull(board);
        System.out.println(board);
        board = boardService.getMatchStockBoard(specs, new BigDecimal(6), "热板");
        assertNotNull(board);
        System.out.println(board);
    }
}
