package com.cat.service;

import com.cat.enums.ActionCategory;
import com.cat.enums.ForwardEdge;
import com.cat.mapper.ActionMapper;
import com.cat.pojo.BoardList;
import com.cat.pojo.CutBoard;
import com.cat.pojo.MachineAction;
import com.cat.pojo.NormalBoard;
import com.cat.utils.ArithmeticUtil;
import com.cat.utils.BoardUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author CAT
 */
@Service
public class ProcessBoardService {
    private final ActionMapper actionMapper;

    public ProcessBoardService(ActionMapper actionMapper) {
        this.actionMapper = actionMapper;
    }

    /**
     * 板材裁剪函数，负责下料板材的实际旋转和裁剪操作。
     *
     * @param cutBoard    下料板
     * @param forwardEdge 朝向
     * @param targetBoard 目标板材
     */
    public void cuttingBoard(CutBoard cutBoard, ForwardEdge forwardEdge, NormalBoard targetBoard) {
        if (targetBoard.getCutTimes() > 0) {
            if (cutBoard.getForwardEdge() != forwardEdge) {
                cutBoard.setForwardEdge(forwardEdge);
                this.actionMapper.insertMachineAction(MachineAction.of(ActionCategory.ROTATE, BigDecimal.ZERO, cutBoard));
            }

            BigDecimal dis = targetBoard.getWidth();
            if (cutBoard.getForwardEdge() == ForwardEdge.LONG) {
                cutBoard.setWidth(ArithmeticUtil.sub(cutBoard.getWidth(), dis));
            } else {
                cutBoard.setLength(ArithmeticUtil.sub(cutBoard.getLength(), dis));
            }

            if (cutBoard.getWidth().compareTo(BigDecimal.ZERO) > 0) {
                this.actionMapper.insertMachineAction(MachineAction.of(ActionCategory.CUT, dis, targetBoard));
            } else {
                this.actionMapper.insertMachineAction(MachineAction.of(ActionCategory.SEND, BigDecimal.ZERO, targetBoard));
            }
        }
    }

    /**
     * 板材裁剪流程，负责定义下料板整个裁剪流程。
     *
     * @param cutBoard       下料板
     * @param boardList      目标板材列表
     * @param wasteThreshold 废料阈值
     */
    public void newCutting(CutBoard cutBoard, BoardList boardList, BigDecimal wasteThreshold) {
        List<NormalBoard> boards = boardList.getBoards();
        NormalBoard lastNormalBoard = boards.get(boards.size() - 1);
        if (BoardUtil.isAllowBackToFront(lastNormalBoard.getAllWidth(), lastNormalBoard.getWidth())) {
            NormalBoard extraBoard = BoardUtil.getExtraBoard(cutBoard, ForwardEdge.LONG, boardList.getBoardAllWidth(), wasteThreshold);
            this.cuttingBoard(cutBoard, ForwardEdge.LONG, extraBoard);
            for (NormalBoard board : boards) {
                if (board.getCutTimes() > 0) {
                    extraBoard = BoardUtil.getExtraBoard(cutBoard, ForwardEdge.SHORT, board.getLength(), wasteThreshold);
                    this.cuttingBoard(cutBoard, ForwardEdge.SHORT, extraBoard);
                }
                for (int i = 0; i < board.getCutTimes(); i++) {
                    this.cuttingBoard(cutBoard, ForwardEdge.LONG, board);
                }
            }
        } else {
            this.newestCutting(cutBoard, boardList, wasteThreshold);
        }
    }

    /**
     * 板材裁剪流程，负责定义下料板整个裁剪流程。
     *
     * @param cutBoard       下料板
     * @param boardList      目标板材列表
     * @param wasteThreshold 废料阈值
     */
    public void newestCutting(CutBoard cutBoard, BoardList boardList, BigDecimal wasteThreshold) {
        for (NormalBoard board : boardList.getBoards()) {
            if (board.getCutTimes() > 0) {
                NormalBoard extraBoard = BoardUtil.getExtraBoard(cutBoard, ForwardEdge.SHORT, board.getLength(), wasteThreshold);
                this.cuttingBoard(cutBoard, ForwardEdge.SHORT, extraBoard);
            }
            for (int i = 0; i < board.getCutTimes(); i++) {
                BigDecimal remainingWidth = ArithmeticUtil.sub(cutBoard.getWidth(), board.getWidth());
                if (remainingWidth.compareTo(BoardUtil.CLAMP_DEPTH) <= 0) {
                    NormalBoard extraBoard = BoardUtil.getExtraBoard(cutBoard, ForwardEdge.LONG, board.getWidth(), wasteThreshold);
                    this.cuttingBoard(cutBoard, ForwardEdge.LONG, extraBoard);
                }
                this.cuttingBoard(cutBoard, ForwardEdge.LONG, board);
            }
        }
        if (cutBoard.getWidth().compareTo(BigDecimal.ZERO) > 0) {
            NormalBoard extraBoard = BoardUtil.getExtraBoard(cutBoard, ForwardEdge.LONG, BigDecimal.ZERO, wasteThreshold);
            this.cuttingBoard(cutBoard, ForwardEdge.LONG, extraBoard);
        }
    }
}
