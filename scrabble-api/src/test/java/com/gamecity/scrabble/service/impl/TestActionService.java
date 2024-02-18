package com.gamecity.scrabble.service.impl;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.dao.ActionDao;
import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.ActionType;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.service.ActionService;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

class TestActionService extends AbstractServiceTest {

    @Mock
    private ActionDao actionDao;

    @InjectMocks
    private ActionService actionService = new ActionServiceImpl();

    @BeforeEach
    void beforeEach() {
        ((ActionServiceImpl) actionService).setBaseDao(actionDao);
    }

    @Test
    void test_add_action() {
        final Game mockGame = mock(Game.class);

        when(actionDao.save(any())).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        final Action action = actionService.add(mockGame, DEFAULT_USER_ID, 0, ActionType.CREATE);

        assertThat(action, notNullValue());
        assertThat(action.getGameId(), equalTo(mockGame.getId()));
        assertThat(action.getUserId(), equalTo(DEFAULT_USER_ID));
        assertThat(action.getVersion(), equalTo(mockGame.getVersion()));
        assertThat(action.getGameStatus(), equalTo(mockGame.getStatus()));
        assertThat(action.getType(), equalTo(ActionType.CREATE));
        assertThat(action.getCurrentPlayerNumber(), equalTo(mockGame.getCurrentPlayerNumber()));
        assertThat(action.getRoundNumber(), equalTo(mockGame.getRoundNumber()));
        assertThat(action.getRemainingTileCount(), equalTo(mockGame.getRemainingTileCount()));
        assertThat(action.getScore(), equalTo(0));
        assertThat(action.getCreatedDate(), equalTo(mockGame.getLastUpdatedDate()));
        assertThat(action.getLastUpdatedDate(), equalTo(mockGame.getLastUpdatedDate()));

        verify(actionDao, times(1)).save(action);
    }

    @Test
    void test_has_new_action() {
        when(actionDao.getLastAction(DEFAULT_GAME_ID)).thenReturn(Action.builder().version(1).build());

        assertThat(actionService.hasNewAction(DEFAULT_GAME_ID, 1), equalTo(true));
    }

    @Test
    void test_has_no_new_action() {
        when(actionDao.getLastAction(DEFAULT_GAME_ID)).thenReturn(Action.builder().version(1).build());

        assertThat(actionService.hasNewAction(DEFAULT_GAME_ID, 2), equalTo(false));
    }

    @Test
    void test_maximum_skip_count_reached() {
        final Integer playerCount = 2; // 2 players

        final Integer skippedCount = Constants.Game.MAXIMUM_SKIPPED_ROUNDS_IN_A_ROW * playerCount;

        final List<Action> lastActions = Arrays.asList(Action.builder().type(ActionType.TIMEOUT).build(),
                Action.builder().type(ActionType.SKIP).build(), Action.builder().type(ActionType.TIMEOUT).build(),
                Action.builder().type(ActionType.SKIP).build());

        when(actionDao.getLastActionsByCount(DEFAULT_GAME_ID, skippedCount)).thenReturn(lastActions);

        assertThat(actionService.isMaximumSkipCountReached(DEFAULT_GAME_ID, playerCount), equalTo(true));
    }

    @Test
    void test_maximum_skip_count_not_reached() {
        final Integer playerCount = 2; // 2 players

        final Integer skippedCount = Constants.Game.MAXIMUM_SKIPPED_ROUNDS_IN_A_ROW * playerCount;

        final List<Action> lastActions = Arrays.asList(Action.builder().type(ActionType.TIMEOUT).build(),
                Action.builder().type(ActionType.SKIP).build(), Action.builder().type(ActionType.TIMEOUT).build(),
                Action.builder().type(ActionType.PLAY).build());

        when(actionDao.getLastActionsByCount(DEFAULT_GAME_ID, skippedCount)).thenReturn(lastActions);

        assertThat(actionService.isMaximumSkipCountReached(DEFAULT_GAME_ID, playerCount), equalTo(false));
    }

    @Test
    void test_skip_count_is_less_than_action_count() {
        final Integer playerCount = 2; // 2 players

        final Integer skippedCount = Constants.Game.MAXIMUM_SKIPPED_ROUNDS_IN_A_ROW * playerCount;

        final List<Action> lastActions = Arrays.asList(Action.builder().type(ActionType.SKIP).build(),
                Action.builder().type(ActionType.TIMEOUT).build(), Action.builder().type(ActionType.PLAY).build());

        when(actionDao.getLastActionsByCount(DEFAULT_GAME_ID, skippedCount)).thenReturn(lastActions);

        assertThat(actionService.isMaximumSkipCountReached(DEFAULT_GAME_ID, playerCount), equalTo(false));
    }

}
