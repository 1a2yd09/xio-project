package com.cat;

import com.cat.enums.BoardCategory;
import com.cat.pojo.Inventory;
import com.cat.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryTest extends BaseTest {
    @Autowired
    InventoryService inventoryService;

    /**
     * 测试相同规格、材质、类型的存货是否会相互叠加。
     */
    @Test
    void testUpdateInventoryQuantity() {
        int retVal = inventoryService.getInventoryCount();
        assertEquals(0, retVal);

        Inventory inventory = new Inventory("2.5×309×1016", "镀锌板", BoardCategory.STOCK.value);
        inventory.setQuantity(1);
        inventoryService.updateInventoryQuantity(inventory);
        retVal = inventoryService.getInventoryCount();
        assertEquals(1, retVal);

        inventory.setQuantity(5);
        inventoryService.updateInventoryQuantity(inventory);
        retVal = inventoryService.getInventoryCount();
        assertEquals(1, retVal);

        inventory = new Inventory("2.5×309×1016", "镀锌板", BoardCategory.SEMI_PRODUCT.value);
        inventory.setQuantity(1);
        inventoryService.updateInventoryQuantity(inventory);
        retVal = inventoryService.getInventoryCount();
        assertEquals(2, retVal);
    }
}
