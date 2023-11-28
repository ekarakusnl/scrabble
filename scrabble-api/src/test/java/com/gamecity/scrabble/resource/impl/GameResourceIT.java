package com.gamecity.scrabble.resource.impl;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.entity.ActionType;
import com.gamecity.scrabble.entity.GameStatus;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.Tile;
import com.gamecity.scrabble.model.Mapper;
import com.gamecity.scrabble.model.rest.ActionDto;
import com.gamecity.scrabble.model.rest.ChatDto;
import com.gamecity.scrabble.model.rest.GameDto;
import com.gamecity.scrabble.model.rest.PlayerDto;
import com.gamecity.scrabble.model.rest.VirtualBoardDto;
import com.gamecity.scrabble.model.rest.VirtualCellDto;
import com.gamecity.scrabble.model.rest.VirtualRackDto;
import com.gamecity.scrabble.model.rest.VirtualTileDto;
import com.gamecity.scrabble.model.rest.WordDto;
import com.gamecity.scrabble.util.JsonUtils;
import com.google.common.io.Resources;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class GameResourceIT extends AbstractIntegrationTest {

    private static final Map<String, Tile> TILE_MAP = new HashMap<>();

    static {
        TILE_MAP.put("A", Tile.builder().letter("A").count(9).value(1).build());
        TILE_MAP.put("B", Tile.builder().letter("B").count(2).value(3).build());
        TILE_MAP.put("C", Tile.builder().letter("C").count(2).value(3).build());
        TILE_MAP.put("D", Tile.builder().letter("D").count(4).value(2).build());
        TILE_MAP.put("E", Tile.builder().letter("E").count(12).value(1).build());
        TILE_MAP.put("F", Tile.builder().letter("F").count(2).value(4).build());
        TILE_MAP.put("G", Tile.builder().letter("G").count(3).value(2).build());
        TILE_MAP.put("H", Tile.builder().letter("H").count(2).value(4).build());
        TILE_MAP.put("I", Tile.builder().letter("I").count(9).value(1).build());
        TILE_MAP.put("J", Tile.builder().letter("J").count(1).value(8).build());
        TILE_MAP.put("K", Tile.builder().letter("K").count(1).value(5).build());
        TILE_MAP.put("L", Tile.builder().letter("L").count(4).value(1).build());
        TILE_MAP.put("M", Tile.builder().letter("M").count(2).value(3).build());
        TILE_MAP.put("N", Tile.builder().letter("N").count(6).value(1).build());
        TILE_MAP.put("O", Tile.builder().letter("O").count(8).value(1).build());
        TILE_MAP.put("P", Tile.builder().letter("P").count(2).value(3).build());
        TILE_MAP.put("Q", Tile.builder().letter("Q").count(1).value(10).build());
        TILE_MAP.put("R", Tile.builder().letter("R").count(6).value(1).build());
        TILE_MAP.put("S", Tile.builder().letter("S").count(4).value(1).build());
        TILE_MAP.put("T", Tile.builder().letter("T").count(6).value(1).build());
        TILE_MAP.put("U", Tile.builder().letter("U").count(4).value(1).build());
        TILE_MAP.put("V", Tile.builder().letter("V").count(2).value(4).build());
        TILE_MAP.put("W", Tile.builder().letter("W").count(2).value(4).build());
        TILE_MAP.put("X", Tile.builder().letter("X").count(1).value(8).build());
        TILE_MAP.put("Y", Tile.builder().letter("Y").count(2).value(4).build());
        TILE_MAP.put("Z", Tile.builder().letter("Z").count(1).value(10).build());
    }

    @Test
    void test_create_game() throws IOException {
        final GameDto game = createNewGame(2);

        assertThat(game.getName(), equalTo("My game"));
        assertThat(game.getId(), notNullValue());

        final List<PlayerDto> players = getPlayers(game.getId(), game.getVersion());

        assertThat(players.size(), equalTo(1));
        assertThat(players.get(0).getUsername(), equalTo("user"));
    }

    @Test
    void test_get_game() throws IOException {
        final GameDto game = createNewGame(2);

        final GameDto createdGame = getGame(game.getId());

        assertThat(createdGame.getName(), equalTo("My game"));
        assertThat(createdGame.getId(), notNullValue());
    }

    @Test
    void test_search_own_game() throws IOException {
        createNewGame(2);

        final Response response = target("/games").queryParam("userId", 1)
                .queryParam("includeUser", true)
                .request()
                .get();

        if (Status.OK.getStatusCode() != response.getStatus()) {
            assertThat(response.readEntity(String.class), response.getStatus(), equalTo(Status.OK.getStatusCode()));
        }

        final List<GameDto> games = response.readEntity(new GenericType<List<GameDto>>() {
        });

        response.close();

        assertThat(games, notNullValue());
        assertThat(games.size(), not(equalTo(0)));
        assertThat(games.stream().allMatch(game -> game.getOwnerId().equals(1L)), equalTo(true));
    }

    @Test
    void test_search_another_player_game() throws IOException {
        createNewGame(2);

        final Response response = target("/games").queryParam("userId", 2).request().get();

        if (Status.OK.getStatusCode() != response.getStatus()) {
            assertThat(response.readEntity(String.class), response.getStatus(), equalTo(Status.OK.getStatusCode()));
        }

        final List<GameDto> games = response.readEntity(new GenericType<List<GameDto>>() {
        });

        response.close();

        assertThat(games, notNullValue());
        assertThat(games.size(), not(equalTo(0)));
        assertThat(games.stream().noneMatch(game -> game.getOwnerId().equals(2L)), equalTo(true));
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
            assertThat(updateGameResponse.readEntity(String.class), updateGameResponse.getStatus(),
                    equalTo(Status.OK.getStatusCode()));
        }

        assertThat(updateGameResponse.getHeaderString(HttpHeaders.ETAG), notNullValue());

        final GameDto updateGame = updateGameResponse.readEntity(GameDto.class);

        assertThat(updateGame.getName(), equalTo("My updated game"));

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

        assertThat(updateGameResponse.readEntity(String.class), equalTo("If-Match header is missing"));

        updateGameResponse.close();
        response.close();
    }

    @Test
    void test_join_game() throws IOException {
        final GameDto game = createNewGame(3);

        final GameDto joinedGame = joinGame(game.getId(), 2L);

        assertThat(joinedGame.getVersion(), equalTo(2));

        final List<PlayerDto> players = getPlayers(game.getId(), joinedGame.getVersion());

        assertThat(players.size(), equalTo(2));
        assertThat(players.get(1).getUsername(), equalTo("admin"));
    }

    @Test
    void test_leave_game() throws IOException {
        final GameDto game = createNewGame(3);

        joinGame(game.getId(), 2L);

        final Response leaveGameResponse = target("/games/" + game.getId() + "/users/2").request().delete();

        if (Status.OK.getStatusCode() != leaveGameResponse.getStatus()) {
            assertThat(leaveGameResponse.readEntity(String.class), leaveGameResponse.getStatus(),
                    equalTo(Status.OK.getStatusCode()));
        }

        final GameDto leftGame = leaveGameResponse.readEntity(GameDto.class);

        assertThat(leftGame.getVersion(), equalTo(3));

        final List<PlayerDto> players = getPlayers(game.getId(), leftGame.getVersion());

        assertThat(players.size(), equalTo(1));
        assertThat(players.get(0).getUsername(), equalTo("user"));

        leaveGameResponse.close();
    }

    @Test
    void test_start_game() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2);

        final GameDto joinedGame = joinGame(game.getId(), 2L);

        assertThat(joinedGame.getVersion(), equalTo(2));
        assertThat(joinedGame.getStatus(), equalTo(GameStatus.READY_TO_START.name()));

        waitUntilGameStarts();

        final GameDto startedGame = getGame(game.getId());

        assertThat(startedGame.getVersion(), equalTo(3));
        assertThat(startedGame.getCurrentPlayerNumber(), equalTo(1));
        assertThat(startedGame.getStatus(), equalTo(GameStatus.IN_PROGRESS.name()));

        final VirtualBoardDto virtualBoard = getVirtualBoard(game.getId(), startedGame.getVersion(),
                game.getExpectedPlayerCount());

        assertThat(virtualBoard, notNullValue());
        assertThat(virtualBoard.getCells().size(), equalTo(225));

        virtualBoard.getCells().stream().forEach(cell -> {
            assertThat(cell.getRoundNumber(), nullValue());
        });

        final VirtualRackDto rack = getVirtualRack(game.getId(), game.getOwnerId(), startedGame.getRoundNumber());

        assertThat(rack, notNullValue());
        assertThat(rack.getTiles().stream().allMatch(tile -> tile.getRoundNumber().equals(1)), equalTo(true));
        assertThat(rack.getTiles().stream().noneMatch(VirtualTileDto::isSealed), equalTo(true));
    }

    @Test
    void test_play_two_rounds() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2);

        joinGame(game.getId(), 2L);

        waitUntilGameStarts();

        final GameDto startedGame = getGame(game.getId());

        // round 1 player 1

        VirtualRackDto rack = updateRack(game.getId(), game.getOwnerId(), startedGame.getCurrentPlayerNumber(),
                startedGame.getRoundNumber(), "WEAKOLE".toCharArray());

        // WEAK
        setRackTile(rack, 1, 8, 7, true);
        setRackTile(rack, 2, 8, 8, true);
        setRackTile(rack, 3, 8, 9, true);
        setRackTile(rack, 4, 8, 10, true);

        GameDto playedGame = playWord(game.getId(), game.getOwnerId(), rack);

        assertThat(playedGame, notNullValue());
        assertThat(playedGame.getVersion(), equalTo(4));
        assertThat(playedGame.getCurrentPlayerNumber(), equalTo(2));

        List<PlayerDto> players = getPlayers(game.getId(), playedGame.getVersion());

        assertThat(players.get(0).getScore(), equalTo(11));
        assertThat(players.get(1).getScore(), equalTo(0));

        VirtualRackDto updatedRack = getVirtualRack(game.getId(), game.getOwnerId(), playedGame.getRoundNumber());

        assertThat(updatedRack.getTiles().stream().allMatch(tile -> tile.getRoundNumber().equals(1)), equalTo(true));
        assertThat(updatedRack.getTiles().subList(0, 4).stream().allMatch(VirtualTileDto::isSealed), equalTo(true));
        assertThat(updatedRack.getTiles().subList(4, 7).stream().noneMatch(VirtualTileDto::isSealed), equalTo(true));

        VirtualBoardDto board = getVirtualBoard(game.getId(), playedGame.getVersion(), game.getExpectedPlayerCount());

        assertThat(board.getCells()
                .subList(111, 115)
                .stream()
                .map(VirtualCellDto::getLetter)
                .collect(Collectors.joining()), equalTo("WEAK"));
        assertThat(board.getCells().subList(111, 115).stream().allMatch(cell -> cell.getRoundNumber().equals(1)),
                equalTo(true));
        assertThat(board.getCells().subList(111, 115).stream().allMatch(VirtualCellDto::isLastPlayed), equalTo(true));

        VirtualRackDto refreshedRack = getVirtualRack(game.getId(), game.getOwnerId(), playedGame.getRoundNumber() + 1);

        assertThat(refreshedRack.getTiles().subList(0, 4).stream().allMatch(tile -> tile.getRoundNumber().equals(2)),
                equalTo(true));
        assertThat(refreshedRack.getTiles().subList(4, 7).stream().allMatch(tile -> tile.getRoundNumber().equals(1)),
                equalTo(true));
        assertThat(refreshedRack.getTiles().stream().noneMatch(VirtualTileDto::isSealed), equalTo(true));

        // round 1 player 2

        rack = updateRack(game.getId(), 2L, playedGame.getCurrentPlayerNumber(), playedGame.getRoundNumber(),
                "RALYERE".toCharArray());

        // RA(W)LY
        setRackTile(rack, 1, 6, 7, true);
        setRackTile(rack, 2, 7, 7, true);
        setRackTile(rack, 3, 9, 7, true);
        setRackTile(rack, 4, 10, 7, true);

        playedGame = playWord(game.getId(), 2L, rack);

        assertThat(playedGame, notNullValue());
        assertThat(playedGame.getVersion(), equalTo(5));
        assertThat(playedGame.getCurrentPlayerNumber(), equalTo(1));

        players = getPlayers(game.getId(), playedGame.getVersion());

        assertThat(players.get(0).getScore(), equalTo(11));
        assertThat(players.get(1).getScore(), equalTo(13));

        updatedRack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber() - 1);

        assertThat(updatedRack.getTiles().stream().allMatch(tile -> tile.getRoundNumber().equals(1)), equalTo(true));
        assertThat(updatedRack.getTiles().subList(0, 4).stream().allMatch(VirtualTileDto::isSealed), equalTo(true));
        assertThat(updatedRack.getTiles().subList(4, 7).stream().noneMatch(VirtualTileDto::isSealed), equalTo(true));

        board = getVirtualBoard(game.getId(), playedGame.getVersion(), game.getExpectedPlayerCount());

        List<VirtualCellDto> verticalCells = board.getCells()
                .stream()
                .filter(cell -> cell.getColumnNumber().equals(7) && cell.getLetter() != null)
                .collect(Collectors.toList());

        assertThat(verticalCells.stream().map(VirtualCellDto::getLetter).collect(Collectors.joining()),
                equalTo("RAWLY"));
        assertThat(verticalCells.stream().allMatch(tile -> tile.getRoundNumber().equals(1)), equalTo(true));
        assertThat(verticalCells.stream().allMatch(VirtualCellDto::isLastPlayed), equalTo(true));
        assertThat(board.getCells().subList(112, 115).stream().noneMatch(VirtualCellDto::isLastPlayed), equalTo(true));

        refreshedRack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber());

        assertThat(refreshedRack.getTiles().subList(0, 4).stream().allMatch(tile -> tile.getRoundNumber().equals(2)),
                equalTo(true));
        assertThat(refreshedRack.getTiles().subList(4, 7).stream().allMatch(tile -> tile.getRoundNumber().equals(1)),
                equalTo(true));

        assertThat(refreshedRack.getTiles().stream().noneMatch(VirtualTileDto::isSealed), equalTo(true));

        // round 2 player 1

        rack = getVirtualRack(game.getId(), game.getOwnerId(), playedGame.getRoundNumber());

        // (R)OLE
        setRackTile(rack, 5, 6, 8, true);
        setRackTile(rack, 6, 6, 9, true);
        setRackTile(rack, 7, 6, 10, true);

        playedGame = playWord(game.getId(), game.getOwnerId(), rack);

        assertThat(playedGame, notNullValue());
        assertThat(playedGame.getVersion(), equalTo(6));
        assertThat(playedGame.getCurrentPlayerNumber(), equalTo(2));

        players = getPlayers(game.getId(), playedGame.getVersion());

        assertThat(players.get(0).getScore(), equalTo(17));
        assertThat(players.get(1).getScore(), equalTo(13));

        updatedRack = getVirtualRack(game.getId(), game.getOwnerId(), playedGame.getRoundNumber());

        board = getVirtualBoard(game.getId(), playedGame.getVersion(), game.getExpectedPlayerCount());

        assertThat(
                board.getCells().subList(81, 85).stream().map(VirtualCellDto::getLetter).collect(Collectors.joining()),
                equalTo("ROLE"));
        assertThat(board.getCells().subList(81, 82).stream().allMatch(cell -> cell.getRoundNumber().equals(1)),
                equalTo(true));
        assertThat(board.getCells().subList(82, 85).stream().allMatch(cell -> cell.getRoundNumber().equals(2)),
                equalTo(true));
        assertThat(board.getCells().subList(81, 85).stream().allMatch(VirtualCellDto::isLastPlayed), equalTo(true));

        verticalCells = board.getCells()
                .stream()
                .filter(cell -> cell.getColumnNumber().equals(7) && cell.getLetter() != null)
                .collect(Collectors.toList());

        // R OF RAWLY
        assertThat(verticalCells.subList(0, 1).stream().allMatch(VirtualCellDto::isLastPlayed), equalTo(true));

        // AWLY OF RAWLY
        assertThat(verticalCells.subList(1, 5).stream().noneMatch(VirtualCellDto::isLastPlayed), equalTo(true));

        refreshedRack = getVirtualRack(game.getId(), game.getOwnerId(), playedGame.getRoundNumber() + 1);

        assertThat(refreshedRack.getTiles().subList(0, 4).stream().allMatch(tile -> tile.getRoundNumber().equals(2)),
                equalTo(true));
        assertThat(refreshedRack.getTiles().subList(4, 7).stream().allMatch(tile -> tile.getRoundNumber().equals(3)),
                equalTo(true));

        assertThat(refreshedRack.getTiles().stream().noneMatch(VirtualTileDto::isSealed), equalTo(true));

        // round 2 player 2

        rack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber());

        // (WEAK)ER
        setRackTile(rack, 5, 8, 11, true);
        setRackTile(rack, 6, 8, 12, true);

        playedGame = playWord(game.getId(), 2L, rack);

        assertThat(playedGame, notNullValue());
        assertThat(playedGame.getVersion(), equalTo(7));
        assertThat(playedGame.getCurrentPlayerNumber(), equalTo(1));

        players = getPlayers(game.getId(), playedGame.getVersion());

        assertThat(players.get(0).getScore(), equalTo(17));
        assertThat(players.get(1).getScore(), equalTo(27));

        updatedRack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber() - 1);

        board = getVirtualBoard(game.getId(), playedGame.getVersion(), game.getExpectedPlayerCount());

        assertThat(board.getCells()
                .subList(111, 117)
                .stream()
                .map(VirtualCellDto::getLetter)
                .collect(Collectors.joining()), equalTo("WEAKER"));
        assertThat(board.getCells().subList(111, 115).stream().allMatch(cell -> cell.getRoundNumber().equals(1)),
                equalTo(true));
        assertThat(board.getCells().subList(115, 117).stream().allMatch(cell -> cell.getRoundNumber().equals(2)),
                equalTo(true));
        assertThat(board.getCells().subList(111, 117).stream().allMatch(VirtualCellDto::isLastPlayed), equalTo(true));
        assertThat(board.getCells().subList(81, 85).stream().noneMatch(VirtualCellDto::isLastPlayed), equalTo(true));

        refreshedRack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber());

        assertThat(refreshedRack.getTiles().subList(0, 4).stream().allMatch(tile -> tile.getRoundNumber().equals(2)),
                equalTo(true));
        assertThat(refreshedRack.getTiles().subList(4, 6).stream().allMatch(tile -> tile.getRoundNumber().equals(3)),
                equalTo(true));
        assertThat(refreshedRack.getTiles().subList(6, 7).stream().allMatch(tile -> tile.getRoundNumber().equals(1)),
                equalTo(true));

        assertThat(refreshedRack.getTiles().stream().noneMatch(VirtualTileDto::isSealed), equalTo(true));

        sendChat(game.getId(), game.getOwnerId(), "A new message");

        // validate messages

        final List<ChatDto> chats = getChats(game.getId());

        assertThat(chats, notNullValue());
        assertThat(chats.size(), equalTo(1));
        assertThat(chats.get(0).getMessage(), equalTo("A new message"));

        // validate words

        final List<WordDto> words = getWords(game.getId());

        assertThat(words, notNullValue());
        assertThat(words.size(), equalTo(4));
    }

    @Test
    void test_play_player_is_rewarded_with_bingo_bonus() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2);

        joinGame(game.getId(), 2L);

        waitUntilGameStarts();

        final GameDto startedGame = getGame(game.getId());

        // round 1 player 1

        final VirtualRackDto rack = updateRack(game.getId(), game.getOwnerId(), startedGame.getCurrentPlayerNumber(),
                startedGame.getRoundNumber(), "FARADAY".toCharArray());

        // FARADAY
        setRackTile(rack, 1, 8, 7, true);
        setRackTile(rack, 2, 8, 8, true);
        setRackTile(rack, 3, 8, 9, true);
        setRackTile(rack, 4, 8, 10, true);
        setRackTile(rack, 5, 8, 11, true);
        setRackTile(rack, 6, 8, 12, true);
        setRackTile(rack, 7, 8, 13, true);

        final GameDto playedGame = playWord(game.getId(), game.getOwnerId(), rack);

        assertThat(playedGame, notNullValue());
        assertThat(playedGame.getVersion(), equalTo(4));
        assertThat(playedGame.getCurrentPlayerNumber(), equalTo(2));

        final List<PlayerDto> players = getPlayers(game.getId(), playedGame.getVersion());

        assertThat(players.get(0).getScore(), equalTo(65));
        assertThat(players.get(1).getScore(), equalTo(0));

        final VirtualRackDto updatedRack = getVirtualRack(game.getId(), game.getOwnerId(), playedGame.getRoundNumber());

        assertThat(updatedRack.getTiles().stream().allMatch(tile -> tile.getRoundNumber().equals(1)), equalTo(true));
        assertThat(updatedRack.getTiles().subList(0, 7).stream().allMatch(VirtualTileDto::isSealed), equalTo(true));

        final VirtualBoardDto board = getVirtualBoard(game.getId(), playedGame.getVersion(),
                game.getExpectedPlayerCount());

        assertThat(board.getCells()
                .subList(111, 118)
                .stream()
                .map(VirtualCellDto::getLetter)
                .collect(Collectors.joining()), equalTo("FARADAY"));
        assertThat(board.getCells().subList(111, 118).stream().allMatch(cell -> cell.getRoundNumber().equals(1)),
                equalTo(true));
        assertThat(board.getCells().subList(111, 118).stream().allMatch(VirtualCellDto::isLastPlayed), equalTo(true));

        final VirtualRackDto refreshedRack = getVirtualRack(game.getId(), game.getOwnerId(),
                playedGame.getRoundNumber() + 1);

        assertThat(refreshedRack.getTiles().subList(0, 7).stream().allMatch(tile -> tile.getRoundNumber().equals(2)),
                equalTo(true));
        assertThat(refreshedRack.getTiles().stream().noneMatch(VirtualTileDto::isSealed), equalTo(true));

        final List<ActionDto> actions = getActions(game.getId());

        assertThat(actions.stream().anyMatch(action -> ActionType.BONUS_BINGO.name().equals(action.getType())),
                equalTo(true));
    }

    @Test
    void test_skip_round() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2);

        joinGame(game.getId(), 2L);

        waitUntilGameStarts();

        final GameDto startedGame = getGame(game.getId());

        final VirtualRackDto rack = getVirtualRack(game.getId(), game.getOwnerId(), startedGame.getRoundNumber());

        final GameDto skippedGame = playWord(game.getId(), game.getOwnerId(), rack);

        assertThat(skippedGame, notNullValue());
        assertThat(skippedGame.getVersion(), equalTo(4));
        assertThat(skippedGame.getCurrentPlayerNumber(), equalTo(2));

        final List<ActionDto> actions = getActions(game.getId());

        assertThat(actions.stream().anyMatch(action -> ActionType.SKIP.name().equals(action.getType())), equalTo(true));
    }

    @Test
    void test_exchange_letters() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2);

        joinGame(game.getId(), 2L);

        waitUntilGameStarts();

        final GameDto startedGame = getGame(game.getId());

        final VirtualRackDto rack = getVirtualRack(game.getId(), 1L, startedGame.getRoundNumber());
        rack.getTiles().forEach(tile -> {
            tile.setLetter("C");
            tile.setValue(TILE_MAP.get("C").getValue());
        });

        redisTemplate.boundListOps(Constants.CacheKey.RACK + ":" + game.getId() + ":" + 1L)
                .set(0, Mapper.toEntity(rack));

        rack.getTiles().forEach(tile -> tile.setExchanged(true));

        final GameDto exchangedGame = playWord(game.getId(), game.getOwnerId(), rack);

        assertThat(exchangedGame, notNullValue());
        assertThat(exchangedGame.getVersion(), equalTo(4));
        assertThat(exchangedGame.getCurrentPlayerNumber(), equalTo(2));

        final List<ActionDto> actions = getActions(game.getId());

        assertThat(actions.stream().anyMatch(action -> ActionType.EXCHANGE.name().equals(action.getType())),
                equalTo(true));

        final VirtualRackDto updatedRack = getVirtualRack(game.getId(), 1L, exchangedGame.getRoundNumber());

        assertThat(updatedRack, notNullValue());
        assertThat(updatedRack.getTiles().stream().filter(tile -> tile.getLetter().equals("C")).count(), equalTo(7L));
        assertThat(updatedRack.getTiles().stream().allMatch(VirtualTileDto::isExchanged), equalTo(true));

        final VirtualRackDto nextRoundRack = getVirtualRack(game.getId(), 1L, exchangedGame.getRoundNumber() + 1);

        assertThat(nextRoundRack, notNullValue());
        assertThat(nextRoundRack.getTiles().stream().filter(tile -> tile.getLetter().equals("C")).count(),
                not(equalTo(7L)));
    }

    @Test
    void test_game_ends_after_last_round() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2, Language.fr);

        joinGame(game.getId(), 2L);

        waitUntilGameStarts();

        final GameDto startedGame = getGame(game.getId());

        // round 1 player 1

        updateRack(game.getId(), game.getOwnerId(), startedGame.getCurrentPlayerNumber(), startedGame.getRoundNumber(),
                "FARADAY".toCharArray());

        VirtualRackDto rack = getVirtualRack(game.getId(), 1L, startedGame.getRoundNumber());

        setRackTile(rack, 1, 8, 7, true);
        setRackTile(rack, 2, 8, 8, true);
        setRackTile(rack, 3, 8, 9, true);
        setRackTile(rack, 4, 8, 10, true);
        setRackTile(rack, 5, 8, 11, true);
        setRackTile(rack, 6, 8, 12, true);
        setRackTile(rack, 7, 8, 13, true);

        GameDto playedGame = playWord(game.getId(), 1L, rack);

        assertThat(playedGame, notNullValue());
        assertThat(playedGame.getVersion(), equalTo(4));
        assertThat(playedGame.getCurrentPlayerNumber(), equalTo(2));
        assertThat(playedGame.getStatus(), equalTo(GameStatus.IN_PROGRESS.name()));

        VirtualRackDto refreshedVirtualRack = getVirtualRack(game.getId(), game.getOwnerId(),
                playedGame.getRoundNumber() + 1);

        assertThat(refreshedVirtualRack.getTiles().size(), equalTo(1));

        // round 1 player 2

        updateRack(game.getId(), 2L, playedGame.getCurrentPlayerNumber(), playedGame.getRoundNumber(),
                "ARADAYW".toCharArray());

        rack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber());

        setRackTile(rack, 1, 9, 7, true);
        setRackTile(rack, 2, 10, 7, true);
        setRackTile(rack, 3, 11, 7, true);
        setRackTile(rack, 4, 12, 7, true);
        setRackTile(rack, 5, 13, 7, true);
        setRackTile(rack, 6, 14, 7, true);

        playedGame = playWord(game.getId(), 2L, rack);

        assertThat(playedGame, notNullValue());
        assertThat(playedGame.getVersion(), equalTo(5));
        assertThat(playedGame.getCurrentPlayerNumber(), equalTo(1));
        assertThat(playedGame.getStatus(), equalTo(GameStatus.IN_PROGRESS.name()));

        refreshedVirtualRack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber() + 1);

        assertThat(refreshedVirtualRack, nullValue());

        // round 2 player 1

        rack = getVirtualRack(game.getId(), 1L, playedGame.getRoundNumber());

        setRackTile(rack, 1, 12, 8, true);

        playedGame = playWord(game.getId(), 1L, rack);

        assertThat(playedGame, notNullValue());
        assertThat(playedGame.getVersion(), equalTo(6));
        assertThat(playedGame.getCurrentPlayerNumber(), equalTo(2));
        assertThat(playedGame.getStatus(), equalTo(GameStatus.READY_TO_END.name()));

        waitUntilGameEnds();

        final GameDto endedGame = getGame(game.getId());

        assertThat(endedGame, notNullValue());
        assertThat(endedGame.getVersion(), equalTo(7));
        assertThat(endedGame.getCurrentPlayerNumber(), equalTo(1));
        assertThat(endedGame.getStatus(), equalTo(GameStatus.ENDED.name()));
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

        assertThat(skippedGame.getVersion(), equalTo(startedGame.getVersion() + 1));
    }

    @Test
    void test_skip_turn_job_is_triggered_while_playing() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2);

        joinGame(game.getId(), 2L);

        waitUntilGameStarts();

        final GameDto startedGame = getGame(game.getId());

        // round 1 player 1

        final VirtualRackDto rack = updateRack(game.getId(), game.getOwnerId(), startedGame.getCurrentPlayerNumber(),
                startedGame.getRoundNumber(), "WEAKOLE".toCharArray());

        setRackTile(rack, 1, 8, 7, true);
        setRackTile(rack, 2, 8, 8, true);
        setRackTile(rack, 3, 8, 9, true);
        setRackTile(rack, 4, 8, 10, true);

        Thread.sleep(3850);

        final GameDto playedGame = playWord(game.getId(), game.getOwnerId(), rack);

        assertThat(playedGame, notNullValue());
        assertThat(playedGame.getVersion(), equalTo(4));
        assertThat(playedGame.getCurrentPlayerNumber(), equalTo(2));

        final List<PlayerDto> players = getPlayers(game.getId(), playedGame.getVersion());

        assertThat(players.get(0).getScore(), equalTo(11));
        assertThat(players.get(1).getScore(), equalTo(0));

        final VirtualBoardDto board = getVirtualBoard(game.getId(), playedGame.getVersion(),
                game.getExpectedPlayerCount());

        assertThat(board.getCells()
                .subList(111, 115)
                .stream()
                .map(VirtualCellDto::getLetter)
                .collect(Collectors.joining()), equalTo("WEAK"));

        final VirtualRackDto refreshedVirtualRack = getVirtualRack(game.getId(), game.getOwnerId(),
                playedGame.getRoundNumber() + 1);

        assertThat(refreshedVirtualRack.getTiles().size(), equalTo(7));
    }

    @Test
    @SuppressWarnings("unused")
    void test_word_is_played_while_skip_turn_job_is_running() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2);

        joinGame(game.getId(), 2L);

        waitUntilGameStarts();

        final GameDto startedGame = getGame(game.getId());

        // round 1 player 1

        final VirtualRackDto rack = updateRack(game.getId(), game.getOwnerId(), startedGame.getCurrentPlayerNumber(),
                startedGame.getRoundNumber(), "WEAKOLE".toCharArray());

        setRackTile(rack, 1, 8, 7, true);
        setRackTile(rack, 2, 8, 8, true);
        setRackTile(rack, 3, 8, 9, true);
        setRackTile(rack, 4, 8, 10, true);

        Thread.sleep(4100);

        try {
            playWord(game.getId(), game.getOwnerId(), rack);

            throw new IllegalStateException("Concurrent execution of play service wasn't stopped");
        } catch (Exception e) {
            // nothing to do here
        }

        final GameDto updatedGame = getGame(game.getId());

        assertThat(updatedGame, notNullValue());
        assertThat(updatedGame.getVersion(), equalTo(4));
        assertThat(updatedGame.getCurrentPlayerNumber(), equalTo(2));

        final List<PlayerDto> players = getPlayers(game.getId(), updatedGame.getVersion());

        assertThat(players.get(0).getScore(), equalTo(0));
        assertThat(players.get(1).getScore(), equalTo(0));

        final VirtualBoardDto virtualBoard = getVirtualBoard(game.getId(), updatedGame.getVersion(),
                game.getExpectedPlayerCount());

        boolean hasNonEmptyCells = virtualBoard.getCells().stream().anyMatch(VirtualCellDto::isSealed);

        assertThat(hasNonEmptyCells, equalTo(false));

        final VirtualRackDto refreshedVirtualRack = getVirtualRack(game.getId(), game.getOwnerId(),
                updatedGame.getRoundNumber() + 1);

        assertThat(refreshedVirtualRack.getTiles().size(), equalTo(7));
    }

    @Test
    void test_skip_2_rounds_in_a_row_by_the_players_ends_the_game() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2);

        joinGame(game.getId(), 2L);

        waitUntilGameStarts();

        final GameDto startedGame = getGame(game.getId());

        VirtualRackDto rack = getVirtualRack(game.getId(), 1L, startedGame.getRoundNumber());

        GameDto playedGame = playWord(game.getId(), 1L, rack);

        rack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber());

        playedGame = playWord(game.getId(), 2L, rack);

        rack = getVirtualRack(game.getId(), 1L, playedGame.getRoundNumber());

        playedGame = playWord(game.getId(), 1L, rack);

        rack = getVirtualRack(game.getId(), 2L, playedGame.getRoundNumber());

        playedGame = playWord(game.getId(), 2L, rack);

        // wait until the end game job ends
        Thread.sleep(2000);

        final GameDto endedGame = getGame(game.getId());

        assertThat(endedGame, notNullValue());
        assertThat(endedGame.getVersion(), equalTo(8));
        assertThat(endedGame.getStatus(), equalTo(GameStatus.ENDED.name()));
    }

    @Test
    void test_skip_2_rounds_in_a_row_by_the_job_ends_the_game() throws IOException, InterruptedException {
        final GameDto game = createNewGame(2);

        joinGame(game.getId(), 2L);

        waitUntilGameStarts();

        // Wait more than 4 turns durations
        Thread.sleep(25000);

        final GameDto endedGame = getGame(game.getId());

        assertThat(endedGame, notNullValue());
        assertThat(endedGame.getVersion(), equalTo(8));
        assertThat(endedGame.getStatus(), equalTo(GameStatus.ENDED.name()));

        final List<ActionDto> actions = getActions(game.getId());

        assertThat(actions.stream().filter(action -> ActionType.TIMEOUT.name().equals(action.getType())).count(),
                equalTo(4L));
    }

    private GameDto getGame(Long gameId) {
        final Response response = target("/games/" + gameId).request().get();

        if (Status.OK.getStatusCode() != response.getStatus()) {
            assertThat(response.readEntity(String.class), response.getStatus(), equalTo(Status.OK.getStatusCode()));
        }

        assertThat(response.getHeaderString(HttpHeaders.ETAG), notNullValue());

        final GameDto game = response.readEntity(GameDto.class);

        response.close();

        return game;
    }

    private GameDto createNewGame(Integer playerCount) throws IOException {
        return createNewGame(playerCount, Language.en);
    }

    private GameDto createNewGame(Integer playerCount, Language language) throws IOException {
        final URL resource = GameResourceIT.class.getResource("/json/game.json");

        final GameDto game = JsonUtils.toDto(Resources.toString(resource, StandardCharsets.UTF_8), GameDto.class);
        game.setExpectedPlayerCount(playerCount);
        game.setLanguage(language.name());

        final Response gameResponse = target("/games").request().put(Entity.entity(game, MediaType.APPLICATION_JSON));

        if (Status.OK.getStatusCode() != gameResponse.getStatus()) {
            assertThat(gameResponse.readEntity(String.class), gameResponse.getStatus(),
                    equalTo(Status.OK.getStatusCode()));
        }

        assertThat(gameResponse.getHeaderString(HttpHeaders.ETAG), notNullValue());

        final GameDto responseDto = gameResponse.readEntity(GameDto.class);
        gameResponse.close();

        return responseDto;
    }

    private GameDto joinGame(Long gameId, Long userId) {
        final Response joinGameResponse = target("/games/" + gameId + "/users/" + userId).request()
                .put(Entity.entity("", MediaType.APPLICATION_JSON));

        if (Status.OK.getStatusCode() != joinGameResponse.getStatus()) {
            assertThat(joinGameResponse.readEntity(String.class), joinGameResponse.getStatus(),
                    equalTo(Status.OK.getStatusCode()));
        }

        final GameDto joinGameDto = joinGameResponse.readEntity(GameDto.class);

        joinGameResponse.close();

        return joinGameDto;
    }

    private GameDto playWord(Long gameId, Long userId, VirtualRackDto virtualRack) {
        final Response playedGameResponse = target("/games/" + gameId + "/users/" + userId + "/rack").request()
                .post(Entity.entity(virtualRack, MediaType.APPLICATION_JSON));

        final GameDto playedGame = playedGameResponse.readEntity(GameDto.class);

        playedGameResponse.close();

        return playedGame;
    }

    private VirtualRackDto updateRack(Long gameId, Long userId, Integer playerNumber, Integer roundNumber,
                                      char[] letters) {
        final VirtualRackDto virtualRack = getVirtualRack(gameId, userId, roundNumber);

        List<VirtualTileDto> tiles = new ArrayList<>();

        IntStream.range(1, 8).forEach(tileNumber -> {
            final Character letter = letters[tileNumber - 1];
            tiles.add(VirtualTileDto.builder()
                    .letter(letter.toString())
                    .value(TILE_MAP.get(letter.toString()).getValue())
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

    void setRackTile(final VirtualRackDto virtualRack, final int tileNumber, final int rowNumber,
                     final int columnnNumber, final boolean sealed) {
        virtualRack.getTiles().get(tileNumber - 1).setRowNumber(rowNumber);
        virtualRack.getTiles().get(tileNumber - 1).setColumnNumber(columnnNumber);
        virtualRack.getTiles().get(tileNumber - 1).setSealed(sealed);
    }

    private List<PlayerDto> getPlayers(Long gameId, Integer version) {
        final Response playersResponse = target("/games/" + gameId + "/players").queryParam("version", version)
                .request()
                .get();

        if (Status.OK.getStatusCode() != playersResponse.getStatus()) {
            assertThat(playersResponse.readEntity(String.class), playersResponse.getStatus(),
                    equalTo(Status.OK.getStatusCode()));
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

    private List<ActionDto> getActions(Long gameId) {
        final Response actionsResponse = target("/games/" + gameId + "/actions").request().get();

        if (Status.OK.getStatusCode() != actionsResponse.getStatus()) {
            assertThat(actionsResponse.readEntity(String.class), actionsResponse.getStatus(),
                    equalTo(Status.OK.getStatusCode()));
        }

        final List<ActionDto> actions = actionsResponse.readEntity(new GenericType<List<ActionDto>>() {
        });

        actionsResponse.close();

        return actions;
    }

    private ChatDto sendChat(Long gameId, Long userId, String message) {
        final ChatDto chat = ChatDto.builder().gameId(gameId).userId(userId).message(message).build();

        final Response sendChatResponse = target("/games/" + gameId + "/chats").request()
                .put(Entity.entity(chat, MediaType.APPLICATION_JSON));

        final ChatDto savedChat = sendChatResponse.readEntity(ChatDto.class);

        sendChatResponse.close();

        return savedChat;
    }

    private List<WordDto> getWords(Long gameId) {
        final Response wordsResponse = target("/games/" + gameId + "/words").request().get();

        if (Status.OK.getStatusCode() != wordsResponse.getStatus()) {
            assertThat(wordsResponse.readEntity(String.class), wordsResponse.getStatus(),
                    equalTo(Status.OK.getStatusCode()));
        }

        final List<WordDto> words = wordsResponse.readEntity(new GenericType<List<WordDto>>() {
        });

        wordsResponse.close();

        return words;
    }

    private List<ChatDto> getChats(Long gameId) {
        final Response chatsResponse = target("/games/" + gameId + "/chats").request().get();

        if (Status.OK.getStatusCode() != chatsResponse.getStatus()) {
            assertThat(chatsResponse.readEntity(String.class), chatsResponse.getStatus(),
                    equalTo(Status.OK.getStatusCode()));
        }

        final List<ChatDto> chats = chatsResponse.readEntity(new GenericType<List<ChatDto>>() {
        });

        chatsResponse.close();

        return chats;
    }

    private void waitUntilGameStarts() throws InterruptedException {
        Thread.sleep(1000);
    }

    private void waitUntilGameEnds() throws InterruptedException {
        Thread.sleep(1000);
    }

}
