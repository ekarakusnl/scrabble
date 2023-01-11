package com.gamecity.scrabble.dao.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.dao.PlayerDao;
import com.gamecity.scrabble.entity.Player;

@Repository(value = "playerDao")
class PlayerDaoImpl extends AbstractDaoImpl<Player> implements PlayerDao {

    @Override
    public Player getByUserId(Long gameId, Long userId) {
        return getByNamedQuery(Constants.NamedQuery.getPlayersByUserId,
                Arrays.asList(Pair.of("gameId", gameId), Pair.of("userId", userId)));
    }

    @Override
    public Player getByPlayerNumber(Long gameId, Integer playerNumber) {
        return getByNamedQuery(Constants.NamedQuery.getPlayerByPlayerNumber,
                Arrays.asList(Pair.of("gameId", gameId), Pair.of("playerNumber", playerNumber)));
    }

    @Override
    public List<Player> getCurrentPlayers(Long gameId) {
        return listByNamedQuery(Constants.NamedQuery.getCurrentPlayers, Arrays.asList(Pair.of("gameId", gameId)));
    }

}
