package com.cat;

import com.cat.entity.board.NormalBoard;
import com.cat.enums.BoardCategory;
import com.cat.service.BoardService;
import com.cat.utils.BoardUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest extends BaseTest {
    @Autowired
    BoardService boardService;

    @Test
    void testCompareTwoSpecStr() {
        String s1 = "2.5×200×2185";
        String s2 = "2.50×200.00×2185.00";
        assertEquals(0, BoardUtils.compareTwoSpecStr(s1, s2));
        s1 = "2.5×200×2185";
        s2 = "2.4×200×2185";
        assertNotEquals(0, BoardUtils.compareTwoSpecStr(s1, s2));
    }

    @Test
    void testSpecStrToDecList() {
        List<BigDecimal> list = BoardUtils.specStrToDecList("1.2×722.4×1250");
        assertNotNull(list);
        System.out.println(list);
        list = BoardUtils.specStrToDecList("2.5×1250×1589");
        assertNotNull(list);
        System.out.println(list);
    }

    @Test
    void testGetStandardSpecStr() {
        String result = BoardUtils.getStandardSpecStr(new BigDecimal("1.2"), new BigDecimal("722.4"), new BigDecimal("1250"));
        assertNotNull(result);
        System.out.println(result);
        result = BoardUtils.getStandardSpecStr("2.5×1250×1589");
        assertNotNull(result);
        System.out.println(result);
    }

    @Test
    void testCalBoardCategory() {
        BoardCategory bc = BoardUtils.calBoardCategory(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO);
        assertEquals(BoardCategory.REMAINING, bc);
        bc = BoardUtils.calBoardCategory(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals(BoardCategory.REMAINING, bc);
        bc = BoardUtils.calBoardCategory(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE);
        assertEquals(BoardCategory.WASTE, bc);
    }

    @Test
    void testCompareTwoSpec() {
        assertTrue(BoardUtils.isFirstSpecGeSecondSpec("2.5×1200×1500", "2.5×1100×1400"));
        assertTrue(BoardUtils.isFirstSpecGeSecondSpec("2.5×1200×1500", "2.5×1400×1100"));
        assertTrue(BoardUtils.isFirstSpecGeSecondSpec("2.5×1200×1500", "2.5×1200×1500"));
        assertTrue(BoardUtils.isFirstSpecGeSecondSpec("2.5×1200×1500", "2.5×1200×1400"));
        assertTrue(BoardUtils.isFirstSpecGeSecondSpec("2.5×1300×1400", "2.5×1200×1400"));
        assertFalse(BoardUtils.isFirstSpecGeSecondSpec("2.5×1200×1500", "2.5×1300×1400"));
        assertFalse(BoardUtils.isFirstSpecGeSecondSpec("2.5×1200×1500", "2.5×1100×1600"));
        assertFalse(BoardUtils.isFirstSpecGeSecondSpec("2.5×1200×1500", "2.5×1300×1600"));
    }

    @Test
    void testBoardQueue() {
        Queue<NormalBoard> queue = BoardUtils.getBoardLengthPriorityQueue();
        NormalBoard board1 = new NormalBoard("2.5×800×1000", "镀锌板", BoardCategory.PRODUCT);
        board1.setCutTimes(1);
        NormalBoard board2 = new NormalBoard("2.5×1000×2000", "镀锌板", BoardCategory.PRODUCT);
        board2.setCutTimes(1);
        NormalBoard board3 = new NormalBoard("2.5×1000×1600", "镀锌板", BoardCategory.PRODUCT);
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
