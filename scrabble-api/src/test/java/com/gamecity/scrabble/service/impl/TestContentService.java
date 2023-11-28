package com.gamecity.scrabble.service.impl;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.service.ContentService;
import com.gamecity.scrabble.service.VirtualBoardService;
import com.gamecity.scrabble.service.VirtualRackService;

import static org.mockito.Mockito.*;

class TestContentService extends AbstractServiceTest {

    @Mock
    private VirtualBoardService virtualBoardService;

    @Mock
    private VirtualRackService virtualRackService;

    @InjectMocks
    private ContentService contentService = new ContentServiceImpl(virtualBoardService, virtualRackService);

    @Test
    void test_create_content() {
        final Language language = Language.en;

        final Game mockGame = mock(Game.class);

        when(mockGame.getExpectedPlayerCount()).thenReturn(4); // 4 players
        when(mockGame.getLanguage()).thenReturn(language);

        contentService.create(mockGame);

        verify(virtualBoardService, times(1)).createBoard(mockGame.getId());
        verify(virtualRackService, times(1)).createRack(mockGame.getId(), language, 1);
        verify(virtualRackService, times(1)).createRack(mockGame.getId(), language, 2);
        verify(virtualRackService, times(1)).createRack(mockGame.getId(), language, 3);
        verify(virtualRackService, times(1)).createRack(mockGame.getId(), language, 4);
    }

    @Test
    void test_update_content() {
        final Language language = Language.en;

        final Game mockGame = mock(Game.class);

        when(mockGame.getLanguage()).thenReturn(language);

        final VirtualRack mockRack = mock(VirtualRack.class);
        final VirtualBoard mockBoard = mock(VirtualBoard.class);

        contentService.update(mockGame, mockRack, mockBoard, DEFAULT_PLAYER_NUMBER, DEFAULT_ROUND_NUMBER);

        verify(virtualRackService, times(1)).updateRack(mockGame.getId(), DEFAULT_PLAYER_NUMBER, DEFAULT_ROUND_NUMBER,
                mockRack);
        verify(virtualRackService, times(1)).fillRack(mockGame.getId(), language, DEFAULT_PLAYER_NUMBER,
                DEFAULT_ROUND_NUMBER + 1, mockRack);
        verify(virtualBoardService, times(1)).updateBoard(mockGame.getId(), mockBoard);
    }

}
