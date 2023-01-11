package com.gamecity.scrabble.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gamecity.scrabble.dao.RedisRepository;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Tile;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.model.VirtualTile;
import com.gamecity.scrabble.service.VirtualBagService;
import com.gamecity.scrabble.service.VirtualRackService;
import com.gamecity.scrabble.service.exception.GameException;
import com.gamecity.scrabble.service.exception.error.GameError;

import lombok.extern.slf4j.Slf4j;

@Service(value = "virtualRackService")
@Slf4j
class VirtualRackServiceImpl implements VirtualRackService {

    private static final Integer RACK_SIZE = 7;

    private VirtualBagService virtualBagService;
    private RedisRepository redisRepository;

    @Autowired
    void setVirtualBagService(VirtualBagService virtualBagService) {
        this.virtualBagService = virtualBagService;
    }

    @Autowired
    void setRedisRepository(RedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    @Override
    public void createRack(Long gameId, Long bagId, Integer playerNumber) {
        final VirtualTile[] tiles = new VirtualTile[RACK_SIZE];

        IntStream.range(1, RACK_SIZE + 1).forEach(tileNumber -> {
            final VirtualTile tile = createTile(gameId, bagId, playerNumber, tileNumber, 1, null);
            tiles[tile.getNumber() - 1] = tile;
        });

        final VirtualRack rack = new VirtualRack(false, Arrays.asList(tiles));
        redisRepository.refreshRack(gameId, playerNumber, rack);
        log.debug("Rack has been created for player {} on game {}.", playerNumber, gameId);
    }

    /**
     * Creates a {@link VirtualTile tile} on a {@link VirtualRack rack} in a {@link Game game}
     * 
     * @param gameId       <code>id</code> of the game
     * @param bagId        <code>id</code> of the bag used by the game
     * @param playerNumber <code>number</code> of the player
     * @param tileNumber   tile number of the rack
     * @param roundNumber  the round number that the tile is added
     * @param vowel        whether a vowel letter should be selected
     * @return the created tile
     */
    private VirtualTile createTile(Long gameId, Long bagId, Integer playerNumber, int tileNumber, int roundNumber,
            Boolean vowel) {
        final List<Tile> tiles = virtualBagService.getTiles(gameId, bagId);

        VirtualTile tile = null;
        int index = 0;
        while (true) {
            if (tiles.isEmpty()) {
                break;
            }

            // a vowel letter is requested but there are no vowels left in the bag
            if (Boolean.TRUE.equals(vowel) && tiles.stream().filter(Tile::isVowel).count() == 0) {
                return null;
            }

            index = new Random().nextInt(tiles.size()) + 1;
            final Tile tileRule = tiles.get(index - 1);

            // filter by vowel
            if (vowel != null && !vowel.equals(tileRule.isVowel())) {
                continue;
            }

            if (tileRule.getCount() > 0) {
                tile = VirtualTile.builder()
                        .letter(tileRule.getLetter())
                        .number(tileNumber)
                        .playerNumber(playerNumber)
                        .roundNumber(roundNumber)
                        .sealed(false)
                        .value(tileRule.getValue())
                        .vowel(tileRule.isVowel())
                        .build();

                // remove the tile from the list when the count is zero
                if (tileRule.getCount() == 0) {
                    tiles.remove(tileRule);
                } else {
                    tileRule.setCount(tileRule.getCount() - 1);
                }

                break;
            }
            tiles.remove(tileRule);
        }

        virtualBagService.updateTiles(gameId, tiles);
        return tile;
    }

    @Override
    public VirtualRack refreshRack(Long gameId, Long bagId, Integer playerNumber, Integer roundNumber,
            VirtualRack virtualRack) {
        final List<VirtualTile> updatedTiles = virtualRack.getTiles().stream().map(tile -> {
            if (Boolean.TRUE.equals(tile.isSealed())) {
                return createTile(gameId, bagId, playerNumber, tile.getNumber(), roundNumber, null);
            }
            return tile;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        final VirtualRack refreshedRack = new VirtualRack(false, updatedTiles);
        redisRepository.refreshRack(gameId, playerNumber, refreshedRack);
        log.debug("Rack has been updated for player {} on game {}.", playerNumber, gameId);

        return refreshedRack;
    }

    @Override
    public void updateRack(Long gameId, Long bagId, Integer playerNumber, Integer roundNumber,
            VirtualRack virtualRack) {
        redisRepository.updateRack(gameId, playerNumber, roundNumber, virtualRack);
        log.debug("Rack has been updated for player {} on game {}.", playerNumber, gameId);
    }

    @Override
    public VirtualRack getRack(Long gameId, Integer playerNumber, Integer roundNumber) {
        return redisRepository.getRack(gameId, playerNumber, roundNumber);
    }

    @Override
    public VirtualRack exchangeTile(Long gameId, Long bagId, Integer playerNumber, Integer roundNumber,
            Integer tileNumber) {
        final VirtualRack virtualRack = getRack(gameId, playerNumber, roundNumber);
        if (virtualRack.isExchanged()) {
            throw new GameException(GameError.EXCHANGED);
        }

        final VirtualTile exchangedVirtualTile = virtualRack.getTiles().get(tileNumber - 1);
        final VirtualTile newVirtualTile = createTile(gameId, bagId, playerNumber, tileNumber, roundNumber, true);
        final List<VirtualTile> updatedTiles = new ArrayList<>(virtualRack.getTiles());
        updatedTiles.set(tileNumber - 1, newVirtualTile);

        final List<Tile> tiles = virtualBagService.getTiles(gameId, bagId);
        final Tile updatedTile = tiles.stream()
                .filter(tile -> exchangedVirtualTile.getLetter().equals(tile.getLetter()))
                .findFirst()
                .orElse(null);

        // no vowels have been found in the bag, do not exchange the letter and return the rack back
        if (updatedTile == null) {
            return virtualRack;
        }
        updatedTile.setCount(updatedTile.getCount() + 1);
        virtualBagService.updateTiles(gameId, tiles);

        final VirtualRack updatedVirtualRack = new VirtualRack(true, updatedTiles);
        updateRack(gameId, bagId, playerNumber, roundNumber, updatedVirtualRack);

        return updatedVirtualRack;
    }

}
