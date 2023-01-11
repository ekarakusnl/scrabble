package com.gamecity.scrabble.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gamecity.scrabble.dao.BoardDao;
import com.gamecity.scrabble.dao.CellDao;
import com.gamecity.scrabble.entity.Board;
import com.gamecity.scrabble.entity.Cell;
import com.gamecity.scrabble.service.BoardService;

@Service(value = "boardService")
class BoardServiceImpl extends AbstractServiceImpl<Board, BoardDao> implements BoardService {

    private CellDao cellDao;

    @Autowired
    public void setCellDao(CellDao cellDao) {
        this.cellDao = cellDao;
    }

    @Override
    public List<Cell> getCells(Long boardId) {
        return cellDao.getCells(boardId);
    }

}
