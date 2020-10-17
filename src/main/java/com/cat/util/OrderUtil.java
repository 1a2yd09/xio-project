package com.cat.util;

public class OrderUtil {
    public static Integer calUnfinishedAmount(String amount, String completedAmount) {
        Integer targetAmount = Integer.parseInt(amount);
        Integer finishedAmount = completedAmount == null ? 0 : Integer.parseInt(completedAmount);
        return targetAmount - finishedAmount;
    }
}
