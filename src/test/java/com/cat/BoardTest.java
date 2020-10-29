package com.cat;

import com.cat.entity.Board;
import com.cat.entity.CutBoard;
import com.cat.entity.enums.BoardCategory;
import com.cat.service.BoardService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    void testCalNotProductCutTimes() {
        CutBoard b1 = new CutBoard("2.5×400×2185", "热板");
        Board b2 = new Board("2.5×200×2185", "热板", BoardCategory.PRODUCT);
        Board b3 = new Board("2.5×100×2185", "热板", BoardCategory.STOCK);
        int ret = boardService.calNotProductCutTimes(b1, b2.getWidth(), 1, b3);
        assertEquals(2, ret);
        b3 = new Board("2.5×0×2185", "热板", BoardCategory.STOCK);
        ret = boardService.calNotProductCutTimes(b1, b2.getWidth(), 1, b3);
        assertEquals(0, ret);
        b3 = new Board("2.5×0×2200", "热板", BoardCategory.STOCK);
        ret = boardService.calNotProductCutTimes(b1, b2.getWidth(), 1, b3);
        assertEquals(0, ret);
        b3 = new Board("2.5×300×2200", "热板", BoardCategory.STOCK);
        ret = boardService.calNotProductCutTimes(b1, b2.getWidth(), 1, b3);
        assertEquals(0, ret);
    }
}
