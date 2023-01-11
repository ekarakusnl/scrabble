package com.gamecity.scrabble.dao.impl;

import org.springframework.stereotype.Repository;

import com.gamecity.scrabble.dao.BoardDao;
import com.gamecity.scrabble.entity.Board;

@Repository(value = "boardDao")
class BoardDaoImpl extends AbstractDaoImpl<Board> implements BoardDao {

}
