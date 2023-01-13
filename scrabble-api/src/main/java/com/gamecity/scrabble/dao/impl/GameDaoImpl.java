package com.gamecity.scrabble.dao.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
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

    @Override
    public List<Game> getByUser(Long userId) {
        return listByNamedQuery(Constants.NamedQuery.getByUser, Arrays.asList(Pair.of("userId", userId)));
    }

}
