package com.cat;

import com.cat.entity.Inventory;
import com.cat.entity.enums.BoardCategory;
import com.cat.service.InventoryService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryTest {
    static ApplicationContext context;
    static InventoryService inventoryService;

    @BeforeAll
    static void init() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        inventoryService = context.getBean(InventoryService.class);
    }

    @Test
    void testGetStockMap() {
        inventoryService.addNewInventory("3.00×192.00×2000.00", "热板", 3, BoardCategory.STOCK.value);
        inventoryService.addNewInventory("4.00×245.00×3190.00", "热板", 4, BoardCategory.STOCK.value);
        inventoryService.addNewInventory("5.00×192.00×3000.00", "热板", 5, BoardCategory.STOCK.value);
        Map<String, Inventory> stockMap = inventoryService.getStockMap();
        assertEquals(3, stockMap.size());
        for (String spec : stockMap.keySet()) {
            System.out.println(spec);
            System.out.println(stockMap.get(spec));
        }
    }
}
