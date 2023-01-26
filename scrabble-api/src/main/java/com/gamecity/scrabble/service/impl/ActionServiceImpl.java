package com.gamecity.scrabble.service.impl;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.gamecity.scrabble.dao.ActionDao;
import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.ActionType;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.service.ActionService;

@Service(value = "actionService")
class ActionServiceImpl extends AbstractServiceImpl<Action, ActionDao> implements ActionService {

    @Override
    @Transactional
    public Action add(Game game, Long userId, ActionType actionType) {
        final Action action = new Action();
        action.setGameId(game.getId());
        action.setUserId(userId);
        action.setVersion(game.getVersion());
        action.setGameStatus(game.getStatus());
        action.setType(actionType);
        action.setCurrentPlayerNumber(game.getCurrentPlayerNumber());
        action.setRoundNumber(game.getRoundNumber());
        action.setCreatedDate(game.getLastUpdatedDate());
        action.setLastUpdatedDate(game.getLastUpdatedDate());
        return baseDao.save(action);
    }

    @Override
    public boolean hasNewAction(Long gameId, Integer version) {
        final Action lastAction = baseDao.getLastAction(gameId);

        if (version > lastAction.getVersion()) {
            return false;
        }

        return true;
    }

    @Override
    public Action getAction(Long gameId, Integer version) {
        return baseDao.getActionByVersion(gameId, version);
    }

}
