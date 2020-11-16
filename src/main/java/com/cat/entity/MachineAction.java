package com.cat.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MachineAction {
    private Long id;
    private Boolean completed;
    private String actionCategory;
    private BigDecimal cutDistance;
    private String boardCategory;
    private String boardSpecification;
    private String boardMaterial;
    private Integer workOrderId;
    private String workOrderModule;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public String getActionCategory() {
        return actionCategory;
    }

    public void setActionCategory(String actionCategory) {
        this.actionCategory = actionCategory;
    }

    public BigDecimal getCutDistance() {
        return cutDistance;
    }

    public void setCutDistance(BigDecimal cutDistance) {
        this.cutDistance = cutDistance;
    }

    public String getBoardCategory() {
        return boardCategory;
    }

    public void setBoardCategory(String boardCategory) {
        this.boardCategory = boardCategory;
    }

    public String getBoardSpecification() {
        return boardSpecification;
    }

    public void setBoardSpecification(String boardSpecification) {
        this.boardSpecification = boardSpecification;
    }

    public String getBoardMaterial() {
        return boardMaterial;
    }

    public void setBoardMaterial(String boardMaterial) {
        this.boardMaterial = boardMaterial;
    }

    public Integer getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Integer workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getWorkOrderModule() {
        return workOrderModule;
    }

    public void setWorkOrderModule(String workOrderModule) {
        this.workOrderModule = workOrderModule;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "MachineAction{" +
                "id=" + id +
                ", completed=" + completed +
                ", actionCategory='" + actionCategory + '\'' +
                ", cutDistance=" + cutDistance +
                ", boardCategory='" + boardCategory + '\'' +
                ", boardSpecification='" + boardSpecification + '\'' +
                ", boardMaterial='" + boardMaterial + '\'' +
                ", workOrderId=" + workOrderId +
                ", workOrderModule='" + workOrderModule + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
