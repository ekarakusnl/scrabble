package com.gamecity.scrabble.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.gamecity.scrabble.dao.RedisRepository;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.Tile;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.model.VirtualTile;
import com.gamecity.scrabble.service.VirtualRackService;
import com.gamecity.scrabble.service.exception.GameException;
import com.gamecity.scrabble.service.exception.error.GameError;
import com.gamecity.scrabble.service.VirtualBagService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestVirtualRackService extends AbstractServiceTest {

    @InjectMocks
    private VirtualRackService virtualRackService = new VirtualRackServiceImpl();

    @Mock
    private VirtualBagService virtualBagService;

    @Mock
    private RedisRepository redisRepository;

    @Test
    void test_exchange_tiles() {
        final List<Tile> tiles = new ArrayList<>();

        tiles.add(Tile.builder().count(0).letter("A").vowel(true).build());
        tiles.add(Tile.builder().count(0).letter("B").vowel(false).build());
        tiles.add(Tile.builder().count(0).letter("C").vowel(false).build());
        tiles.add(Tile.builder().count(0).letter("D").vowel(false).build());
        tiles.add(Tile.builder().count(0).letter("E").vowel(true).build());
        tiles.add(Tile.builder().count(0).letter("F").vowel(false).build());
        tiles.add(Tile.builder().count(0).letter("G").vowel(false).build());
        tiles.add(Tile.builder().count(1).letter("H").vowel(false).build());
        tiles.add(Tile.builder().count(1).letter("I").vowel(true).build());
        tiles.add(Tile.builder().count(1).letter("J").vowel(false).build());
        tiles.add(Tile.builder().count(1).letter("K").vowel(false).build());
        tiles.add(Tile.builder().count(1).letter("L").vowel(false).build());

        when(virtualBagService.getTiles(eq(DEFAULT_GAME_ID), eq(Language.valueOf(DEFAULT_BAG_LANGUAGE)))).thenReturn(tiles);

        final Integer playerNumber = 1;
        final Integer roundNumber = 1;

        final List<Pair<String, Integer>> letterValuePairs = new ArrayList<>();

        letterValuePairs.add(Pair.of("A", 1));
        letterValuePairs.add(Pair.of("B", 1));
        letterValuePairs.add(Pair.of("C", 1));
        letterValuePairs.add(Pair.of("D", 1));
        letterValuePairs.add(Pair.of("E", 1));
        letterValuePairs.add(Pair.of("F", 1));
        letterValuePairs.add(Pair.of("G", 1));

        final VirtualRack virtualRack = createVirtualRack(playerNumber, roundNumber, letterValuePairs);

        when(redisRepository.getRack(eq(DEFAULT_GAME_ID), eq(playerNumber), eq(roundNumber))).thenReturn(virtualRack);

        virtualRack.getTiles().get(0).setExchanged(true);
        virtualRack.getTiles().get(2).setExchanged(true);
        virtualRack.getTiles().get(4).setExchanged(true);
        virtualRack.getTiles().get(6).setExchanged(true);

        virtualRackService.exchange(DEFAULT_GAME_ID, Language.valueOf(DEFAULT_BAG_LANGUAGE), playerNumber, roundNumber, virtualRack);
    }

    @Test
    void test_cannot_exchange_tiles_when_rack_is_not_full() {
        final Integer playerNumber = 1;
        final Integer roundNumber = 1;

        final List<Pair<String, Integer>> letterValuePairs = new ArrayList<>();

        letterValuePairs.add(Pair.of("A", 1));
        letterValuePairs.add(Pair.of("B", 1));
        letterValuePairs.add(Pair.of("C", 1));
        letterValuePairs.add(Pair.of("D", 1));
        letterValuePairs.add(Pair.of("E", 1));
        letterValuePairs.add(Pair.of("F", 1));
        letterValuePairs.add(null);

        final VirtualRack virtualRack = createVirtualRack(playerNumber, roundNumber, letterValuePairs);

        when(redisRepository.getRack(eq(DEFAULT_GAME_ID), eq(playerNumber), eq(roundNumber))).thenReturn(virtualRack);
        when(virtualBagService.getTiles(eq(DEFAULT_GAME_ID), eq(Language.valueOf(DEFAULT_BAG_LANGUAGE))))
                .thenReturn(Collections.emptyList());

        virtualRack.getTiles().get(0).setExchanged(true);
        virtualRack.getTiles().get(2).setExchanged(true);
        virtualRack.getTiles().get(4).setExchanged(true);

        try {
            virtualRackService.exchange(DEFAULT_GAME_ID, Language.valueOf(DEFAULT_BAG_LANGUAGE), playerNumber, roundNumber, virtualRack);
            fail("Tiles are exchanged when the rack is not full");
        } catch (GameException e) {
            assertEquals(GameError.RACK_IS_NOT_FULL.getCode(), e.getCode());
        }
    }

    @Test
    void test_cannot_exchange_tiles_when_there_are_insufficient_tiles_in_bag() {
        final List<Tile> tiles = new ArrayList<>();

        tiles.add(Tile.builder().count(2).letter("A").vowel(true).build());
        tiles.add(Tile.builder().count(0).letter("B").vowel(true).build());
        tiles.add(Tile.builder().count(0).letter("C").vowel(true).build());
        tiles.add(Tile.builder().count(0).letter("D").vowel(true).build());
        tiles.add(Tile.builder().count(0).letter("E").vowel(true).build());
        tiles.add(Tile.builder().count(0).letter("F").vowel(true).build());
        tiles.add(Tile.builder().count(0).letter("G").vowel(true).build());
        tiles.add(Tile.builder().count(0).letter("H").vowel(true).build());

        final Integer playerNumber = 1;
        final Integer roundNumber = 1;

        final List<Pair<String, Integer>> letterValuePairs = new ArrayList<>();

        letterValuePairs.add(Pair.of("A", 1));
        letterValuePairs.add(Pair.of("B", 1));
        letterValuePairs.add(Pair.of("C", 1));
        letterValuePairs.add(Pair.of("D", 1));
        letterValuePairs.add(Pair.of("E", 1));
        letterValuePairs.add(Pair.of("F", 1));
        letterValuePairs.add(Pair.of("G", 1));

        final VirtualRack virtualRack = createVirtualRack(playerNumber, roundNumber, letterValuePairs);

        when(redisRepository.getRack(eq(DEFAULT_GAME_ID), eq(playerNumber), eq(roundNumber))).thenReturn(virtualRack);
        when(virtualBagService.getTiles(eq(DEFAULT_GAME_ID), eq(Language.valueOf(DEFAULT_BAG_LANGUAGE)))).thenReturn(tiles);

        virtualRack.getTiles().get(0).setExchanged(true);
        virtualRack.getTiles().get(2).setExchanged(true);
        virtualRack.getTiles().get(4).setExchanged(true);

        try {
            virtualRackService.exchange(DEFAULT_GAME_ID, Language.valueOf(DEFAULT_BAG_LANGUAGE), playerNumber, roundNumber, virtualRack);
            fail("Tiles are exchanged when there are insufficient tiles");
        } catch (GameException e) {
            assertEquals(GameError.INSUFFICIENT_TILES.getCode(), e.getCode());
        }
    }

    private VirtualRack createVirtualRack(Integer playerNumber, Integer roundNumber, List<Pair<String, Integer>> letterValuePairs) {

        final List<VirtualTile> virtualTiles = new ArrayList<>();
        IntStream.range(1, 8).forEach(tileNumber -> {
            final Pair<String, Integer> letterValuePair = letterValuePairs.get(tileNumber - 1);
            if (letterValuePair != null) {
                virtualTiles.add(VirtualTile.builder()
                        .letter(letterValuePair.getKey())
                        .value(letterValuePair.getValue())
                        .number(tileNumber)
                        .playerNumber(playerNumber)
                        .roundNumber(roundNumber)
                        .build());
            }
        });

        return new VirtualRack(virtualTiles);
    }

}
