package com.cat.mapper;

import com.cat.pojo.Inventory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author CAT
 */
@Mapper
public interface InventoryMapper {
    /**
     * 根据存货类型获取存货集合，或根据存货类型以及存货材质获取存货集合。
     *
     * @param inventory 存货对象
     * @return 存货集合
     */
    List<Inventory> getInventories(Inventory inventory);

    /**
     * 新增存货记录。
     *
     * @param inventory 存货对象
     */
    void insertInventory(Inventory inventory);

    /**
     * 更新指定存货数量。
     *
     * @param inventory 存货对象
     */
    void updateInventoryQuantity(Inventory inventory);

    /**
     * 统计存货表记录数量，其中包括存货数量为零的记录。
     *
     * @return 记录数量
     */
    int getInventoryCount();
}
