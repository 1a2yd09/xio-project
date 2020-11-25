package com.cat.service;

import com.cat.dao.InventoryDao;
import com.cat.entity.bean.Inventory;
import com.cat.utils.BoardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InventoryService {
    @Autowired
    InventoryDao inventoryDao;

    public Inventory getInventory(String specification, String material, String category) {
        return this.inventoryDao.getInventories(material, category)
                .stream()
                .filter(inventory -> BoardUtils.compareTwoSpecStr(inventory.getSpecStr(), specification) == 0)
                .findFirst()
                .orElse(null);
    }

    public void updateInventoryAmount(Inventory inventory) {
        Inventory existedInventory = this.getInventory(inventory.getSpecStr(), inventory.getMaterial(), inventory.getCategory());
        if (existedInventory != null) {
            existedInventory.setAmount(existedInventory.getAmount() + inventory.getAmount());
            this.inventoryDao.updateInventoryAmount(existedInventory);
        } else {
            this.insertInventory(inventory.getSpecStr(), inventory.getMaterial(), inventory.getAmount(), inventory.getCategory());
        }
    }

    public void insertInventory(String specification, String material, Integer amount, String category) {
        this.inventoryDao.insertInventory(specification, material, amount, category);
    }

    public Integer getInventoryCount() {
        return this.inventoryDao.getInventoryCount();
    }
}
