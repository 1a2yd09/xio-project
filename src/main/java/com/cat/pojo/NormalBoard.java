package com.cat.pojo;

import com.cat.enums.BoardCategory;
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
    private Integer cutTimes;

    public NormalBoard() {
    }

    public NormalBoard(BigDecimal height, BigDecimal width, BigDecimal length, String material, BoardCategory category) {
        super(height, width, length, material, category);
        this.cutTimes = 0;
    }

    public NormalBoard(String specification, String material, BoardCategory category) {
        super(specification, material, category);
        this.cutTimes = 0;
    }

    /**
     * 获取加工当前板材所需总宽度。
     *
     * @return 总宽度
     */
    public BigDecimal getNormalBoardAllWidth() {
        return this.getWidth().multiply(BigDecimal.valueOf(this.cutTimes));
    }
}
