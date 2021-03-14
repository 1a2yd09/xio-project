package com.cat.mapper;

import com.cat.entity.param.StockSpecification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface StockSpecMapper {
    /**
     * 根据厚度对库存规格进行分组，每个分组中仅有对应厚度中最新被写入的规格。
     *
     * @return 库存件规格集合
     */
    List<StockSpecification> getGroupStockSpecs();

    /**
     * 新增库存件规格。
     *
     * @param height 厚度
     * @param width  宽度
     * @param length 长度
     */
    void insertStockSpec(@Param("height") BigDecimal height, @Param("width") BigDecimal width, @Param("length") BigDecimal length);
}
