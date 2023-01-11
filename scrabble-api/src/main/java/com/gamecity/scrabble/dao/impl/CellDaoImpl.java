package com.gamecity.scrabble.dao.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.dao.CellDao;
import com.gamecity.scrabble.entity.Cell;

@Repository(value = "cellDao")
class CellDaoImpl extends AbstractDaoImpl<Cell> implements CellDao {

    @Override
    public List<Cell> getCells(Long boardId) {
        return listByNamedQuery(Constants.NamedQuery.getCells, Arrays.asList(Pair.of("boardId", boardId)));
    }

}
