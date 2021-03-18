package com.cat.service;

import com.cat.mapper.InventoryMapper;
import com.cat.pojo.Inventory;
import com.cat.utils.BoardUtil;
import org.springframework.stereotype.Service;

/**
 * @author CAT
 */
@Service
public class InventoryService {
    private final InventoryMapper inventoryMapper;

    public InventoryService(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
    }

    /**
     * 根据存货规格、材质、类型获取唯一的存货记录，不存在则返回 null。
     *
     * @param specification 规格
     * @param material      材质
     * @param category      类型
     * @return 存货
     */
    public Inventory getInventory(String specification, String material, String category) {
        return this.inventoryMapper.getInventories(category)
                .stream()
                .filter(inventory -> material.equals(inventory.getMaterial()))
                .filter(inventory -> BoardUtil.compareTwoSpecStr(inventory.getSpecification(), specification) == 0)
                .findFirst()
                .orElse(null);
    }

    /**
     * 更新存货数量，如果不存在相应存货记录，则新增该存货记录。
     *
     * @param inventory 对象
     */
    public void updateInventoryQuantity(Inventory inventory) {
        Inventory existedInventory = this.getInventory(inventory.getSpecification(), inventory.getMaterial(), inventory.getCategory());
        if (existedInventory != null) {
            existedInventory.setQuantity(existedInventory.getQuantity() + inventory.getQuantity());
            this.inventoryMapper.updateInventoryQuantity(existedInventory);
        } else {
            this.insertInventory(inventory);
        }
    }

    /**
     * 新增存货记录。
     *
     * @param inventory 存货对象
     */
    public void insertInventory(Inventory inventory) {
        this.inventoryMapper.insertInventory(inventory);
    }

    /**
     * 统计存货表记录数量，其中包括存货数量为零的记录。
     *
     * @return 记录数量
     */
    public Integer getInventoryCount() {
        return this.inventoryMapper.getInventoryCount();
    }
}
