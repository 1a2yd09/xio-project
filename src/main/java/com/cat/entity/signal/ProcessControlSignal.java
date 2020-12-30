package com.cat.entity.signal;

/**
 * @author CAT
 */
public class ProcessControlSignal extends BaseSignal {
    private Integer category;

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return super.toString() +
                "ProcessControlSignal{" +
                "category=" + category +
                '}';
    }
}
