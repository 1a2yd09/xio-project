package com.cat;

import com.cat.entity.CutBoard;
import com.cat.entity.NormalBoard;
import com.cat.entity.enums.BoardCategoryEnum;
import com.cat.service.BoardService;
import com.cat.util.BoardUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {
    static ApplicationContext context;
    static BoardService boardService;

    @BeforeAll
    static void init() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        boardService = context.getBean(BoardService.class);
    }

    @Test
    void testCompareBoard() {
        NormalBoard b1 = new NormalBoard("2.5×121×2185", "热板", BoardCategoryEnum.PRODUCT);
        NormalBoard b2 = new NormalBoard("2.5×121×2185", "冷板", BoardCategoryEnum.PRODUCT);
        assertEquals(-1, b1.compareTo(b2));

        b1 = new NormalBoard("2.5×121×2185", "热板", BoardCategoryEnum.PRODUCT);
        b2 = new NormalBoard("2×121×2185", "热板", BoardCategoryEnum.PRODUCT);
        assertEquals(-1, b1.compareTo(b2));

        b1 = new NormalBoard("2.5×120×2180", "热板", BoardCategoryEnum.PRODUCT);
        b2 = new NormalBoard("2.5×121×2185", "热板", BoardCategoryEnum.PRODUCT);
        assertEquals(-1, b1.compareTo(b2));

        b1 = new NormalBoard("2.5×121×2185", "热板", BoardCategoryEnum.PRODUCT);
        b2 = new NormalBoard("2.5×121×2185", "热板", BoardCategoryEnum.PRODUCT);
        assertEquals(0, b1.compareTo(b2));

        b1 = new NormalBoard("2.5×122×2186", "热板", BoardCategoryEnum.PRODUCT);
        b2 = new NormalBoard("2.5×121×2185", "热板", BoardCategoryEnum.PRODUCT);
        assertEquals(1, b1.compareTo(b2));
    }

    @Test
    void testCalNotProductCutTimes() {
        CutBoard b1 = new CutBoard("2.5×400×2185", "热板");
        NormalBoard b2 = new NormalBoard("2.5×200×2185", "热板", BoardCategoryEnum.PRODUCT);
        NormalBoard b3 = new NormalBoard("2.5×100×2185", "热板", BoardCategoryEnum.STOCK);
        int ret = boardService.calNotProductCutTimes(b1, b2.getWidth(), 1, b3);
        assertEquals(2, ret);
        b3 = new NormalBoard("2.5×0×2185", "热板", BoardCategoryEnum.STOCK);
        ret = boardService.calNotProductCutTimes(b1, b2.getWidth(), 1, b3);
        assertEquals(0, ret);
        b3 = new NormalBoard("2.5×0×2200", "热板", BoardCategoryEnum.STOCK);
        ret = boardService.calNotProductCutTimes(b1, b2.getWidth(), 1, b3);
        assertEquals(0, ret);
        b3 = new NormalBoard("2.5×300×2200", "热板", BoardCategoryEnum.STOCK);
        ret = boardService.calNotProductCutTimes(b1, b2.getWidth(), 1, b3);
        assertEquals(0, ret);
    }

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
        BoardCategoryEnum bc = BoardUtil.calBoardCategory(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO);
        assertEquals(BoardCategoryEnum.REMAINING, bc);
        bc = BoardUtil.calBoardCategory(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals(BoardCategoryEnum.REMAINING, bc);
        bc = BoardUtil.calBoardCategory(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE);
        assertEquals(BoardCategoryEnum.WASTED, bc);
    }
}
