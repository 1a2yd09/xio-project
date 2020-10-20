package com.cat;

import com.cat.entity.StockSpecification;
import com.cat.service.StockSpecificationService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.util.List;

public class StockSpecTest {
    static ApplicationContext context;
    static StockSpecificationService stockSpecificationService;

    @BeforeAll
    static void init() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        stockSpecificationService = context.getBean(StockSpecificationService.class);
    }

    @Test
    void testSpecification() {
        List<StockSpecification> specs = stockSpecificationService.getGroupSpecification();
        specs.forEach(System.out::println);
        StockSpecification ss = stockSpecificationService.getMatchSpecification(new BigDecimal(3));
        System.out.println(ss);
    }
}
