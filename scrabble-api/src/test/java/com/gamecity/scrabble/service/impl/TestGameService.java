package com.gamecity.scrabble.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.dao.GameDao;
import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.ActionType;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.entity.Tile;
import com.gamecity.scrabble.entity.GameStatus;
import com.gamecity.scrabble.entity.Word;
import com.gamecity.scrabble.entity.User;
import com.gamecity.scrabble.model.DictionaryWord;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.VirtualCell;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.model.VirtualTile;
import com.gamecity.scrabble.service.ActionService;
import com.gamecity.scrabble.service.DictionaryService;
import com.gamecity.scrabble.service.PlayerService;
import com.gamecity.scrabble.service.GameService;
import com.gamecity.scrabble.service.ContentService;
import com.gamecity.scrabble.service.VirtualRackService;
import com.gamecity.scrabble.service.UserService;
import com.gamecity.scrabble.service.VirtualBagService;
import com.gamecity.scrabble.service.VirtualBoardService;
import com.gamecity.scrabble.service.WordService;
import com.gamecity.scrabble.service.exception.GameException;
import com.gamecity.scrabble.service.exception.error.GameError;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;

class TestGameService extends AbstractServiceTest {

    private static final List<Integer> TRIPLE_WORD_CELLS = Arrays.asList(1, 8, 15, 106, 120, 211, 218, 225);
    private static final List<Integer> DOUBLE_WORD_CELLS = Arrays.asList(17, 29, 33, 43, 49, 57, 65, 71, 113, 155, 161,
            169, 177, 183, 193, 197, 209);

    private static final List<Integer> TRIPLE_LETTER_CELLS = Arrays.asList(21, 25, 77, 81, 85, 89, 137, 141, 145, 149,
            201, 205);
    private static final List<Integer> DOUBLE_LETTER_CELLS = Arrays.asList(4, 12, 37, 39, 46, 53, 60, 93, 97, 99, 103,
            109, 117, 123, 127, 129, 133, 166, 173, 180, 187, 189, 214, 222);

    private static final Map<String, Tile> TILE_MAP = new HashMap<>();

    @Mock
    private GameDao gameDao;

    @Mock
    private UserService userService;

    @Mock
    private PlayerService playerService;

    @Mock
    private ContentService contentService;

    @Mock
    private VirtualBoardService virtualBoardService;

    @Mock
    private VirtualRackService virtualRackService;

    @Mock
    private VirtualBagService virtualBagService;

    @Mock
    private WordService wordService;

    @Mock
    private DictionaryService dictionaryService;

    @Mock
    private ActionService actionService;

    @Mock
    private ScoreServiceImpl scoreService;

    @InjectMocks
    private GameService gameService = new GameServiceImpl(userService, playerService, virtualBoardService,
            virtualRackService, virtualBagService, contentService, dictionaryService, wordService, actionService,
            scoreService);

    private List<VirtualTile> tiles;
    private VirtualCell[][] boardMatrix;

    @BeforeEach
    void beforeEach() {
        ((GameServiceImpl) gameService).setBaseDao(gameDao);
        tiles = new ArrayList<>(Constants.Game.RACK_SIZE);
    }

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
    void test_game_not_found() {
        try {
            gameService.get(DEFAULT_GAME_ID);

            fail("Found not existing game");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.NOT_FOUND.getCode()));
        }
    }

    @Test
    void test_get_terminated_game() {
        try {
            when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(Game.builder().status(GameStatus.TERMINATED).build());

            gameService.get(DEFAULT_GAME_ID);

            fail("Found terminated game");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.NOT_FOUND.getCode()));
        }
    }

    @Test
    void test_get_waiting_game() {
        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(Game.builder().status(GameStatus.WAITING).build());

        assertThat(gameService.get(DEFAULT_GAME_ID), notNullValue());
    }

    @Test
    void test_get_ready_game() {
        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(Game.builder().status(GameStatus.READY_TO_START).build());

        assertThat(gameService.get(DEFAULT_GAME_ID), notNullValue());
    }

    @Test
    void test_get_started_game() {
        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(Game.builder().status(GameStatus.IN_PROGRESS).build());

        assertThat(gameService.get(DEFAULT_GAME_ID), notNullValue());
    }

    @Test
    void test_create_game() {
        final User mockUser = mock(User.class);

        final Game sampleGame = Game.builder().ownerId(mockUser.getId()).activePlayerCount(2).build();

        when(userService.get(eq(mockUser.getId()))).thenReturn(mockUser);
        when(gameDao.save(sampleGame)).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        final Game game = gameService.save(sampleGame);

        assertThat(game, notNullValue());
        assertThat(game.getStatus(), equalTo(GameStatus.WAITING));
        assertThat(game.getOwnerId(), equalTo(mockUser.getId()));
        assertThat(game.getVersion(), equalTo(1));
        assertThat(game.getActivePlayerCount(), equalTo(1));

        verify(gameDao, times(1)).save(game);
        verify(playerService, times(1)).add(sampleGame.getId(), sampleGame.getOwnerId(),
                sampleGame.getActivePlayerCount());
        verify(actionService, times(1)).add(game, sampleGame.getOwnerId(), Constants.Game.NO_SCORE, ActionType.CREATE);
    }

    @Test
    void test_join_game() {
        final Long joiningUserId = 2L;

        final Game sampleGame = Game.builder()
                .id(DEFAULT_GAME_ID)
                .activePlayerCount(1)
                .status(GameStatus.WAITING)
                .version(1)
                .build();

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);
        when(gameDao.save(any(Game.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        final Game game = gameService.join(DEFAULT_GAME_ID, joiningUserId);

        assertThat(game.getStatus(), equalTo(GameStatus.WAITING));
        assertThat(game.getVersion(), equalTo(2));
        assertThat(game.getActivePlayerCount(), equalTo(2));

        verify(gameDao, times(1)).save(game);
        verify(playerService, times(1)).add(sampleGame.getId(), joiningUserId, sampleGame.getActivePlayerCount());
        verify(actionService, times(1)).add(game, joiningUserId, Constants.Game.NO_SCORE, ActionType.JOIN);
    }

    @Test
    void test_join_started_game() {
        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(Game.builder().status(GameStatus.IN_PROGRESS).build());

        try {
            gameService.join(DEFAULT_GAME_ID, 2L);

            fail("Joined started game");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.IN_PROGRESS.getCode()));
        }
    }

    @Test
    void test_join_game_with_an_existing_player() {
        final Long joiningUserId = 2L;

        final Game sampleGame = Game.builder().id(DEFAULT_GAME_ID).status(GameStatus.WAITING).build();

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);
        when(playerService.getByUserId(eq(DEFAULT_GAME_ID), eq(joiningUserId))).thenReturn(mock(Player.class));

        try {
            gameService.join(DEFAULT_GAME_ID, joiningUserId);

            fail("The player in the game joined the game");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.IN_THE_GAME.getCode()));
        }
    }

    @Test
    void test_join_game_reaches_expected_player_count() {
        final Game sampleGame = Game.builder()
                .id(DEFAULT_GAME_ID)
                .activePlayerCount(1)
                .expectedPlayerCount(2)
                .status(GameStatus.WAITING)
                .version(1)
                .build();

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);
        when(gameDao.save(any(Game.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        final Game game = gameService.join(DEFAULT_GAME_ID, 2L);

        assertThat(game.getStatus(), equalTo(GameStatus.READY_TO_START));
    }

    @Test
    void test_leave_game() {
        final Long leavingUserId = 2L;

        final Game sampleGame = Game.builder()
                .id(DEFAULT_GAME_ID)
                .ownerId(DEFAULT_USER_ID)
                .activePlayerCount(2)
                .expectedPlayerCount(3)
                .status(GameStatus.WAITING)
                .version(2)
                .build();

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);
        when(gameDao.save(any(Game.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(playerService.getByUserId(eq(DEFAULT_GAME_ID), eq(leavingUserId))).thenReturn(mock(Player.class));

        final Game game = gameService.leave(DEFAULT_GAME_ID, leavingUserId);

        assertThat(game.getStatus(), equalTo(GameStatus.WAITING));
        assertThat(game.getVersion(), equalTo(3));
        assertThat(game.getActivePlayerCount(), equalTo(1));

        verify(gameDao, times(1)).save(game);
        verify(playerService, times(1)).remove(any(Player.class));
        verify(actionService, times(1)).add(game, leavingUserId, Constants.Game.NO_SCORE, ActionType.LEAVE);
    }

    @Test
    void test_leave_started_game() {
        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(Game.builder().status(GameStatus.IN_PROGRESS).build());

        try {
            gameService.leave(DEFAULT_GAME_ID, 2L);

            fail("Player left started game");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.IN_PROGRESS.getCode()));
        }
    }

    @Test
    void test_leave_game_with_not_existing_player() {
        final Game sampleGame = Game.builder().id(DEFAULT_GAME_ID).status(GameStatus.WAITING).build();

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);

        try {
            gameService.leave(DEFAULT_GAME_ID, 2L);

            fail("Player that is not in the game left the game");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.NOT_IN_THE_GAME.getCode()));
        }
    }

    @Test
    void test_leave_game_with_owner() {
        final Game sampleGame = Game.builder()
                .id(DEFAULT_GAME_ID)
                .ownerId(DEFAULT_USER_ID)
                .status(GameStatus.WAITING)
                .build();

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);
        when(playerService.getByUserId(eq(DEFAULT_GAME_ID), eq(1L))).thenReturn(mock(Player.class));

        try {
            gameService.leave(DEFAULT_GAME_ID, 1L);

            fail("Owner left the game");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.OWNER_CANNOT_LEAVE.getCode()));
        }
    }

    @Test
    void test_delete_game() {
        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(Game.builder().ownerId(DEFAULT_USER_ID).version(1).build());
        when(gameDao.save(any())).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        final Game game = gameService.delete(DEFAULT_GAME_ID);

        assertThat(game, notNullValue());
        assertThat(game.getStatus(), equalTo(GameStatus.DELETED));
        assertThat(game.getVersion(), equalTo(2));

        verify(gameDao, times(1)).save(game);
        verify(actionService, times(1)).add(game, DEFAULT_USER_ID, Constants.Game.NO_SCORE, ActionType.DELETE);
    }

    @Test
    void test_delete_started_game() {
        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(Game.builder().status(GameStatus.IN_PROGRESS).build());

        try {
            gameService.delete(DEFAULT_GAME_ID);

            fail("Started game is deleted");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.IN_PROGRESS.getCode()));
        }
    }

    @Test
    void test_terminate_game() {
        final Game sampleGame = Game.builder().ownerId(DEFAULT_USER_ID).status(GameStatus.WAITING).version(1).build();

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);
        when(gameDao.save(any())).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        final Game game = gameService.terminate(DEFAULT_GAME_ID);

        assertThat(game, notNullValue());
        assertThat(game.getStatus(), equalTo(GameStatus.TERMINATED));
        assertThat(game.getVersion(), equalTo(2));

        verify(gameDao, times(1)).save(game);
        verify(actionService, times(1)).add(game, DEFAULT_USER_ID, Constants.Game.NO_SCORE, ActionType.TERMINATE);
    }

    @Test
    void test_terminate_started_game() {
        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(Game.builder().status(GameStatus.IN_PROGRESS).build());

        try {
            gameService.terminate(DEFAULT_GAME_ID);

            fail("Started game is deleted");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.IN_PROGRESS.getCode()));
        }
    }

    @Test
    void test_start_game() {
        final Game sampleGame = Game.builder()
                .id(DEFAULT_GAME_ID)
                .ownerId(DEFAULT_USER_ID)
                .name("My game")
                .duration(60)
                .language(Language.en)
                .activePlayerCount(2)
                .expectedPlayerCount(2)
                .status(GameStatus.READY_TO_START)
                .version(2)
                .build();

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);
        when(gameDao.save(any(Game.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(virtualBagService.getTiles(eq(DEFAULT_GAME_ID), eq(Language.en)))
                .thenReturn(TILE_MAP.values().stream().collect(Collectors.toList()));

        final Game game = gameService.start(DEFAULT_GAME_ID);

        assertThat(game.getStartDate(), notNullValue());
        assertThat(game.getStatus(), equalTo(GameStatus.IN_PROGRESS));
        assertThat(game.getName(), equalTo("My game"));
        assertThat(game.getExpectedPlayerCount(), equalTo(2));
        assertThat(game.getDuration(), equalTo(60));
        assertThat(game.getCurrentPlayerNumber(), equalTo(1));
        assertThat(game.getRoundNumber(), equalTo(1));
        assertThat(game.getVersion(), equalTo(3));
        assertThat(game.getRemainingTileCount(), equalTo(84));

        verify(gameDao, times(1)).save(game);
        verify(actionService, times(1)).add(game, DEFAULT_USER_ID, Constants.Game.NO_SCORE, ActionType.START);
    }

    @Test
    void test_start_waiting_game() {
        final Game sampleGame = Game.builder().id(DEFAULT_GAME_ID).status(GameStatus.WAITING).build();

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);

        try {
            gameService.start(DEFAULT_GAME_ID);

            fail("Waiting game is started");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.WAITING.getCode()));
        }
    }

    @Test
    void test_start_started_game() {
        final Game sampleGame = Game.builder().id(DEFAULT_GAME_ID).status(GameStatus.IN_PROGRESS).build();

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);

        try {
            gameService.start(DEFAULT_GAME_ID);

            fail("Started game is started");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.IN_PROGRESS.getCode()));
        }
    }

    @Test
    void test_update_game() {
        final Game existingGame = Game.builder()
                .id(DEFAULT_GAME_ID)
                .name("My game")
                .duration(60)
                .expectedPlayerCount(2)
                .version(1)
                .build();

        final Game sampleGame = Game.builder()
                .id(DEFAULT_GAME_ID)
                .name("The best game")
                .duration(120)
                .expectedPlayerCount(4)
                .version(1)
                .build();

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(existingGame);
        when(gameDao.save(any(Game.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        final Game game = gameService.save(sampleGame);

        assertThat(game.getName(), equalTo("The best game"));
        assertThat(game.getExpectedPlayerCount(), equalTo(4));
        assertThat(game.getDuration(), equalTo(120));
    }

    @Test
    void test_update_waiting_game_with_players() {
        final Game mockGame = mock(Game.class);

        final Game existingGame = Game.builder().id(mockGame.getId()).version(2).build();

        when(gameDao.get(mockGame.getId())).thenReturn(existingGame);

        try {
            gameService.save(mockGame);

            fail("Game with players is updated");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.CANNOT_UPDATE_GAME.getCode()));
        }
    }

    @Test
    void test_end_game() {
        final Long winningUserId = 2L;

        final Game sampleGame = Game.builder()
                .ownerId(DEFAULT_USER_ID)
                .status(GameStatus.READY_TO_END)
                .version(1)
                .build();

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);
        when(gameDao.save(any())).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // player 2 is the winner
        final List<Player> players = Arrays.asList(
                Player.builder().playerNumber(1).userId(DEFAULT_USER_ID).score(5).build(),
                Player.builder().playerNumber(2).userId(winningUserId).score(15).build());

        when(playerService.getPlayers(eq(DEFAULT_GAME_ID))).thenReturn(players);

        final Game game = gameService.end(DEFAULT_GAME_ID);

        assertThat(game, notNullValue());
        assertThat(game.getStatus(), equalTo(GameStatus.ENDED));
        assertThat(game.getEndDate(), notNullValue());
        assertThat(game.getVersion(), equalTo(2));
        assertThat(game.getCurrentPlayerNumber(), equalTo(2));

        verify(gameDao, times(1)).save(game);
        verify(actionService, times(1)).add(game, winningUserId, Constants.Game.NO_SCORE, ActionType.END);
    }

    @Test
    void test_end_waiting_game() {
        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(Game.builder().status(GameStatus.WAITING).version(2).build());

        try {
            gameService.end(DEFAULT_GAME_ID);

            fail("Waiting gane is ended");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.WAITING.getCode()));
        }
    }

    @Test
    void test_end_started_game() {
        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(Game.builder().status(GameStatus.IN_PROGRESS).build());

        try {
            gameService.end(DEFAULT_GAME_ID);

            fail("Started gane is ended");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.IN_PROGRESS.getCode()));
        }
    }

    @Test
    void test_play_not_started_game() {
        final Game sampleGame = Game.builder().status(GameStatus.WAITING).build();

        when(gameDao.getAndLock(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(), ActionType.PLAY);

            fail("Played in a waiting game");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.WAITING.getCode()));
        }
    }

    @Test
    void test_play_ended_game() {
        final Game sampleGame = Game.builder().status(GameStatus.ENDED).build();

        when(gameDao.getAndLock(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(), ActionType.PLAY);

            fail("Played in an ended game");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.NOT_FOUND.getCode()));
        }
    }

    @Test
    void test_play_terminated_game() {
        final Game sampleGame = Game.builder().status(GameStatus.TERMINATED).build();

        when(gameDao.getAndLock(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(), ActionType.PLAY);

            fail("Played in a terminated game");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.NOT_FOUND.getCode()));
        }
    }

    @Test
    void test_play_not_existing_game() {
        when(gameDao.getAndLock(eq(DEFAULT_GAME_ID))).thenReturn(null);

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(), ActionType.PLAY);

            fail("Played in a not existing game");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.NOT_FOUND.getCode()));
        }
    }

    @Test
    void test_play_with_wrong_player() {
        prepareGame();

        when(playerService.getByUserId(eq(DEFAULT_GAME_ID), eq(2L)))
                .thenReturn(Player.builder().playerNumber(2).build());
        try {
            gameService.play(DEFAULT_GAME_ID, 2L, new VirtualRack(), ActionType.PLAY);

            fail("Played with the wrong player");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.TURN_OF_ANOTHER_PLAYER.getCode()));
        }
    }

    @Test
    void test_play_center_is_empty() {
        prepareGame();
        preparePlayer();
        prepareBoard();

        // the word WEAK is not using the center
        prepareUsedRackByRow(3, 7, "WEAK");

        prepareRepository();

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

            fail("Played when the starting cell is empty");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.CENTER_CANNOT_BE_EMPTY.getCode()));
        }
    }

    @Test
    void test_play_word_is_not_valid() {
        prepareGame();
        preparePlayer();
        prepareBoard();

        // create the word WEAK
        prepareUsedRackByRow(8, 7, "WEAK");

        prepareRepository();

        // the word is not valid
        when(dictionaryService.getWord(eq("WEAK"), any(Language.class))).thenReturn(null);

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

            fail("Invalid word is played");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.WORDS_ARE_NOT_FOUND.getCode()));
        }
    }

    @Test
    void test_play_locate_tile_on_a_non_empty_cell() {
        prepareGame();
        preparePlayer();
        prepareBoard();

        // the word WEAK is an existing word in the board
        prepareExistingWordByRow(8, 7, "WEAK");

        // the word WARN is using the center
        prepareUsedRackByRow(8, 7, "WARN");

        prepareRepository();

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

            fail("Located a tile on a non empty cell");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.CELL_IS_NOT_EMPTY.getCode()));
        }
    }

    @Test
    void test_play_first_word_by_using_center() {
        prepareGame();
        preparePlayer();
        prepareBoard();

        // the word WEAK is using the center
        prepareUsedRackByRow(8, 7, "WEAK");

        prepareRepository();
        prepareScoreService();

        // the word is valid
        when(dictionaryService.getWord(any(String.class), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("WEAK").build());

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

        // the word WEAK is found in the dictionary
        verify(dictionaryService, times(1)).getWord("WEAK", Language.en);

        // the word score is added to the player score
        verify(playerService, times(1)).updateScore(DEFAULT_GAME_ID, 1, 22);

        final Word weak = Word.builder()
                .actionId(DEFAULT_ACTION_ID)
                .gameId(DEFAULT_GAME_ID)
                .userId(DEFAULT_USER_ID)
                .roundNumber(1)
                .score(22)
                .word("WEAK")
                .build();

        // the word is logged in the words
        verify(wordService, times(1)).saveAll(Arrays.asList(weak));
    }

    @Test
    void test_play_new_horizontal_and_vertical_words() {
        prepareGame();

        preparePlayer();
        prepareBoard();

        // create the word WEAK
        prepareUsedRackByRow(8, 7, "WEAK");

        // create the word WAR
        prepareUsedRackByColumn(9, 7, "AR");

        prepareRepository();
        prepareScoreService();

        // the words are valid
        when(dictionaryService.getWord(eq("WEAK"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("WEAK").build());
        when(dictionaryService.getWord(eq("WAR"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("WAR").build());

        when(gameDao.save(any())).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());

        final VirtualRack virtualRack = new VirtualRack(tiles);

        final Game game = gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, virtualRack, ActionType.PLAY);

        assertThat(game.getStatus(), equalTo(GameStatus.IN_PROGRESS));
        assertThat(game.getVersion(), equalTo(4));
        assertThat(game.getCurrentPlayerNumber(), equalTo(2));
        assertThat(game.getRemainingTileCount(), equalTo(92)); // 6 tiles are used

        // the words are found in the dictionary
        verify(dictionaryService, times(1)).getWord("WEAK", Language.en);
        verify(dictionaryService, times(1)).getWord("WAR", Language.en);

        // the word score is added to the player score
        verify(playerService, times(1)).updateScore(DEFAULT_GAME_ID, 1, 29);

        final Word weak = Word.builder()
                .actionId(DEFAULT_ACTION_ID)
                .gameId(DEFAULT_GAME_ID)
                .userId(DEFAULT_USER_ID)
                .roundNumber(1)
                .score(22)
                .word("WEAK")
                .build();

        final Word war = Word.builder()
                .actionId(DEFAULT_ACTION_ID)
                .gameId(DEFAULT_GAME_ID)
                .userId(DEFAULT_USER_ID)
                .roundNumber(1)
                .score(7)
                .word("WAR")
                .build();

        // the words are logged in the words
        verify(wordService, times(1)).saveAll(Arrays.asList(weak, war));

        verify(virtualRackService, times(1)).validateRack(DEFAULT_GAME_ID, DEFAULT_PLAYER_NUMBER, DEFAULT_ROUND_NUMBER,
                virtualRack);
        verify(gameDao, times(1)).save(any());
        verify(contentService, times(1)).update(any(), any(), any(), eq(DEFAULT_PLAYER_NUMBER), eq(DEFAULT_DURATION));
    }

    @Test
    void test_play_round_number_increases_when_owner_gets_the_turn() {
        final Game game = prepareGame();
        game.setCurrentPlayerNumber(2);

        prepareBoard();

        // create the word WEAK
        prepareUsedRackByRow(8, 7, "WEAK");

        prepareRepository();
        prepareScoreService();

        // the words are valid
        when(dictionaryService.getWord(eq("WEAK"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("WEAK").build());

        when(playerService.getByUserId(eq(DEFAULT_GAME_ID), eq(2L)))
                .thenReturn(Player.builder().playerNumber(2).build());

        when(gameDao.save(any())).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());

        final VirtualRack virtualRack = new VirtualRack(tiles);

        gameService.play(DEFAULT_GAME_ID, 2L, virtualRack, ActionType.PLAY);

        assertThat(game.getRoundNumber(), equalTo(2));
    }

    @Test
    void test_play_new_two_word_not_linked_to_existing_words() {
        prepareGame();
        preparePlayer();
        prepareBoard();

        // the word WEAK is an existing word in the board
        prepareExistingWordByRow(8, 7, "WEAK");

        // the word HEAL not linked to an existing word
        prepareUsedRackByRow(3, 7, "HEAL");

        // the word HOP not linked to an existing word, but linked to HEAL word
        prepareUsedRackByColumn(4, 7, "OST");

        prepareRepository();

        // the words are valid
        when(dictionaryService.getWord(eq("HEAL"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("HEAL").build());
        when(dictionaryService.getWord(eq("HOST"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("HOST").build());

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

            fail("Unlinked word is played");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.WORDS_ARE_NOT_LINKED.getCode()));
        }
    }

    @Test
    void test_play_two_words_linked_to_existing_words() {
        prepareGame();
        preparePlayer();
        prepareBoard();

        // the word WEAK is an existing word in the board
        prepareExistingWordByRow(8, 7, "WEAK");

        // the word SNO(W) is directly linked to the existing WEAK word
        prepareUsedRackByColumn(5, 7, "SNO");

        // the word BULL(S) is linked to the new RAW word
        prepareUsedRackByRow(5, 3, "BULL");

        prepareRepository();
        prepareScoreService();

        // the words are valid
        when(dictionaryService.getWord(eq("SNOW"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("SNOW").build());
        when(dictionaryService.getWord(eq("BULLS"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("BULLS").build());

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

        // the words are found in the dictionary
        verify(dictionaryService, times(1)).getWord("SNOW", Language.en);
        verify(dictionaryService, times(1)).getWord("BULLS", Language.en);

        // the word score is added to the player score
        verify(playerService, times(1)).updateScore(DEFAULT_GAME_ID, 1, 22);

        final Word snow = Word.builder()
                .actionId(DEFAULT_ACTION_ID)
                .gameId(DEFAULT_GAME_ID)
                .userId(DEFAULT_USER_ID)
                .roundNumber(1)
                .score(8)
                .word("SNOW")
                .build();

        final Word bulls = Word.builder()
                .actionId(DEFAULT_ACTION_ID)
                .gameId(DEFAULT_GAME_ID)
                .userId(DEFAULT_USER_ID)
                .roundNumber(1)
                .score(14)
                .word("BULLS")
                .build();

        // the words are logged in the words
        verify(wordService, times(1)).saveAll(Arrays.asList(bulls, snow));
    }

    @Test
    void test_play_word_detection_ends_in_right() {
        prepareGame();
        preparePlayer();
        prepareBoard();

        // the word WEAK is an existing word in the board
        prepareExistingWordByRow(8, 8, "WEAK");

        // the extension ENIN is linked to the WEAK word
        prepareUsedRackByRow(8, 12, "ENIN");

        // the extension G is added to the next cell that is in the next row
        prepareUsedRackByRow(9, 1, "G");

        prepareRepository();

        // the word is not valid
        when(dictionaryService.getWord(eq("WEAKENIN"), any(Language.class))).thenReturn(null);

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

            fail("Word detection does not end in right");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.WORDS_ARE_NOT_FOUND.getCode()));
        }
    }

    @Test
    void test_play_word_detection_ends_in_bottom() {
        prepareGame();
        preparePlayer();
        prepareBoard();

        // the word WEAK is an existing word in the board
        prepareExistingWordByColumn(8, 8, "WEAK");

        // the extension ENIN is linked to the WEAK word
        prepareUsedRackByColumn(12, 8, "ENIN");

        // the extension G is added to the next cell that is in the next column
        prepareUsedRackByColumn(1, 9, "G");

        prepareRepository();

        // the word is not valid
        when(dictionaryService.getWord(eq("WEAKENIN"), any(Language.class))).thenReturn(null);

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

            fail("Word detection does not end in bottom");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.WORDS_ARE_NOT_FOUND.getCode()));
        }
    }

    @Test
    void test_play_new_word_extends_an_existing_word() {
        prepareGame();
        preparePlayer();
        prepareBoard();

        // the word WEAK is an existing word in the board
        prepareExistingWordByRow(8, 7, "WEAK");

        // the word ENING will extend the existing WEAK word
        prepareUsedRackByRow(8, 11, "ENING");

        prepareRepository();
        prepareScoreService();

        // the word is valid
        when(dictionaryService.getWord(eq("WEAKENING"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("WEAKENING").build());

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

        // the word is found in the dictionary
        verify(dictionaryService, times(1)).getWord("WEAKENING", Language.en);

        // the word score is added to the player score
        verify(playerService, times(1)).updateScore(DEFAULT_GAME_ID, 1, 54);

        final Word weakening = Word.builder()
                .actionId(DEFAULT_ACTION_ID)
                .gameId(DEFAULT_GAME_ID)
                .userId(DEFAULT_USER_ID)
                .roundNumber(1)
                .score(54)
                .word("WEAKENING")
                .build();

        // the word is logged in the words
        verify(wordService, times(1)).saveAll(Arrays.asList(weakening));
    }

    @Test
    void test_play_use_a_letter_from_an_extended_word() {
        prepareGame();
        preparePlayer();
        prepareBoard();

        // the word WEAK is an existing word in the board
        prepareExistingWordByRow(8, 7, "WEAK");

        // the word (WEAK)ENING will extend the existing WEAK word
        prepareUsedRackByRow(8, 11, "ENING");

        // the word (G)OAL will be linked to the WEAKENING word
        prepareUsedRackByColumn(9, 15, "OAL");

        prepareRepository();
        prepareScoreService();

        // the words are valid
        when(dictionaryService.getWord(eq("WEAKENING"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("WEAKENING").build());
        when(dictionaryService.getWord(eq("GOAL"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("GOAL").build());

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

        // the words are found in the dictionary
        verify(dictionaryService, times(1)).getWord("WEAKENING", Language.en);
        verify(dictionaryService, times(1)).getWord("GOAL", Language.en);

        // the word score is added to the player score
        verify(playerService, times(1)).updateScore(DEFAULT_GAME_ID, 1, 69);

        final Word weakening = Word.builder()
                .actionId(DEFAULT_ACTION_ID)
                .gameId(DEFAULT_GAME_ID)
                .userId(DEFAULT_USER_ID)
                .roundNumber(1)
                .score(54)
                .word("WEAKENING")
                .build();

        final Word goal = Word.builder()
                .actionId(DEFAULT_ACTION_ID)
                .gameId(DEFAULT_GAME_ID)
                .userId(DEFAULT_USER_ID)
                .roundNumber(1)
                .score(15)
                .word("GOAL")
                .build();

        // the words are logged in the words
        verify(wordService, times(1)).saveAll(Arrays.asList(weakening, goal));
    }

    @Test
    void test_play_the_same_word_more_than_once() {
        prepareGame();
        preparePlayer();
        prepareBoard();

        // the word WEAK is an existing word in the board
        prepareExistingWordByRow(8, 7, "WEAK");

        // the word (W)EAK will be linked to the W letter
        prepareUsedRackByColumn(9, 7, "EAK");

        prepareRepository();
        prepareScoreService();

        // the word is valid
        when(dictionaryService.getWord(eq("WEAK"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("WEAK").build());

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

        // the word is found in the dictionary
        verify(dictionaryService, times(1)).getWord("WEAK", Language.en);

        // the word score is added to the player score
        verify(playerService, times(1)).updateScore(DEFAULT_GAME_ID, 1, 12);

        final Word weak = Word.builder()
                .actionId(DEFAULT_ACTION_ID)
                .gameId(DEFAULT_GAME_ID)
                .userId(DEFAULT_USER_ID)
                .roundNumber(1)
                .score(12)
                .word("WEAK")
                .build();

        // the word is logged in the words
        verify(wordService, times(1)).saveAll(Arrays.asList(weak));
    }

    @Test
    void test_play_single_letter_not_allowed() {
        prepareGame();
        preparePlayer();
        prepareBoard();

        // the letter A is not linked to anywhere
        prepareUsedRackByColumn(8, 8, "A");

        prepareRepository();

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);
            fail("Single letter words are not detected");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.SINGLE_LETTER_WORDS_NOT_ALLOWED.getCode()));
        }
    }

    @Test
    void test_play_single_letter_with_valid_word_not_allowed() {
        prepareGame();
        preparePlayer();
        prepareBoard();

        // the word WEAK is an existing word in the board
        prepareUsedRackByColumn(8, 8, "WEAK");

        // the letter A is not linked to anywhere
        prepareUsedRackByColumn(1, 1, "A");

        prepareRepository();

        // the word is valid
        when(dictionaryService.getWord(eq("WEAK"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("WEAK").build());

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

            fail("Single letter words are not detected");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.SINGLE_LETTER_WORDS_NOT_ALLOWED.getCode()));
        }
    }

    @Test
    void test_play_multiplier_cell_value_used_in_double_words_in_same_round() {
        prepareGame();
        preparePlayer();
        prepareBoard();

        // create the word WEAK
        prepareUsedRackByRow(8, 7, "WEAK");

        // create the word (E)RRAT
        prepareUsedRackByColumn(9, 8, "RRAT");

        prepareRepository();
        prepareScoreService();

        // the words are valid
        when(dictionaryService.getWord(eq("WEAK"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("WEAK").build());
        when(dictionaryService.getWord(eq("ERRAT"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("ERRAT").build());

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

        // the words are found in the dictionary
        verify(dictionaryService, times(1)).getWord("WEAK", Language.en);
        verify(dictionaryService, times(1)).getWord("ERRAT", Language.en);

        // the word score is added to the player score
        verify(playerService, times(1)).updateScore(DEFAULT_GAME_ID, 1, 34);

        final Word weak = Word.builder()
                .actionId(DEFAULT_ACTION_ID)
                .gameId(DEFAULT_GAME_ID)
                .userId(DEFAULT_USER_ID)
                .roundNumber(1)
                .score(22)
                .word("WEAK")
                .build();

        final Word errat = Word.builder()
                .actionId(DEFAULT_ACTION_ID)
                .gameId(DEFAULT_GAME_ID)
                .userId(DEFAULT_USER_ID)
                .roundNumber(1)
                .score(12)
                .word("ERRAT")
                .build();

        // the words are logged in the words
        verify(wordService, times(1)).saveAll(Arrays.asList(weak, errat));
    }

    @Test
    void test_play_multiplier_cell_value_not_used_multiple_rounds() {
        prepareGame();
        preparePlayer();
        prepareBoard();

        // the word WEAK is an existing word in the board
        prepareExistingWordByRow(8, 7, "WEAK");

        // extend the word WEAK(ER)
        prepareUsedRackByRow(8, 11, "ER");

        // create the word (E)RRAT
        prepareUsedRackByColumn(9, 8, "RRAT");

        prepareRepository();
        prepareScoreService();

        // the words are valid
        when(dictionaryService.getWord(eq("WEAKER"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("WEAKER").build());
        when(dictionaryService.getWord(eq("ERRAT"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("ERRAT").build());

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

        // the words are found in the dictionary
        verify(dictionaryService, times(1)).getWord("WEAKER", Language.en);
        verify(dictionaryService, times(1)).getWord("ERRAT", Language.en);

        // the word score is added to the player score
        verify(playerService, times(1)).updateScore(DEFAULT_GAME_ID, 1, 20);

        final Word weaker = Word.builder()
                .actionId(DEFAULT_ACTION_ID)
                .gameId(DEFAULT_GAME_ID)
                .userId(DEFAULT_USER_ID)
                .roundNumber(1)
                .score(14)
                .word("WEAKER")
                .build();

        final Word errat = Word.builder()
                .actionId(DEFAULT_ACTION_ID)
                .gameId(DEFAULT_GAME_ID)
                .userId(DEFAULT_USER_ID)
                .roundNumber(1)
                .score(6)
                .word("ERRAT")
                .build();

        // the words are logged in the words
        verify(wordService, times(1)).saveAll(Arrays.asList(weaker, errat));
    }

    @Test
    void test_play_all_tiles_on_different_words_gets_no_bingo_bonus_score() {
        prepareGame();
        preparePlayer();
        prepareBoard();

        // create the word PREP
        prepareUsedRackByRow(8, 7, "PREP");

        // create the word (R)EPO
        prepareUsedRackByColumn(9, 8, "EPO");

        prepareRepository();
        prepareScoreService();

        // the words are valid
        when(dictionaryService.getWord(eq("PREP"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("PREP").build());
        when(dictionaryService.getWord(eq("REPO"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("REPO").build());

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

        // the words are found in the dictionary
        verify(dictionaryService, times(1)).getWord("PREP", Language.en);
        verify(dictionaryService, times(1)).getWord("REPO", Language.en);

        // the word score is added to the player score
        verify(playerService, times(1)).updateScore(DEFAULT_GAME_ID, 1, 28);

        final Word prep = Word.builder()
                .actionId(DEFAULT_ACTION_ID)
                .gameId(DEFAULT_GAME_ID)
                .userId(DEFAULT_USER_ID)
                .roundNumber(1)
                .score(16)
                .word("PREP")
                .build();

        final Word repo = Word.builder()
                .actionId(DEFAULT_ACTION_ID)
                .gameId(DEFAULT_GAME_ID)
                .userId(DEFAULT_USER_ID)
                .roundNumber(1)
                .score(12)
                .word("REPO")
                .build();

        // the words are logged in the words
        verify(wordService, times(1)).saveAll(Arrays.asList(prep, repo));

        verify(actionService, times(0)).add(any(), eq(DEFAULT_USER_ID), eq(Constants.Game.BINGO_SCORE),
                eq(ActionType.BONUS_BINGO));
    }

    @Test
    void test_play_all_tiles_in_one_word_gets_bonus_score() {
        prepareGame();
        preparePlayer();
        prepareBoard();

        // create the word PREPARE
        prepareUsedRackByRow(8, 7, "PREPARE");

        prepareRepository();
        prepareScoreService();

        // the word is valid
        when(dictionaryService.getWord(eq("PREPARE"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("PREPARE").build());

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

        // the word is found in the dictionary
        verify(dictionaryService, times(1)).getWord("PREPARE", Language.en);

        // the word score is added to the player score
        verify(playerService, times(1)).updateScore(DEFAULT_GAME_ID, 1, 74);

        final Word prepare = Word.builder()
                .actionId(DEFAULT_ACTION_ID)
                .gameId(DEFAULT_GAME_ID)
                .userId(DEFAULT_USER_ID)
                .roundNumber(1)
                .score(24)
                .word("PREPARE")
                .build();

        // the word is logged in the words
        verify(wordService, times(1)).saveAll(Arrays.asList(prepare));

        verify(actionService, times(1)).add(any(), eq(DEFAULT_USER_ID), eq(Constants.Game.BINGO_SCORE),
                eq(ActionType.BONUS_BINGO));
    }

    @Test
    void test_play_set_game_as_ready_to_end_when_no_tiles_in_the_bag_and_player_plays_all_tiles_in_the_rack() {
        final Game game = prepareGame();
        game.setRemainingTileCount(0);

        preparePlayer();
        prepareBoard();

        // create the word PREPARE
        prepareUsedRackByRow(8, 7, "PREPARE");

        prepareRepository();
        prepareScoreService();

        // the word is valid
        when(dictionaryService.getWord(eq("PREPARE"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("PREPARE").build());

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());

        final Game playedGame = gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles),
                ActionType.PLAY);

        assertThat(playedGame.getStatus(), equalTo(GameStatus.READY_TO_END));
    }

    @Test
    void test_play_do_not_set_game_as_ready_to_end_when_no_tiles_in_the_bag_and_but_the_player_still_has_tiles_in_the_rack() {
        final Game game = prepareGame();
        game.setRemainingTileCount(0);

        preparePlayer();
        prepareBoard();

        // create the word PREP
        prepareUsedRackByRow(8, 7, "PREP");

        // add a not used tiles
        tiles.add(VirtualTile.builder().number(5).build());

        prepareRepository();
        prepareScoreService();

        // the word is valid
        when(dictionaryService.getWord(eq("PREP"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("PREP").build());

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());

        final Game playedGame = gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles),
                ActionType.PLAY);

        assertThat(playedGame.getStatus(), equalTo(GameStatus.IN_PROGRESS));
    }

    @Test
    void test_exchange_tiles() {
        prepareGame();
        preparePlayer();
        prepareBoard();

        // add a not used tiles
        tiles.add(VirtualTile.builder().number(1).letter("Q").build());

        prepareRepository();

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());

        final VirtualRack virtualRack = new VirtualRack(tiles);

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, virtualRack, ActionType.EXCHANGE);

        verify(virtualRackService, times(1)).exchange(DEFAULT_GAME_ID, Language.en, DEFAULT_PLAYER_NUMBER,
                DEFAULT_ROUND_NUMBER, virtualRack);
        verify(actionService, times(1)).add(any(), eq(DEFAULT_USER_ID), eq(Constants.Game.NO_SCORE),
                eq(ActionType.EXCHANGE));
    }

    @Test
    void test_skip_turn() {
        prepareGame();
        preparePlayer();
        prepareBoard();
        prepareRepository();

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());

        final Game game = gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.SKIP);

        assertThat(game.getStatus(), equalTo(GameStatus.IN_PROGRESS));
        assertThat(game.getVersion(), equalTo(4));
        assertThat(game.getCurrentPlayerNumber(), equalTo(2));
        assertThat(game.getRemainingTileCount(), equalTo(98));

        verify(actionService, times(1)).add(any(), eq(DEFAULT_USER_ID), eq(Constants.Game.NO_SCORE),
                eq(ActionType.SKIP));
    }

    @Test
    void test_skip_turn_by_timeout() {
        prepareGame();
        preparePlayer();
        prepareBoard();
        prepareRepository();

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());

        final Game game = gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles),
                ActionType.TIMEOUT);

        assertThat(game.getStatus(), equalTo(GameStatus.IN_PROGRESS));
        assertThat(game.getVersion(), equalTo(4));
        assertThat(game.getCurrentPlayerNumber(), equalTo(2));
        assertThat(game.getRemainingTileCount(), equalTo(98));

        verify(actionService, times(1)).add(any(), eq(DEFAULT_USER_ID), eq(Constants.Game.NO_SCORE),
                eq(ActionType.TIMEOUT));
    }

    private Game prepareGame() {
        final Game game = Game.builder()
                .id(DEFAULT_GAME_ID)
                .status(GameStatus.IN_PROGRESS)
                .expectedPlayerCount(2)
                .language(Language.en)
                .currentPlayerNumber(DEFAULT_PLAYER_NUMBER)
                .version(3)
                .roundNumber(DEFAULT_ROUND_NUMBER)
                .remainingTileCount(98)
                .build();

        when(gameDao.getAndLock(eq(DEFAULT_GAME_ID))).thenReturn(game);

        return game;
    }

    private void preparePlayer() {
        when(playerService.getByUserId(eq(DEFAULT_GAME_ID), eq(DEFAULT_USER_ID)))
                .thenReturn(Player.builder().playerNumber(DEFAULT_PLAYER_NUMBER).build());
    }

    private void prepareBoard() {
        boardMatrix = new VirtualCell[15][15];

        IntStream.range(1, 16).forEach(rowNumber -> {
            IntStream.range(1, 16).forEach(columnNumber -> {
                final Integer cellNumber = (rowNumber - 1) * 15 + columnNumber;
                final VirtualCell cell = VirtualCell.builder()
                        .cellNumber(cellNumber)
                        .center(rowNumber == 8 && columnNumber == 8)
                        .columnNumber(columnNumber)
                        .hasBottom(rowNumber != 15)
                        .hasLeft(columnNumber != 1)
                        .hasRight(columnNumber != 15)
                        .hasTop(rowNumber != 1)
                        .letterValueMultiplier(getLetterValueMultiplier(cellNumber))
                        .rowNumber(rowNumber)
                        .wordScoreMultiplier(getWordScoreMultiplier(cellNumber))
                        .build();

                boardMatrix[cell.getRowNumber() - 1][cell.getColumnNumber() - 1] = cell;
            });
        });
    }

    private void prepareUsedRackByRow(int startingRow, int startingColumn, String word) {
        int tileNumber = tiles.size() + 1;

        int columnNumber = startingColumn;

        for (char letter : word.toCharArray()) {
            tiles.add(VirtualTile.builder()
                    .columnNumber(columnNumber)
                    .letter(String.valueOf(letter).toUpperCase())
                    .number(tileNumber++)
                    .playerNumber(1)
                    .rowNumber(startingRow)
                    .sealed(true)
                    .value(TILE_MAP.get(String.valueOf(letter).toUpperCase()).getValue())
                    .build());

            columnNumber = columnNumber + 1;
        }
    }

    private void prepareUsedRackByColumn(int startingRow, int startingColumn, String word) {
        int tileNumber = tiles.size() + 1;

        int rowNumber = startingRow;

        for (char letter : word.toCharArray()) {
            tiles.add(VirtualTile.builder()
                    .columnNumber(startingColumn)
                    .letter(String.valueOf(letter).toUpperCase())
                    .number(tileNumber++)
                    .playerNumber(1)
                    .rowNumber(rowNumber)
                    .sealed(true)
                    .value(TILE_MAP.get(String.valueOf(letter).toUpperCase()).getValue())
                    .build());

            rowNumber = rowNumber + 1;
        }
    }

    private void prepareExistingWordByRow(int startingRow, int startingColumn, String word) {
        int columnNumber = startingColumn;

        for (char letter : word.toCharArray()) {
            boardMatrix[startingRow - 1][columnNumber - 1].setLetter(String.valueOf(letter).toUpperCase());
            boardMatrix[startingRow - 1][columnNumber - 1].setSealed(true);
            boardMatrix[startingRow - 1][columnNumber - 1]
                    .setValue(TILE_MAP.get(String.valueOf(letter).toUpperCase()).getValue());

            columnNumber = columnNumber + 1;
        }
    }

    private void prepareExistingWordByColumn(int startingRow, int startingColumn, String word) {
        int rowNumber = startingRow;

        for (char letter : word.toCharArray()) {
            boardMatrix[rowNumber - 1][startingColumn - 1].setLetter(String.valueOf(letter).toUpperCase());
            boardMatrix[rowNumber - 1][startingColumn - 1].setSealed(true);
            boardMatrix[rowNumber - 1][startingColumn - 1]
                    .setValue(TILE_MAP.get(String.valueOf(letter).toUpperCase()).getValue());

            rowNumber = rowNumber + 1;
        }
    }

    private void prepareRepository() {
        final List<VirtualCell> cells = Arrays.stream(boardMatrix).flatMap(Arrays::stream).collect(Collectors.toList());

        when(virtualBoardService.getBoard(eq(DEFAULT_GAME_ID), eq(1))).thenReturn(new VirtualBoard(cells));
    }

    private void prepareScoreService() {
        when(scoreService.calculateConstructedWordScore(any())).thenCallRealMethod();
        when(scoreService.calculateBonuses(any(), any())).thenCallRealMethod();
    }

    private Integer getLetterValueMultiplier(Integer cellNumber) {
        if (TRIPLE_LETTER_CELLS.contains(cellNumber)) {
            return 3;
        } else if (DOUBLE_LETTER_CELLS.contains(cellNumber)) {
            return 2;
        } else {
            return 1;
        }
    }

    private Integer getWordScoreMultiplier(Integer cellNumber) {
        if (TRIPLE_WORD_CELLS.contains(cellNumber)) {
            return 3;
        } else if (DOUBLE_WORD_CELLS.contains(cellNumber)) {
            return 2;
        } else {
            return 1;
        }
    }

}
