package com.cat.entity.bean;

import com.cat.utils.OrderUtils;

import java.time.LocalDateTime;

/**
 * @author CAT
 */
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

    public WorkOrder() {
    }

    public WorkOrder(String productSpecification, String productQuantity, String material, Integer id, String completedQuantity) {
        this.productSpecification = productSpecification;
        this.productQuantity = productQuantity;
        this.material = material;
        this.id = id;
        this.completedQuantity = completedQuantity;
    }

    /**
     * 查询工单成品的未完成数目。
     *
     * @return 未完成数目
     */
    public int getIncompleteQuantity() {
        return OrderUtils.quantityPropStrToInt(this.productQuantity) - OrderUtils.quantityPropStrToInt(this.completedQuantity);
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProcessNumber() {
        return processNumber;
    }

    public void setProcessNumber(String processNumber) {
        this.processNumber = processNumber;
    }

    public String getProductSpecification() {
        return productSpecification;
    }

    public void setProductSpecification(String productSpecification) {
        this.productSpecification = productSpecification;
    }

    public String getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(String productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getImplementationDate() {
        return implementationDate;
    }

    public void setImplementationDate(String implementationDate) {
        this.implementationDate = implementationDate;
    }

    public String getCuttingSize() {
        return cuttingSize;
    }

    public void setCuttingSize(String cuttingSize) {
        this.cuttingSize = cuttingSize;
    }

    public String getCuttingQuantity() {
        return cuttingQuantity;
    }

    public void setCuttingQuantity(String cuttingQuantity) {
        this.cuttingQuantity = cuttingQuantity;
    }

    public String getProgramNumber() {
        return programNumber;
    }

    public void setProgramNumber(String programNumber) {
        this.programNumber = programNumber;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getWorkCentre() {
        return workCentre;
    }

    public void setWorkCentre(String workCentre) {
        this.workCentre = workCentre;
    }

    public String getCompletedQuantity() {
        return completedQuantity;
    }

    public void setCompletedQuantity(String completedQuantity) {
        this.completedQuantity = completedQuantity;
    }

    public String getReportQuantity() {
        return reportQuantity;
    }

    public void setReportQuantity(String reportQuantity) {
        this.reportQuantity = reportQuantity;
    }

    public String getScrapQuantity() {
        return scrapQuantity;
    }

    public void setScrapQuantity(String scrapQuantity) {
        this.scrapQuantity = scrapQuantity;
    }

    public String getOperationState() {
        return operationState;
    }

    public void setOperationState(String operationState) {
        this.operationState = operationState;
    }

    public String getSiteModule() {
        return siteModule;
    }

    public void setSiteModule(String siteModule) {
        this.siteModule = siteModule;
    }

    public LocalDateTime getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDateTime completionDate) {
        this.completionDate = completionDate;
    }

    @Override
    public String toString() {
        return "WorkOrder{" +
                "batchNumber='" + batchNumber + '\'' +
                ", contractNumber='" + contractNumber + '\'' +
                ", itemNumber='" + itemNumber + '\'' +
                ", productName='" + productName + '\'' +
                ", processNumber='" + processNumber + '\'' +
                ", productSpecification='" + productSpecification + '\'' +
                ", productQuantity='" + productQuantity + '\'' +
                ", material='" + material + '\'' +
                ", implementationDate='" + implementationDate + '\'' +
                ", cuttingSize='" + cuttingSize + '\'' +
                ", cuttingQuantity='" + cuttingQuantity + '\'' +
                ", programNumber='" + programNumber + '\'' +
                ", sequenceNumber='" + sequenceNumber + '\'' +
                ", orderNumber='" + orderNumber + '\'' +
                ", oid='" + oid + '\'' +
                ", id=" + id +
                ", workCentre='" + workCentre + '\'' +
                ", completedQuantity='" + completedQuantity + '\'' +
                ", reportQuantity='" + reportQuantity + '\'' +
                ", scrapQuantity='" + scrapQuantity + '\'' +
                ", operationState='" + operationState + '\'' +
                ", siteModule='" + siteModule + '\'' +
                ", completionDate=" + completionDate +
                '}';
    }
}
