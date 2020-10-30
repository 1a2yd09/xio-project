package com.cat;

import com.cat.entity.CutBoard;
import com.cat.entity.NormalBoard;
import com.cat.entity.enums.BoardCategory;
import com.cat.service.BoardService;
import com.cat.util.BoardUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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
        NormalBoard b1 = new NormalBoard("2.5×121×2185", "热板", BoardCategory.PRODUCT);
        NormalBoard b2 = new NormalBoard("2.5×121×2185", "冷板", BoardCategory.PRODUCT);
        assertEquals(-1, b1.compareTo(b2));

        b1 = new NormalBoard("2.5×121×2185", "热板", BoardCategory.PRODUCT);
        b2 = new NormalBoard("2×121×2185", "热板", BoardCategory.PRODUCT);
        assertEquals(-1, b1.compareTo(b2));

        b1 = new NormalBoard("2.5×120×2180", "热板", BoardCategory.PRODUCT);
        b2 = new NormalBoard("2.5×121×2185", "热板", BoardCategory.PRODUCT);
        assertEquals(-1, b1.compareTo(b2));

        b1 = new NormalBoard("2.5×121×2185", "热板", BoardCategory.PRODUCT);
        b2 = new NormalBoard("2.5×121×2185", "热板", BoardCategory.PRODUCT);
        assertEquals(0, b1.compareTo(b2));

        b1 = new NormalBoard("2.5×122×2186", "热板", BoardCategory.PRODUCT);
        b2 = new NormalBoard("2.5×121×2185", "热板", BoardCategory.PRODUCT);
        assertEquals(1, b1.compareTo(b2));
    }

    @Test
    void testCalNotProductCutTimes() {
        CutBoard b1 = new CutBoard("2.5×400×2185", "热板");
        NormalBoard b2 = new NormalBoard("2.5×200×2185", "热板", BoardCategory.PRODUCT);
        NormalBoard b3 = new NormalBoard("2.5×100×2185", "热板", BoardCategory.STOCK);
        int ret = boardService.calNotProductCutTimes(b1, b2.getWidth(), 1, b3);
        assertEquals(2, ret);
        b3 = new NormalBoard("2.5×0×2185", "热板", BoardCategory.STOCK);
        ret = boardService.calNotProductCutTimes(b1, b2.getWidth(), 1, b3);
        assertEquals(0, ret);
        b3 = new NormalBoard("2.5×0×2200", "热板", BoardCategory.STOCK);
        ret = boardService.calNotProductCutTimes(b1, b2.getWidth(), 1, b3);
        assertEquals(0, ret);
        b3 = new NormalBoard("2.5×300×2200", "热板", BoardCategory.STOCK);
        ret = boardService.calNotProductCutTimes(b1, b2.getWidth(), 1, b3);
        assertEquals(0, ret);
    }

    @Test
    void testCompareTwoSpecStr() {
        String s1 = "2.5×200×2185";
        String s2 = "2.50×200.00×2185.00";
        assertTrue(BoardUtil.compareTwoSpecStr(s1, s2));
        s1 = "2.5×200×2185";
        s2 = "2.4×200×2185";
        assertFalse(BoardUtil.compareTwoSpecStr(s1, s2));
    }
}
