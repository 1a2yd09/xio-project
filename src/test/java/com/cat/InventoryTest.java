package com.cat;

import com.cat.entity.Inventory;
import com.cat.entity.enums.BoardCategory;
import com.cat.service.InventoryService;
import com.cat.util.BoardUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
@Rollback
class InventoryTest extends BaseTest {
    @Autowired
    InventoryService inventoryService;

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
        Inventory inventory = stockMap.get(BoardUtil.getStandardSpecStr("3.00×192×2000"));
        assertNotNull(inventory);
        System.out.println(inventory);
    }
}
