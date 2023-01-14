package com.gamecity.scrabble.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.gamecity.scrabble.dao.RedisRepository;
import com.gamecity.scrabble.entity.Tile;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.model.VirtualTile;
import com.gamecity.scrabble.service.VirtualRackService;
import com.gamecity.scrabble.service.VirtualBagService;

class TestVirtualRackService extends AbstractServiceTest {

    @InjectMocks
    private VirtualRackService virtualRackService = new VirtualRackServiceImpl();

    @Mock
    private VirtualBagService virtualBagService;

    @Mock
    private RedisRepository redisRepository;

    private List<Tile> tiles;

    @BeforeEach
    void beforeEach() {
        tiles = new ArrayList<>();

        tiles.add(Tile.builder().count(25).letter("A").vowel(true).build());
        tiles.add(Tile.builder().count(25).letter("B").vowel(false).build());
        tiles.add(Tile.builder().count(25).letter("C").vowel(false).build());
        tiles.add(Tile.builder().count(25).letter("D").vowel(false).build());
        tiles.add(Tile.builder().count(25).letter("F").vowel(false).build());
        tiles.add(Tile.builder().count(25).letter("G").vowel(false).build());
        tiles.add(Tile.builder().count(25).letter("H").vowel(false).build());

        when(virtualBagService.getTiles(eq(DEFAULT_GAME_ID), eq(DEFAULT_BAG_ID))).thenReturn(tiles);
    }

    @Test
    void test_exchange_tile() {
        final Integer playerNumber = 1;
        final Integer roundNumber = 1;

        IntStream.range(1, 26).forEach(times -> {
            final List<Pair<String, Integer>> letterValuePairs = new ArrayList<>();

            letterValuePairs.add(Pair.of("B", 1));
            letterValuePairs.add(Pair.of("C", 1));
            letterValuePairs.add(Pair.of("D", 1));
            letterValuePairs.add(Pair.of("F", 1));
            letterValuePairs.add(Pair.of("G", 1));
            letterValuePairs.add(Pair.of("H", 1));
            letterValuePairs.add(Pair.of("K", 1));

            final VirtualRack virtualRack = createVirtualRack(playerNumber, roundNumber, letterValuePairs);

            when(redisRepository.getRack(eq(DEFAULT_GAME_ID), eq(playerNumber), eq(roundNumber)))
                    .thenReturn(virtualRack);

            final VirtualRack updatedVirtualRack =
                    virtualRackService.exchangeTile(DEFAULT_GAME_ID, DEFAULT_BAG_ID, playerNumber, roundNumber, 1);

            assertNotNull(updatedVirtualRack);
            assertNotEquals("B", updatedVirtualRack.getTiles().get(0).getLetter());
            assertTrue(updatedVirtualRack.getTiles().get(0).isVowel());
        });
    }

    private VirtualRack createVirtualRack(Integer playerNumber, Integer roundNumber,
            List<Pair<String, Integer>> letterValuePairs) {

        final List<VirtualTile> virtualTiles = new ArrayList<>();
        IntStream.range(1, 8).forEach(tileNumber -> {
            final Pair<String, Integer> letterValuePair = letterValuePairs.get(tileNumber - 1);
            virtualTiles.add(VirtualTile.builder()
                    .letter(letterValuePair.getKey())
                    .value(letterValuePair.getValue())
                    .number(tileNumber)
                    .playerNumber(playerNumber)
                    .roundNumber(roundNumber)
                    .build());
        });

        return new VirtualRack(false, virtualTiles);
    }

}
