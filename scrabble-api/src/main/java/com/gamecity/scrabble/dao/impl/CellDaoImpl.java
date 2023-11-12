package com.gamecity.scrabble.dao.impl;

import org.springframework.stereotype.Repository;

import com.gamecity.scrabble.dao.CellDao;
import com.gamecity.scrabble.entity.Cell;

@Repository(value = "cellDao")
class CellDaoImpl extends AbstractDaoImpl<Cell> implements CellDao {

}
