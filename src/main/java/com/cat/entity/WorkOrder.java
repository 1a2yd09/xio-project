package com.cat.entity;

import com.cat.util.OrderUtil;

import java.time.LocalDateTime;

public class WorkOrder {
    private String batchNumber;
    private String contractNumber;
    private String itemNumber;
    private String name;
    private String processNumber;
    private String specification;
    private String amount;
    private String material;
    private String implementationDate;
    private String cuttingSize;
    private String cuttingAmount;
    private String programNumber;
    private String sequenceNumber;
    private String workOrderNumber;
    private String oid;
    private Integer id;
    private String workCentre;
    private String completedAmount;
    private String reportingAmount;
    private String rejectionAmount;
    private String operationState;
    private String siteModule;
    private LocalDateTime completionDate;

    public int getUnfinishedAmount() {
        return OrderUtil.amountPropStrToInt(this.amount) - OrderUtil.amountPropStrToInt(this.completedAmount);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProcessNumber() {
        return processNumber;
    }

    public void setProcessNumber(String processNumber) {
        this.processNumber = processNumber;
    }

    public String getSpecStr() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
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

    public String getCuttingAmount() {
        return cuttingAmount;
    }

    public void setCuttingAmount(String cuttingAmount) {
        this.cuttingAmount = cuttingAmount;
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

    public String getWorkOrderNumber() {
        return workOrderNumber;
    }

    public void setWorkOrderNumber(String workOrderNumber) {
        this.workOrderNumber = workOrderNumber;
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

    public String getCompletedAmount() {
        return completedAmount;
    }

    public void setCompletedAmount(String completedAmount) {
        this.completedAmount = completedAmount;
    }

    public String getReportingAmount() {
        return reportingAmount;
    }

    public void setReportingAmount(String reportingAmount) {
        this.reportingAmount = reportingAmount;
    }

    public String getRejectionAmount() {
        return rejectionAmount;
    }

    public void setRejectionAmount(String rejectionAmount) {
        this.rejectionAmount = rejectionAmount;
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
                ", name='" + name + '\'' +
                ", processNumber='" + processNumber + '\'' +
                ", specification='" + specification + '\'' +
                ", amount='" + amount + '\'' +
                ", material='" + material + '\'' +
                ", implementationDate='" + implementationDate + '\'' +
                ", cuttingSize='" + cuttingSize + '\'' +
                ", cuttingAmount='" + cuttingAmount + '\'' +
                ", programNumber='" + programNumber + '\'' +
                ", sequenceNumber='" + sequenceNumber + '\'' +
                ", workOrderNumber='" + workOrderNumber + '\'' +
                ", oid='" + oid + '\'' +
                ", id=" + id +
                ", workCentre='" + workCentre + '\'' +
                ", completedAmount='" + completedAmount + '\'' +
                ", reportingAmount='" + reportingAmount + '\'' +
                ", rejectionAmount='" + rejectionAmount + '\'' +
                ", operationState='" + operationState + '\'' +
                ", siteModule='" + siteModule + '\'' +
                ", completionDate=" + completionDate +
                '}';
    }
}
