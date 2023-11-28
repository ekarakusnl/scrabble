package com.gamecity.scrabble.service.impl;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.gamecity.scrabble.dao.PlayerDao;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.service.PlayerService;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

class TestPlayerService extends AbstractServiceTest {

    @Mock
    private PlayerDao playerDao;

    @InjectMocks
    private PlayerService playerService = new PlayerServiceImpl();

    @Test
    void test_add_player() {
        when(playerDao.save(any())).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        final Player player = playerService.add(DEFAULT_GAME_ID, DEFAULT_USER_ID, DEFAULT_PLAYER_NUMBER);

        assertThat(player, notNullValue());
        assertThat(player.getGameId(), equalTo(DEFAULT_GAME_ID));
        assertThat(player.getUserId(), equalTo(DEFAULT_USER_ID));
        assertThat(player.getPlayerNumber(), equalTo(DEFAULT_PLAYER_NUMBER));
        assertThat(player.getScore(), equalTo(0));
        assertThat(player.getJoinedDate(), notNullValue());
        assertThat(player.getLeftDate(), nullValue());

        verify(playerDao, times(1)).save(player);
    }

    @Test
    void test_remove_player() {
        final Player mockPlayer = mock(Player.class);

        when(playerDao.save(any())).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        playerService.remove(mockPlayer);

        verify(mockPlayer, times(1)).setLeftDate(any());
        verify(playerDao, times(1)).save(mockPlayer);
    }

    @Test
    void test_update_score() {
        final Integer addedScore = 15;

        final Player mockPlayer = mock(Player.class);

        when(mockPlayer.getScore()).thenReturn(10);

        when(playerDao.getByPlayerNumber(eq(DEFAULT_GAME_ID), eq(DEFAULT_PLAYER_NUMBER))).thenReturn(mockPlayer);
        when(playerDao.save(any())).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        playerService.updateScore(DEFAULT_GAME_ID, DEFAULT_PLAYER_NUMBER, addedScore);

        verify(mockPlayer, times(1)).setScore(25);
        verify(playerDao, times(1)).save(mockPlayer);
    }

}
