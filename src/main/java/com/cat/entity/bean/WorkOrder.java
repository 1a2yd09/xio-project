package com.cat.entity.bean;

import com.cat.utils.OrderUtils;
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
    private String batchNumber;
    private String contractNumber;
    private String itemNumber;
    private String productName;
    private String processNumber;
    private String productSpecification;
    private String productQuantity;
    private String material;
    private String implementationDate;
    private String cuttingSize;
    private String cuttingQuantity;
    private String programNumber;
    private String sequenceNumber;
    private String orderNumber;
    private String oid;
    private Integer id;
    private String workCentre;
    private String completedQuantity;
    private String reportQuantity;
    private String scrapQuantity;
    private String operationState;
    private String siteModule;
    private LocalDateTime completionDate;

    public WorkOrder(String productSpecification, String productQuantity, String material, Integer id, String completedQuantity) {
        this.productSpecification = productSpecification;
        this.productQuantity = productQuantity;
        this.material = material;
        this.id = id;
        this.completedQuantity = completedQuantity;
    }

    /**
     * 获取成品板材的未完成数目。
     *
     * @return 成品未完成数目
     */
    public int getIncompleteQuantity() {
        return OrderUtils.quantityPropStrToInt(this.productQuantity) - OrderUtils.quantityPropStrToInt(this.completedQuantity);
    }
}
