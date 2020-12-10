package com.cat.entity.board;

import com.cat.enums.BoardCategory;
import com.cat.utils.BoardUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author CAT
 */
public abstract class BaseBoard {
    private BigDecimal height;
    private BigDecimal width;
    private BigDecimal length;
    private String material;
    private BoardCategory category;

    protected BaseBoard() {
    }

    protected BaseBoard(BigDecimal height, BigDecimal width, BigDecimal length, String material, BoardCategory category) {
        this.height = height;
        this.width = width;
        this.length = length;
        this.material = material;
        this.category = category;
    }

    protected BaseBoard(String specification, String material, BoardCategory category) {
        List<BigDecimal> list = BoardUtils.specStrToDecList(specification);
        this.height = list.get(0);
        this.width = list.get(1);
        this.length = list.get(2);
        this.material = material;
        this.category = category;
    }

    /**
     * 获取标准规格字符串。
     *
     * @return 标准规格字符串
     */
    public String getStandardSpecStr() {
        return BoardUtils.getStandardSpecStr(this.height, this.width, this.length);
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getWidth() {
        return width;
    }

    public void setWidth(BigDecimal width) {
        this.width = width;
    }

    public BigDecimal getLength() {
        return length;
    }

    public void setLength(BigDecimal length) {
        this.length = length;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public BoardCategory getCategory() {
        return category;
    }

    public void setCategory(BoardCategory category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "BaseBoard{" +
                "height=" + height +
                ", width=" + width +
                ", length=" + length +
                ", material='" + material + '\'' +
                ", category=" + category +
                '}';
    }
}
