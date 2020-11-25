package com.cat.dao;

import com.cat.entity.bean.Inventory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author CAT
 */
@Component
public class InventoryDao extends BaseDao {
    RowMapper<Inventory> inventoryM = new BeanPropertyRowMapper<>(Inventory.class);

    /**
     * 根据材质以及类型获取存货集合
     *
     * @param material 材质
     * @param category 类型
     * @return 存货集合
     */
    public List<Inventory> getInventories(String material, String category) {
        return this.jdbcTemplate.query("SELECT * FROM tb_inventory WHERE material = ? AND category = ?", this.inventoryM, material, category);
    }

    /**
     * 根据存货类型获取对应的可用存货集合
     *
     * @param category 存货类型
     * @return 存货集合
     */
    public List<Inventory> getInventories(String category) {
        return this.jdbcTemplate.query("SELECT * FROM tb_inventory WHERE amount > 0 AND category = ?", this.inventoryM, category);
    }

    /**
     * 新增存货记录
     *
     * @param specification 规格
     * @param material      材质
     * @param amount        数量
     * @param category      类型
     */
    public void insertInventory(String specification, String material, Integer amount, String category) {
        this.jdbcTemplate.update("INSERT INTO tb_inventory(specification, material, amount, category) " +
                "VALUES (?, ?, ?, ?)", specification, material, amount, category);
    }

    /**
     * 更新存货数量
     *
     * @param inventory 存货
     */
    public void updateInventoryAmount(Inventory inventory) {
        this.jdbcTemplate.update("UPDATE tb_inventory SET amount = ? WHERE id = ?", inventory.getAmount(), inventory.getId());
    }

    /**
     * 获取所有的存货记录数量，包括数量为零的记录
     *
     * @return 数量
     */
    public Integer getInventoryCount() {
        return this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_inventory", Integer.class);
    }
}
