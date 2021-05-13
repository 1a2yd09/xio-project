package com.cat.service;

import com.cat.enums.ActionCategory;
import com.cat.enums.ForwardEdge;
import com.cat.mapper.ActionMapper;
import com.cat.pojo.*;
import com.cat.utils.BoardUtil;
import com.cat.utils.DecimalUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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
     * 板材裁剪流程。
     *
     * @param cutBoard       下料板
     * @param boardList      目标板材列表
     * @param wasteThreshold 废料阈值
     */
    public void cutting(CutBoard cutBoard, BoardList boardList, BigDecimal wasteThreshold, CuttingSignal signal) {
        if (BoardUtil.isAllowBackToFront(boardList.getLastBoard())) {
            this.backToFrontCutting(cutBoard, boardList, wasteThreshold, signal);
        } else {
            this.frontToBackCutting(cutBoard, boardList, wasteThreshold, signal);
        }
    }

    /**
     * 板材裁剪流程，整体板材从后向前紧挨着排版。
     *
     * @param cutBoard       下料板
     * @param boardList      目标板材列表
     * @param wasteThreshold 废料阈值
     */
    private void backToFrontCutting(CutBoard cutBoard, BoardList boardList, BigDecimal wasteThreshold, CuttingSignal signal) {
        cutBoard.setWidth(cutBoard.getWidth().add(signal.getLongEdgeTrim()));
        NormalBoard extraBoard = BoardUtil.getExtraBoard(cutBoard, ForwardEdge.SHORT, boardList.getBoards().get(0).getLength(), wasteThreshold);
        this.cuttingBoard(cutBoard, ForwardEdge.SHORT, extraBoard);
        extraBoard = BoardUtil.getExtraBoard(cutBoard, ForwardEdge.LONG, boardList.getBoardAllWidth(), wasteThreshold);
        this.cuttingBoard(cutBoard, ForwardEdge.LONG, extraBoard);
        for (NormalBoard board : boardList.getBoards()) {
            extraBoard = BoardUtil.getExtraBoard(cutBoard, ForwardEdge.SHORT, board.getLength(), wasteThreshold);
            this.cuttingBoard(cutBoard, ForwardEdge.SHORT, extraBoard);
            for (int i = 0; i < board.getCutTimes(); i++) {
                this.cuttingBoard(cutBoard, ForwardEdge.LONG, board);
            }
        }
    }

    /**
     * 板材裁剪流程，整体板材从前向后紧挨着排版。
     *
     * @param cutBoard       下料板
     * @param boardList      目标板材列表
     * @param wasteThreshold 废料阈值
     */
    private void frontToBackCutting(CutBoard cutBoard, BoardList boardList, BigDecimal wasteThreshold, CuttingSignal signal) {
        cutBoard.setWidth(cutBoard.getWidth().add(signal.getLongEdgeTrim()));
        for (NormalBoard board : boardList.getBoards()) {
            NormalBoard extraBoard = BoardUtil.getExtraBoard(cutBoard, ForwardEdge.SHORT, board.getLength(), wasteThreshold);
            this.cuttingBoard(cutBoard, ForwardEdge.SHORT, extraBoard);
            for (int i = 0; i < board.getCutTimes(); i++) {
                if (i == 0) {
                    extraBoard = BoardUtil.getExtraBoard(cutBoard, ForwardEdge.LONG, cutBoard.getWidth().subtract(signal.getLongEdgeTrim()), wasteThreshold);
                    this.cuttingBoard(cutBoard, ForwardEdge.LONG, extraBoard);
                }
                BigDecimal remainingWidth = DecimalUtil.sub(cutBoard.getWidth(), board.getWidth());
                if (remainingWidth.compareTo(BoardUtil.CLAMP_DEPTH) <= 0) {
                    extraBoard = BoardUtil.getExtraBoard(cutBoard, ForwardEdge.LONG, board.getWidth(), wasteThreshold);
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

    /**
     * 板材裁剪函数，负责下料板材的实际旋转和裁剪操作。
     *
     * @param cutBoard    下料板
     * @param forwardEdge 朝向
     * @param targetBoard 目标板材
     */
    private void cuttingBoard(CutBoard cutBoard, ForwardEdge forwardEdge, NormalBoard targetBoard) {
        if (targetBoard.getCutTimes() > 0) {
            if (cutBoard.getForwardEdge() != forwardEdge) {
                cutBoard.setForwardEdge(forwardEdge);
                this.actionMapper.insertMachineAction(MachineAction.of(ActionCategory.ROTATE, BigDecimal.ZERO, cutBoard));
            }

            BigDecimal dis = targetBoard.getWidth();
            if (cutBoard.getForwardEdge() == ForwardEdge.LONG) {
                cutBoard.setWidth(DecimalUtil.sub(cutBoard.getWidth(), dis));
            } else {
                cutBoard.setLength(DecimalUtil.sub(cutBoard.getLength(), dis));
            }

            if (cutBoard.getWidth().compareTo(BigDecimal.ZERO) > 0) {
                this.actionMapper.insertMachineAction(MachineAction.of(ActionCategory.CUT, dis, targetBoard));
            } else {
                this.actionMapper.insertMachineAction(MachineAction.of(ActionCategory.SEND, BigDecimal.ZERO, targetBoard));
            }
        }
    }
}
