package com.cat;

import com.cat.entity.NormalBoard;
import com.cat.entity.enums.BoardCategory;
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
    void testAddInventoryAmount() {
        int retVal = inventoryService.getInventoryCount();
        assertEquals(0, retVal);

        NormalBoard inventory = new NormalBoard("2.5×309×1016", "镀锌板", BoardCategory.STOCK);
        inventoryService.addInventoryAmount(inventory, 1);
        retVal = inventoryService.getInventoryCount();
        assertEquals(1, retVal);

        inventoryService.addInventoryAmount(inventory, 5);
        retVal = inventoryService.getInventoryCount();
        assertEquals(1, retVal);

        inventory = new NormalBoard("2.5×309×1016", "镀锌板", BoardCategory.SEMI_PRODUCT);
        inventoryService.addInventoryAmount(inventory, 1);
        retVal = inventoryService.getInventoryCount();
        assertEquals(2, retVal);
    }
}
