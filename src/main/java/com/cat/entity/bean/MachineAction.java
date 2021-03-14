package com.cat.entity.bean;

import com.cat.entity.board.BaseBoard;
import com.cat.enums.ActionCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author CAT
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MachineAction {
    private Long id;
    private String state;
    private String actionCategory;
    private BigDecimal cutDistance;
    private String boardCategory;
    private String boardSpecification;
    private String boardMaterial;
    private Integer orderId;
    private LocalDateTime createdAt;

    public static MachineAction of(ActionCategory actionCategory, BigDecimal dis, BaseBoard baseBoard, Integer orderId) {
        MachineAction action = new MachineAction();
        action.setActionCategory(actionCategory.value);
        action.setCutDistance(dis);
        action.setBoardCategory(baseBoard.getCategory().value);
        action.setBoardSpecification(baseBoard.getStandardSpecStr());
        action.setBoardMaterial(baseBoard.getMaterial());
        action.setOrderId(orderId);
        return action;
    }
}
