package com.cat.pojo;

import com.cat.utils.OrderUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author CAT
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrder {
    private String operationState;
    private String productSpecification;
    private String material;
    private String productQuantity;
    private LocalDateTime completionDate;
    private Integer id;
    private String batchNumber;
    private String sequenceNumber;
    private String cuttingSize;
    private String siteModule;
    private String completedQuantity;

    public WorkOrder(String productSpecification, String material, String productQuantity, Integer id) {
        this.productSpecification = productSpecification;
        this.material = material;
        this.productQuantity = productQuantity;
        this.id = id;
    }

    /**
     * 获取成品板材的未完成数目。
     *
     * @return 成品未完成数目
     */
    public int getIncompleteQuantity() {
        return OrderUtil.quantityPropStrToInt(this.productQuantity) - OrderUtil.quantityPropStrToInt(this.completedQuantity);
    }
}
