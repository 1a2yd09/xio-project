package com.cat.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author CAT
 */
@Data
@AllArgsConstructor
public class OrderCount {
    private String date;
    private int count;
}
