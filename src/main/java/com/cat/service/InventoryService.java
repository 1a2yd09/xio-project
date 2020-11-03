package com.cat.service;

import com.cat.dao.InventoryDao;
import com.cat.entity.Inventory;
import com.cat.entity.NormalBoard;
import com.cat.entity.enums.BoardCategory;
import com.cat.util.BoardUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class InventoryService {
    @Autowired
    InventoryDao inventoryDao;

    public Map<String, Inventory> getStockMap() {
        return this.inventoryDao.getInventories(BoardCategory.STOCK.value)
                .stream()
                .collect(Collectors.toMap(inventory -> BoardUtil.getStandardSpecStr(inventory.getSpecification()), Function.identity()));
    }

    public Inventory getInventory(String specification, String material, String category) {
        return this.inventoryDao.getInventories(material, category)
                .stream()
                .filter(inventory -> BoardUtil.isTwoSpecStrEqual(inventory.getSpecification(), specification))
                .findFirst()
                .orElse(null);
    }

    public void addInventoryAmount(NormalBoard board, int amount) {
        Inventory inventory = this.getInventory(board.getSpecification(), board.getMaterial(), board.getCategory().value);
        if (inventory != null) {
            this.updateInventoryAmount(inventory.getAmount() + amount, inventory.getId());
        } else {
            this.addNewInventory(board.getSpecification(), board.getMaterial(), amount, board.getCategory().value);
        }
    }

    public void addNewInventory(String specification, String material, Integer amount, String category) {
        this.inventoryDao.insertInventory(specification, material, amount, category);
    }

    public void updateInventoryAmount(Integer amount, Integer id) {
        this.inventoryDao.updateInventoryAmount(amount, id);
    }

    public void clearInventoryTable() {
        this.inventoryDao.truncateTable();
    }
}
