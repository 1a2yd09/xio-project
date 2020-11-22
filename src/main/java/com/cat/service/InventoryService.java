package com.cat.service;

import com.cat.dao.InventoryDao;
import com.cat.entity.Inventory;
import com.cat.entity.NormalBoard;
import com.cat.util.BoardUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InventoryService implements Clearable {
    @Autowired
    InventoryDao inventoryDao;

    public Inventory getInventory(String specification, String material, String category) {
        return this.inventoryDao.getInventories(material, category)
                .stream()
                .filter(inventory -> BoardUtil.isTwoSpecStrEqual(inventory.getSpecStr(), specification))
                .findFirst()
                .orElse(null);
    }

    public void addInventoryAmount(NormalBoard board, int amount) {
        Inventory inventory = this.getInventory(board.getSpecStr(), board.getMaterial(), board.getCategory().value);
        if (inventory != null) {
            inventory.setAmount(inventory.getAmount() + amount);
            this.inventoryDao.updateInventoryAmount(inventory);
        } else {
            this.insertInventory(board.getSpecStr(), board.getMaterial(), amount, board.getCategory().value);
        }
    }

    public void insertInventory(String specification, String material, Integer amount, String category) {
        this.inventoryDao.insertInventory(specification, material, amount, category);
    }

    public Integer getInventoryCount() {
        return this.inventoryDao.getInventoryCount();
    }

    @Override
    public void clearTable() {
        this.inventoryDao.truncateInventory();
    }
}
