package com.cat.pojo;

import com.cat.enums.BoardCategory;
import com.cat.utils.DecimalUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author CAT
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class NormalBoard extends BaseBoard {
    private Integer cutTimes = 0;

    public NormalBoard() {
    }

    public NormalBoard(BigDecimal height, BigDecimal width, BigDecimal length, String material, BoardCategory category, Integer orderId) {
        super(height, width, length, material, category, orderId);
    }

    public NormalBoard(String specification, String material, BoardCategory category, Integer orderId) {
        super(specification, material, category, orderId);
    }

    /**
     * 获取加工当前板材所需总宽度。
     *
     * @return 总宽度
     */
    public BigDecimal getAllWidth() {
        return DecimalUtil.mul(this.getWidth(), this.cutTimes);
    }
}
