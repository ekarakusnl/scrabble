package com.gamecity.scrabble.dao.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.dao.GameDao;
import com.gamecity.scrabble.entity.Game;

@Repository(value = "gameDao")
class GameDaoImpl extends AbstractDaoImpl<Game> implements GameDao {

    @Override
    public List<Game> search(Long userId, boolean includeUser) {
        final List<Pair<String, Object>> parameters = Arrays.asList(Pair.of("userId", userId));
        if (includeUser) {
            return listByNamedQuery(Constants.NamedQuery.searchByUser, parameters);
        }

        return listByNamedQuery(Constants.NamedQuery.searchGames, parameters);
    }

}
