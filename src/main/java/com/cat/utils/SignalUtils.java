package com.cat.utils;

import com.cat.entity.bean.WorkOrder;
import com.cat.entity.signal.CuttingSignal;
import com.cat.enums.ForwardEdge;

/**
 * @author CAT
 */
public class SignalUtils {
    private SignalUtils() {

    }

    public static CuttingSignal getDefaultCuttingSignal(WorkOrder order) {
        return new CuttingSignal(order.getCuttingSize(), ForwardEdge.SHORT.code, order.getId());
    }
}
