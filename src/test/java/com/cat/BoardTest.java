package com.cat;

import com.cat.entity.NormalBoard;
import com.cat.entity.enums.BoardCategory;
import com.cat.service.BoardService;
import com.cat.util.BoardUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest extends BaseTest {
    @Autowired
    BoardService boardService;

    @Test
    void testCompareTwoSpecStr() {
        String s1 = "2.5×200×2185";
        String s2 = "2.50×200.00×2185.00";
        assertTrue(BoardUtil.isTwoSpecStrEqual(s1, s2));
        s1 = "2.5×200×2185";
        s2 = "2.4×200×2185";
        assertFalse(BoardUtil.isTwoSpecStrEqual(s1, s2));
    }

    @Test
    void testSpecStrToDecList() {
        List<BigDecimal> list = BoardUtil.specStrToDecList("1.2×722.4×1250");
        assertNotNull(list);
        System.out.println(list);
        list = BoardUtil.specStrToDecList("2.5×1250×1589");
        assertNotNull(list);
        System.out.println(list);
    }

    @Test
    void testGetStandardSpecStr() {
        String result = BoardUtil.getStandardSpecStr(new BigDecimal("1.2"), new BigDecimal("722.4"), new BigDecimal("1250"));
        assertNotNull(result);
        System.out.println(result);
        result = BoardUtil.getStandardSpecStr("2.5×1250×1589");
        assertNotNull(result);
        System.out.println(result);
    }

    @Test
    void testCalBoardCategory() {
        BoardCategory bc = BoardUtil.calBoardCategory(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO);
        assertEquals(BoardCategory.REMAINING, bc);
        bc = BoardUtil.calBoardCategory(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals(BoardCategory.REMAINING, bc);
        bc = BoardUtil.calBoardCategory(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE);
        assertEquals(BoardCategory.WASTED, bc);
    }
}
