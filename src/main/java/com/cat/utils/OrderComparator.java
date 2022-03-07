package com.cat.utils;

import com.cat.pojo.WorkOrder;

import java.util.Map;

/**
 * @author CAT
 */

public enum OrderComparator {
    /**
     * 按照顺序号进行工单排序。
     */
    SEQ("SEQ") {
        @Override
        public int compare(WorkOrder o1, WorkOrder o2) {
            Integer sn1 = Integer.parseInt(o1.getSequenceNumber());
            Integer sn2 = Integer.parseInt(o2.getSequenceNumber());
            return sn1.equals(sn2) ? o1.getId() - o2.getId() : sn1.compareTo(sn2);
        }
    },
    /**
     * 按照规格进行工单排序。
     */
    SPEC("SPEC") {
        @Override
        public int compare(WorkOrder o1, WorkOrder o2) {
            int retVal = BoardUtil.compareTwoSpecStr(o1.getProductSpecification(), o2.getProductSpecification());
            return retVal != 0 ? -retVal : o1.getId() - o2.getId();
        }
    },
    /**
     * 先按批次号再按顺序号进行排序。
     */
    PCH_SEQ("PCH_SEQ") {
        @Override
        public int compare(WorkOrder o1, WorkOrder o2) {
            Integer bn1 = Integer.parseInt(o1.getBatchNumber());
            Integer bn2 = Integer.parseInt(o2.getBatchNumber());
            if (!bn1.equals(bn2)) {
                return bn1 - bn2;
            }
            Integer sn1 = Integer.parseInt(o1.getSequenceNumber());
            Integer sn2 = Integer.parseInt(o2.getSequenceNumber());
            return sn1.equals(sn2) ? o1.getId() - o2.getId() : sn1.compareTo(sn2);
        }
    },
    /**
     * 先按批次号再按规格进行排序。
     */
    PCH_SPEC("PCH_SPEC") {
        @Override
        public int compare(WorkOrder o1, WorkOrder o2) {
            Integer bn1 = Integer.parseInt(o1.getBatchNumber());
            Integer bn2 = Integer.parseInt(o2.getBatchNumber());
            if (!bn1.equals(bn2)) {
                return bn1 - bn2;
            }
            int retVal = BoardUtil.compareSpec(o1.getProductSpecification(), o2.getProductSpecification());
            return retVal != 0 ? retVal : o1.getId() - o2.getId();
        }
    };

    String value;

    private static final Map<String, OrderComparator> LOOKUP = Map.of(
            SEQ.value, SEQ,
            SPEC.value, SPEC,
            PCH_SEQ.value, PCH_SEQ,
            PCH_SPEC.value, PCH_SPEC
    );

    OrderComparator(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static OrderComparator getComparator(String name) {
        return LOOKUP.get(name);
    }

    public abstract int compare(WorkOrder o1, WorkOrder o2);
}
