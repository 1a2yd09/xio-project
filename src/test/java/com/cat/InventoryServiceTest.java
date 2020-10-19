package com.cat;

import com.cat.service.InventoryService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class InventoryServiceTest {
    static ApplicationContext context;
    static InventoryService inventoryService;

    @BeforeAll
    public static void init() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        inventoryService = context.getBean(InventoryService.class);
    }

    @Test
    public void testAddInventory() {
        inventoryService.addInventory("1.20×192.00×1500.00", "冷板", 1, "库存件");
    }
}