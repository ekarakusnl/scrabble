package com.gamecity.scrabble.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.dao.RedisRepository;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.Tile;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.model.VirtualTile;
import com.gamecity.scrabble.service.VirtualRackService;
import com.gamecity.scrabble.service.exception.GameException;
import com.gamecity.scrabble.service.exception.error.GameError;
import com.gamecity.scrabble.service.VirtualBagService;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestVirtualRackService extends AbstractServiceTest {

    @Mock
    private VirtualBagService virtualBagService;

    @Mock
    private RedisRepository redisRepository;

    @InjectMocks
    private VirtualRackService virtualRackService = new VirtualRackServiceImpl(virtualBagService, redisRepository);

    @Test
    @SuppressWarnings("unchecked")
    void test_create_rack() {
        final List<Tile> tiles = new ArrayList<>();

        // 8 tiles available
        tiles.add(Tile.builder().count(1).letter("A").build());
        tiles.add(Tile.builder().count(1).letter("B").build());
        tiles.add(Tile.builder().count(1).letter("C").build());
        tiles.add(Tile.builder().count(1).letter("D").build());
        tiles.add(Tile.builder().count(1).letter("E").build());
        tiles.add(Tile.builder().count(1).letter("F").build());
        tiles.add(Tile.builder().count(1).letter("G").build());
        tiles.add(Tile.builder().count(1).letter("H").build());

        when(virtualBagService.getTiles(eq(DEFAULT_GAME_ID), eq(Language.valueOf(DEFAULT_BAG_LANGUAGE))))
                .thenReturn(tiles);

        virtualRackService.createRack(DEFAULT_GAME_ID, Language.en, DEFAULT_PLAYER_NUMBER);

        final ArgumentCaptor<List<Tile>> updatedTiles = ArgumentCaptor.forClass(List.class);

        verify(virtualBagService).updateTiles(eq(DEFAULT_GAME_ID), updatedTiles.capture());

        // used 7 tiles, now 1 tile available
        assertThat(updatedTiles.getValue().stream().mapToInt(Tile::getCount).sum(), equalTo(1));

        final ArgumentCaptor<VirtualRack> rack = ArgumentCaptor.forClass(VirtualRack.class);

        verify(redisRepository).fillRack(eq(DEFAULT_GAME_ID), eq(DEFAULT_PLAYER_NUMBER), rack.capture());

        assertThat(rack.getValue().getTiles().size(), equalTo(Constants.Game.RACK_SIZE));
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_refill_sealed_tiles() {
        final List<Tile> tiles = new ArrayList<>();

        // 4 tiles available
        tiles.add(Tile.builder().count(1).letter("A").build());
        tiles.add(Tile.builder().count(1).letter("B").build());
        tiles.add(Tile.builder().count(1).letter("C").build());
        tiles.add(Tile.builder().count(1).letter("D").build());

        when(virtualBagService.getTiles(eq(DEFAULT_GAME_ID), eq(Language.valueOf(DEFAULT_BAG_LANGUAGE))))
                .thenReturn(tiles);

        final VirtualRack virtualRack = createVirtualRack(DEFAULT_PLAYER_NUMBER, DEFAULT_ROUND_NUMBER, "ABCDEFG");

        virtualRack.getTiles().get(0).setSealed(true); // A
        virtualRack.getTiles().get(2).setSealed(true); // C
        virtualRack.getTiles().get(4).setSealed(true); // E

        virtualRackService.fillRack(DEFAULT_GAME_ID, Language.en, DEFAULT_PLAYER_NUMBER, DEFAULT_DURATION, virtualRack);

        final ArgumentCaptor<List<Tile>> updatedTiles = ArgumentCaptor.forClass(List.class);

        verify(virtualBagService).updateTiles(eq(DEFAULT_GAME_ID), updatedTiles.capture());

        // used 3 tiles, now 1 tiles available
        assertThat(updatedTiles.getValue().stream().mapToInt(Tile::getCount).sum(), equalTo(1));

        final ArgumentCaptor<VirtualRack> rack = ArgumentCaptor.forClass(VirtualRack.class);

        verify(redisRepository).fillRack(eq(DEFAULT_GAME_ID), eq(DEFAULT_PLAYER_NUMBER), rack.capture());

        assertThat(rack.getValue().getTiles().size(), equalTo(Constants.Game.RACK_SIZE));
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_refill_exchanged_tiles() {
        final List<Tile> tiles = new ArrayList<>();

        // 4 available
        tiles.add(Tile.builder().count(1).letter("A").build());
        tiles.add(Tile.builder().count(1).letter("B").build());
        tiles.add(Tile.builder().count(1).letter("C").build());
        tiles.add(Tile.builder().count(1).letter("D").build());

        when(virtualBagService.getTiles(eq(DEFAULT_GAME_ID), eq(Language.valueOf(DEFAULT_BAG_LANGUAGE))))
                .thenReturn(tiles);

        final VirtualRack virtualRack = createVirtualRack(DEFAULT_PLAYER_NUMBER, DEFAULT_ROUND_NUMBER, "ABCDEFG");

        virtualRack.getTiles().get(0).setExchanged(true); // A
        virtualRack.getTiles().get(2).setExchanged(true); // C
        virtualRack.getTiles().get(4).setExchanged(true); // E

        virtualRackService.fillRack(DEFAULT_GAME_ID, Language.en, DEFAULT_PLAYER_NUMBER, DEFAULT_DURATION, virtualRack);

        final ArgumentCaptor<List<Tile>> updatedTiles = ArgumentCaptor.forClass(List.class);

        verify(virtualBagService).updateTiles(eq(DEFAULT_GAME_ID), updatedTiles.capture());

        // exchanged 3 tiles, now 1 tiles available
        assertThat(updatedTiles.getValue().stream().mapToInt(Tile::getCount).sum(), equalTo(1));

        final ArgumentCaptor<VirtualRack> rack = ArgumentCaptor.forClass(VirtualRack.class);

        verify(redisRepository).fillRack(eq(DEFAULT_GAME_ID), eq(DEFAULT_PLAYER_NUMBER), rack.capture());

        assertThat(rack.getValue().getTiles().size(), equalTo(Constants.Game.RACK_SIZE));
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_refill_when_no_tiles_sealed_or_exchanged() {
        final List<Tile> tiles = new ArrayList<>();

        // 4 tiles available
        tiles.add(Tile.builder().count(1).letter("A").build());
        tiles.add(Tile.builder().count(1).letter("B").build());
        tiles.add(Tile.builder().count(1).letter("C").build());
        tiles.add(Tile.builder().count(1).letter("D").build());

        when(virtualBagService.getTiles(eq(DEFAULT_GAME_ID), eq(Language.valueOf(DEFAULT_BAG_LANGUAGE))))
                .thenReturn(tiles);

        final VirtualRack virtualRack = createVirtualRack(DEFAULT_PLAYER_NUMBER, DEFAULT_ROUND_NUMBER, "ABCDEFG");

        virtualRackService.fillRack(DEFAULT_GAME_ID, Language.en, DEFAULT_PLAYER_NUMBER, DEFAULT_DURATION, virtualRack);

        final ArgumentCaptor<List<Tile>> updatedTiles = ArgumentCaptor.forClass(List.class);

        verify(virtualBagService).updateTiles(eq(DEFAULT_GAME_ID), updatedTiles.capture());

        // no tiles used or exchanged, now 4 tiles available
        assertThat(updatedTiles.getValue().stream().mapToInt(Tile::getCount).sum(), equalTo(4));

        final ArgumentCaptor<VirtualRack> rack = ArgumentCaptor.forClass(VirtualRack.class);

        verify(redisRepository).fillRack(eq(DEFAULT_GAME_ID), eq(DEFAULT_PLAYER_NUMBER), rack.capture());

        assertThat(rack.getValue().getTiles().size(), equalTo(Constants.Game.RACK_SIZE));
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_refill_when_no_tiles_available() {
        final List<Tile> tiles = new ArrayList<>();

        // 4 tiles available with 0 count
        tiles.add(Tile.builder().count(0).letter("A").build());
        tiles.add(Tile.builder().count(0).letter("B").build());
        tiles.add(Tile.builder().count(0).letter("C").build());
        tiles.add(Tile.builder().count(0).letter("D").build());

        when(virtualBagService.getTiles(eq(DEFAULT_GAME_ID), eq(Language.valueOf(DEFAULT_BAG_LANGUAGE))))
                .thenReturn(tiles);

        final VirtualRack virtualRack = createVirtualRack(DEFAULT_PLAYER_NUMBER, DEFAULT_ROUND_NUMBER, "ABCDEFG");

        virtualRack.getTiles().get(0).setExchanged(true); // A
        virtualRack.getTiles().get(2).setExchanged(true); // C
        virtualRack.getTiles().get(4).setExchanged(true); // E

        virtualRackService.fillRack(DEFAULT_GAME_ID, Language.en, DEFAULT_PLAYER_NUMBER, DEFAULT_DURATION, virtualRack);

        final ArgumentCaptor<List<Tile>> updatedTiles = ArgumentCaptor.forClass(List.class);

        verify(virtualBagService).updateTiles(eq(DEFAULT_GAME_ID), updatedTiles.capture());

        // used 3 tiles, now 0 tiles available
        assertThat(updatedTiles.getValue().stream().mapToInt(Tile::getCount).sum(), equalTo(0));

        final ArgumentCaptor<VirtualRack> rack = ArgumentCaptor.forClass(VirtualRack.class);

        verify(redisRepository).fillRack(eq(DEFAULT_GAME_ID), eq(DEFAULT_PLAYER_NUMBER), rack.capture());

        // no tiles refilled, now 4 tiles available
        assertThat(rack.getValue().getTiles().size(), equalTo(4));
    }

    @Test
    void test_validate_played_rack() {
        final VirtualRack virtualRack = createVirtualRack(DEFAULT_PLAYER_NUMBER, DEFAULT_ROUND_NUMBER, "ABCDEFG");

        when(redisRepository.getRack(DEFAULT_GAME_ID, DEFAULT_PLAYER_NUMBER, DEFAULT_ROUND_NUMBER))
                .thenReturn(virtualRack);

        virtualRackService.validateRack(DEFAULT_GAME_ID, DEFAULT_PLAYER_NUMBER, DEFAULT_DURATION, virtualRack);
    }

    @Test
    void test_validate_played_rack_fails_when_tile_count_does_not_match() {
        final VirtualRack virtualRack = createVirtualRack(DEFAULT_PLAYER_NUMBER, DEFAULT_ROUND_NUMBER, "ABCDEFG");

        when(redisRepository.getRack(DEFAULT_GAME_ID, DEFAULT_PLAYER_NUMBER, DEFAULT_ROUND_NUMBER))
                .thenReturn(virtualRack);

        final VirtualRack playedRack = new VirtualRack(
                Arrays.asList(VirtualTile.builder().number(1).letter("Z").build()));

        try {
            virtualRackService.validateRack(DEFAULT_GAME_ID, DEFAULT_PLAYER_NUMBER, DEFAULT_DURATION, playedRack);

            fail("Not matching rack has been played");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.RACK_DOES_NOT_MATCH.getCode()));
        }
    }

    @Test
    void test_validate_played_rack_fails_when_tile_letter_does_not_match() {
        final VirtualRack virtualRack = createVirtualRack(DEFAULT_PLAYER_NUMBER, DEFAULT_ROUND_NUMBER, "ABCDEFG");

        when(redisRepository.getRack(DEFAULT_GAME_ID, DEFAULT_PLAYER_NUMBER, DEFAULT_ROUND_NUMBER))
                .thenReturn(virtualRack);

        final VirtualRack playedRack = createVirtualRack(DEFAULT_PLAYER_NUMBER, DEFAULT_ROUND_NUMBER, "ABCDEFG");
        playedRack.getTiles().get(6).setLetter("Z");

        try {
            virtualRackService.validateRack(DEFAULT_GAME_ID, DEFAULT_PLAYER_NUMBER, DEFAULT_DURATION, playedRack);

            fail("Not matching rack has been played");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.RACK_DOES_NOT_MATCH.getCode()));
        }
    }

    @Test
    void test_update_rack() {
        final VirtualRack virtualRack = createVirtualRack(DEFAULT_PLAYER_NUMBER, DEFAULT_ROUND_NUMBER, "ABCDEFG");

        virtualRackService.updateRack(DEFAULT_GAME_ID, DEFAULT_DURATION, DEFAULT_VERSION, virtualRack);

        verify(redisRepository, times(1)).updateRack(DEFAULT_GAME_ID, DEFAULT_DURATION, DEFAULT_VERSION, virtualRack);
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_exchange_tiles() {
        final List<Tile> tiles = new ArrayList<>();

        // 5 tiles available
        tiles.add(Tile.builder().count(0).letter("A").build());
        tiles.add(Tile.builder().count(0).letter("B").build());
        tiles.add(Tile.builder().count(0).letter("C").build());
        tiles.add(Tile.builder().count(0).letter("D").build());
        tiles.add(Tile.builder().count(0).letter("E").build());
        tiles.add(Tile.builder().count(0).letter("F").build());
        tiles.add(Tile.builder().count(0).letter("G").build());
        tiles.add(Tile.builder().count(1).letter("H").build());
        tiles.add(Tile.builder().count(1).letter("I").build());
        tiles.add(Tile.builder().count(1).letter("J").build());
        tiles.add(Tile.builder().count(1).letter("K").build());
        tiles.add(Tile.builder().count(1).letter("L").build());

        when(virtualBagService.getTiles(eq(DEFAULT_GAME_ID), eq(Language.valueOf(DEFAULT_BAG_LANGUAGE))))
                .thenReturn(tiles);

        final VirtualRack virtualRack = createVirtualRack(DEFAULT_PLAYER_NUMBER, DEFAULT_ROUND_NUMBER, "ABCDEFG");

        virtualRack.getTiles().get(0).setExchanged(true); // A
        virtualRack.getTiles().get(2).setExchanged(true); // C
        virtualRack.getTiles().get(4).setExchanged(true); // E
        virtualRack.getTiles().get(6).setExchanged(true); // G

        virtualRackService.exchange(DEFAULT_GAME_ID, Language.valueOf(DEFAULT_BAG_LANGUAGE), DEFAULT_PLAYER_NUMBER,
                DEFAULT_ROUND_NUMBER, virtualRack);

        final ArgumentCaptor<List<Tile>> updatedTiles = ArgumentCaptor.forClass(List.class);

        verify(virtualBagService).updateTiles(eq(DEFAULT_GAME_ID), updatedTiles.capture());

        // exchanged 4 tiles, now 9 tiles available
        assertThat(updatedTiles.getValue().stream().mapToInt(Tile::getCount).sum(), equalTo(9));
    }

    @Test
    void test_cannot_exchange_tiles_when_rack_is_not_full() {
        final VirtualRack virtualRack = createVirtualRack(DEFAULT_PLAYER_NUMBER, DEFAULT_ROUND_NUMBER, "ABCDEF");

        when(virtualBagService.getTiles(eq(DEFAULT_GAME_ID), eq(Language.valueOf(DEFAULT_BAG_LANGUAGE))))
                .thenReturn(Collections.emptyList());

        virtualRack.getTiles().get(0).setExchanged(true); // A
        virtualRack.getTiles().get(2).setExchanged(true); // C
        virtualRack.getTiles().get(4).setExchanged(true); // E

        try {
            virtualRackService.exchange(DEFAULT_GAME_ID, Language.valueOf(DEFAULT_BAG_LANGUAGE), DEFAULT_PLAYER_NUMBER,
                    DEFAULT_ROUND_NUMBER, virtualRack);

            fail("Tiles are exchanged when the rack is not full");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.RACK_IS_NOT_FULL.getCode()));
        }
    }

    @Test
    void test_cannot_exchange_tiles_when_there_are_insufficient_tiles_in_bag() {
        final List<Tile> tiles = new ArrayList<>();

        tiles.add(Tile.builder().count(2).letter("A").build());
        tiles.add(Tile.builder().count(0).letter("B").build());
        tiles.add(Tile.builder().count(0).letter("C").build());
        tiles.add(Tile.builder().count(0).letter("D").build());
        tiles.add(Tile.builder().count(0).letter("E").build());
        tiles.add(Tile.builder().count(0).letter("F").build());
        tiles.add(Tile.builder().count(0).letter("G").build());
        tiles.add(Tile.builder().count(0).letter("H").build());

        when(virtualBagService.getTiles(eq(DEFAULT_GAME_ID), eq(Language.valueOf(DEFAULT_BAG_LANGUAGE))))
                .thenReturn(tiles);

        final VirtualRack virtualRack = createVirtualRack(DEFAULT_PLAYER_NUMBER, DEFAULT_ROUND_NUMBER, "ABCDEFG");

        virtualRack.getTiles().get(0).setExchanged(true); // A
        virtualRack.getTiles().get(2).setExchanged(true); // C
        virtualRack.getTiles().get(4).setExchanged(true); // E

        try {
            virtualRackService.exchange(DEFAULT_GAME_ID, Language.valueOf(DEFAULT_BAG_LANGUAGE), DEFAULT_PLAYER_NUMBER,
                    DEFAULT_ROUND_NUMBER, virtualRack);

            fail("Tiles are exchanged when there are insufficient tiles");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.INSUFFICIENT_TILES.getCode()));
        }
    }

    private VirtualRack createVirtualRack(Integer playerNumber, Integer roundNumber, CharSequence letters) {
        final List<VirtualTile> virtualTiles = IntStream.range(1, letters.length() + 1).mapToObj(tileNumber -> {
            final Character letter = letters.charAt(tileNumber - 1);
            return VirtualTile.builder()
                    .letter(letter.toString())
                    .number(tileNumber)
                    .playerNumber(playerNumber)
                    .roundNumber(roundNumber)
                    .build();
        }).collect(Collectors.toList());

        return new VirtualRack(virtualTiles);
    }
}
