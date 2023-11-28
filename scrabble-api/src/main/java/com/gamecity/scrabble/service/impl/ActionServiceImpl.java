package com.gamecity.scrabble.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.dao.ActionDao;
import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.ActionType;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.service.ActionService;

@Service(value = "actionService")
class ActionServiceImpl extends AbstractServiceImpl<Action, ActionDao> implements ActionService {

    @Override
    @Transactional
    public Action add(Game game, Long userId, Integer score, ActionType actionType) {
        final Action action = Action.builder()
                .gameId(game.getId())
                .userId(userId)
                .version(game.getVersion())
                .gameStatus(game.getStatus())
                .type(actionType)
                .currentPlayerNumber(game.getCurrentPlayerNumber())
                .roundNumber(game.getRoundNumber())
                .remainingTileCount(game.getRemainingTileCount())
                .score(score)
                .createdDate(game.getLastUpdatedDate())
                .lastUpdatedDate(game.getLastUpdatedDate())
                .build();
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
    public boolean isMaximumSkipCountReached(Long gameId, Integer playerCount) {
        final Integer maximumSkippedTurns = Constants.Game.MAXIMUM_SKIPPED_ROUNDS_IN_A_ROW * playerCount;
        // if 2 rounds have been skipped in a row, then the game should end
        final List<Action> lastActions = baseDao.getLastActionsByCount(gameId, maximumSkippedTurns);
        return lastActions.size() == maximumSkippedTurns && lastActions.stream()
                .allMatch(action -> ActionType.TIMEOUT == action.getType() || ActionType.SKIP == action.getType());
    }

    @Override
    public List<Action> getActions(Long gameId) {
        return baseDao.getActions(gameId);
    }

}
