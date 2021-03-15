package com.cat.mapper;

import com.cat.pojo.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InventoryMapper {
    /**
     * 根据材质以及类型获取存货集合，如果材质为 null，将根据类型获取。
     *
     * @param material 材质
     * @param category 类型
     * @return 存货集合
     */
    List<Inventory> getInventories(@Param("material") String material, @Param("category") String category);

    /**
     * 新增存货记录。
     *
     * @param specification 规格
     * @param material      材质
     * @param quantity      数量
     * @param category      类型
     */
    void insertInventory(@Param("specification") String specification, @Param("material") String material, @Param("quantity") Integer quantity, @Param("category") String category);

    /**
     * 更新指定存货数量。
     *
     * @param inventory 存货对象
     */
    void updateInventoryQuantity(Inventory inventory);

    /**
     * 查询存货表记录数量，包括存货数量为零的记录。
     *
     * @return 记录数量
     */
    int getInventoryCount();
}
