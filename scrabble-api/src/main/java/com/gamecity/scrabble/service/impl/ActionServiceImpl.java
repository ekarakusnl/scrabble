package com.gamecity.scrabble.service.impl;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.gamecity.scrabble.dao.ActionDao;
import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.ActionType;
import com.gamecity.scrabble.entity.GameStatus;
import com.gamecity.scrabble.service.ActionService;

@Service(value = "actionService")
class ActionServiceImpl extends AbstractServiceImpl<Action, ActionDao> implements ActionService {

    @Override
    @Transactional
    public Action add(Long gameId, Long userId, Integer counter, Integer currentPlayerNumber, Integer roundNumber,
            ActionType actionType, GameStatus status) {
        final Action action = new Action();
        action.setGameId(gameId);
        action.setUserId(userId);
        action.setCounter(counter);
        action.setGameStatus(status);
        action.setType(actionType);
        action.setCurrentPlayerNumber(currentPlayerNumber);
        action.setRoundNumber(roundNumber);
        return baseDao.save(action);
    }

    @Override
    public boolean hasNewAction(Long gameId, Integer counter) {
        final Action lastAction = baseDao.getLastAction(gameId);

        if (counter > lastAction.getCounter()) {
            return false;
        }

        return true;
    }

}
