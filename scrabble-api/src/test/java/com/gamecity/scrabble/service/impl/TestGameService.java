package com.gamecity.scrabble.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

import com.gamecity.scrabble.dao.GameDao;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.entity.GameStatus;
import com.gamecity.scrabble.entity.Word;
import com.gamecity.scrabble.entity.User;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.VirtualCell;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.model.VirtualTile;
import com.gamecity.scrabble.service.BagService;
import com.gamecity.scrabble.service.BoardService;
import com.gamecity.scrabble.service.DictionaryService;
import com.gamecity.scrabble.service.PlayerService;
import com.gamecity.scrabble.service.GameService;
import com.gamecity.scrabble.service.UpdaterService;
import com.gamecity.scrabble.service.VirtualRackService;
import com.gamecity.scrabble.service.UserService;
import com.gamecity.scrabble.service.VirtualBoardService;
import com.gamecity.scrabble.service.WordService;
import com.gamecity.scrabble.service.exception.GameException;
import com.gamecity.scrabble.service.exception.error.GameError;

class TestGameService extends AbstractServiceTest {

    private static final Map<String, Integer> LETTER_VALUE_MAP = new HashMap<>();

    @InjectMocks
    private GameService gameService = new GameServiceImpl();

    @Mock
    private GameDao gameDao;

    @Mock
    private UserService userService;

    @Mock
    private BoardService boardService;

    @Mock
    private BagService bagService;

    @Mock
    private PlayerService playerService;

    @Mock
    private UpdaterService updaterService;

    @Mock
    private VirtualBoardService virtualBoardService;

    @Mock
    private VirtualRackService virtualRackService;

    @Mock
    private WordService wordService;

    @Mock
    private DictionaryService dictionaryService;

    private Game game;
    private List<VirtualTile> tiles;
    private VirtualCell[][] boardMatrix;

    @BeforeEach
    void beforeEach() {
        tiles = new ArrayList<>(7);
    }

    static {
        LETTER_VALUE_MAP.put("A", 1);
        LETTER_VALUE_MAP.put("B", 3);
        LETTER_VALUE_MAP.put("C", 3);
        LETTER_VALUE_MAP.put("D", 2);
        LETTER_VALUE_MAP.put("E", 1);
        LETTER_VALUE_MAP.put("F", 4);
        LETTER_VALUE_MAP.put("G", 2);
        LETTER_VALUE_MAP.put("H", 4);
        LETTER_VALUE_MAP.put("I", 1);
        LETTER_VALUE_MAP.put("J", 8);
        LETTER_VALUE_MAP.put("K", 5);
        LETTER_VALUE_MAP.put("L", 1);
        LETTER_VALUE_MAP.put("M", 3);
        LETTER_VALUE_MAP.put("N", 1);
        LETTER_VALUE_MAP.put("O", 1);
        LETTER_VALUE_MAP.put("P", 3);
        LETTER_VALUE_MAP.put("Q", 10);
        LETTER_VALUE_MAP.put("R", 1);
        LETTER_VALUE_MAP.put("S", 1);
        LETTER_VALUE_MAP.put("T", 1);
        LETTER_VALUE_MAP.put("U", 1);
        LETTER_VALUE_MAP.put("V", 4);
        LETTER_VALUE_MAP.put("W", 4);
        LETTER_VALUE_MAP.put("X", 8);
        LETTER_VALUE_MAP.put("Y", 4);
        LETTER_VALUE_MAP.put("Z", 10);
    }

    @Test
    void test_game_not_found() {
        try {
            gameService.get(DEFAULT_GAME_ID);
            fail("Game exists");
        } catch (GameException e) {
            assertEquals(GameError.NOT_FOUND.getCode(), e.getCode());
        }
    }

    @Test
    void test_get_terminated_game() {
        try {
            when(gameDao.get(eq(DEFAULT_GAME_ID))).thenAnswer(invocation -> {
                final Game game = new Game();
                game.setStatus(GameStatus.TERMINATED);
                return game;
            });
            gameService.get(DEFAULT_GAME_ID);
            fail("Game is not terminated");
        } catch (GameException e) {
            assertEquals(GameError.NOT_FOUND.getCode(), e.getCode());
        }
    }

    @Test
    void test_get_waiting_game() {
        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenAnswer(invocation -> {
            final Game game = new Game();
            game.setStatus(GameStatus.WAITING);
            return game;
        });
        assertNotNull(gameService.get(DEFAULT_GAME_ID));
    }

    @Test
    void test_get_ready_game() {
        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenAnswer(invocation -> {
            final Game game = new Game();
            game.setStatus(GameStatus.READY_TO_START);
            return game;
        });
        assertNotNull(gameService.get(DEFAULT_GAME_ID));
    }

    @Test
    void test_get_started_game() {
        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenAnswer(invocation -> {
            final Game game = new Game();
            game.setStatus(GameStatus.IN_PROGRESS);
            return game;
        });
        assertNotNull(gameService.get(DEFAULT_GAME_ID));
    }

    @Test
    void test_create_game() {
        final Game sampleGame = createSampleGame(DEFAULT_USER_ID, 2);

        when(userService.get(eq(DEFAULT_USER_ID))).thenAnswer(invocation -> {
            final User user = new User();
            user.setId(DEFAULT_USER_ID);
            return user;
        });

        when(boardService.get(eq(DEFAULT_BOARD_ID))).thenReturn(createSampleBoard());

        when(gameDao.save(any(Game.class))).thenAnswer(invocation -> {
            final Game game = invocation.getArgument(0);
            game.setId(DEFAULT_GAME_ID);
            return game;
        });

        final Game game = gameService.save(sampleGame);

        verify(playerService, times(1)).add(sampleGame.getId(), sampleGame.getOwnerId(),
                sampleGame.getActivePlayerCount());

        assertNotNull(game.getId());
        assertEquals(GameStatus.WAITING, game.getStatus());
        assertEquals(DEFAULT_USER_ID, game.getOwnerId());
        assertEquals(DEFAULT_BOARD_ID, game.getBoardId());
        assertEquals(1, game.getActionCounter());
        assertEquals(1, game.getActivePlayerCount());
    }

    @Test
    void test_join_game() {
        final Game sampleGame = createSampleGame(DEFAULT_USER_ID, 3);
        sampleGame.setId(DEFAULT_GAME_ID);

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);

        when(gameDao.save(any(Game.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        final Game game = gameService.join(DEFAULT_GAME_ID, 2L);

        verify(playerService, times(1)).add(sampleGame.getId(), 2L, sampleGame.getActivePlayerCount());

        assertEquals(GameStatus.WAITING, game.getStatus());
        assertEquals(2, game.getActionCounter());
        assertEquals(2, game.getActivePlayerCount());
    }

    @Test
    void test_join_started_game() {
        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenAnswer(invocation -> {
            final Game game = new Game();
            game.setStatus(GameStatus.IN_PROGRESS);
            return game;
        });

        try {
            gameService.join(DEFAULT_GAME_ID, 2L);
            fail("The game is not in progress");
        } catch (GameException e) {
            assertEquals(GameError.IN_PROGRESS.getCode(), e.getCode());
        }
    }

    @Test
    void test_join_game_with_an_existing_player() {
        final Game sampleGame = createSampleGame(DEFAULT_USER_ID, 3);
        sampleGame.setId(DEFAULT_GAME_ID);

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);
        when(playerService.loadByUserId(eq(DEFAULT_GAME_ID), eq(2L))).thenAnswer(invocation -> {
            return Mockito.mock(Player.class);
        });

        try {
            gameService.join(DEFAULT_GAME_ID, 2L);
            fail("Player is not in the game");
        } catch (GameException e) {
            assertEquals(GameError.IN_THE_GAME.getCode(), e.getCode());
        }
    }

    @Test
    void test_join_game_reaches_expected_player_count() {
        final Game sampleGame = createSampleGame(DEFAULT_USER_ID, 2);

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);

        when(gameDao.save(any(Game.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        final Game game = gameService.join(DEFAULT_GAME_ID, 2L);

        assertEquals(GameStatus.READY_TO_START, game.getStatus());
    }

    @Test
    void test_leave_game() {
        final Game sampleGame = createSampleGame(DEFAULT_USER_ID, 3);
        sampleGame.setId(DEFAULT_GAME_ID);
        sampleGame.setActionCounter(2);
        sampleGame.setActivePlayerCount(2);

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);

        when(gameDao.save(any(Game.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        when(playerService.loadByUserId(eq(DEFAULT_GAME_ID), eq(2L))).thenAnswer(invocation -> {
            return Mockito.mock(Player.class);
        });

        final Game game = gameService.leave(DEFAULT_GAME_ID, 2L);

        verify(playerService, times(1)).remove(any(Player.class));

        assertEquals(GameStatus.WAITING, game.getStatus());
        assertEquals(3, game.getActionCounter());
        assertEquals(1, game.getActivePlayerCount());
    }

    @Test
    void test_leave_started_game() {
        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenAnswer(invocation -> {
            final Game game = new Game();
            game.setStatus(GameStatus.IN_PROGRESS);
            return game;
        });

        try {
            gameService.leave(DEFAULT_GAME_ID, 2L);
            fail("The game is not in progress");
        } catch (GameException e) {
            assertEquals(GameError.IN_PROGRESS.getCode(), e.getCode());
        }
    }

    @Test
    void test_leave_game_with_not_existing_player() {
        final Game sampleGame = createSampleGame(DEFAULT_USER_ID, 3);

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);

        try {
            gameService.leave(DEFAULT_GAME_ID, 2L);
            fail("Player is in the game");
        } catch (GameException e) {
            assertEquals(GameError.NOT_IN_THE_GAME.getCode(), e.getCode());
        }
    }

    @Test
    void test_leave_game_with_owner() {
        final Game sampleGame = createSampleGame(DEFAULT_USER_ID, 3);
        sampleGame.setId(DEFAULT_GAME_ID);

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);

        when(playerService.loadByUserId(eq(DEFAULT_GAME_ID), eq(1L))).thenAnswer(invocation -> {
            final Player player = new Player();
            player.setUserId(invocation.getArgument(0));
            return player;
        });

        try {
            gameService.leave(DEFAULT_GAME_ID, 1L);
            fail("Owner is able to leave the game");
        } catch (GameException e) {
            assertEquals(GameError.OWNER_CANNOT_LEAVE.getCode(), e.getCode());
        }
    }

    @Test
    void test_start_game() {
        final Game sampleGame = createSampleGame(DEFAULT_USER_ID, 2);
        sampleGame.setStatus(GameStatus.READY_TO_START);

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);

        when(gameDao.save(any(Game.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        final Game game = gameService.start(DEFAULT_GAME_ID);

        assertNotNull(game.getStartDate());
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertEquals("My game", game.getName());
        assertEquals(2, game.getExpectedPlayerCount());
        assertEquals(2, game.getDuration());
        assertEquals(1, game.getCurrentPlayerNumber());
        assertEquals(1, game.getRoundNumber());
        assertEquals(2, game.getActionCounter());
    }

    @Test
    void test_start_waiting_game() {
        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenAnswer(invocation -> {
            final Game game = new Game();
            game.setStatus(GameStatus.WAITING);
            return game;
        });

        try {
            gameService.start(DEFAULT_GAME_ID);
            fail("The game is not in progress");
        } catch (GameException e) {
            assertEquals(GameError.WAITING.getCode(), e.getCode());
        }
    }

    @Test
    void test_start_started_game() {
        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenAnswer(invocation -> {
            final Game game = new Game();
            game.setStatus(GameStatus.IN_PROGRESS);
            return game;
        });

        try {
            gameService.start(DEFAULT_GAME_ID);
            fail("The game is not in progress");
        } catch (GameException e) {
            assertEquals(GameError.IN_PROGRESS.getCode(), e.getCode());
        }
    }

    @Test
    void test_update_game() {
        final Game sampleGame = createSampleGame(DEFAULT_USER_ID, 2);
        sampleGame.setId(DEFAULT_GAME_ID);
        sampleGame.setName("The best game");
        sampleGame.setExpectedPlayerCount(4);
        sampleGame.setDuration(4);

        final Game existingGame = createSampleGame(DEFAULT_USER_ID, 2);

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(existingGame);

        when(gameDao.save(any(Game.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        final Game game = gameService.save(sampleGame);

        assertEquals("The best game", game.getName());
        assertEquals(4, game.getExpectedPlayerCount());
        assertEquals(4, game.getDuration());
    }

    @Test
    void test_update_waiting_game() {
        final Game sampleGame = createSampleGame(DEFAULT_USER_ID, 2);
        sampleGame.setId(DEFAULT_GAME_ID);
        sampleGame.setName("The best game");
        sampleGame.setExpectedPlayerCount(4);
        sampleGame.setDuration(4);

        final Game existingGame = createSampleGame(DEFAULT_USER_ID, 2);
        existingGame.setActionCounter(2);

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(existingGame);

        try {
            gameService.save(sampleGame);
            fail("The game is not waiting");
        } catch (GameException e) {
            assertEquals(GameError.IN_PROGRESS.getCode(), e.getCode());
        }
    }

    @Test
    void test_play_not_started_game() {
        final Game sampleGame = createSampleGame(DEFAULT_USER_ID, 2);

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack());
            fail("The game is started");
        } catch (GameException e) {
            assertEquals(GameError.WAITING.getCode(), e.getCode());
        }
    }

    @Test
    void test_play_center_is_empty() {
        prepareGame();
        prepareBoard();
        // the word WEAK is not using the center
        prepareUsedRackByRow(3, 7, "WEAK");
        prepareRepository();

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles));
            fail("Starting cell is not empty");
        } catch (GameException e) {
            assertEquals(GameError.CENTER_CANNOT_BE_EMPTY.getCode(), e.getCode());
        }
    }

    @Test
    void test_play_center_is_not_empty() {
        prepareGame();
        preparePlayer(0);
        prepareBoard();
        // the word WEAK is using the center
        prepareUsedRackByRow(8, 7, "WEAK");
        prepareRepository();

        // the word is valid
        when(dictionaryService.hasWord(any(String.class), any(Language.class))).thenReturn(true);

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles));

        // the word HEAL is found in the dictionary
        verify(dictionaryService, times(1)).hasWord("WEAK", Language.en);

        final Player updatedPlayer = new Player();
        updatedPlayer.setPlayerNumber(1);
        updatedPlayer.setScore(11);

        // the word score is added to the player score
        verify(playerService, times(1)).save(updatedPlayer);

        final Word word = new Word();
        word.setGameId(DEFAULT_GAME_ID);
        word.setPlayerNumber(1);
        word.setRoundNumber(1);
        word.setScore(11);
        word.setWord("WEAK");

        // the word WEAK is logged in the words
        verify(wordService, times(1)).save(word);
    }

    @Test
    void test_play_word_is_not_valid() {
        prepareGame();
        prepareBoard();
        prepareUsedRackByRow(8, 7, "WEAK");
        prepareRepository();

        // the word is not valid
        when(dictionaryService.hasWord(eq("WEAK"), any(Language.class))).thenReturn(false);

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles));
            fail("The word is valid");
        } catch (GameException e) {
            assertEquals(GameError.WORDS_ARE_NOT_FOUND.getCode(), e.getCode());
        }
    }

    @Test
    void test_play_new_word_not_linked_to_existing_words() {
        prepareGame();
        prepareBoard();
        prepareExistingWordByRow(8, 7, "WEAK");
        // the word HEAL not linked to an existing word
        prepareUsedRackByRow(3, 7, "HEAL");
        prepareRepository();

        // the words are valid
        when(dictionaryService.hasWord(any(String.class), any(Language.class))).thenReturn(true);

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles));
            fail("New word is linked to existing words");
        } catch (GameException e) {
            assertEquals(GameError.WORDS_ARE_NOT_LINKED.getCode(), e.getCode());
        }
    }

    @Test
    void test_play_new_words_linked_to_existing_words() {
        prepareGame();
        preparePlayer(11);
        prepareBoard();
        // the word WEAK is an existing word in the board
        prepareExistingWordByRow(8, 7, "WEAK");
        // the word RA(W) is directly linked to the existing WEAK word
        prepareUsedRackByColumn(6, 7, "RA");
        // the word (R)OLE is linked to the new RAW word
        prepareUsedRackByRow(6, 8, "OLE");
        // the word W(E) is linked to the new ROLE word
        prepareUsedRackByColumn(5, 10, "W");
        prepareRepository();

        // the words are valid
        when(dictionaryService.hasWord(any(String.class), any(Language.class))).thenReturn(true);

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles));

        // the words are found in the dictionary
        verify(dictionaryService, times(1)).hasWord("RAW", Language.en);
        verify(dictionaryService, times(1)).hasWord("ROLE", Language.en);
        verify(dictionaryService, times(1)).hasWord("WE", Language.en);

        final Player updatedPlayer = new Player();
        updatedPlayer.setPlayerNumber(1);
        updatedPlayer.setScore(31);

        // the word score is added to the player score
        verify(playerService, times(1)).save(updatedPlayer);

        Word word = new Word();
        word.setGameId(DEFAULT_GAME_ID);
        word.setPlayerNumber(1);
        word.setRoundNumber(1);
        word.setScore(7);
        word.setWord("RAW");

        // the word RAW is logged in the words
        verify(wordService, times(1)).save(word);

        word = new Word();
        word.setGameId(DEFAULT_GAME_ID);
        word.setPlayerNumber(1);
        word.setRoundNumber(1);
        word.setScore(6);
        word.setWord("ROLE");

        // the word ROLE is logged in the words
        verify(wordService, times(1)).save(word);

        word = new Word();
        word.setGameId(DEFAULT_GAME_ID);
        word.setPlayerNumber(1);
        word.setRoundNumber(1);
        word.setScore(7);
        word.setWord("WE");

        // the word WE is logged in the words
        verify(wordService, times(1)).save(word);
    }

    @Test
    void test_play_word_detection_ends_in_right() {
        prepareGame();
        prepareBoard();
        prepareExistingWordByRow(8, 8, "WEAK");
        prepareUsedRackByRow(8, 12, "ENIN");
        prepareUsedRackByRow(9, 1, "G");
        prepareRepository();

        // the words are valid
        when(dictionaryService.hasWord(eq("WEAKENIN"), any(Language.class))).thenReturn(false);

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles));
            fail("Word detection does not end in right");
        } catch (GameException e) {
            assertEquals(GameError.WORDS_ARE_NOT_FOUND.getCode(), e.getCode());
        }
    }

    @Test
    void test_play_word_detection_ends_in_bottom() {
        prepareGame();
        prepareBoard();
        prepareExistingWordByColumn(8, 8, "WEAK");
        prepareUsedRackByColumn(12, 8, "ENIN");
        prepareUsedRackByColumn(1, 9, "G");
        prepareRepository();

        // the words are valid
        when(dictionaryService.hasWord(eq("WEAKENIN"), any(Language.class))).thenReturn(false);

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles));
            fail("Word detection does not end in bottom");
        } catch (GameException e) {
            assertEquals(GameError.WORDS_ARE_NOT_FOUND.getCode(), e.getCode());
        }
    }

    @Test
    void test_play_extend_an_existing_word() {
        prepareGame();
        preparePlayer(11);
        prepareBoard();
        // the word WEAK is an existing word in the board
        prepareExistingWordByRow(8, 7, "WEAK");
        // the word (WEAK)ENING will extend the existing WEAK word
        prepareUsedRackByRow(8, 11, "ENING");
        prepareRepository();

        // the words are valid
        when(dictionaryService.hasWord(any(String.class), any(Language.class))).thenReturn(true);

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles));

        // the words are found in the dictionary
        verify(dictionaryService, times(1)).hasWord("WEAKENING", Language.en);

        final Player updatedPlayer = new Player();
        updatedPlayer.setPlayerNumber(1);
        updatedPlayer.setScore(65);

        // the word score is added to the player score
        verify(playerService, times(1)).save(updatedPlayer);

        Word word = new Word();
        word.setGameId(DEFAULT_GAME_ID);
        word.setPlayerNumber(1);
        word.setRoundNumber(1);
        word.setScore(54);
        word.setWord("WEAKENING");

        // the word WE is logged in the words
        verify(wordService, times(1)).save(word);
    }

    @Test
    void test_play_use_a_letter_from_an_extended_word() {
        prepareGame();
        preparePlayer(11);
        prepareBoard();
        // the word WEAK is an existing word in the board
        prepareExistingWordByRow(8, 7, "WEAK");
        // the word (WEAK)ENING will extend the existing WEAK word
        prepareUsedRackByRow(8, 11, "ENING");
        // the word (G)OAL will be linked to the WEAKENING word
        prepareUsedRackByColumn(9, 15, "OAL");
        prepareRepository();

        // the words are valid
        when(dictionaryService.hasWord(any(String.class), any(Language.class))).thenReturn(true);

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles));

        // the words are found in the dictionary
        verify(dictionaryService, times(1)).hasWord("WEAKENING", Language.en);
        verify(dictionaryService, times(1)).hasWord("GOAL", Language.en);

        final Player updatedPlayer = new Player();
        updatedPlayer.setPlayerNumber(1);
        updatedPlayer.setScore(80);

        // the word score is added to the player score
        verify(playerService, times(1)).save(updatedPlayer);

        // the word WEAKENING is logged in the words
        Word word = new Word();
        word.setGameId(DEFAULT_GAME_ID);
        word.setPlayerNumber(1);
        word.setRoundNumber(1);
        word.setScore(54);
        word.setWord("WEAKENING");

        verify(wordService, times(1)).save(word);

        // the word WEAKENING is logged in the words
        word = new Word();
        word.setGameId(DEFAULT_GAME_ID);
        word.setPlayerNumber(1);
        word.setRoundNumber(1);
        word.setScore(15);
        word.setWord("GOAL");

        verify(wordService, times(1)).save(word);
    }

    @Test
    void test_play_the_same_word_more_than_once() {
        prepareGame();
        preparePlayer(11);
        prepareBoard();
        // the word WEAK is an existing word in the board
        prepareExistingWordByRow(8, 7, "WEAK");
        // the word (G)OAL will be linked to the WEAKENING word
        prepareUsedRackByColumn(9, 7, "EAK");
        prepareRepository();

        // the words are valid
        when(dictionaryService.hasWord(any(String.class), any(Language.class))).thenReturn(true);

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles));

        // the word is found in the dictionary
        verify(dictionaryService, times(1)).hasWord("WEAK", Language.en);

        final Player updatedPlayer = new Player();
        updatedPlayer.setPlayerNumber(1);
        updatedPlayer.setScore(23);

        // the word score is added to the player score
        verify(playerService, times(1)).save(updatedPlayer);

        // the word WEAKENING is logged in the words
        Word word = new Word();
        word.setGameId(DEFAULT_GAME_ID);
        word.setPlayerNumber(1);
        word.setRoundNumber(1);
        word.setScore(12);
        word.setWord("WEAK");

        verify(wordService, times(1)).save(word);
    }

    private void prepareGame() {
        game = createSampleGame(DEFAULT_USER_ID, 2);
        game.setId(DEFAULT_GAME_ID);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setCurrentPlayerNumber(1);
        game.setActionCounter(3);
        game.setRoundNumber(1);

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(game);
        when(boardService.get(eq(DEFAULT_BOARD_ID))).thenReturn(createSampleBoard());
        when(bagService.get(eq(DEFAULT_BAG_ID))).thenReturn(createSampleBag());

        when(playerService.loadByUserId(eq(DEFAULT_GAME_ID), eq(1L))).thenAnswer(invocation -> {
            final Player player = new Player();
            player.setPlayerNumber(1);
            return player;
        });
    }

    private void preparePlayer(int score) {
        when(playerService.loadByPlayerNumber(eq(DEFAULT_GAME_ID), eq(1))).thenAnswer(invocation -> {
            final Player player = new Player();
            player.setPlayerNumber(1);
            player.setScore(score);
            return player;
        });
    }

    private void prepareUsedRackByRow(int startingRow, int startingColumn, String word) {
        int tileNumber = tiles.size() + 1;
        int columnNumber = startingColumn;
        for (char letter : word.toCharArray()) {
            final VirtualTile tile = VirtualTile.builder()
                    .columnNumber(columnNumber)
                    .letter(String.valueOf(letter).toUpperCase())
                    .number(tileNumber++)
                    .playerNumber(1)
                    .rowNumber(startingRow)
                    .sealed(true)
                    .value(LETTER_VALUE_MAP.get(String.valueOf(letter).toUpperCase()))
                    .build();
            tiles.add(tile);
            columnNumber = columnNumber + 1;
        }
    }

    private void prepareUsedRackByColumn(int startingRow, int startingColumn, String word) {
        int tileNumber = tiles.size() + 1;
        int rowNumber = startingRow;
        for (char letter : word.toCharArray()) {
            final VirtualTile tile = VirtualTile.builder()
                    .columnNumber(startingColumn)
                    .letter(String.valueOf(letter).toUpperCase())
                    .number(tileNumber++)
                    .playerNumber(1)
                    .rowNumber(rowNumber)
                    .sealed(true)
                    .value(LETTER_VALUE_MAP.get(String.valueOf(letter).toUpperCase()))
                    .build();
            tiles.add(tile);
            rowNumber = rowNumber + 1;
        }
    }

    private void prepareExistingWordByRow(int startingRow, int startingColumn, String word) {
        int columnNumber = startingColumn;
        for (char letter : word.toCharArray()) {
            boardMatrix[startingRow - 1][columnNumber - 1].setLetter(String.valueOf(letter).toUpperCase());
            boardMatrix[startingRow - 1][columnNumber - 1].setSealed(true);
            boardMatrix[startingRow - 1][columnNumber - 1]
                    .setValue(LETTER_VALUE_MAP.get(String.valueOf(letter).toUpperCase()));
            columnNumber = columnNumber + 1;
        }
    }

    private void prepareExistingWordByColumn(int startingRow, int startingColumn, String word) {
        int rowNumber = startingRow;
        for (char letter : word.toCharArray()) {
            boardMatrix[rowNumber - 1][startingColumn - 1].setLetter(String.valueOf(letter).toUpperCase());
            boardMatrix[rowNumber - 1][startingColumn - 1].setSealed(true);
            boardMatrix[rowNumber - 1][startingColumn - 1]
                    .setValue(LETTER_VALUE_MAP.get(String.valueOf(letter).toUpperCase()));
            rowNumber = rowNumber + 1;
        }
    }

    private void prepareBoard() {
        // ---------------------- prepare the board ---------------------- //
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

    private void prepareRepository() {
        final List<VirtualCell> cells = Arrays.stream(boardMatrix).flatMap(Arrays::stream).collect(Collectors.toList());
        when(virtualBoardService.getBoard(eq(DEFAULT_GAME_ID), eq(1))).thenReturn(new VirtualBoard(cells));
        when(virtualRackService.getRack(eq(DEFAULT_GAME_ID), eq(1), eq(1))).thenReturn(new VirtualRack(false, tiles));
    }

    private Integer getLetterValueMultiplier(Integer cellNumber) {
        if (Arrays.asList(21, 25, 77, 81, 85, 89, 137, 141, 145, 149, 201, 205).contains(cellNumber)) {
            return 3;
        } else if (Arrays
                .asList(4, 12, 37, 39, 46, 53, 60, 93, 97, 99, 103, 109, 117, 123, 127, 129, 133, 166, 173, 180, 187,
                        189, 214, 222)
                .contains(cellNumber)) {
            return 2;
        } else {
            return 1;
        }
    }

    private Integer getWordScoreMultiplier(Integer cellNumber) {
        if (Arrays.asList(1, 8, 15, 106, 120, 211, 218, 225).contains(cellNumber)) {
            return 3;
        } else if (Arrays.asList(17, 29, 33, 43, 49, 57, 65, 71, 155, 161, 169, 177, 183, 193, 197, 209)
                .contains(cellNumber)) {
            return 2;
        } else {
            return 1;
        }
    }

}
