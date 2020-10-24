package com.cat.service;

import com.cat.entity.Board;
import com.cat.entity.Inventory;
import com.cat.entity.enums.BoardCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class InventoryService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    RowMapper<Inventory> inventoryM = new BeanPropertyRowMapper<>(Inventory.class);

    public Map<String, Inventory> getStockMap() {
        return this.getInventoriesByCategory(BoardCategory.STOCK.value).stream().collect(Collectors.toMap(Inventory::getSpecification, Function.identity()));
    }

    public void addInventoryAmount(Board board, int amount) {
        Inventory inventory = this.getInventory(board.getSpecification(), board.getMaterial(), board.getCategory().value);
        if (inventory != null) {
            inventory.setAmount(inventory.getAmount() + amount);
            this.updateInventoryAmount(inventory.getAmount(), inventory.getId());
        } else {
            this.addNewInventory(board.getSpecification(), board.getMaterial(), amount, board.getCategory().value);
        }
    }

    public Inventory getInventory(String specification, String material, String category) {
        // TODO: 这里直接比较规格字符串还是有点不妥，可以先根据材质和类型获取集合，然后调用比较规格字符串是否相等的函数一一比较。
        List<Inventory> list = this.jdbcTemplate.query("SELECT * FROM tb_inventory WHERE specification = ? AND material = ? AND category = ?", this.inventoryM, specification, material, category);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Inventory> getInventoriesByCategory(String category) {
        return this.jdbcTemplate.query("SELECT * FROM tb_inventory WHERE amount > 0 AND category = ?", this.inventoryM, category);
    }

    public void addNewInventory(String specification, String material, Integer amount, String category) {
        this.jdbcTemplate.update("INSERT INTO tb_inventory (specification, material, amount, category) " +
                "VALUES (?, ?, ?, ?)", specification, material, amount, category);
    }

    public void updateInventoryAmount(Integer amount, Integer id) {
        this.jdbcTemplate.update("UPDATE tb_inventory SET amount = ? WHERE id = ?", amount, id);
    }

    public void truncateInventory() {
        this.jdbcTemplate.update("TRUNCATE TABLE tb_inventory");
    }
}
