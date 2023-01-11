package com.gamecity.scrabble.dao.impl;

import org.springframework.stereotype.Repository;

import com.gamecity.scrabble.dao.BagDao;
import com.gamecity.scrabble.entity.Bag;

@Repository(value = "bagDao")
class BagDaoImpl extends AbstractDaoImpl<Bag> implements BagDao {

}
