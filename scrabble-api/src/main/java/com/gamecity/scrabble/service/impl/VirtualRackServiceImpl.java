package com.gamecity.scrabble.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.dao.RedisRepository;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.Tile;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.model.VirtualTile;
import com.gamecity.scrabble.service.VirtualBagService;
import com.gamecity.scrabble.service.VirtualRackService;
import com.gamecity.scrabble.service.exception.GameException;
import com.gamecity.scrabble.service.exception.error.GameError;

import lombok.extern.slf4j.Slf4j;

import static com.gamecity.scrabble.Constants.Game.RACK_SIZE;

@Service(value = "virtualRackService")
@Slf4j
class VirtualRackServiceImpl implements VirtualRackService {

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
    public void createRack(Long gameId, Language language, Integer playerNumber) {
        final List<Tile> tiles = virtualBagService.getTiles(gameId, language);

        final VirtualTile[] virtualTiles = new VirtualTile[RACK_SIZE];
        IntStream.range(1, RACK_SIZE + 1).forEach(tileNumber -> {
            final VirtualTile tile = createTile(gameId, language, playerNumber, tileNumber, 1, tiles);
            virtualTiles[tile.getNumber() - 1] = tile;
        });

        // update the cached tiles
        virtualBagService.updateTiles(gameId, tiles);

        final VirtualRack rack = new VirtualRack(Arrays.asList(virtualTiles));
        redisRepository.fillRack(gameId, playerNumber, rack);
        log.info("Rack has been created for player {} on game {}", playerNumber, gameId);
    }

    /**
     * Creates a {@link VirtualTile tile} on a {@link VirtualRack rack} in a {@link Game game}
     * 
     * @param gameId       <code>id</code> of the game
     * @param language     <code>language</code> of the bag used by the game
     * @param playerNumber <code>number</code> of the player
     * @param tileNumber   tile number of the rack
     * @param roundNumber  the round number that the tile is added
     * @param vowel        whether a vowel letter should be selected
     * @return the created tile
     */
    private VirtualTile createTile(Long gameId, Language language, Integer playerNumber, int tileNumber, int roundNumber,
                                   List<Tile> tiles) {
        final List<Tile> availableTiles = tiles.stream().filter(tile -> tile.getCount() > 0).collect(Collectors.toList());

        VirtualTile virtualTile = null;
        int index = 0;
        while (true) {
            // there are no tiles left in the bag
            if (availableTiles.isEmpty()) {
                return null;
            }

            index = new Random().nextInt(availableTiles.size()) + 1;
            final Tile tile = availableTiles.get(index - 1);

            if (tile.getCount() > 0) {
                virtualTile = VirtualTile.builder()
                        .letter(tile.getLetter())
                        .number(tileNumber)
                        .playerNumber(playerNumber)
                        .roundNumber(roundNumber)
                        .sealed(false)
                        .value(tile.getValue())
                        .vowel(tile.isVowel())
                        .build();

                tile.setCount(tile.getCount() - 1);
                log.debug("Tile {} has been created for player {}", tile.getLetter(), playerNumber);
                break;
            }
        }

        return virtualTile;
    }

    @Override
    public void fillRack(Long gameId, Language language, Integer playerNumber, Integer roundNumber, VirtualRack virtualRack) {
        final List<Tile> tiles = virtualBagService.getTiles(gameId, language);
        final List<VirtualTile> updatedVirtualTiles = virtualRack.getTiles().stream().map(tile -> {
            if (Boolean.TRUE.equals(tile.isSealed()) || Boolean.TRUE.equals(tile.isExchanged())) {
                return createTile(gameId, language, playerNumber, tile.getNumber(), roundNumber, tiles);
            }
            return tile;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        // update the cached tiles
        virtualBagService.updateTiles(gameId, tiles);

        final VirtualRack filledRack = new VirtualRack(updatedVirtualTiles);
        redisRepository.fillRack(gameId, playerNumber, filledRack);
        log.info("Rack has been refilled for player {} on game {}", playerNumber, gameId);
    }

    @Override
    public void updateRack(Long gameId, Integer playerNumber, Integer roundNumber, VirtualRack virtualRack) {
        redisRepository.updateRack(gameId, playerNumber, roundNumber, virtualRack);
        log.info("Rack has been updated for player {} on game {}", playerNumber, gameId);
    }

    @Override
    public VirtualRack getRack(Long gameId, Integer playerNumber, Integer roundNumber) {
        return redisRepository.getRack(gameId, playerNumber, roundNumber);
    }

    @Override
    public void validateRack(Long gameId, Integer playerNumber, Integer roundNumber, VirtualRack playedRack) {
        final VirtualRack existingRack = getRack(gameId, playerNumber, roundNumber);

        final Map<Integer, String> tileMap = existingRack.getTiles()
                .stream()
                .collect(Collectors.toMap(VirtualTile::getNumber, VirtualTile::getLetter));

        final Predicate<VirtualTile> filter = tile -> tileMap.containsKey(tile.getNumber())
                && tileMap.get(tile.getNumber()).equals(tile.getLetter());

        long rackMatchCount = playedRack.getTiles().stream().filter(filter).count();
        if (rackMatchCount != playedRack.getTiles().size()) {
            throw new GameException(GameError.RACK_DOES_NOT_MATCH);
        }
    }

    @Override
    public void exchange(Long gameId, Language language, Integer playerNumber, Integer roundNumber, VirtualRack exchangedRack) {
        final List<Tile> tiles = virtualBagService.getTiles(gameId, language);

        // rack should be full to be able to exchange
        if (exchangedRack.getTiles().stream().filter(Objects::nonNull).count() < Constants.Game.RACK_SIZE) {
            throw new GameException(GameError.RACK_IS_NOT_FULL);
        }

        // number of exchanged tiles cannot be more than the number of tiles in the bag
        if (exchangedRack.getTiles().stream().filter(VirtualTile::isExchanged).count() > tiles.stream().mapToInt(Tile::getCount).sum()) {
            throw new GameException(GameError.INSUFFICIENT_TILES);
        }

        exchangedRack.getTiles().stream().forEach(exchangedRackTile -> {
            // this tile is not exchanged, return the existing rack tile
            if (exchangedRackTile.isExchanged()) {
                // increase the count of the exchanged tile after creating a new tile
                final Tile exchangedTile = tiles.stream()
                        .filter(tile -> exchangedRackTile.getLetter().equals(tile.getLetter()))
                        .findFirst()
                        .orElse(null);
                exchangedTile.setCount(exchangedTile.getCount() + 1);
            }
        });

        // update the cached tiles
        virtualBagService.updateTiles(gameId, tiles);

        final String exchangedRackLetters = exchangedRack.getTiles()
                .stream()
                .filter(VirtualTile::isExchanged)
                .map(VirtualTile::getLetter)
                .collect(Collectors.joining(","));
        log.info("Letters {} has been exchanged for player {} on game {}", exchangedRackLetters, playerNumber, gameId);
    }

}
