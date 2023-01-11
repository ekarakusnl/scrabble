package com.gamecity.scrabble.dao.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.dao.GameDao;
import com.gamecity.scrabble.entity.Game;

@Repository(value = "gameDao")
class GameDaoImpl extends AbstractDaoImpl<Game> implements GameDao {

    @Override
    public List<Game> getLastGames(int count) {
        return listByNamedQuery(Constants.NamedQuery.getLastGames, Collections.emptyList(), count);
    }

}
