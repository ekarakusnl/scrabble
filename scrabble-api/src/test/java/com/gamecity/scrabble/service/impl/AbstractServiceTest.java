package com.gamecity.scrabble.service.impl;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.GameStatus;
import com.gamecity.scrabble.entity.User;

@ExtendWith(MockitoExtension.class)
abstract class AbstractServiceTest {

    protected static final Long DEFAULT_ACTION_ID = 1L;
    protected static final Long DEFAULT_BOARD_ID = 1L;
    protected static final String DEFAULT_BAG_LANGUAGE = "en";
    protected static final Long DEFAULT_GAME_ID = 1L;
    protected static final Long DEFAULT_USER_ID = 1L;

    protected User createSampleUser() {
        final User user = new User();
        user.setEmail("mukawwaa_by_scrabble@gamecity.com");
        user.setUsername("mukawwaa");
        user.setPassword("Scrabble.102");
        return user;
    }

    protected Game createSampleGame(Long userId, Integer playerCount) {
        final Game game = new Game();
        game.setName("My game");
        game.setOwnerId(userId);
        game.setLanguage(Language.valueOf(DEFAULT_BAG_LANGUAGE));
        game.setExpectedPlayerCount(playerCount);
        game.setDuration(2);
        game.setStatus(GameStatus.WAITING);
        game.setVersion(1);
        game.setActivePlayerCount(1);
        game.setRemainingTileCount(98);
        return game;
    }

    protected Action createSampleAction() {
        final Action action = new Action();
        action.setId(DEFAULT_ACTION_ID);
        return action;
    }

}
