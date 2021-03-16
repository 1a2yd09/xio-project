package com.cat.pojo;

import com.cat.enums.BoardCategory;
import com.cat.utils.BoardUtil;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author CAT
 */
@Data
public abstract class BaseBoard {
    private BigDecimal height;
    private BigDecimal width;
    private BigDecimal length;
    private String material;
    private BoardCategory category;
    private Integer orderId;

    protected BaseBoard() {
    }

    protected BaseBoard(BigDecimal height, BigDecimal width, BigDecimal length, String material, BoardCategory category, Integer orderId) {
        this.height = height;
        this.width = width;
        this.length = length;
        this.material = material;
        this.category = category;
        this.orderId = orderId;
    }

    protected BaseBoard(String specification, String material, BoardCategory category, Integer orderId) {
        List<BigDecimal> list = BoardUtil.specStrToDecList(specification);
        this.height = list.get(0);
        this.width = list.get(1);
        this.length = list.get(2);
        this.material = material;
        this.category = category;
        this.orderId = orderId;
    }

    /**
     * 获取标准板材规格字符串。
     *
     * @return 标准板材规格字符串
     */
    public String getStandardSpecStr() {
        return BoardUtil.getStandardSpecStr(this.height, this.width, this.length);
    }
}
