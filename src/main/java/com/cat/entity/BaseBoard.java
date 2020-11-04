package com.cat.entity;

import com.cat.entity.enums.BoardCategoryEnum;
import com.cat.util.BoardUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public abstract class BaseBoard implements Comparable<BaseBoard> {
    private BigDecimal height;
    private BigDecimal width;
    private BigDecimal length;
    private String material;
    private BoardCategoryEnum category;

    public BaseBoard() {
    }

    public BaseBoard(BigDecimal height, BigDecimal width, BigDecimal length, String material, BoardCategoryEnum category) {
        this.height = height;
        this.width = width;
        this.length = length;
        this.material = material;
        this.category = category;
    }

    public BaseBoard(String specification, String material, BoardCategoryEnum category) {
        List<BigDecimal> list = BoardUtil.specStrToDecList(specification);
        this.height = list.get(0);
        this.width = list.get(1);
        this.length = list.get(2);
        this.material = material;
        this.category = category;
    }

    public String getSpecStr() {
        return BoardUtil.getStandardSpecStr(this.height, this.width, this.length);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.material, this.height, this.width, this.length);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BaseBoard) {
            BaseBoard ab = (BaseBoard) obj;
            return Objects.equals(this.material, ab.material) && this.height.compareTo(ab.height) == 0 && this.width.compareTo(ab.width) == 0 && this.length.compareTo(ab.length) == 0;
        }
        return false;
    }

    @Override
    public int compareTo(BaseBoard o) {
        if (o == null) {
            return -1;
        }
        if (this.material.equals(o.material) && this.height.compareTo(o.height) == 0) {
            if (this.width.compareTo(o.width) < 0 || this.length.compareTo(o.length) < 0) {
                return -1;
            } else if (this.width.compareTo(o.width) == 0 && this.length.compareTo(o.length) == 0) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return -1;
        }
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

    public BoardCategoryEnum getCategory() {
        return category;
    }

    public void setCategory(BoardCategoryEnum category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "AbstractBoard{" +
                "height=" + height +
                ", width=" + width +
                ", length=" + length +
                ", material='" + material + '\'' +
                ", category=" + category +
                '}';
    }
}
