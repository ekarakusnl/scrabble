package com.gamecity.scrabble.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
import com.gamecity.scrabble.entity.GameStatus;
import com.gamecity.scrabble.entity.GameType;
import com.gamecity.scrabble.entity.Word;
import com.gamecity.scrabble.entity.User;
import com.gamecity.scrabble.entity.UserType;
import com.gamecity.scrabble.model.DictionaryWord;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.VirtualCell;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.model.VirtualTile;
import com.gamecity.scrabble.service.ActionService;
import com.gamecity.scrabble.service.DictionaryService;
import com.gamecity.scrabble.service.PlayerService;
import com.gamecity.scrabble.service.SchedulerService;
import com.gamecity.scrabble.service.GameService;
import com.gamecity.scrabble.service.ContentService;
import com.gamecity.scrabble.service.VirtualRackService;
import com.gamecity.scrabble.service.UserService;
import com.gamecity.scrabble.service.VirtualBagService;
import com.gamecity.scrabble.service.WordService;
import com.gamecity.scrabble.service.exception.GameException;
import com.gamecity.scrabble.service.exception.error.GameError;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;

class TestGameService extends AbstractServiceTest {

    @Mock
    private GameDao gameDao;

    @Mock
    private UserService userService;

    @Mock
    private PlayerService playerService;

    @Mock
    private ContentService contentService;

    @Mock
    private VirtualBoardServiceImpl virtualBoardService;

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

    @Mock
    private SchedulerService schedulerService;

    @InjectMocks
    private GameService gameService = new GameServiceImpl(userService, playerService, virtualBoardService,
            virtualRackService, virtualBagService, contentService, dictionaryService, wordService, actionService,
            scoreService, schedulerService);

    @BeforeEach
    void beforeEach() {
        // set gameDao
        ((GameServiceImpl) gameService).setBaseDao(gameDao);

        // create new tiles
        tiles = new ArrayList<>(Constants.Game.RACK_SIZE);
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
    void test_create_user_game() {
        final User mockUser = mock(User.class);

        final Game sampleGame = Game.builder()
                .ownerId(mockUser.getId())
                .activePlayerCount(2)
                .type(GameType.USER)
                .build();

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
        assertThat(game.getType(), equalTo(GameType.USER));

        verify(gameDao, times(1)).save(game);
        verify(playerService, times(1)).add(sampleGame.getId(), sampleGame.getOwnerId(),
                sampleGame.getActivePlayerCount());
        verify(actionService, times(1)).add(game, sampleGame.getOwnerId(), Constants.Game.NO_SCORE, ActionType.CREATE);
    }

    @Test
    void test_join_user_game() {
        final Game sampleGame = Game.builder()
                .id(DEFAULT_GAME_ID)
                .activePlayerCount(1)
                .status(GameStatus.WAITING)
                .type(GameType.USER)
                .version(1)
                .build();

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);
        when(gameDao.save(any(Game.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        when(userService.get(any())).thenReturn(User.builder().type(UserType.NORMAL).build());

        final Game game = gameService.join(DEFAULT_GAME_ID, ALTERNATIVE_USER_ID);

        assertThat(game.getStatus(), equalTo(GameStatus.WAITING));
        assertThat(game.getVersion(), equalTo(2));
        assertThat(game.getActivePlayerCount(), equalTo(2));

        verify(gameDao, times(1)).save(game);
        verify(playerService, times(1)).add(game.getId(), ALTERNATIVE_USER_ID, game.getActivePlayerCount());
        verify(actionService, times(1)).add(game, ALTERNATIVE_USER_ID, Constants.Game.NO_SCORE, ActionType.JOIN);
    }

    @Test
    void test_join_started_game() {
        when(gameDao.get(eq(DEFAULT_GAME_ID)))
                .thenReturn(Game.builder().status(GameStatus.IN_PROGRESS).type(GameType.USER).build());
        when(userService.get(any())).thenReturn(User.builder().type(UserType.NORMAL).build());

        try {
            gameService.join(DEFAULT_GAME_ID, ALTERNATIVE_USER_ID);

            fail("Joined started game");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.IN_PROGRESS.getCode()));
        }
    }

    @Test
    void test_join_game_with_an_existing_player() {
        final Game sampleGame = Game.builder()
                .id(DEFAULT_GAME_ID)
                .status(GameStatus.WAITING)
                .type(GameType.USER)
                .build();

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);
        when(playerService.getByUserId(eq(DEFAULT_GAME_ID), eq(ALTERNATIVE_USER_ID))).thenReturn(mock(Player.class));
        when(userService.get(any())).thenReturn(User.builder().type(UserType.NORMAL).build());

        try {
            gameService.join(DEFAULT_GAME_ID, ALTERNATIVE_USER_ID);

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
                .type(GameType.USER)
                .version(1)
                .build();

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);
        when(gameDao.save(any(Game.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        when(userService.get(any())).thenReturn(User.builder().type(UserType.NORMAL).build());

        final Game game = gameService.join(DEFAULT_GAME_ID, ALTERNATIVE_USER_ID);

        assertThat(game.getStatus(), equalTo(GameStatus.READY_TO_START));
    }

    @Test
    void test_leave_game() {
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
        when(playerService.getByUserId(eq(DEFAULT_GAME_ID), eq(ALTERNATIVE_USER_ID))).thenReturn(mock(Player.class));

        final Game game = gameService.leave(DEFAULT_GAME_ID, ALTERNATIVE_USER_ID);

        assertThat(game.getStatus(), equalTo(GameStatus.WAITING));
        assertThat(game.getVersion(), equalTo(3));
        assertThat(game.getActivePlayerCount(), equalTo(1));

        verify(gameDao, times(1)).save(game);
        verify(playerService, times(1)).remove(any(Player.class));
        verify(actionService, times(1)).add(game, ALTERNATIVE_USER_ID, Constants.Game.NO_SCORE, ActionType.LEAVE);
    }

    @Test
    void test_leave_started_game() {
        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(Game.builder().status(GameStatus.IN_PROGRESS).build());

        try {
            gameService.leave(DEFAULT_GAME_ID, ALTERNATIVE_USER_ID);

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
            gameService.leave(DEFAULT_GAME_ID, ALTERNATIVE_USER_ID);

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
        when(playerService.getByUserId(eq(DEFAULT_GAME_ID), eq(DEFAULT_USER_ID))).thenReturn(mock(Player.class));

        try {
            gameService.leave(DEFAULT_GAME_ID, DEFAULT_USER_ID);

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
    void test_start_user_game() {
        final Game sampleGame = Game.builder()
                .id(DEFAULT_GAME_ID)
                .ownerId(DEFAULT_USER_ID)
                .name("My game")
                .duration(60)
                .language(Language.en)
                .activePlayerCount(2)
                .expectedPlayerCount(2)
                .status(GameStatus.READY_TO_START)
                .type(GameType.USER)
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
        assertThat(game.getCurrentPlayerNumber(), equalTo(DEFAULT_PLAYER_NUMBER));
        assertThat(game.getRoundNumber(), equalTo(DEFAULT_ROUND_NUMBER));
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

        final Game updatedGame = Game.builder()
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

        final Game game = gameService.save(updatedGame);

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
                Player.builder().playerNumber(2).userId(ALTERNATIVE_USER_ID).score(15).build());

        when(playerService.getPlayers(eq(DEFAULT_GAME_ID))).thenReturn(players);

        final Game game = gameService.end(DEFAULT_GAME_ID);

        assertThat(game, notNullValue());
        assertThat(game.getStatus(), equalTo(GameStatus.ENDED));
        assertThat(game.getEndDate(), notNullValue());
        assertThat(game.getVersion(), equalTo(2));
        assertThat(game.getCurrentPlayerNumber(), equalTo(2));

        verify(gameDao, times(1)).save(game);
        verify(actionService, times(1)).add(game, ALTERNATIVE_USER_ID, Constants.Game.NO_SCORE, ActionType.END);
    }

    @Test
    void test_end_waiting_game() {
        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(Game.builder().status(GameStatus.WAITING).version(2).build());

        try {
            gameService.end(DEFAULT_GAME_ID);

            fail("Waiting game is ended");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.WAITING.getCode()));
        }
    }

    @Test
    void test_end_started_game() {
        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(Game.builder().status(GameStatus.IN_PROGRESS).build());

        try {
            gameService.end(DEFAULT_GAME_ID);

            fail("Started game is ended");
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
        createGame();

        when(playerService.getByUserId(eq(DEFAULT_GAME_ID), eq(ALTERNATIVE_USER_ID)))
                .thenReturn(Player.builder().playerNumber(ALTERNATIVE_PLAYER_NUMBER).build());
        try {
            gameService.play(DEFAULT_GAME_ID, ALTERNATIVE_USER_ID, new VirtualRack(), ActionType.PLAY);

            fail("Played with the wrong player");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.TURN_OF_ANOTHER_PLAYER.getCode()));
        }
    }

    @Test
    void test_play_center_is_empty() {
        createGame();
        createPlayer();
        createBoardMatrix();

        // the word WEAK is not using the center
        createNewHorizontalWord(3, 7, "WEAK");

        createVirtualBoard();

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

            fail("Played when the starting cell is empty");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.CENTER_CANNOT_BE_EMPTY.getCode()));
        }
    }

    @Test
    void test_play_word_is_not_valid() {
        createGame();
        createPlayer();
        createBoardMatrix();

        // create the word WEAK
        createNewHorizontalWord(8, 7, "WEAK");

        createVirtualBoard();

        // the word is not valid
        when(dictionaryService.get(eq("WEAK"), any(Language.class))).thenReturn(null);
        when(virtualBoardService.scanWords(any(), any(), any())).thenCallRealMethod();

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

            fail("Invalid word is played");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.WORDS_ARE_NOT_FOUND.getCode()));
        }
    }

    @Test
    void test_play_locate_tile_on_a_non_empty_cell() {
        createGame();
        createPlayer();
        createBoardMatrix();

        // the word WEAK is an existing word in the board
        createExistingHorizontalWord(8, 7, "WEAK");

        // the word WARN is using the center
        createNewVerticalWord(8, 7, "WARN");

        createVirtualBoard();

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

            fail("Located a tile on a non empty cell");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.CELL_IS_NOT_EMPTY.getCode()));
        }
    }

    @Test
    void test_play_first_word_by_using_center() {
        createGame();
        createPlayer();
        createBoardMatrix();

        // the word WEAK is using the center
        createNewHorizontalWord(8, 7, "WEAK");

        createVirtualBoard();
        createScoreCalculator();

        // the word is valid
        when(dictionaryService.get(any(String.class), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("WEAK").build());

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());
        when(virtualBoardService.scanWords(any(), any(), any())).thenCallRealMethod();

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

        // the word WEAK is found in the dictionary
        verify(dictionaryService, times(1)).get("WEAK", Language.en);

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
    void test_play_new_horizontal_and_vertical_words_in_user_game() {
        createGame();
        createPlayer();
        createBoardMatrix();

        // create the word WEAK
        createNewHorizontalWord(8, 7, "WEAK");

        // create the word WAR
        createNewVerticalWord(9, 7, "AR");

        createVirtualBoard();
        createScoreCalculator();

        // the words are valid
        when(dictionaryService.get(eq("WEAK"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("WEAK").build());
        when(dictionaryService.get(eq("WAR"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("WAR").build());

        when(gameDao.save(any())).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());
        when(virtualBoardService.scanWords(any(), any(), any())).thenCallRealMethod();

        final VirtualRack virtualRack = new VirtualRack(tiles);

        final Game game = gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, virtualRack, ActionType.PLAY);

        assertThat(game.getStatus(), equalTo(GameStatus.IN_PROGRESS));
        assertThat(game.getVersion(), equalTo(4));
        assertThat(game.getCurrentPlayerNumber(), equalTo(2));
        assertThat(game.getRemainingTileCount(), equalTo(92)); // 6 tiles are used

        // the words are found in the dictionary
        verify(dictionaryService, times(1)).get("WEAK", Language.en);
        verify(dictionaryService, times(1)).get("WAR", Language.en);

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
        final Game game = createGame();
        game.setCurrentPlayerNumber(ALTERNATIVE_PLAYER_NUMBER);

        createBoardMatrix();

        // create the word WEAK
        createNewHorizontalWord(8, 7, "WEAK");

        createVirtualBoard();
        createScoreCalculator();

        // the words are valid
        when(dictionaryService.get(eq("WEAK"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("WEAK").build());

        when(playerService.getByUserId(eq(DEFAULT_GAME_ID), eq(ALTERNATIVE_USER_ID)))
                .thenReturn(Player.builder().playerNumber(ALTERNATIVE_PLAYER_NUMBER).build());

        when(gameDao.save(any())).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());
        when(virtualBoardService.scanWords(any(), any(), any())).thenCallRealMethod();

        final VirtualRack virtualRack = new VirtualRack(tiles);

        gameService.play(DEFAULT_GAME_ID, ALTERNATIVE_USER_ID, virtualRack, ActionType.PLAY);

        assertThat(game.getRoundNumber(), equalTo(2));
    }

    @Test
    void test_play_multiplier_cell_value_used_in_double_words_in_same_round() {
        createGame();
        createPlayer();
        createBoardMatrix();

        // create the word WEAK
        createNewHorizontalWord(8, 7, "WEAK");

        // create the word (E)RRAT
        createNewVerticalWord(9, 8, "RRAT");

        createVirtualBoard();
        createScoreCalculator();

        // the words are valid
        when(dictionaryService.get(eq("WEAK"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("WEAK").build());
        when(dictionaryService.get(eq("ERRAT"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("ERRAT").build());

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());
        when(virtualBoardService.scanWords(any(), any(), any())).thenCallRealMethod();

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

        // the words are found in the dictionary
        verify(dictionaryService, times(1)).get("WEAK", Language.en);
        verify(dictionaryService, times(1)).get("ERRAT", Language.en);

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
        createGame();
        createPlayer();
        createBoardMatrix();

        // the word WEAK is an existing word in the board
        createExistingHorizontalWord(8, 7, "WEAK");

        // extend the word WEAK(ER)
        createNewHorizontalWord(8, 11, "ER");

        // create the word (E)RRAT
        createNewVerticalWord(9, 8, "RRAT");

        createVirtualBoard();
        createScoreCalculator();

        // the words are valid
        when(dictionaryService.get(eq("WEAKER"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("WEAKER").build());
        when(dictionaryService.get(eq("ERRAT"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("ERRAT").build());

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());
        when(virtualBoardService.scanWords(any(), any(), any())).thenCallRealMethod();

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

        // the words are found in the dictionary
        verify(dictionaryService, times(1)).get("WEAKER", Language.en);
        verify(dictionaryService, times(1)).get("ERRAT", Language.en);

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
        createGame();
        createPlayer();
        createBoardMatrix();

        // create the word PREP
        createNewHorizontalWord(8, 7, "PREP");

        // create the word (R)EPO
        createNewVerticalWord(9, 8, "EPO");

        createVirtualBoard();
        createScoreCalculator();

        // the words are valid
        when(dictionaryService.get(eq("PREP"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("PREP").build());
        when(dictionaryService.get(eq("REPO"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("REPO").build());

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());
        when(virtualBoardService.scanWords(any(), any(), any())).thenCallRealMethod();

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

        // the words are found in the dictionary
        verify(dictionaryService, times(1)).get("PREP", Language.en);
        verify(dictionaryService, times(1)).get("REPO", Language.en);

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
        createGame();
        createPlayer();
        createBoardMatrix();

        // create the word PREPARE
        createNewHorizontalWord(8, 7, "PREPARE");

        createVirtualBoard();
        createScoreCalculator();

        // the word is valid
        when(dictionaryService.get(eq("PREPARE"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("PREPARE").build());

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());
        when(virtualBoardService.scanWords(any(), any(), any())).thenCallRealMethod();

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles), ActionType.PLAY);

        // the word is found in the dictionary
        verify(dictionaryService, times(1)).get("PREPARE", Language.en);

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
        final Game game = createGame();
        game.setRemainingTileCount(0);

        createPlayer();
        createBoardMatrix();

        // create the word PREPARE
        createNewHorizontalWord(8, 7, "PREPARE");

        createVirtualBoard();
        createScoreCalculator();

        // the word is valid
        when(dictionaryService.get(eq("PREPARE"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("PREPARE").build());

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());
        when(virtualBoardService.scanWords(any(), any(), any())).thenCallRealMethod();

        final Game playedGame = gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles),
                ActionType.PLAY);

        assertThat(playedGame.getStatus(), equalTo(GameStatus.READY_TO_END));
    }

    @Test
    void test_play_do_not_set_game_as_ready_to_end_when_no_tiles_in_the_bag_and_the_player_still_has_tiles_in_the_rack() {
        final Game game = createGame();
        game.setRemainingTileCount(0);

        createPlayer();
        createBoardMatrix();

        // create the word PREP
        createNewHorizontalWord(8, 7, "PREP");

        // add a not used tiles
        tiles.add(VirtualTile.builder().number(5).build());

        createVirtualBoard();
        createScoreCalculator();

        // the word is valid
        when(dictionaryService.get(eq("PREP"), any(Language.class)))
                .thenReturn(DictionaryWord.builder().word("PREP").build());

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any(), any())).thenReturn(Action.builder().id(DEFAULT_ACTION_ID).build());
        when(virtualBoardService.scanWords(any(), any(), any())).thenCallRealMethod();

        final Game playedGame = gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(tiles),
                ActionType.PLAY);

        assertThat(playedGame.getStatus(), equalTo(GameStatus.IN_PROGRESS));
    }

    @Test
    void test_exchange_tiles() {
        createGame();
        createPlayer();
        createBoardMatrix();

        // add an used tile
        tiles.add(VirtualTile.builder().number(1).letter("Q").build());

        createVirtualBoard();

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
        createGame();
        createPlayer();
        createBoardMatrix();
        createVirtualBoard();

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
        createGame();
        createPlayer();
        createBoardMatrix();
        createVirtualBoard();

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

    private Game createGame() {
        final Game game = Game.builder()
                .id(DEFAULT_GAME_ID)
                .status(GameStatus.IN_PROGRESS)
                .expectedPlayerCount(2)
                .language(Language.en)
                .currentPlayerNumber(DEFAULT_PLAYER_NUMBER)
                .version(3)
                .roundNumber(DEFAULT_ROUND_NUMBER)
                .remainingTileCount(DEFAULT_REMAINING_TILE_COUNT)
                .type(GameType.USER)
                .build();

        when(gameDao.getAndLock(eq(DEFAULT_GAME_ID))).thenReturn(game);

        return game;
    }

    private void createPlayer() {
        when(playerService.getByUserId(eq(DEFAULT_GAME_ID), eq(DEFAULT_USER_ID)))
                .thenReturn(Player.builder().playerNumber(DEFAULT_PLAYER_NUMBER).build());
    }

    private void createVirtualBoard() {
        final List<VirtualCell> cells = Arrays.stream(boardMatrix).flatMap(Arrays::stream).collect(Collectors.toList());

        when(virtualBoardService.getBoard(eq(DEFAULT_GAME_ID), any(Integer.class))).thenReturn(new VirtualBoard(cells));
    }

    private void createScoreCalculator() {
        when(scoreService.calculateConstructedWordScore(any())).thenCallRealMethod();
        when(scoreService.calculateBonuses(any(), any())).thenCallRealMethod();
    }

}
