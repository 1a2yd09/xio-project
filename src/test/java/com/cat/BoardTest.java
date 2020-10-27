package com.cat;

import com.cat.entity.Board;
import com.cat.entity.enums.BoardCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BoardTest {
    @Test
    void testCompareBoard() {
        Board b1 = new Board("2.5×121×2185", "热板", BoardCategory.PRODUCT);
        Board b2 = new Board("2.5×121×2185", "冷板", BoardCategory.PRODUCT);
        assertEquals(-1, b1.compareTo(b2));

        b1 = new Board("2.5×121×2185", "热板", BoardCategory.PRODUCT);
        b2 = new Board("2×121×2185", "热板", BoardCategory.PRODUCT);
        assertEquals(-1, b1.compareTo(b2));

        b1 = new Board("2.5×120×2180", "热板", BoardCategory.PRODUCT);
        b2 = new Board("2.5×121×2185", "热板", BoardCategory.PRODUCT);
        assertEquals(-1, b1.compareTo(b2));

        b1 = new Board("2.5×121×2185", "热板", BoardCategory.PRODUCT);
        b2 = new Board("2.5×121×2185", "热板", BoardCategory.PRODUCT);
        assertEquals(0, b1.compareTo(b2));

        b1 = new Board("2.5×122×2186", "热板", BoardCategory.PRODUCT);
        b2 = new Board("2.5×121×2185", "热板", BoardCategory.PRODUCT);
        assertEquals(1, b1.compareTo(b2));
    }
}
