package com.cat;

import com.cat.enums.BoardCategory;
import com.cat.pojo.NormalBoard;
import com.cat.service.ProcessBoardService;
import com.cat.utils.BoardUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest extends BaseTest {
    @Autowired
    ProcessBoardService processBoardService;

    @Test
    void testCompareTwoSpecStr() {
        String s1 = "2.5×200×2185";
        String s2 = "2.50×200.00×2185.00";
        assertEquals(0, BoardUtil.compareTwoSpecStr(s1, s2));
        s1 = "2.5×200×2185";
        s2 = "2.4×200×2185";
        assertNotEquals(0, BoardUtil.compareTwoSpecStr(s1, s2));
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
        assertEquals(BoardCategory.WASTE, bc);
    }

    @Test
    void testCompareTwoSpec() {
        assertTrue(BoardUtil.isFirstSpecGeSecondSpec("2.5×1200×1500", "2.5×1100×1400"));
        assertTrue(BoardUtil.isFirstSpecGeSecondSpec("2.5×1200×1500", "2.5×1400×1100"));
        assertTrue(BoardUtil.isFirstSpecGeSecondSpec("2.5×1200×1500", "2.5×1200×1500"));
        assertTrue(BoardUtil.isFirstSpecGeSecondSpec("2.5×1200×1500", "2.5×1200×1400"));
        assertTrue(BoardUtil.isFirstSpecGeSecondSpec("2.5×1300×1400", "2.5×1200×1400"));
        assertFalse(BoardUtil.isFirstSpecGeSecondSpec("2.5×1200×1500", "2.5×1300×1400"));
        assertFalse(BoardUtil.isFirstSpecGeSecondSpec("2.5×1200×1500", "2.5×1100×1600"));
        assertFalse(BoardUtil.isFirstSpecGeSecondSpec("2.5×1200×1500", "2.5×1300×1600"));
    }

    @Test
    void testBoardQueue() {
        Queue<NormalBoard> queue = BoardUtil.getBoardLengthPriorityQueue();
        NormalBoard board1 = new NormalBoard("2.5×800×1000", "镀锌板", BoardCategory.PRODUCT, 10000);
        board1.setCutTimes(1);
        NormalBoard board2 = new NormalBoard("2.5×1000×2000", "镀锌板", BoardCategory.PRODUCT, 10000);
        board2.setCutTimes(1);
        NormalBoard board3 = new NormalBoard("2.5×1000×1600", "镀锌板", BoardCategory.PRODUCT, 10000);
        board3.setCutTimes(2);
        queue.add(board1);
        queue.add(board2);
        queue.add(board3);
        assertEquals(3, queue.size());
        System.out.println(queue.poll());
        System.out.println(queue.poll());
        System.out.println(queue.poll());
    }
}
