package com.gamecity.scrabble.dao.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.dao.ActionDao;
import com.gamecity.scrabble.entity.Action;

@Repository(value = "actionDao")
class ActionDaoImpl extends AbstractDaoImpl<Action> implements ActionDao {

    @Override
    public List<Action> getActions(Long gameId) {
        return listByNamedQuery(Constants.NamedQuery.getActions, Arrays.asList(Pair.of("gameId", gameId)));
    }

    @Override
    public Action getLastAction(Long gameId) {
        return getByNamedQuery(Constants.NamedQuery.getLastAction, Arrays.asList(Pair.of("gameId", gameId)));
    }

}
