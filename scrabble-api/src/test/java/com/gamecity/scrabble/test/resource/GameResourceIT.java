package com.gamecity.scrabble.test.resource;

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
import com.gamecity.scrabble.model.rest.VirtualRackDto;
import com.gamecity.scrabble.model.rest.VirtualTileDto;
import com.gamecity.scrabble.service.exception.error.GameError;
import com.gamecity.scrabble.util.JsonUtils;
import com.google.common.io.Resources;

class GameResourceIT extends AbstractIntegrationTest {

    @Test
    void test_create_game() throws IOException {
        final GameDto game = createNewGame(2);

        assertEquals("My game", game.getName());
        assertNotNull(game.getId());

        final List<PlayerDto> players = getPlayers(game.getId(), game.getActionCounter());
        assertEquals(1, players.size());
        assertEquals("Edi", players.get(0).getUsername());
    }

    @Test
    void test_get_game() throws IOException {
        final GameDto game = createNewGame(2);

        final Response response = target("/games/" + game.getId()).request().get();

        if (Status.OK.getStatusCode() != response.getStatus()) {
            assertEquals(Status.OK.getStatusCode(), response.getStatus(), response.readEntity(String.class));
        }
        assertNotNull(response.getHeaderString(HttpHeaders.ETAG));

        final GameDto getGame = response.readEntity(GameDto.class);

        assertEquals("My game", getGame.getName());
        assertNotNull(getGame.getId());

        response.close();
    }

    @Test
    void test_update_game() throws IOException {
        final GameDto game = createNewGame(2);

        final Response response = target("/games/" + game.getId()).request().get();

        final String etag = response.getHeaderString(HttpHeaders.ETAG);
        final GameDto getGame = response.readEntity(GameDto.class);
        getGame.setName("My updated game");

        final Response updateGameResponse = target("/games/" + game.getId()).request()
                .header(HttpHeaders.IF_MATCH, etag)
                .put(Entity.entity(getGame, MediaType.APPLICATION_JSON));

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
    void test_update_game_without_etag() throws IOException {
        final GameDto game = createNewGame(2);

        final Response response = target("/games/" + game.getId()).request().get();

        final GameDto updateGame = response.readEntity(GameDto.class);
        updateGame.setName("My updated game");

        final Response updateGameResponse =
                target("/games/" + game.getId()).request().put(Entity.entity(updateGame, MediaType.APPLICATION_JSON));

        assertEquals("If-Match header is missing", updateGameResponse.readEntity(String.class));

        updateGameResponse.close();
        response.close();
    }

    @Test
    void test_join_game() throws IOException {
        final GameDto game = createNewGame(3);
        final GameDto joinGame = joinGame(game.getId(), 2L);

        assertEquals(2, joinGame.getActionCounter());

        final List<PlayerDto> players = getPlayers(game.getId(), joinGame.getActionCounter());
        assertEquals(2, players.size());
        assertEquals("Budu", players.get(1).getUsername());
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

        final GameDto leaveGame = leaveGameResponse.readEntity(GameDto.class);

        assertEquals(3, leaveGame.getActionCounter());

        final List<PlayerDto> players = getPlayers(game.getId(), leaveGame.getActionCounter());
        assertEquals(1, players.size());
        assertEquals("Edi", players.get(0).getUsername());

        leaveGameResponse.close();
    }

    @Test
    void test_start_game() throws IOException {
        final GameDto game = createNewGame(2);
        final GameDto joinGame = joinGame(game.getId(), 2L);

        assertEquals(2, joinGame.getActionCounter());
        assertEquals(GameStatus.READY_TO_START.name(), joinGame.getStatus());

        final GameDto startGame = startGame(game.getId());

        assertEquals(3, startGame.getActionCounter());
        assertEquals(1, startGame.getCurrentPlayerNumber());
        assertEquals(GameStatus.IN_PROGRESS.name(), startGame.getStatus());

        final VirtualBoardDto virtualBoard =
                getVirtualBoard(game.getId(), startGame.getActionCounter(), game.getExpectedPlayerCount());

        assertNotNull(virtualBoard);
        assertEquals(225, virtualBoard.getCells().size());

        virtualBoard.getCells().stream().forEach(cell -> {
            assertNull(cell.getRoundNumber());
        });

        final VirtualRackDto virtualRack = getVirtualRack(game.getId(), game.getOwnerId(), startGame.getRoundNumber());

        assertNotNull(virtualRack);

        virtualRack.getTiles().stream().forEach(tile -> assertEquals(1, tile.getRoundNumber()));
        virtualRack.getTiles().stream().forEach(tile -> assertFalse(tile.isSealed()));
    }

    @Test
    void test_play_two_rounds() throws IOException {
        final GameDto game = createNewGame(2);
        joinGame(game.getId(), 2L);
        final GameDto startGame = startGame(game.getId());

        // round 1 player 1

        List<Pair<String, Integer>> letterValuePairs = new ArrayList<>();

        letterValuePairs.add(Pair.of("W", 4));
        letterValuePairs.add(Pair.of("E", 1));
        letterValuePairs.add(Pair.of("A", 1));
        letterValuePairs.add(Pair.of("K", 5));
        letterValuePairs.add(Pair.of("O", 1));
        letterValuePairs.add(Pair.of("L", 1));
        letterValuePairs.add(Pair.of("E", 1));

        VirtualRackDto virtualRack = updateRack(game.getId(), game.getOwnerId(), startGame.getCurrentPlayerNumber(),
                startGame.getRoundNumber(), letterValuePairs);

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

        GameDto playGame = playWord(game.getId(), game.getOwnerId(), virtualRack);

        assertNotNull(playGame);
        assertEquals(4, playGame.getActionCounter());
        assertEquals(2, playGame.getCurrentPlayerNumber());

        List<PlayerDto> players = getPlayers(game.getId(), playGame.getActionCounter());
        assertEquals(11, players.get(0).getScore());
        assertEquals(0, players.get(1).getScore());

        VirtualRackDto updatedVirtualRack = getVirtualRack(game.getId(), game.getOwnerId(), playGame.getRoundNumber());

        updatedVirtualRack.getTiles().stream().forEach(tile -> assertEquals(1, tile.getRoundNumber()));

        assertTrue(updatedVirtualRack.getTiles().get(0).isSealed());
        assertTrue(updatedVirtualRack.getTiles().get(1).isSealed());
        assertTrue(updatedVirtualRack.getTiles().get(2).isSealed());
        assertTrue(updatedVirtualRack.getTiles().get(3).isSealed());
        assertFalse(updatedVirtualRack.getTiles().get(4).isSealed());
        assertFalse(updatedVirtualRack.getTiles().get(5).isSealed());
        assertFalse(updatedVirtualRack.getTiles().get(6).isSealed());

        VirtualBoardDto virtualBoard =
                getVirtualBoard(game.getId(), playGame.getActionCounter(), game.getExpectedPlayerCount());

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

        VirtualRackDto refreshedVirtualRack =
                getVirtualRack(game.getId(), game.getOwnerId(), playGame.getRoundNumber() + 1);

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

        virtualRack = updateRack(game.getId(), 2L, playGame.getCurrentPlayerNumber(), playGame.getRoundNumber(),
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

        playGame = playWord(game.getId(), 2L, virtualRack);

        assertNotNull(playGame);
        assertEquals(5, playGame.getActionCounter());
        assertEquals(1, playGame.getCurrentPlayerNumber());

        players = getPlayers(game.getId(), playGame.getActionCounter());
        assertEquals(11, players.get(0).getScore());
        assertEquals(13, players.get(1).getScore());

        updatedVirtualRack = getVirtualRack(game.getId(), 2L, playGame.getRoundNumber() - 1);

        updatedVirtualRack.getTiles().stream().forEach(tile -> assertEquals(1, tile.getRoundNumber()));

        assertTrue(updatedVirtualRack.getTiles().get(0).isSealed());
        assertTrue(updatedVirtualRack.getTiles().get(1).isSealed());
        assertTrue(updatedVirtualRack.getTiles().get(2).isSealed());
        assertTrue(updatedVirtualRack.getTiles().get(3).isSealed());
        assertFalse(updatedVirtualRack.getTiles().get(4).isSealed());
        assertFalse(updatedVirtualRack.getTiles().get(5).isSealed());
        assertFalse(updatedVirtualRack.getTiles().get(6).isSealed());

        virtualBoard = getVirtualBoard(game.getId(), playGame.getActionCounter(), game.getExpectedPlayerCount());

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

        refreshedVirtualRack = getVirtualRack(game.getId(), 2L, playGame.getRoundNumber());

        assertEquals(2, refreshedVirtualRack.getTiles().get(0).getRoundNumber());
        assertEquals(2, refreshedVirtualRack.getTiles().get(1).getRoundNumber());
        assertEquals(2, refreshedVirtualRack.getTiles().get(2).getRoundNumber());
        assertEquals(2, refreshedVirtualRack.getTiles().get(3).getRoundNumber());
        assertEquals(1, refreshedVirtualRack.getTiles().get(4).getRoundNumber());
        assertEquals(1, refreshedVirtualRack.getTiles().get(5).getRoundNumber());
        assertEquals(1, refreshedVirtualRack.getTiles().get(6).getRoundNumber());

        refreshedVirtualRack.getTiles().stream().forEach(tile -> assertFalse(tile.isSealed()));

        // round 2 player 1

        virtualRack = getVirtualRack(game.getId(), game.getOwnerId(), playGame.getRoundNumber());

        virtualRack.getTiles().get(4).setRowNumber(6);
        virtualRack.getTiles().get(4).setColumnNumber(8);
        virtualRack.getTiles().get(4).setSealed(true);

        virtualRack.getTiles().get(5).setRowNumber(6);
        virtualRack.getTiles().get(5).setColumnNumber(9);
        virtualRack.getTiles().get(5).setSealed(true);

        virtualRack.getTiles().get(6).setRowNumber(6);
        virtualRack.getTiles().get(6).setColumnNumber(10);
        virtualRack.getTiles().get(6).setSealed(true);

        playGame = playWord(game.getId(), game.getOwnerId(), virtualRack);

        assertNotNull(playGame);
        assertEquals(6, playGame.getActionCounter());
        assertEquals(2, playGame.getCurrentPlayerNumber());

        players = getPlayers(game.getId(), playGame.getActionCounter());
        assertEquals(17, players.get(0).getScore());
        assertEquals(13, players.get(1).getScore());

        updatedVirtualRack = getVirtualRack(game.getId(), game.getOwnerId(), playGame.getRoundNumber());

        virtualBoard = getVirtualBoard(game.getId(), playGame.getActionCounter(), game.getExpectedPlayerCount());

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

        refreshedVirtualRack = getVirtualRack(game.getId(), game.getOwnerId(), playGame.getRoundNumber() + 1);

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

        virtualRack = getVirtualRack(game.getId(), 2L, playGame.getRoundNumber());

        virtualRack.getTiles().get(4).setRowNumber(8);
        virtualRack.getTiles().get(4).setColumnNumber(11);
        virtualRack.getTiles().get(4).setSealed(true);

        virtualRack.getTiles().get(5).setRowNumber(8);
        virtualRack.getTiles().get(5).setColumnNumber(12);
        virtualRack.getTiles().get(5).setSealed(true);

        playGame = playWord(game.getId(), 2L, virtualRack);

        assertNotNull(playGame);
        assertEquals(7, playGame.getActionCounter());
        assertEquals(1, playGame.getCurrentPlayerNumber());

        players = getPlayers(game.getId(), playGame.getActionCounter());
        assertEquals(17, players.get(0).getScore());
        assertEquals(27, players.get(1).getScore());

        updatedVirtualRack = getVirtualRack(game.getId(), 2L, playGame.getRoundNumber() - 1);

        virtualBoard = getVirtualBoard(game.getId(), playGame.getActionCounter(), game.getExpectedPlayerCount());

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

        refreshedVirtualRack = getVirtualRack(game.getId(), 2L, playGame.getRoundNumber());

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
    void test_exchange_tile_only_once_in_a_round() throws IOException {
        final GameDto game = createNewGame(2);
        joinGame(game.getId(), 2L);
        final GameDto startGame = startGame(game.getId());

        final List<Pair<String, Integer>> letterValuePairs = new ArrayList<>();

        letterValuePairs.add(Pair.of("B", 1));
        letterValuePairs.add(Pair.of("C", 1));
        letterValuePairs.add(Pair.of("D", 1));
        letterValuePairs.add(Pair.of("F", 1));
        letterValuePairs.add(Pair.of("G", 1));
        letterValuePairs.add(Pair.of("H", 1));
        letterValuePairs.add(Pair.of("K", 1));

        updateRack(game.getId(), game.getOwnerId(), startGame.getCurrentPlayerNumber(), startGame.getRoundNumber(),
                letterValuePairs);

        List<Tile> tiles = getTiles(game.getId());
        Tile exchangedTile = tiles.stream().filter(tile -> "B".equals(tile.getLetter())).findFirst().orElse(null);

        int countBeforeExchange = exchangedTile.getCount();

        final Response exchangeTileResponse =
                target("/games/" + game.getId() + "/racks/users/" + game.getOwnerId() + "/tiles/" + 1).request()
                        .post(Entity.entity("", MediaType.APPLICATION_JSON));

        VirtualRackDto virtualRack = exchangeTileResponse.readEntity(VirtualRackDto.class);

        assertNotNull(virtualRack);
        assertNotEquals("B", virtualRack.getTiles().get(0).getLetter());
        assertTrue(virtualRack.getTiles().get(0).isVowel());

        final VirtualRackDto updatedRack = getVirtualRack(game.getId(), game.getOwnerId(), startGame.getRoundNumber());

        assertEquals(virtualRack.getTiles().get(0).getLetter(), updatedRack.getTiles().get(0).getLetter());
        assertEquals(virtualRack.getTiles().get(0).isVowel(), updatedRack.getTiles().get(0).isVowel());

        tiles = getTiles(game.getId());
        exchangedTile = tiles.stream().filter(tile -> "B".equals(tile.getLetter())).findFirst().orElse(null);

        int countAfterExchange = exchangedTile.getCount();

        assertEquals(countBeforeExchange + 1, countAfterExchange);

        exchangedTile = tiles.stream().filter(tile -> "C".equals(tile.getLetter())).findFirst().orElse(null);

        final Response secondExchangeTileResponse =
                target("/games/" + game.getId() + "/racks/users/" + game.getOwnerId() + "/tiles/" + 2).request()
                        .post(Entity.entity("", MediaType.APPLICATION_JSON));

        final ExceptionDto exception = secondExchangeTileResponse.readEntity(ExceptionDto.class);

        assertNotNull(exception);
        assertEquals(GameError.EXCHANGED.getCode(), exception.getCode());

        secondExchangeTileResponse.close();
        exchangeTileResponse.close();
    }

    @Test
    void test_do_not_exchange_tile_on_empty_bag() throws IOException {
        final GameDto game = createNewGame(2);
        joinGame(game.getId(), 2L);
        final GameDto startGame = startGame(game.getId());

        final List<Pair<String, Integer>> letterValuePairs = new ArrayList<>();

        letterValuePairs.add(Pair.of("B", 1));
        letterValuePairs.add(Pair.of("C", 1));
        letterValuePairs.add(Pair.of("D", 1));
        letterValuePairs.add(Pair.of("F", 1));
        letterValuePairs.add(Pair.of("G", 1));
        letterValuePairs.add(Pair.of("H", 1));
        letterValuePairs.add(Pair.of("K", 1));

        updateRack(game.getId(), game.getOwnerId(), startGame.getCurrentPlayerNumber(), startGame.getRoundNumber(),
                letterValuePairs);

        updateTiles(game.getId(), new ArrayList<>());

        List<Tile> tiles = getTiles(game.getId());
        Tile exchangedTile = tiles.stream().filter(tile -> "B".equals(tile.getLetter())).findFirst().orElse(null);

        assertNull(exchangedTile);

        final Response exchangeTileResponse =
                target("/games/" + game.getId() + "/racks/users/" + game.getOwnerId() + "/tiles/" + 1).request()
                        .post(Entity.entity("", MediaType.APPLICATION_JSON));

        final VirtualRackDto virtualRack = exchangeTileResponse.readEntity(VirtualRackDto.class);

        assertNotNull(virtualRack);
        assertEquals("B", virtualRack.getTiles().get(0).getLetter());

        final VirtualRackDto updatedRack = getVirtualRack(game.getId(), game.getOwnerId(), startGame.getRoundNumber());

        assertEquals(virtualRack.getTiles().get(0).getLetter(), updatedRack.getTiles().get(0).getLetter());
        assertEquals(virtualRack.getTiles().get(0).isVowel(), updatedRack.getTiles().get(0).isVowel());

        tiles = getTiles(game.getId());
        exchangedTile = tiles.stream().filter(tile -> "B".equals(tile.getLetter())).findFirst().orElse(null);

        assertNull(exchangedTile);

        exchangeTileResponse.close();
    }

    @Test
    void test_do_not_exchange_tile_when_no_vowels_left() throws IOException {
        final GameDto game = createNewGame(2);
        joinGame(game.getId(), 2L);
        final GameDto startGame = startGame(game.getId());

        final List<Pair<String, Integer>> letterValuePairs = new ArrayList<>();

        letterValuePairs.add(Pair.of("B", 1));
        letterValuePairs.add(Pair.of("C", 1));
        letterValuePairs.add(Pair.of("D", 1));
        letterValuePairs.add(Pair.of("F", 1));
        letterValuePairs.add(Pair.of("G", 1));
        letterValuePairs.add(Pair.of("H", 1));
        letterValuePairs.add(Pair.of("K", 1));

        updateRack(game.getId(), game.getOwnerId(), startGame.getCurrentPlayerNumber(), startGame.getRoundNumber(),
                letterValuePairs);

        final List<Tile> updatedTiles = new ArrayList<>();
        updatedTiles.add(Tile.builder().bagId(startGame.getBagId()).count(4).letter("C").value(1).vowel(false).build());
        updatedTiles.add(Tile.builder().bagId(startGame.getBagId()).count(4).letter("C").value(1).vowel(false).build());
        updatedTiles.add(Tile.builder().bagId(startGame.getBagId()).count(4).letter("F").value(1).vowel(false).build());

        updateTiles(game.getId(), updatedTiles);

        List<Tile> tiles = getTiles(game.getId());
        Tile exchangedTile = tiles.stream().filter(tile -> "B".equals(tile.getLetter())).findFirst().orElse(null);

        assertNull(exchangedTile);

        final Response exchangeTileResponse =
                target("/games/" + game.getId() + "/racks/users/" + game.getOwnerId() + "/tiles/" + 1).request()
                        .post(Entity.entity("", MediaType.APPLICATION_JSON));

        final VirtualRackDto virtualRack = exchangeTileResponse.readEntity(VirtualRackDto.class);

        assertNotNull(virtualRack);
        assertEquals("B", virtualRack.getTiles().get(0).getLetter());

        final VirtualRackDto updatedRack = getVirtualRack(game.getId(), game.getOwnerId(), startGame.getRoundNumber());

        assertEquals(virtualRack.getTiles().get(0).getLetter(), updatedRack.getTiles().get(0).getLetter());
        assertEquals(virtualRack.getTiles().get(0).isVowel(), updatedRack.getTiles().get(0).isVowel());

        tiles = getTiles(game.getId());
        exchangedTile = tiles.stream().filter(tile -> "B".equals(tile.getLetter())).findFirst().orElse(null);

        assertNull(exchangedTile);

        exchangeTileResponse.close();
    }

    @Test
    void test_last_round_and_game_end() throws IOException {
        final GameDto game = createNewGame(2);
        joinGame(game.getId(), 2L);
        final GameDto startGame = startGame(game.getId());

        updateTiles(game.getId(), new ArrayList<>());

        // round 1 player 1

        List<Pair<String, Integer>> letterValuePairs = new ArrayList<>();

        letterValuePairs.add(Pair.of("P", 1));
        letterValuePairs.add(Pair.of("A", 1));
        letterValuePairs.add(Pair.of("R", 1));
        letterValuePairs.add(Pair.of("K", 1));
        letterValuePairs.add(Pair.of("I", 1));
        letterValuePairs.add(Pair.of("N", 1));
        letterValuePairs.add(Pair.of("G", 1));

        updateRack(game.getId(), game.getOwnerId(), startGame.getCurrentPlayerNumber(), startGame.getRoundNumber(),
                letterValuePairs);

        VirtualRackDto virtualRack = getVirtualRack(game.getId(), 1L, startGame.getRoundNumber());

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

        GameDto playGame = playWord(game.getId(), 1L, virtualRack);

        assertNotNull(playGame);
        assertEquals(4, playGame.getActionCounter());
        assertEquals(2, playGame.getCurrentPlayerNumber());
        assertEquals(GameStatus.IN_PROGRESS.name(), playGame.getStatus());

        VirtualRackDto refreshedVirtualRack =
                getVirtualRack(game.getId(), game.getOwnerId(), playGame.getRoundNumber() + 1);

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

        updateRack(game.getId(), 2L, playGame.getCurrentPlayerNumber(), playGame.getRoundNumber(), letterValuePairs);

        virtualRack = getVirtualRack(game.getId(), 2L, playGame.getRoundNumber());

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

        playGame = playWord(game.getId(), 2L, virtualRack);

        assertNotNull(playGame);
        assertEquals(5, playGame.getActionCounter());
        assertEquals(1, playGame.getCurrentPlayerNumber());
        assertEquals(GameStatus.LAST_ROUND.name(), playGame.getStatus());

        refreshedVirtualRack = getVirtualRack(game.getId(), 2L, playGame.getRoundNumber() + 1);

        assertNull(refreshedVirtualRack);

        // round 2 player 1

        virtualRack = getVirtualRack(game.getId(), 1L, playGame.getRoundNumber());

        virtualRack.getTiles().get(0).setRowNumber(8);
        virtualRack.getTiles().get(0).setColumnNumber(11);
        virtualRack.getTiles().get(0).setSealed(true);

        virtualRack.getTiles().get(1).setRowNumber(8);
        virtualRack.getTiles().get(1).setColumnNumber(12);
        virtualRack.getTiles().get(1).setSealed(true);

        virtualRack.getTiles().get(2).setRowNumber(8);
        virtualRack.getTiles().get(2).setColumnNumber(13);
        virtualRack.getTiles().get(2).setSealed(true);

        playGame = playWord(game.getId(), 1L, virtualRack);

        assertNotNull(playGame);
        assertEquals(6, playGame.getActionCounter());
        assertEquals(2, playGame.getCurrentPlayerNumber());
        assertEquals(GameStatus.LAST_ROUND.name(), playGame.getStatus());

        // round 2 player 2

        virtualRack = getVirtualRack(game.getId(), 2L, playGame.getRoundNumber());

        playGame = playWord(game.getId(), 2L, virtualRack);

        assertNotNull(playGame);
        assertEquals(7, playGame.getActionCounter());
        assertEquals(1, playGame.getCurrentPlayerNumber());
        assertEquals(GameStatus.READY_TO_END.name(), playGame.getStatus());

    }

    private GameDto createNewGame(Integer playerCount) throws IOException {
        final URL resource = GameResourceIT.class.getResource("/json/game.json");
        final GameDto game = JsonUtils.toEntity(Resources.toString(resource, StandardCharsets.UTF_8), GameDto.class);
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

    private GameDto startGame(Long gameId) {
        final Response startGameResponse =
                target("/games/" + gameId + "/start").request().post(Entity.entity("", MediaType.APPLICATION_JSON));

        final GameDto startGameDto = startGameResponse.readEntity(GameDto.class);

        startGameResponse.close();

        return startGameDto;
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

    private List<PlayerDto> getPlayers(Long gameId, Integer actionCounter) {
        final Response playersResponse =
                target("/games/" + gameId + "/players").queryParam("actionCounter", actionCounter).request().get();

        if (Status.OK.getStatusCode() != playersResponse.getStatus()) {
            assertEquals(Status.OK.getStatusCode(), playersResponse.getStatus(),
                    playersResponse.readEntity(String.class));
        }

        final List<PlayerDto> players = playersResponse.readEntity(new GenericType<List<PlayerDto>>() {});

        playersResponse.close();

        return players;
    }

    private VirtualBoardDto getVirtualBoard(Long gameId, Integer actionCounter, Integer playerCount) {
        final Response virtualBoardResponse =
                target("/games/" + gameId + "/boards").queryParam("actionCounter", actionCounter - playerCount)
                        .request()
                        .get();

        final VirtualBoardDto virtualBoard = virtualBoardResponse.readEntity(VirtualBoardDto.class);

        virtualBoardResponse.close();

        return virtualBoard;
    }

    private VirtualRackDto getVirtualRack(Long gameId, Long userId, Integer roundNumber) {
        final Response virtualRackResponse =
                target("/games/" + gameId + "/racks/users/" + userId).queryParam("roundNumber", roundNumber)
                        .request()
                        .get();

        final VirtualRackDto virtualRack = virtualRackResponse.readEntity(VirtualRackDto.class);

        virtualRackResponse.close();

        return virtualRack;
    }

    private List<Tile> getTiles(Long gameId) {
        return (List<Tile>) redisTemplate.opsForValue().get(Constants.CacheKey.TILES + ":" + gameId);
    }

}
