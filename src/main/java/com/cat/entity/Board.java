package com.cat.entity;

import com.cat.entity.board.NormalBoard;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Board {
    private Integer orderId;
    private NormalBoard normalBoard;
}
