package com.gamecity.scrabble.service.impl;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.Bag;
import com.gamecity.scrabble.entity.Board;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.GameStatus;
import com.gamecity.scrabble.entity.User;

@ExtendWith(MockitoExtension.class)
abstract class AbstractServiceTest {

    protected static final Long DEFAULT_ACTION_ID = 1L;
    protected static final Long DEFAULT_BOARD_ID = 1L;
    protected static final Long DEFAULT_BAG_ID = 1L;
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
        game.setBagId(DEFAULT_BAG_ID);
        game.setBoardId(DEFAULT_BOARD_ID);
        game.setExpectedPlayerCount(playerCount);
        game.setDuration(2);
        game.setStatus(GameStatus.WAITING);
        game.setVersion(1);
        game.setActivePlayerCount(1);
        return game;
    }

    protected Board createSampleBoard() {
        final Board board = new Board();
        board.setId(DEFAULT_BOARD_ID);
        board.setColumnSize(15);
        board.setRowSize(15);
        return board;
    }

    protected Bag createSampleBag() {
        final Bag bag = new Bag();
        bag.setId(DEFAULT_BAG_ID);
        bag.setLanguage(Language.en);
        bag.setTileCount(96);
        return bag;
    }

    protected Action createSampleAction() {
        final Action action = new Action();
        action.setId(DEFAULT_ACTION_ID);
        return action;
    }

}
