package com.cat.service;

import com.cat.entity.bean.Inventory;
import com.cat.mapper.InventoryMapper;
import com.cat.utils.BoardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author CAT
 */
@Service
public class InventoryService {
    @Autowired
    InventoryMapper inventoryMapper;

    /**
     * 根据存货规格、材质、类型获取唯一的存货记录，不存在则返回 null。
     *
     * @param specification 规格
     * @param material      材质
     * @param category      类型
     * @return 存货
     */
    public Inventory getInventory(String specification, String material, String category) {
        return this.inventoryMapper.getInventories(material, category)
                .stream()
                .filter(inventory -> BoardUtils.compareTwoSpecStr(inventory.getSpecification(), specification) == 0)
                .findFirst()
                .orElse(null);
    }

    /**
     * 更新存货数量，如果不存在相应存货，则新增该存货记录。
     *
     * @param inventory 存货
     */
    public void updateInventoryQuantity(Inventory inventory) {
        Inventory existedInventory = this.getInventory(inventory.getSpecification(), inventory.getMaterial(), inventory.getCategory());
        if (existedInventory != null) {
            existedInventory.setQuantity(existedInventory.getQuantity() + inventory.getQuantity());
            this.inventoryMapper.updateInventoryQuantity(existedInventory);
        } else {
            this.insertInventory(inventory.getSpecification(), inventory.getMaterial(), inventory.getQuantity(), inventory.getCategory());
        }
    }

    /**
     * 新增存货记录。
     *
     * @param specification 规格
     * @param material      材质
     * @param quantity      数量
     * @param category      类型
     */
    public void insertInventory(String specification, String material, Integer quantity, String category) {
        this.inventoryMapper.insertInventory(specification, material, quantity, category);
    }

    /**
     * 查询存货表记录数量，包括数量为零的记录。
     *
     * @return 记录数量
     */
    public Integer getInventoryCount() {
        return this.inventoryMapper.getInventoryCount();
    }
}
