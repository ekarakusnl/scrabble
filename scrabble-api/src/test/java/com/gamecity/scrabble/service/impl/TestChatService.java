package com.gamecity.scrabble.service.impl;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.gamecity.scrabble.dao.ChatDao;
import com.gamecity.scrabble.dao.RedisRepository;
import com.gamecity.scrabble.entity.Chat;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.service.ChatService;
import com.gamecity.scrabble.service.GameService;
import com.gamecity.scrabble.service.PlayerService;
import com.gamecity.scrabble.service.exception.GameException;
import com.gamecity.scrabble.service.exception.error.GameError;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestChatService extends AbstractServiceTest {

    @Mock
    private GameService gameService;

    @Mock
    private PlayerService playerService;

    @Mock
    private RedisRepository redisRepository;

    @Mock
    private ChatDao chatDao;

    @InjectMocks
    private ChatService chatService = new ChatServiceImpl(gameService, playerService, redisRepository);

    @Test
    void test_save_chat() {
        final Game mockGame = mock(Game.class);

        final Chat mockChat = mock(Chat.class);

        when(mockChat.getMessage()).thenReturn("A test message");

        when(gameService.get(mockChat.getGameId())).thenReturn(mockGame);
        when(playerService.getByUserId(mockGame.getId(), mockChat.getUserId())).thenReturn(mock(Player.class));
        when(chatDao.save(any())).thenReturn(mockChat);

        assertThat(chatService.save(mockChat), notNullValue());

        verify(chatDao, times(1)).save(mockChat);
        verify(redisRepository, times(1)).publishChat(mockChat.getGameId(), mockChat);
    }

    @Test
    void test_save_chat_with_not_playing_player() {
        final Game mockGame = mock(Game.class);

        final Chat mockChat = mock(Chat.class);

        when(gameService.get(mockChat.getGameId())).thenReturn(mockGame);
        when(playerService.getByUserId(mockGame.getId(), mockChat.getUserId())).thenReturn(null);

        try {
            chatService.save(mock(Chat.class));

            fail("Chat is saved with a non playing player");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.NOT_IN_THE_GAME.getCode()));
        }
    }

}
