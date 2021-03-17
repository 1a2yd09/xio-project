package com.cat.utils;

import com.cat.enums.ForwardEdge;
import com.cat.pojo.CuttingSignal;
import com.cat.pojo.WorkOrder;

/**
 * @author CAT
 */
public class SignalUtil {
    private SignalUtil() {

    }

    public static CuttingSignal getDefaultCuttingSignal(WorkOrder order) {
        return new CuttingSignal(order.getCuttingSize(), ForwardEdge.SHORT.code, order.getId());
    }
}
