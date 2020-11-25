package com.cat;

import com.cat.entity.bean.Inventory;
import com.cat.enums.BoardCategory;
import com.cat.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@Rollback
class InventoryTest extends BaseTest {
    @Autowired
    InventoryService inventoryService;

    @Test
    void testUpdateInventoryAmount() {
        int retVal = inventoryService.getInventoryCount();
        assertEquals(0, retVal);

        Inventory inventory = new Inventory("2.5×309×1016", "镀锌板", BoardCategory.STOCK.value);
        inventory.setAmount(1);
        inventoryService.updateInventoryAmount(inventory);
        retVal = inventoryService.getInventoryCount();
        assertEquals(1, retVal);

        inventory.setAmount(5);
        inventoryService.updateInventoryAmount(inventory);
        retVal = inventoryService.getInventoryCount();
        assertEquals(1, retVal);

        inventory = new Inventory("2.5×309×1016", "镀锌板", BoardCategory.SEMI_PRODUCT.value);
        inventory.setAmount(1);
        inventoryService.updateInventoryAmount(inventory);
        retVal = inventoryService.getInventoryCount();
        assertEquals(2, retVal);
    }
}
