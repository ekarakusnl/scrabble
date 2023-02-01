package com.gamecity.scrabble.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.gamecity.scrabble.dao.ActionDao;
import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.ActionType;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.service.ActionService;

@Service(value = "actionService")
class ActionServiceImpl extends AbstractServiceImpl<Action, ActionDao> implements ActionService {

    private static final Integer MAXIMUM_SKIP_COUNT_IN_A_ROW = 4;

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

    @Override
    public boolean isMaximumSkipCountReached(Long gameId) {
        // if 4 turns has been skipped in a row, then the game should end
        final List<Action> lastActions = baseDao.getLastActionsByCount(gameId, MAXIMUM_SKIP_COUNT_IN_A_ROW);
        return lastActions.size() == MAXIMUM_SKIP_COUNT_IN_A_ROW
                && lastActions.stream().allMatch(action -> ActionType.SKIP == action.getType());
    }

}
