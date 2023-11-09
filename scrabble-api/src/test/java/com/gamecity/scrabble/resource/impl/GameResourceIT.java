package com.gamecity.scrabble.resource.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.entity.GameStatus;
import com.gamecity.scrabble.entity.Tile;
import com.gamecity.scrabble.model.Mapper;
import com.gamecity.scrabble.model.rest.ExceptionDto;
import com.gamecity.scrabble.model.rest.GameDto;
import com.gamecity.scrabble.model.rest.PlayerDto;
import com.gamecity.scrabble.model.rest.VirtualBoardDto;
import com.gamecity.scrabble.model.rest.VirtualCellDto;
import com.gamecity.scrabble.model.rest.VirtualRackDto;
import com.gamecity.scrabble.model.rest.VirtualTileDto;
import com.gamecity.scrabble.service.exception.error.GameError;
import com.gamecity.scrabble.util.JsonUtils;
import com.google.common.io.Resources;

@SuppressWarnings("unchecked")
class GameResourceIT extends AbstractIntegrationTest {

    @Test
    void test_create_game() throws IOException {
        final GameDto game = createNewGame(2);

        assertEquals("My game", game.getName());
        assertNotNull(game.getId());

        final List<PlayerDto> players = getPlayers(game.getId(), game.getVersion());
        assertEquals(1, players.size());
        assertEquals("user", players.get(0).getUsername());
    }

    @Test
    void test_get_game() throws IOException {
        final GameDto game = createNewGame(2);
        final GameDto createdGame = getGame(game.getId());

        assertEquals("My game", createdGame.getName());
        assertNotNull(createdGame.getId());
    }

    @Test
    void test_update_game() throws IOException {
        final GameDto game = createNewGame(2);

        final Response response = target("/games/" + game.getId()).request().get();

        final String etag = response.getHeaderString(HttpHeaders.ETAG);
        final GameDto createdGame = response.readEntity(GameDto.class);
        createdGame.setName("My updated game");

        final Response updateGameResponse = target("/games/" + game.getId()).request()
                .header(HttpHeaders.IF_MATCH, etag)
                .put(Entity.entity(createdGame, MediaType.APPLICATION_JSON));

        if (Status.OK.getStatusCode() != updateGameResponse.getStatus()) {
            assertEquals(Status.OK.getStatusCode(), updateGameResponse.getStatus(),
                    updateGameResponse.readEntity(String.class));
        }

        assertNotNull(updateGameResponse.getHeaderString(HttpHeaders.ETAG));

        final GameDto updateGame = updateGameResponse.readEntity(GameDto.class);

        assertEquals("My updated game", updateGame.getName());

        updateGameResponse.close();
        response.close();
    }

    @Test
    void test_update_game_fails_without_etag() throws IOException {
        final GameDto game = createNewGame(2);

        final Response response = target("/games/" + game.getId()).request().get();

        final GameDto updatedGame = response.readEntity(GameDto.class);
        updatedGame.setName("My updated game");

        final Response updateGameResponse = target("/games/" + game.getId()).request()
                .put(Entity.entity(updatedGame, MediaType.APPLICATION_JSON));

        assertEquals("If-Match header is missing", updateGameResponse.readEntity(String.class));

        updateGameResponse.close();
        response.close();
    }

    @Test
    void test_join_game() throws IOException {
        final GameDto game = createNewGame(3);
        final GameDto joinedGame = joinGame(game.getId(), 2L);

        assertEquals(2, joinedGame.getVersion());

        final List<PlayerDto> players = getPlayers(game.getId(), joinedGame.getVersion());
        assertEquals(2, players.size());
        assertEquals("admin", players.get(1).getUsername());
    }

    @Test
    void test_leave_game() throws IOException {
        final GameDto game = createNewGame(3);
        joinGame(game.getId(), 2L);

        final Response leaveGameResponse = target("/games/" + game.getId() + "/users/2").request().delete();

        if (Status.OK.getStatusCode() != leaveGameResponse.getStatus()) {
            assertEquals(Status.OK.getStatusCode(), leaveGameResponse.getStatus(),
                    leaveGameResponse.readEntity(String.class));
        }

        final GameDto leftGame = leaveGameResponse.readEntity(GameDto.class);

        assertEquals(3, leftGame.getVersion());

        final List<PlayerDto> players = getPlayers(game.getId(), leftGame.getVersion());
        assertEquals(1, players.size());
        assertEquals("user", players.get(0).getUsername());

        leaveGameResponse.close();
    }

    @Test
    void test_start_game() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2);
        final GameDto joinedGame = joinGame(game.getId(), 2L);

        assertEquals(2, joinedGame.getVersion());
        assertEquals(GameStatus.READY_TO_START.name(), joinedGame.getStatus());

        waitUntilGameStarts();

        final GameDto startedGame = getGame(game.getId());

        assertEquals(3, startedGame.getVersion());
        assertEquals(1, startedGame.getCurrentPlayerNumber());
        assertEquals(GameStatus.IN_PROGRESS.name(), startedGame.getStatus());

        final VirtualBoardDto virtualBoard = getVirtualBoard(game.getId(), startedGame.getVersion(),
                game.getExpectedPlayerCount());

        assertNotNull(virtualBoard);
        assertEquals(225, virtualBoard.getCells().size());

        virtualBoard.getCells().stream().forEach(cell -> {
            assertNull(cell.getRoundNumber());
        });

        final VirtualRackDto virtualRack = getVirtualRack(game.getId(), game.getOwnerId(),
                startedGame.getRoundNumber());

        assertNotNull(virtualRack);

        virtualRack.getTiles().stream().forEach(tile -> assertEquals(1, tile.getRoundNumber()));
        virtualRack.getTiles().stream().forEach(tile -> assertFalse(tile.isSealed()));
    }

    @Test
    void test_play_two_rounds() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2);
        joinGame(game.getId(), 2L);

        waitUntilGameStarts();

        final GameDto startedGame = getGame(game.getId());

        // round 1 player 1

        List<Pair<String, Integer>> letterValuePairs = new ArrayList<>();

        letterValuePairs.add(Pair.of("W", 4));
        letterValuePairs.add(Pair.of("E", 1));
        letterValuePairs.add(Pair.of("A", 1));
        letterValuePairs.add(Pair.of("K", 5));
        letterValuePairs.add(Pair.of("O", 1));
        letterValuePairs.add(Pair.of("L", 1));
        letterValuePairs.add(Pair.of("E", 1));

        VirtualRackDto virtualRack = updateRack(game.getId(), game.getOwnerId(), startedGame.getCurrentPlayerNumber(),
                startedGame.getRoundNumber(), letterValuePairs);

        virtualRack.getTiles().get(0).setRowNumber(8);
        virtualRack.getTiles().get(0).setColumnNumber(7);
        virtualRack.getTiles().get(0).setSealed(true);

        virtualRack.getTiles().get(1).setRowNumber(8);
        virtualRack.getTiles().get(1).setColumnNumber(8);
        virtualRack.getTiles().get(1).setSealed(true);

        virtualRack.getTiles().get(2).setRowNumber(8);
        virtualRack.getTiles().get(2).setColumnNumber(9);
        virtualRack.getTiles().get(2).setSealed(true);

        virtualRack.getTiles().get(3).setRowNumber(8);
        virtualRack.getTiles().get(3).setColumnNumber(10);
        virtualRack.getTiles().get(3).setSealed(true);

        GameDto playedGame = playWord(game.getId(), game.getOwnerId(), virtualRack);

        assertNotNull(playedGame);
        assertEquals(4, playedGame.getVersion());
        assertEquals(2, playedGame.getCurrentPlayerNumber());

        List<PlayerDto> players = getPlayers(game.getId(), playedGame.getVersion());
        assertEquals(11, players.get(0).getScore());
        assertEquals(0, players.get(1).getScore());

        VirtualRackDto updatedVirtualRack = getVirtualRack(game.getId(), game.getOwnerId(),
                playedGame.getRoundNumber());

        updatedVirtualRack.getTiles().stream().forEach(tile -> assertEquals(1, tile.getRoundNumber()));

        assertTrue(updatedVirtualRack.getTiles().get(0).isSealed());
        assertTrue(updatedVirtualRack.getTiles().get(1).isSealed());
        assertTrue(updatedVirtualRack.getTiles().get(2).isSealed());
        assertTrue(updatedVirtualRack.getTiles().get(3).isSealed());
        assertFalse(updatedVirtualRack.getTiles().get(4).isSealed());
        assertFalse(updatedVirtualRack.getTiles().get(5).isSealed());
        assertFalse(updatedVirtualRack.getTiles().get(6).isSealed());

        VirtualBoardDto virtualBoard = getVirtualBoard(game.getId(), playedGame.getVersion(),
                game.getExpectedPlayerCount());

        assertEquals("W", virtualBoard.getCells().get(111).getLetter());
        assertEquals("E", virtualBoard.getCells().get(112).getLetter());
        assertEquals("A", virtualBoard.getCells().get(113).getLetter());
        assertEquals("K", virtualBoard.getCells().get(114).getLetter());

        assertEquals(1, virtualBoard.getCells().get(111).getRoundNumber());
        assertEquals(1, virtualBoard.getCells().get(112).getRoundNumber());
        assertEquals(1, virtualBoard.getCells().get(113).getRoundNumber());
        assertEquals(1, virtualBoard.getCells().get(114).getRoundNumber());

        assertTrue(virtualBoard.getCells().get(111).isLastPlayed());
        assertTrue(virtualBoard.getCells().get(112).isLastPlayed());
        assertTrue(virtualBoard.getCells().get(113).isLastPlayed());
        assertTrue(virtualBoard.getCells().get(114).isLastPlayed());

        VirtualRackDto refreshedVirtualRack = getVirtualRack(game.getId(), game.getOwnerId(),
                playedGame.getRoundNumber() + 1);

        assertEquals(2, refreshedVirtualRack.getTiles().get(0).getRoundNumber());
        assertEquals(2, refreshedVirtualRack.getTiles().get(1).getRoundNumber());
        assertEquals(2, refreshedVirtualRack.getTiles().get(2).getRoundNumber());
        assertEquals(2, refreshedVirtualRack.getTiles().get(3).getRoundNumber());
        assertEquals(1, refreshedVirtualRack.getTiles().get(4).getRoundNumber());
        assertEquals(1, refreshedVirtualRack.getTiles().get(5).getRoundNumber());
        assertEquals(1, refreshedVirtualRack.getTiles().get(6).getRoundNumber());

        refreshedVirtualRack.getTiles().stream().forEach(tile -> assertFalse(tile.isSealed()));

        // round 1 player 2

        letterValuePairs = new ArrayList<>();

        letterValuePairs.add(Pair.of("R", 1));
        letterValuePairs.add(Pair.of("A", 1));
        letterValuePairs.add(Pair.of("L", 1));
        letterValuePairs.add(Pair.of("Y", 4));
        letterValuePairs.add(Pair.of("E", 1));
        letterValuePairs.add(Pair.of("R", 1));
        letterValuePairs.add(Pair.of("E", 1));

        virtualRack = updateRack(game.getId(), 2L, playedGame.getCurrentPlayerNumber(), playedGame.getRoundNumber(),
                letterValuePairs);

        virtualRack.getTiles().get(0).setRowNumber(6);
        virtualRack.getTiles().get(0).setColumnNumber(7);
        virtualRack.getTiles().get(0).setSealed(true);

        virtualRack.getTiles().get(1).setRowNumber(7);
        virtualRack.getTiles().get(1).setColumnNumber(7);
        virtualRack.getTiles().get(1).setSealed(true);

        virtualRack.getTiles().get(2).setRowNumber(9);
        virtualRack.getTiles().get(2).setColumnNumber(7);
        virtualRack.getTiles().get(2).setSealed(true);

        virtualRack.getTiles().get(3).setRowNumber(10);
        virtualRack.getTiles().get(3).setColumnNumber(7);
        virtualRack.getTiles().get(3).setSealed(true);

        playedGame = playWord(game.getId(), 2L, virtualRack);

        assertNotNull(playedGame);
        assertEquals(5, playedGame.getVersion());
        assertEquals(1, playedGame.getCurrentPlayerNumber());

        players = getPlayers(game.getId(), playedGame.getVersion());
        assertEquals(11, players.get(0).getScore());
        assertEquals(13, players.get(1).getScore());

        updatedVirtualRack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber() - 1);

        updatedVirtualRack.getTiles().stream().forEach(tile -> assertEquals(1, tile.getRoundNumber()));

        assertTrue(updatedVirtualRack.getTiles().get(0).isSealed());
        assertTrue(updatedVirtualRack.getTiles().get(1).isSealed());
        assertTrue(updatedVirtualRack.getTiles().get(2).isSealed());
        assertTrue(updatedVirtualRack.getTiles().get(3).isSealed());
        assertFalse(updatedVirtualRack.getTiles().get(4).isSealed());
        assertFalse(updatedVirtualRack.getTiles().get(5).isSealed());
        assertFalse(updatedVirtualRack.getTiles().get(6).isSealed());

        virtualBoard = getVirtualBoard(game.getId(), playedGame.getVersion(), game.getExpectedPlayerCount());

        assertEquals("R", virtualBoard.getCells().get(81).getLetter());
        assertEquals("A", virtualBoard.getCells().get(96).getLetter());
        assertEquals("W", virtualBoard.getCells().get(111).getLetter());
        assertEquals("L", virtualBoard.getCells().get(126).getLetter());
        assertEquals("Y", virtualBoard.getCells().get(141).getLetter());

        assertEquals(1, virtualBoard.getCells().get(81).getRoundNumber());
        assertEquals(1, virtualBoard.getCells().get(96).getRoundNumber());
        assertEquals(1, virtualBoard.getCells().get(111).getRoundNumber());
        assertEquals(1, virtualBoard.getCells().get(126).getRoundNumber());
        assertEquals(1, virtualBoard.getCells().get(141).getRoundNumber());

        assertTrue(virtualBoard.getCells().get(81).isLastPlayed());
        assertTrue(virtualBoard.getCells().get(96).isLastPlayed());
        assertTrue(virtualBoard.getCells().get(111).isLastPlayed());
        assertTrue(virtualBoard.getCells().get(126).isLastPlayed());
        assertTrue(virtualBoard.getCells().get(141).isLastPlayed());

        assertFalse(virtualBoard.getCells().get(112).isLastPlayed());
        assertFalse(virtualBoard.getCells().get(113).isLastPlayed());
        assertFalse(virtualBoard.getCells().get(114).isLastPlayed());

        refreshedVirtualRack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber());

        assertEquals(2, refreshedVirtualRack.getTiles().get(0).getRoundNumber());
        assertEquals(2, refreshedVirtualRack.getTiles().get(1).getRoundNumber());
        assertEquals(2, refreshedVirtualRack.getTiles().get(2).getRoundNumber());
        assertEquals(2, refreshedVirtualRack.getTiles().get(3).getRoundNumber());
        assertEquals(1, refreshedVirtualRack.getTiles().get(4).getRoundNumber());
        assertEquals(1, refreshedVirtualRack.getTiles().get(5).getRoundNumber());
        assertEquals(1, refreshedVirtualRack.getTiles().get(6).getRoundNumber());

        refreshedVirtualRack.getTiles().stream().forEach(tile -> assertFalse(tile.isSealed()));

        // round 2 player 1

        virtualRack = getVirtualRack(game.getId(), game.getOwnerId(), playedGame.getRoundNumber());

        virtualRack.getTiles().get(4).setRowNumber(6);
        virtualRack.getTiles().get(4).setColumnNumber(8);
        virtualRack.getTiles().get(4).setSealed(true);

        virtualRack.getTiles().get(5).setRowNumber(6);
        virtualRack.getTiles().get(5).setColumnNumber(9);
        virtualRack.getTiles().get(5).setSealed(true);

        virtualRack.getTiles().get(6).setRowNumber(6);
        virtualRack.getTiles().get(6).setColumnNumber(10);
        virtualRack.getTiles().get(6).setSealed(true);

        playedGame = playWord(game.getId(), game.getOwnerId(), virtualRack);

        assertNotNull(playedGame);
        assertEquals(6, playedGame.getVersion());
        assertEquals(2, playedGame.getCurrentPlayerNumber());

        players = getPlayers(game.getId(), playedGame.getVersion());
        assertEquals(17, players.get(0).getScore());
        assertEquals(13, players.get(1).getScore());

        updatedVirtualRack = getVirtualRack(game.getId(), game.getOwnerId(), playedGame.getRoundNumber());

        virtualBoard = getVirtualBoard(game.getId(), playedGame.getVersion(), game.getExpectedPlayerCount());

        assertEquals("R", virtualBoard.getCells().get(81).getLetter());
        assertEquals("O", virtualBoard.getCells().get(82).getLetter());
        assertEquals("L", virtualBoard.getCells().get(83).getLetter());
        assertEquals("E", virtualBoard.getCells().get(84).getLetter());

        assertEquals(1, virtualBoard.getCells().get(81).getRoundNumber());
        assertEquals(2, virtualBoard.getCells().get(82).getRoundNumber());
        assertEquals(2, virtualBoard.getCells().get(83).getRoundNumber());
        assertEquals(2, virtualBoard.getCells().get(84).getRoundNumber());

        assertTrue(virtualBoard.getCells().get(81).isLastPlayed());
        assertTrue(virtualBoard.getCells().get(82).isLastPlayed());
        assertTrue(virtualBoard.getCells().get(83).isLastPlayed());
        assertTrue(virtualBoard.getCells().get(84).isLastPlayed());

        assertTrue(virtualBoard.getCells().get(81).isLastPlayed());
        assertFalse(virtualBoard.getCells().get(96).isLastPlayed());
        assertFalse(virtualBoard.getCells().get(111).isLastPlayed());
        assertFalse(virtualBoard.getCells().get(126).isLastPlayed());
        assertFalse(virtualBoard.getCells().get(141).isLastPlayed());

        refreshedVirtualRack = getVirtualRack(game.getId(), game.getOwnerId(), playedGame.getRoundNumber() + 1);

        assertEquals(2, refreshedVirtualRack.getTiles().get(0).getRoundNumber());
        assertEquals(2, refreshedVirtualRack.getTiles().get(1).getRoundNumber());
        assertEquals(2, refreshedVirtualRack.getTiles().get(2).getRoundNumber());
        assertEquals(2, refreshedVirtualRack.getTiles().get(3).getRoundNumber());
        assertEquals(3, refreshedVirtualRack.getTiles().get(4).getRoundNumber());
        assertEquals(3, refreshedVirtualRack.getTiles().get(5).getRoundNumber());
        assertEquals(3, refreshedVirtualRack.getTiles().get(6).getRoundNumber());

        refreshedVirtualRack.getTiles().stream().forEach(tile -> assertFalse(tile.isSealed()));

        // round 2 player 2

        letterValuePairs.add(Pair.of("R", 1));
        letterValuePairs.add(Pair.of("A", 1));
        letterValuePairs.add(Pair.of("L", 1));
        letterValuePairs.add(Pair.of("Y", 4));
        letterValuePairs.add(Pair.of("E", 1));
        letterValuePairs.add(Pair.of("R", 1));
        letterValuePairs.add(Pair.of("E", 1));

        virtualRack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber());

        virtualRack.getTiles().get(4).setRowNumber(8);
        virtualRack.getTiles().get(4).setColumnNumber(11);
        virtualRack.getTiles().get(4).setSealed(true);

        virtualRack.getTiles().get(5).setRowNumber(8);
        virtualRack.getTiles().get(5).setColumnNumber(12);
        virtualRack.getTiles().get(5).setSealed(true);

        playedGame = playWord(game.getId(), 2L, virtualRack);

        assertNotNull(playedGame);
        assertEquals(7, playedGame.getVersion());
        assertEquals(1, playedGame.getCurrentPlayerNumber());

        players = getPlayers(game.getId(), playedGame.getVersion());
        assertEquals(17, players.get(0).getScore());
        assertEquals(27, players.get(1).getScore());

        updatedVirtualRack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber() - 1);

        virtualBoard = getVirtualBoard(game.getId(), playedGame.getVersion(), game.getExpectedPlayerCount());

        assertEquals("W", virtualBoard.getCells().get(111).getLetter());
        assertEquals("E", virtualBoard.getCells().get(112).getLetter());
        assertEquals("A", virtualBoard.getCells().get(113).getLetter());
        assertEquals("K", virtualBoard.getCells().get(114).getLetter());
        assertEquals("E", virtualBoard.getCells().get(115).getLetter());
        assertEquals("R", virtualBoard.getCells().get(116).getLetter());

        assertEquals(1, virtualBoard.getCells().get(111).getRoundNumber());
        assertEquals(1, virtualBoard.getCells().get(112).getRoundNumber());
        assertEquals(1, virtualBoard.getCells().get(113).getRoundNumber());
        assertEquals(1, virtualBoard.getCells().get(114).getRoundNumber());
        assertEquals(2, virtualBoard.getCells().get(115).getRoundNumber());
        assertEquals(2, virtualBoard.getCells().get(116).getRoundNumber());

        assertTrue(virtualBoard.getCells().get(111).isLastPlayed());
        assertTrue(virtualBoard.getCells().get(112).isLastPlayed());
        assertTrue(virtualBoard.getCells().get(113).isLastPlayed());
        assertTrue(virtualBoard.getCells().get(114).isLastPlayed());
        assertTrue(virtualBoard.getCells().get(115).isLastPlayed());
        assertTrue(virtualBoard.getCells().get(116).isLastPlayed());

        assertFalse(virtualBoard.getCells().get(81).isLastPlayed());
        assertFalse(virtualBoard.getCells().get(82).isLastPlayed());
        assertFalse(virtualBoard.getCells().get(83).isLastPlayed());
        assertFalse(virtualBoard.getCells().get(84).isLastPlayed());

        refreshedVirtualRack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber());

        assertEquals(2, refreshedVirtualRack.getTiles().get(0).getRoundNumber());
        assertEquals(2, refreshedVirtualRack.getTiles().get(1).getRoundNumber());
        assertEquals(2, refreshedVirtualRack.getTiles().get(2).getRoundNumber());
        assertEquals(2, refreshedVirtualRack.getTiles().get(3).getRoundNumber());
        assertEquals(3, refreshedVirtualRack.getTiles().get(4).getRoundNumber());
        assertEquals(3, refreshedVirtualRack.getTiles().get(5).getRoundNumber());
        assertEquals(1, refreshedVirtualRack.getTiles().get(6).getRoundNumber());

        refreshedVirtualRack.getTiles().stream().forEach(tile -> assertFalse(tile.isSealed()));
    }

    @Test
    void test_exchange_tile_works_only_once_in_a_round() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2);
        joinGame(game.getId(), 2L);

        waitUntilGameStarts();

        final GameDto startedGame = getGame(game.getId());

        final List<Pair<String, Integer>> letterValuePairs = new ArrayList<>();

        letterValuePairs.add(Pair.of("B", 1));
        letterValuePairs.add(Pair.of("C", 1));
        letterValuePairs.add(Pair.of("D", 1));
        letterValuePairs.add(Pair.of("F", 1));
        letterValuePairs.add(Pair.of("G", 1));
        letterValuePairs.add(Pair.of("H", 1));
        letterValuePairs.add(Pair.of("K", 1));

        updateRack(game.getId(), game.getOwnerId(), startedGame.getCurrentPlayerNumber(), startedGame.getRoundNumber(),
                letterValuePairs);

        List<Tile> tiles = getTiles(game.getId());
        Tile exchangedTile = tiles.stream().filter(tile -> "B".equals(tile.getLetter())).findFirst().orElse(null);

        int countBeforeExchange = exchangedTile.getCount();

        final Response exchangeTileResponse = target(
                "/games/" + game.getId() + "/racks/users/" + game.getOwnerId() + "/tiles/" + 1).request()
                .post(Entity.entity("", MediaType.APPLICATION_JSON));

        VirtualTileDto virtualTile = exchangeTileResponse.readEntity(VirtualTileDto.class);

        assertNotNull(virtualTile);
        assertNotEquals("B", virtualTile.getLetter());
        assertTrue(virtualTile.isVowel());

        final VirtualRackDto updatedRack = getVirtualRack(game.getId(), game.getOwnerId(),
                startedGame.getRoundNumber());

        assertEquals(virtualTile.getLetter(), updatedRack.getTiles().get(0).getLetter());
        assertEquals(virtualTile.isVowel(), updatedRack.getTiles().get(0).isVowel());

        tiles = getTiles(game.getId());
        exchangedTile = tiles.stream().filter(tile -> "B".equals(tile.getLetter())).findFirst().orElse(null);

        int countAfterExchange = exchangedTile.getCount();

        assertEquals(countBeforeExchange + 1, countAfterExchange);

        exchangedTile = tiles.stream().filter(tile -> "C".equals(tile.getLetter())).findFirst().orElse(null);

        final Response secondExchangeTileResponse = target(
                "/games/" + game.getId() + "/racks/users/" + game.getOwnerId() + "/tiles/" + 2).request()
                .post(Entity.entity("", MediaType.APPLICATION_JSON));

        final ExceptionDto exception = secondExchangeTileResponse.readEntity(ExceptionDto.class);

        assertNotNull(exception);
        assertEquals(GameError.EXCHANGED.getCode(), exception.getCode());

        secondExchangeTileResponse.close();
        exchangeTileResponse.close();
    }

    @Test
    void test_tile_is_not_exchanged_on_empty_bag() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2);
        joinGame(game.getId(), 2L);

        waitUntilGameStarts();

        final GameDto startedGame = getGame(game.getId());

        final List<Pair<String, Integer>> letterValuePairs = new ArrayList<>();

        letterValuePairs.add(Pair.of("B", 1));
        letterValuePairs.add(Pair.of("C", 1));
        letterValuePairs.add(Pair.of("D", 1));
        letterValuePairs.add(Pair.of("F", 1));
        letterValuePairs.add(Pair.of("G", 1));
        letterValuePairs.add(Pair.of("H", 1));
        letterValuePairs.add(Pair.of("K", 1));

        updateRack(game.getId(), game.getOwnerId(), startedGame.getCurrentPlayerNumber(), startedGame.getRoundNumber(),
                letterValuePairs);

        final List<Tile> updatedTiles = getTiles(game.getId());
        updatedTiles.stream().forEach(tile -> {
            tile.setCount(0);
        });
        updateTiles(game.getId(), updatedTiles);

        List<Tile> tiles = getTiles(game.getId());
        Tile exchangedTile = tiles.stream().filter(tile -> "B".equals(tile.getLetter())).findFirst().orElse(null);

        assertNotNull(exchangedTile);
        assertEquals(0, exchangedTile.getCount());

        final Response exchangeTileResponse = target(
                "/games/" + game.getId() + "/racks/users/" + game.getOwnerId() + "/tiles/" + 1).request()
                .post(Entity.entity("", MediaType.APPLICATION_JSON));

        final VirtualTileDto virtualTile = exchangeTileResponse.readEntity(VirtualTileDto.class);

        assertNotNull(virtualTile);
        assertEquals("B", virtualTile.getLetter());

        final VirtualRackDto updatedRack = getVirtualRack(game.getId(), game.getOwnerId(),
                startedGame.getRoundNumber());

        assertEquals(virtualTile.getLetter(), updatedRack.getTiles().get(0).getLetter());
        assertEquals(virtualTile.isVowel(), updatedRack.getTiles().get(0).isVowel());

        tiles = getTiles(game.getId());
        exchangedTile = tiles.stream().filter(tile -> "B".equals(tile.getLetter())).findFirst().orElse(null);

        assertNotNull(exchangedTile);
        assertEquals(0, exchangedTile.getCount());

        exchangeTileResponse.close();
    }

    @Test
    void test_tile_is_not_exchanged_when_no_vowels_left() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2);
        joinGame(game.getId(), 2L);

        waitUntilGameStarts();

        final GameDto startedGame = getGame(game.getId());

        final List<Pair<String, Integer>> letterValuePairs = new ArrayList<>();

        letterValuePairs.add(Pair.of("B", 1));
        letterValuePairs.add(Pair.of("C", 1));
        letterValuePairs.add(Pair.of("D", 1));
        letterValuePairs.add(Pair.of("F", 1));
        letterValuePairs.add(Pair.of("G", 1));
        letterValuePairs.add(Pair.of("H", 1));
        letterValuePairs.add(Pair.of("K", 1));

        updateRack(game.getId(), game.getOwnerId(), startedGame.getCurrentPlayerNumber(), startedGame.getRoundNumber(),
                letterValuePairs);

        final List<Tile> updatedTiles = getTiles(game.getId());
        updatedTiles.stream()
                .filter(tile -> !tile.getLetter().equals("C") && !tile.getLetter().equals("F"))
                .forEach(tile -> {
                    tile.setCount(0);
                });
        updateTiles(game.getId(), updatedTiles);

        List<Tile> tiles = getTiles(game.getId());
        Tile exchangedTile = tiles.stream().filter(tile -> "B".equals(tile.getLetter())).findFirst().orElse(null);

        assertNotNull(exchangedTile);
        assertEquals(0, exchangedTile.getCount());

        final Response exchangeTileResponse = target(
                "/games/" + game.getId() + "/racks/users/" + game.getOwnerId() + "/tiles/" + 1).request()
                .post(Entity.entity("", MediaType.APPLICATION_JSON));

        final VirtualTileDto virtualTile = exchangeTileResponse.readEntity(VirtualTileDto.class);

        assertNotNull(virtualTile);
        assertEquals("B", virtualTile.getLetter());

        final VirtualRackDto updatedRack = getVirtualRack(game.getId(), game.getOwnerId(),
                startedGame.getRoundNumber());

        assertEquals(virtualTile.getLetter(), updatedRack.getTiles().get(0).getLetter());
        assertEquals(virtualTile.isVowel(), updatedRack.getTiles().get(0).isVowel());

        tiles = getTiles(game.getId());
        exchangedTile = tiles.stream().filter(tile -> "B".equals(tile.getLetter())).findFirst().orElse(null);

        assertNotNull(exchangedTile);
        assertEquals(0, exchangedTile.getCount());

        exchangeTileResponse.close();
    }

    @Test
    void test_game_ends_after_last_round() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2);
        joinGame(game.getId(), 2L);

        waitUntilGameStarts();

        final GameDto startedGame = getGame(game.getId());

        final List<Tile> updatedTiles = getTiles(game.getId());
        updatedTiles.stream().forEach(tile -> {
            tile.setCount(0);
        });
        updateTiles(game.getId(), updatedTiles);

        // round 1 player 1

        List<Pair<String, Integer>> letterValuePairs = new ArrayList<>();

        letterValuePairs.add(Pair.of("P", 1));
        letterValuePairs.add(Pair.of("A", 1));
        letterValuePairs.add(Pair.of("R", 1));
        letterValuePairs.add(Pair.of("K", 1));
        letterValuePairs.add(Pair.of("I", 1));
        letterValuePairs.add(Pair.of("N", 1));
        letterValuePairs.add(Pair.of("G", 1));

        updateRack(game.getId(), game.getOwnerId(), startedGame.getCurrentPlayerNumber(), startedGame.getRoundNumber(),
                letterValuePairs);

        VirtualRackDto virtualRack = getVirtualRack(game.getId(), 1L, startedGame.getRoundNumber());

        virtualRack.getTiles().get(0).setRowNumber(8);
        virtualRack.getTiles().get(0).setColumnNumber(7);
        virtualRack.getTiles().get(0).setSealed(true);

        virtualRack.getTiles().get(1).setRowNumber(8);
        virtualRack.getTiles().get(1).setColumnNumber(8);
        virtualRack.getTiles().get(1).setSealed(true);

        virtualRack.getTiles().get(2).setRowNumber(8);
        virtualRack.getTiles().get(2).setColumnNumber(9);
        virtualRack.getTiles().get(2).setSealed(true);

        virtualRack.getTiles().get(3).setRowNumber(8);
        virtualRack.getTiles().get(3).setColumnNumber(10);
        virtualRack.getTiles().get(3).setSealed(true);

        GameDto playedGame = playWord(game.getId(), 1L, virtualRack);

        assertNotNull(playedGame);
        assertEquals(4, playedGame.getVersion());
        assertEquals(2, playedGame.getCurrentPlayerNumber());
        assertEquals(GameStatus.IN_PROGRESS.name(), playedGame.getStatus());

        VirtualRackDto refreshedVirtualRack = getVirtualRack(game.getId(), game.getOwnerId(),
                playedGame.getRoundNumber() + 1);

        assertEquals(3, refreshedVirtualRack.getTiles().size());

        // round 1 player 2

        letterValuePairs = new ArrayList<>();

        letterValuePairs.add(Pair.of("S", 1));
        letterValuePairs.add(Pair.of("R", 1));
        letterValuePairs.add(Pair.of("I", 1));
        letterValuePairs.add(Pair.of("N", 1));
        letterValuePairs.add(Pair.of("A", 1));
        letterValuePairs.add(Pair.of("A", 1));
        letterValuePairs.add(Pair.of("A", 1));

        updateRack(game.getId(), 2L, playedGame.getCurrentPlayerNumber(), playedGame.getRoundNumber(),
                letterValuePairs);

        virtualRack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber());

        virtualRack.getTiles().get(0).setRowNumber(7);
        virtualRack.getTiles().get(0).setColumnNumber(8);
        virtualRack.getTiles().get(0).setSealed(true);

        virtualRack.getTiles().get(1).setRowNumber(9);
        virtualRack.getTiles().get(1).setColumnNumber(8);
        virtualRack.getTiles().get(1).setSealed(true);

        virtualRack.getTiles().get(2).setRowNumber(10);
        virtualRack.getTiles().get(2).setColumnNumber(8);
        virtualRack.getTiles().get(2).setSealed(true);

        virtualRack.getTiles().get(3).setRowNumber(11);
        virtualRack.getTiles().get(3).setColumnNumber(8);
        virtualRack.getTiles().get(3).setSealed(true);

        playedGame = playWord(game.getId(), 2L, virtualRack);

        assertNotNull(playedGame);
        assertEquals(5, playedGame.getVersion());
        assertEquals(1, playedGame.getCurrentPlayerNumber());
        assertEquals(GameStatus.LAST_ROUND.name(), playedGame.getStatus());

        refreshedVirtualRack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber() + 1);

        assertNull(refreshedVirtualRack);

        // round 2 player 1

        virtualRack = getVirtualRack(game.getId(), 1L, playedGame.getRoundNumber());

        playedGame = playWord(game.getId(), 1L, virtualRack);

        assertNotNull(playedGame);
        assertEquals(6, playedGame.getVersion());
        assertEquals(2, playedGame.getCurrentPlayerNumber());
        assertEquals(GameStatus.LAST_ROUND.name(), playedGame.getStatus());

        // round 2 player 2

        virtualRack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber());

        playedGame = playWord(game.getId(), 2L, virtualRack);

        assertNotNull(playedGame);
        assertEquals(7, playedGame.getVersion());
        assertEquals(1, playedGame.getCurrentPlayerNumber());
        assertEquals(GameStatus.READY_TO_END.name(), playedGame.getStatus());

        // wait for timeout
        Thread.sleep(10000);

        final GameDto endedGame = getGame(game.getId());

        assertNotNull(endedGame);
        assertEquals(8, endedGame.getVersion());
        assertEquals(1, endedGame.getCurrentPlayerNumber());
        assertEquals(GameStatus.ENDED.name(), endedGame.getStatus());
    }

    @Test
    void test_game_ends_after_skip_turn_on_last_round() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2);
        joinGame(game.getId(), 2L);

        waitUntilGameStarts();

        final GameDto startedGame = getGame(game.getId());

        final List<Tile> updatedTiles = getTiles(game.getId());
        updatedTiles.stream().forEach(tile -> {
            tile.setCount(0);
        });
        updateTiles(game.getId(), updatedTiles);

        // round 1 player 1

        List<Pair<String, Integer>> letterValuePairs = new ArrayList<>();

        letterValuePairs.add(Pair.of("P", 1));
        letterValuePairs.add(Pair.of("A", 1));
        letterValuePairs.add(Pair.of("R", 1));
        letterValuePairs.add(Pair.of("K", 1));
        letterValuePairs.add(Pair.of("I", 1));
        letterValuePairs.add(Pair.of("N", 1));
        letterValuePairs.add(Pair.of("G", 1));

        updateRack(game.getId(), game.getOwnerId(), startedGame.getCurrentPlayerNumber(), startedGame.getRoundNumber(),
                letterValuePairs);

        VirtualRackDto virtualRack = getVirtualRack(game.getId(), 1L, startedGame.getRoundNumber());

        virtualRack.getTiles().get(0).setRowNumber(8);
        virtualRack.getTiles().get(0).setColumnNumber(7);
        virtualRack.getTiles().get(0).setSealed(true);

        virtualRack.getTiles().get(1).setRowNumber(8);
        virtualRack.getTiles().get(1).setColumnNumber(8);
        virtualRack.getTiles().get(1).setSealed(true);

        virtualRack.getTiles().get(2).setRowNumber(8);
        virtualRack.getTiles().get(2).setColumnNumber(9);
        virtualRack.getTiles().get(2).setSealed(true);

        virtualRack.getTiles().get(3).setRowNumber(8);
        virtualRack.getTiles().get(3).setColumnNumber(10);
        virtualRack.getTiles().get(3).setSealed(true);

        GameDto playedGame = playWord(game.getId(), 1L, virtualRack);

        assertNotNull(playedGame);
        assertEquals(4, playedGame.getVersion());
        assertEquals(2, playedGame.getCurrentPlayerNumber());
        assertEquals(GameStatus.IN_PROGRESS.name(), playedGame.getStatus());

        VirtualRackDto refreshedVirtualRack = getVirtualRack(game.getId(), game.getOwnerId(),
                playedGame.getRoundNumber() + 1);

        assertEquals(3, refreshedVirtualRack.getTiles().size());

        // round 1 player 2

        letterValuePairs = new ArrayList<>();

        letterValuePairs.add(Pair.of("S", 1));
        letterValuePairs.add(Pair.of("R", 1));
        letterValuePairs.add(Pair.of("I", 1));
        letterValuePairs.add(Pair.of("N", 1));
        letterValuePairs.add(Pair.of("A", 1));
        letterValuePairs.add(Pair.of("A", 1));
        letterValuePairs.add(Pair.of("A", 1));

        updateRack(game.getId(), 2L, playedGame.getCurrentPlayerNumber(), playedGame.getRoundNumber(),
                letterValuePairs);

        virtualRack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber());

        virtualRack.getTiles().get(0).setRowNumber(7);
        virtualRack.getTiles().get(0).setColumnNumber(8);
        virtualRack.getTiles().get(0).setSealed(true);

        virtualRack.getTiles().get(1).setRowNumber(9);
        virtualRack.getTiles().get(1).setColumnNumber(8);
        virtualRack.getTiles().get(1).setSealed(true);

        virtualRack.getTiles().get(2).setRowNumber(10);
        virtualRack.getTiles().get(2).setColumnNumber(8);
        virtualRack.getTiles().get(2).setSealed(true);

        virtualRack.getTiles().get(3).setRowNumber(11);
        virtualRack.getTiles().get(3).setColumnNumber(8);
        virtualRack.getTiles().get(3).setSealed(true);

        playedGame = playWord(game.getId(), 2L, virtualRack);

        assertNotNull(playedGame);
        assertEquals(5, playedGame.getVersion());
        assertEquals(1, playedGame.getCurrentPlayerNumber());
        assertEquals(GameStatus.LAST_ROUND.name(), playedGame.getStatus());

        refreshedVirtualRack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber() + 1);

        assertNull(refreshedVirtualRack);

        // round 2 player 1

        virtualRack = getVirtualRack(game.getId(), 1L, playedGame.getRoundNumber());

        playedGame = playWord(game.getId(), 1L, virtualRack);

        assertNotNull(playedGame);
        assertEquals(6, playedGame.getVersion());
        assertEquals(2, playedGame.getCurrentPlayerNumber());
        assertEquals(GameStatus.LAST_ROUND.name(), playedGame.getStatus());

        // round 2 player 2

        virtualRack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber());

        // wait for timeout
        Thread.sleep(5000);

        final GameDto skippedGame = getGame(game.getId());

        assertNotNull(skippedGame);
        assertEquals(7, skippedGame.getVersion());
        assertEquals(1, skippedGame.getCurrentPlayerNumber());
        assertEquals(GameStatus.READY_TO_END.name(), skippedGame.getStatus());

        // wait for timeout
        Thread.sleep(6000);

        final GameDto endedGame = getGame(game.getId());

        assertNotNull(endedGame);
        assertEquals(8, endedGame.getVersion());
        assertEquals(1, endedGame.getCurrentPlayerNumber());
        assertEquals(GameStatus.ENDED.name(), endedGame.getStatus());
    }

    @Test
    void test_play_duration_exceeds() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2);
        joinGame(game.getId(), 2L);

        waitUntilGameStarts();

        final GameDto startedGame = getGame(game.getId());

        // wait 100 milliseconds more than play duration
        Thread.sleep(5100);

        // the player should have skipped after exceeding the play duration
        final GameDto skippedGame = getGame(game.getId());

        assertEquals(startedGame.getVersion() + 1, skippedGame.getVersion());
    }

    @Test
    void test_skip_turn_job_is_triggered_while_playing() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2);
        joinGame(game.getId(), 2L);

        waitUntilGameStarts();

        final GameDto startedGame = getGame(game.getId());

        // round 1 player 1

        final List<Pair<String, Integer>> letterValuePairs = new ArrayList<>();

        letterValuePairs.add(Pair.of("W", 4));
        letterValuePairs.add(Pair.of("E", 1));
        letterValuePairs.add(Pair.of("A", 1));
        letterValuePairs.add(Pair.of("K", 5));
        letterValuePairs.add(Pair.of("O", 1));
        letterValuePairs.add(Pair.of("L", 1));
        letterValuePairs.add(Pair.of("E", 1));

        final VirtualRackDto virtualRack = updateRack(game.getId(), game.getOwnerId(),
                startedGame.getCurrentPlayerNumber(), startedGame.getRoundNumber(), letterValuePairs);

        virtualRack.getTiles().get(0).setRowNumber(8);
        virtualRack.getTiles().get(0).setColumnNumber(7);
        virtualRack.getTiles().get(0).setSealed(true);

        virtualRack.getTiles().get(1).setRowNumber(8);
        virtualRack.getTiles().get(1).setColumnNumber(8);
        virtualRack.getTiles().get(1).setSealed(true);

        virtualRack.getTiles().get(2).setRowNumber(8);
        virtualRack.getTiles().get(2).setColumnNumber(9);
        virtualRack.getTiles().get(2).setSealed(true);

        virtualRack.getTiles().get(3).setRowNumber(8);
        virtualRack.getTiles().get(3).setColumnNumber(10);
        virtualRack.getTiles().get(3).setSealed(true);

        Thread.sleep(3850);

        final GameDto playGame = playWord(game.getId(), game.getOwnerId(), virtualRack);

        assertNotNull(playGame);
        assertEquals(4, playGame.getVersion());
        assertEquals(2, playGame.getCurrentPlayerNumber());

        final List<PlayerDto> players = getPlayers(game.getId(), playGame.getVersion());
        assertEquals(11, players.get(0).getScore());
        assertEquals(0, players.get(1).getScore());

        final VirtualBoardDto virtualBoard = getVirtualBoard(game.getId(), playGame.getVersion(),
                game.getExpectedPlayerCount());

        assertEquals("W", virtualBoard.getCells().get(111).getLetter());
        assertEquals("E", virtualBoard.getCells().get(112).getLetter());
        assertEquals("A", virtualBoard.getCells().get(113).getLetter());
        assertEquals("K", virtualBoard.getCells().get(114).getLetter());

        final VirtualRackDto refreshedVirtualRack = getVirtualRack(game.getId(), game.getOwnerId(),
                playGame.getRoundNumber() + 1);

        assertEquals(7, refreshedVirtualRack.getTiles().size());
    }

    @Test
    void test_word_is_played_while_skip_turn_job_is_running() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2);
        joinGame(game.getId(), 2L);

        waitUntilGameStarts();

        final GameDto startedGame = getGame(game.getId());

        // round 1 player 1

        final List<Pair<String, Integer>> letterValuePairs = new ArrayList<>();

        letterValuePairs.add(Pair.of("W", 4));
        letterValuePairs.add(Pair.of("E", 1));
        letterValuePairs.add(Pair.of("A", 1));
        letterValuePairs.add(Pair.of("K", 5));
        letterValuePairs.add(Pair.of("O", 1));
        letterValuePairs.add(Pair.of("L", 1));
        letterValuePairs.add(Pair.of("E", 1));

        final VirtualRackDto virtualRack = updateRack(game.getId(), game.getOwnerId(),
                startedGame.getCurrentPlayerNumber(), startedGame.getRoundNumber(), letterValuePairs);

        virtualRack.getTiles().get(0).setRowNumber(8);
        virtualRack.getTiles().get(0).setColumnNumber(7);
        virtualRack.getTiles().get(0).setSealed(true);

        virtualRack.getTiles().get(1).setRowNumber(8);
        virtualRack.getTiles().get(1).setColumnNumber(8);
        virtualRack.getTiles().get(1).setSealed(true);

        virtualRack.getTiles().get(2).setRowNumber(8);
        virtualRack.getTiles().get(2).setColumnNumber(9);
        virtualRack.getTiles().get(2).setSealed(true);

        virtualRack.getTiles().get(3).setRowNumber(8);
        virtualRack.getTiles().get(3).setColumnNumber(10);
        virtualRack.getTiles().get(3).setSealed(true);

        Thread.sleep(4100);

        try {
            playWord(game.getId(), game.getOwnerId(), virtualRack);
            fail("Concurrent execution of play service wasn't stopped");
        } catch (Exception e) {
            // nothing to do here
        }

        final GameDto updatedGame = getGame(game.getId());

        assertNotNull(updatedGame);
        assertEquals(4, updatedGame.getVersion());
        assertEquals(2, updatedGame.getCurrentPlayerNumber());

        final List<PlayerDto> players = getPlayers(game.getId(), updatedGame.getVersion());
        assertEquals(0, players.get(0).getScore());
        assertEquals(0, players.get(1).getScore());

        final VirtualBoardDto virtualBoard = getVirtualBoard(game.getId(), updatedGame.getVersion(),
                game.getExpectedPlayerCount());

        boolean hasNonEmptyCells = virtualBoard.getCells().stream().anyMatch(VirtualCellDto::isSealed);
        assertFalse(hasNonEmptyCells);

        final VirtualRackDto refreshedVirtualRack = getVirtualRack(game.getId(), game.getOwnerId(),
                updatedGame.getRoundNumber() + 1);

        assertEquals(7, refreshedVirtualRack.getTiles().size());
    }

    @Test
    void test_skip_2_rounds_in_a_row_by_the_players_ends_the_game() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2);
        joinGame(game.getId(), 2L);

        waitUntilGameStarts();

        final GameDto startedGame = getGame(game.getId());

        VirtualRackDto virtualRack = getVirtualRack(game.getId(), 1L, startedGame.getRoundNumber());
        GameDto playedGame = playWord(game.getId(), 1L, virtualRack);

        virtualRack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber());
        playedGame = playWord(game.getId(), 2L, virtualRack);

        virtualRack = getVirtualRack(game.getId(), 1L, playedGame.getRoundNumber());
        playedGame = playWord(game.getId(), 1L, virtualRack);

        virtualRack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber());
        playedGame = playWord(game.getId(), 2L, virtualRack);

        // wait until the end game job ends
        Thread.sleep(1000);

        final GameDto endedGame = getGame(game.getId());

        assertNotNull(endedGame);
        assertEquals(8, endedGame.getVersion());
        assertEquals(GameStatus.ENDED.name(), endedGame.getStatus());
    }

    @Test
    void test_skip_2_rounds_in_a_row_by_the_job_ends_the_game() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2);
        joinGame(game.getId(), 2L);

        waitUntilGameStarts();

        // Wait more than 4 turns durations
        Thread.sleep(25000);

        final GameDto endedGame = getGame(game.getId());

        assertNotNull(endedGame);
        assertEquals(8, endedGame.getVersion());
        assertEquals(GameStatus.ENDED.name(), endedGame.getStatus());
    }

    private GameDto getGame(Long gameId) {
        final Response response = target("/games/" + gameId).request().get();

        if (Status.OK.getStatusCode() != response.getStatus()) {
            assertEquals(Status.OK.getStatusCode(), response.getStatus(), response.readEntity(String.class));
        }
        assertNotNull(response.getHeaderString(HttpHeaders.ETAG));

        final GameDto game = response.readEntity(GameDto.class);

        response.close();

        return game;
    }

    private GameDto createNewGame(Integer playerCount) throws IOException {
        final URL resource = GameResourceIT.class.getResource("/json/game.json");
        final GameDto game = JsonUtils.toDto(Resources.toString(resource, StandardCharsets.UTF_8), GameDto.class);
        game.setExpectedPlayerCount(playerCount);

        final Response gameResponse = target("/games").request().put(Entity.entity(game, MediaType.APPLICATION_JSON));

        if (Status.OK.getStatusCode() != gameResponse.getStatus()) {
            assertEquals(Status.OK.getStatusCode(), gameResponse.getStatus(), gameResponse.readEntity(String.class));
        }
        assertNotNull(gameResponse.getHeaderString(HttpHeaders.ETAG));

        final GameDto responseDto = gameResponse.readEntity(GameDto.class);
        gameResponse.close();
        return responseDto;
    }

    private GameDto joinGame(Long gameId, Long userId) {
        final Response joinGameResponse = target("/games/" + gameId + "/users/" + userId).request()
                .put(Entity.entity("", MediaType.APPLICATION_JSON));

        if (Status.OK.getStatusCode() != joinGameResponse.getStatus()) {
            assertEquals(Status.OK.getStatusCode(), joinGameResponse.getStatus(),
                    joinGameResponse.readEntity(String.class));
        }

        final GameDto joinGameDto = joinGameResponse.readEntity(GameDto.class);

        joinGameResponse.close();

        return joinGameDto;
    }

    private GameDto playWord(Long gameId, Long userId, VirtualRackDto virtualRack) {
        final Response playGameResponse = target("/games/" + gameId + "/users/" + userId + "/rack").request()
                .post(Entity.entity(virtualRack, MediaType.APPLICATION_JSON));

        final GameDto playGame = playGameResponse.readEntity(GameDto.class);

        playGameResponse.close();

        return playGame;
    }

    private VirtualRackDto updateRack(Long gameId, Long userId, Integer playerNumber, Integer roundNumber,
            List<Pair<String, Integer>> letterValuePairs) {
        final VirtualRackDto virtualRack = getVirtualRack(gameId, userId, roundNumber);

        List<VirtualTileDto> tiles = new ArrayList<>();
        IntStream.range(1, 8).forEach(tileNumber -> {
            final Pair<String, Integer> letterValuePair = letterValuePairs.get(tileNumber - 1);
            tiles.add(VirtualTileDto.builder()
                    .letter(letterValuePair.getKey())
                    .value(letterValuePair.getValue())
                    .number(tileNumber)
                    .playerNumber(playerNumber)
                    .roundNumber(roundNumber)
                    .build());
        });

        virtualRack.setTiles(tiles);

        redisTemplate.boundListOps(Constants.CacheKey.RACK + ":" + gameId + ":" + userId)
                .set(0, Mapper.toEntity(virtualRack));

        return virtualRack;
    }

    private void updateTiles(Long gameId, List<Tile> updatedTiles) {
        redisTemplate.opsForValue().set(Constants.CacheKey.TILES + ":" + gameId, updatedTiles);
    }

    private List<PlayerDto> getPlayers(Long gameId, Integer version) {
        final Response playersResponse = target("/games/" + gameId + "/players").queryParam("version", version)
                .request()
                .get();

        if (Status.OK.getStatusCode() != playersResponse.getStatus()) {
            assertEquals(Status.OK.getStatusCode(), playersResponse.getStatus(),
                    playersResponse.readEntity(String.class));
        }

        final List<PlayerDto> players = playersResponse.readEntity(new GenericType<List<PlayerDto>>() {
        });

        playersResponse.close();

        return players;
    }

    private VirtualBoardDto getVirtualBoard(Long gameId, Integer version, Integer playerCount) {
        final Response virtualBoardResponse = target("/games/" + gameId + "/boards")
                .queryParam("version", version - playerCount)
                .request()
                .get();

        final VirtualBoardDto virtualBoard = virtualBoardResponse.readEntity(VirtualBoardDto.class);

        virtualBoardResponse.close();

        return virtualBoard;
    }

    private VirtualRackDto getVirtualRack(Long gameId, Long userId, Integer roundNumber) {
        final Response virtualRackResponse = target("/games/" + gameId + "/racks/users/" + userId)
                .queryParam("roundNumber", roundNumber)
                .request()
                .get();

        final VirtualRackDto virtualRack = virtualRackResponse.readEntity(VirtualRackDto.class);

        virtualRackResponse.close();

        return virtualRack;
    }

    private List<Tile> getTiles(Long gameId) {
        return (List<Tile>) redisTemplate.opsForValue().get(Constants.CacheKey.TILES + ":" + gameId);
    }

    private void waitUntilGameStarts() throws InterruptedException {
        Thread.sleep(1000);
    }

}
