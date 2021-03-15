package com.cat.pojo;

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

    /**
     * 根据指定参数实例化机器动作对象
     *
     * @param actionCategory 动作类型
     * @param dis            进刀距离
     * @param baseBoard      板材对象
     * @param orderId        工单 ID
     * @return 机器动作对象
     */
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
