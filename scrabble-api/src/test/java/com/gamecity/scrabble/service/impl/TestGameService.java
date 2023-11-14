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

import com.gamecity.scrabble.dao.GameDao;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestGameService extends AbstractServiceTest {

    private static final Map<String, Tile> TILE_MAP = new HashMap<>();

    @InjectMocks
    private GameService gameService = new GameServiceImpl();

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

    private Game game;
    private List<VirtualTile> tiles;
    private VirtualCell[][] boardMatrix;

    @BeforeEach
    void beforeEach() {
        tiles = new ArrayList<>(7);
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
        assertEquals(1, game.getVersion());
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
        assertEquals(2, game.getVersion());
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
        when(playerService.getByUserId(eq(DEFAULT_GAME_ID), eq(2L))).thenAnswer(invocation -> {
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
        sampleGame.setVersion(2);
        sampleGame.setActivePlayerCount(2);

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);

        when(gameDao.save(any(Game.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        when(playerService.getByUserId(eq(DEFAULT_GAME_ID), eq(2L))).thenAnswer(invocation -> {
            return Mockito.mock(Player.class);
        });

        final Game game = gameService.leave(DEFAULT_GAME_ID, 2L);

        verify(playerService, times(1)).remove(any(Player.class));

        assertEquals(GameStatus.WAITING, game.getStatus());
        assertEquals(3, game.getVersion());
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

        when(playerService.getByUserId(eq(DEFAULT_GAME_ID), eq(1L))).thenAnswer(invocation -> {
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
        sampleGame.setId(DEFAULT_GAME_ID);
        sampleGame.setStatus(GameStatus.READY_TO_START);

        when(gameDao.get(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);

        when(gameDao.save(any(Game.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(virtualBagService.getTiles(eq(DEFAULT_GAME_ID), eq(Language.valueOf(DEFAULT_BAG_LANGUAGE))))
                .thenReturn(TILE_MAP.values().stream().collect(Collectors.toList()));

        final Game game = gameService.start(DEFAULT_GAME_ID);

        assertNotNull(game.getStartDate());
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertEquals("My game", game.getName());
        assertEquals(2, game.getExpectedPlayerCount());
        assertEquals(2, game.getDuration());
        assertEquals(1, game.getCurrentPlayerNumber());
        assertEquals(1, game.getRoundNumber());
        assertEquals(2, game.getVersion());
        assertEquals(84, game.getRemainingTileCount());
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
        existingGame.setVersion(2);

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

        when(gameDao.getAndLock(eq(DEFAULT_GAME_ID))).thenReturn(sampleGame);

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(), ActionType.PLAY);
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
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles), ActionType.PLAY);
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
        final DictionaryWord weakWord = DictionaryWord.builder().word("WEAK").build();
        when(dictionaryService.getWord(any(String.class), any(Language.class))).thenReturn(weakWord);

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any())).thenReturn(createSampleAction());

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles), ActionType.PLAY);

        // the word HEAL is found in the dictionary
        verify(dictionaryService, times(1)).getWord("WEAK", Language.en);

        final Player updatedPlayer = new Player();
        updatedPlayer.setPlayerNumber(1);
        updatedPlayer.setScore(22);

        // the word score is added to the player score
        verify(playerService, times(1)).save(updatedPlayer);

        final Word word = new Word();
        word.setActionId(DEFAULT_ACTION_ID);
        word.setGameId(DEFAULT_GAME_ID);
        word.setUserId(DEFAULT_USER_ID);
        word.setRoundNumber(1);
        word.setScore(22);
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
        when(dictionaryService.getWord(eq("WEAK"), any(Language.class))).thenReturn(null);

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles), ActionType.PLAY);
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
        final DictionaryWord healWord = DictionaryWord.builder().word("HEAL").build();
        when(dictionaryService.getWord(eq("HEAL"), any(Language.class))).thenReturn(healWord);

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles), ActionType.PLAY);
            fail("New word is linked to existing words");
        } catch (GameException e) {
            assertEquals(GameError.WORDS_ARE_NOT_LINKED.getCode(), e.getCode());
        }
    }

    @Test
    void test_play_new_horizontal_and_vertical_words() {
        prepareGame();
        preparePlayer(0);
        prepareBoard();
        // create the word WEAK
        prepareUsedRackByRow(8, 7, "WEAK");
        // create the word WAR
        prepareUsedRackByColumn(9, 7, "AR");
        prepareRepository();

        // the words are valid
        final DictionaryWord weakWord = DictionaryWord.builder().word("WEAK").build();
        when(dictionaryService.getWord(eq("WEAK"), any(Language.class))).thenReturn(weakWord);
        final DictionaryWord warWord = DictionaryWord.builder().word("WAR").build();
        when(dictionaryService.getWord(eq("WAR"), any(Language.class))).thenReturn(warWord);

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any())).thenReturn(createSampleAction());

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles), ActionType.PLAY);

        // the words are found in the dictionary
        verify(dictionaryService, times(1)).getWord("WEAK", Language.en);
        verify(dictionaryService, times(1)).getWord("WAR", Language.en);

        final Player updatedPlayer = new Player();
        updatedPlayer.setPlayerNumber(1);
        updatedPlayer.setScore(29);

        // the word score is added to the player score
        verify(playerService, times(1)).save(updatedPlayer);

        Word word = new Word();
        word.setActionId(DEFAULT_ACTION_ID);
        word.setGameId(DEFAULT_GAME_ID);
        word.setUserId(DEFAULT_USER_ID);
        word.setRoundNumber(1);
        word.setScore(22);
        word.setWord("WEAK");

        // the word WEAK is logged in the words
        verify(wordService, times(1)).save(word);

        word = new Word();
        word.setActionId(DEFAULT_ACTION_ID);
        word.setGameId(DEFAULT_GAME_ID);
        word.setUserId(DEFAULT_USER_ID);
        word.setRoundNumber(1);
        word.setScore(7);
        word.setWord("WAR");

        // the word WAR is logged in the words
        verify(wordService, times(1)).save(word);
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
        final DictionaryWord rawWord = DictionaryWord.builder().word("RAW").build();
        when(dictionaryService.getWord(eq("RAW"), any(Language.class))).thenReturn(rawWord);
        final DictionaryWord roleWord = DictionaryWord.builder().word("ROLE").build();
        when(dictionaryService.getWord(eq("ROLE"), any(Language.class))).thenReturn(roleWord);
        final DictionaryWord weWord = DictionaryWord.builder().word("WE").build();
        when(dictionaryService.getWord(eq("WE"), any(Language.class))).thenReturn(weWord);

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any())).thenReturn(createSampleAction());

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles), ActionType.PLAY);

        // the words are found in the dictionary
        verify(dictionaryService, times(1)).getWord("RAW", Language.en);
        verify(dictionaryService, times(1)).getWord("ROLE", Language.en);
        verify(dictionaryService, times(1)).getWord("WE", Language.en);

        final Player updatedPlayer = new Player();
        updatedPlayer.setPlayerNumber(1);
        updatedPlayer.setScore(31);

        // the word score is added to the player score
        verify(playerService, times(1)).save(updatedPlayer);

        Word word = new Word();
        word.setActionId(DEFAULT_ACTION_ID);
        word.setGameId(DEFAULT_GAME_ID);
        word.setUserId(DEFAULT_USER_ID);
        word.setRoundNumber(1);
        word.setScore(7);
        word.setWord("RAW");

        // the word RAW is logged in the words
        verify(wordService, times(1)).save(word);

        word = new Word();
        word.setActionId(DEFAULT_ACTION_ID);
        word.setGameId(DEFAULT_GAME_ID);
        word.setUserId(DEFAULT_USER_ID);
        word.setRoundNumber(1);
        word.setScore(6);
        word.setWord("ROLE");

        // the word ROLE is logged in the words
        verify(wordService, times(1)).save(word);

        word = new Word();
        word.setActionId(DEFAULT_ACTION_ID);
        word.setGameId(DEFAULT_GAME_ID);
        word.setUserId(DEFAULT_USER_ID);
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

        // the words are not valid
        when(dictionaryService.getWord(eq("WEAKENIN"), any(Language.class))).thenReturn(null);

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles), ActionType.PLAY);
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

        // the words are not valid
        when(dictionaryService.getWord(eq("WEAKENIN"), any(Language.class))).thenReturn(null);

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles), ActionType.PLAY);
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
        final DictionaryWord weakeningWord = DictionaryWord.builder().word("WEAKENING").build();
        when(dictionaryService.getWord(eq("WEAKENING"), any(Language.class))).thenReturn(weakeningWord);

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any())).thenReturn(createSampleAction());

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles), ActionType.PLAY);

        // the words are found in the dictionary
        verify(dictionaryService, times(1)).getWord("WEAKENING", Language.en);

        final Player updatedPlayer = new Player();
        updatedPlayer.setPlayerNumber(1);
        updatedPlayer.setScore(65);

        // the word score is added to the player score
        verify(playerService, times(1)).save(updatedPlayer);

        Word word = new Word();
        word.setActionId(DEFAULT_ACTION_ID);
        word.setGameId(DEFAULT_GAME_ID);
        word.setUserId(DEFAULT_USER_ID);
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
        final DictionaryWord weakeningWord = DictionaryWord.builder().word("WEAKENING").build();
        when(dictionaryService.getWord(eq("WEAKENING"), any(Language.class))).thenReturn(weakeningWord);
        final DictionaryWord goalWord = DictionaryWord.builder().word("GOAL").build();
        when(dictionaryService.getWord(eq("GOAL"), any(Language.class))).thenReturn(goalWord);

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any())).thenReturn(createSampleAction());

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles), ActionType.PLAY);

        // the words are found in the dictionary
        verify(dictionaryService, times(1)).getWord("WEAKENING", Language.en);
        verify(dictionaryService, times(1)).getWord("GOAL", Language.en);

        final Player updatedPlayer = new Player();
        updatedPlayer.setPlayerNumber(1);
        updatedPlayer.setScore(80);

        // the word score is added to the player score
        verify(playerService, times(1)).save(updatedPlayer);

        // the word WEAKENING is logged in the words
        Word word = new Word();
        word.setActionId(DEFAULT_ACTION_ID);
        word.setGameId(DEFAULT_GAME_ID);
        word.setUserId(DEFAULT_USER_ID);
        word.setRoundNumber(1);
        word.setScore(54);
        word.setWord("WEAKENING");

        verify(wordService, times(1)).save(word);

        // the word WEAKENING is logged in the words
        word = new Word();
        word.setActionId(DEFAULT_ACTION_ID);
        word.setGameId(DEFAULT_GAME_ID);
        word.setUserId(DEFAULT_USER_ID);
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
        final DictionaryWord weakWord = DictionaryWord.builder().word("WEAK").build();
        when(dictionaryService.getWord(eq("WEAK"), any(Language.class))).thenReturn(weakWord);

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any())).thenReturn(createSampleAction());

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles), ActionType.PLAY);

        // the word is found in the dictionary
        verify(dictionaryService, times(1)).getWord("WEAK", Language.en);

        final Player updatedPlayer = new Player();
        updatedPlayer.setPlayerNumber(1);
        updatedPlayer.setScore(23);

        // the word score is added to the player score
        verify(playerService, times(1)).save(updatedPlayer);

        // the word WEAKENING is logged in the words
        Word word = new Word();
        word.setActionId(DEFAULT_ACTION_ID);
        word.setGameId(DEFAULT_GAME_ID);
        word.setUserId(DEFAULT_USER_ID);
        word.setRoundNumber(1);
        word.setScore(12);
        word.setWord("WEAK");

        verify(wordService, times(1)).save(word);
    }

    @Test
    void test_play_single_letter_not_allowed() {
        prepareGame();
        prepareBoard();
        prepareUsedRackByColumn(8, 8, "A");
        prepareRepository();

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles), ActionType.PLAY);
            fail("Single letter words are not detected");
        } catch (GameException e) {
            assertEquals(GameError.SINGLE_LETTER_WORDS_NOT_ALLOWED.getCode(), e.getCode());
        }
    }

    @Test
    void test_play_single_letter_with_valid_word_not_allowed() {
        prepareGame();
        prepareBoard();
        prepareUsedRackByColumn(8, 8, "WEAK");
        prepareUsedRackByColumn(1, 1, "A");
        prepareRepository();

        // the words are valid
        final DictionaryWord weakWord = DictionaryWord.builder().word("WEAK").build();
        when(dictionaryService.getWord(eq("WEAK"), any(Language.class))).thenReturn(weakWord);

        try {
            gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles), ActionType.PLAY);
            fail("Single letter words are not detected");
        } catch (GameException e) {
            assertEquals(GameError.SINGLE_LETTER_WORDS_NOT_ALLOWED.getCode(), e.getCode());
        }
    }

    @Test
    void test_play_multiplier_cell_value_only_used_in_first_word() {
        prepareGame();
        preparePlayer(22);
        prepareBoard();
        // the word WEAK is an existing word in the board
        prepareExistingWordByRow(8, 7, "WEAK");
        // extend the word WEAK(ER)
        prepareUsedRackByRow(8, 11, "ER");
        // create the word WAR
        prepareUsedRackByColumn(9, 8, "RRAT");
        prepareRepository();

        // the words are valid
        final DictionaryWord weakerWord = DictionaryWord.builder().word("WEAKER").build();
        when(dictionaryService.getWord(eq("WEAKER"), any(Language.class))).thenReturn(weakerWord);
        final DictionaryWord erratWord = DictionaryWord.builder().word("ERRAT").build();
        when(dictionaryService.getWord(eq("ERRAT"), any(Language.class))).thenReturn(erratWord);

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any())).thenReturn(createSampleAction());

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles), ActionType.PLAY);

        // the words are found in the dictionary
        verify(dictionaryService, times(1)).getWord("WEAKER", Language.en);
        verify(dictionaryService, times(1)).getWord("ERRAT", Language.en);

        final Player updatedPlayer = new Player();
        updatedPlayer.setPlayerNumber(1);
        updatedPlayer.setScore(42);

        // the word score is added to the player score
        verify(playerService, times(1)).save(updatedPlayer);

        Word word = new Word();
        word.setActionId(DEFAULT_ACTION_ID);
        word.setGameId(DEFAULT_GAME_ID);
        word.setUserId(DEFAULT_USER_ID);
        word.setRoundNumber(1);
        word.setScore(14);
        word.setWord("WEAKER");

        // the word WEAKER is logged in the words
        verify(wordService, times(1)).save(word);

        word = new Word();
        word.setActionId(DEFAULT_ACTION_ID);
        word.setGameId(DEFAULT_GAME_ID);
        word.setUserId(DEFAULT_USER_ID);
        word.setRoundNumber(1);
        word.setScore(6);
        word.setWord("ERRAT");

        // the word ERRAT is logged in the words
        verify(wordService, times(1)).save(word);
    }

    @Test
    void test_play_all_tiles_to_get_bonus_score() {
        prepareGame();
        preparePlayer(0);
        prepareBoard();
        // create the word PREPARE
        prepareUsedRackByRow(8, 7, "PREPARE");
        prepareRepository();

        // the word is valid
        final DictionaryWord prepareWord = DictionaryWord.builder().word("PREPARE").build();
        when(dictionaryService.getWord(eq("PREPARE"), any(Language.class))).thenReturn(prepareWord);

        when(gameDao.save(any())).thenReturn(Mockito.mock(Game.class));
        when(actionService.add(any(), any(), any())).thenReturn(createSampleAction());

        gameService.play(DEFAULT_GAME_ID, DEFAULT_USER_ID, new VirtualRack(false, tiles), ActionType.PLAY);

        // the word is found in the dictionary
        verify(dictionaryService, times(1)).getWord("PREPARE", Language.en);

        final Player updatedPlayer = new Player();
        updatedPlayer.setPlayerNumber(1);
        updatedPlayer.setScore(74);

        // the word score is added to the player score
        verify(playerService, times(1)).save(updatedPlayer);

        Word word = new Word();
        word.setActionId(DEFAULT_ACTION_ID);
        word.setGameId(DEFAULT_GAME_ID);
        word.setUserId(DEFAULT_USER_ID);
        word.setRoundNumber(1);
        word.setScore(24);
        word.setWord("PREPARE");

        // the word PREPARE is logged in the words
        verify(wordService, times(1)).save(word);
    }

    private void prepareGame() {
        game = createSampleGame(DEFAULT_USER_ID, 2);
        game.setId(DEFAULT_GAME_ID);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setCurrentPlayerNumber(1);
        game.setVersion(3);
        game.setRoundNumber(1);

        when(gameDao.getAndLock(eq(DEFAULT_GAME_ID))).thenReturn(game);

        when(playerService.getByUserId(eq(DEFAULT_GAME_ID), eq(1L))).thenAnswer(invocation -> {
            final Player player = new Player();
            player.setPlayerNumber(1);
            return player;
        });
    }

    private void preparePlayer(int score) {
        when(playerService.getByPlayerNumber(eq(DEFAULT_GAME_ID), eq(1))).thenAnswer(invocation -> {
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
                    .value(TILE_MAP.get(String.valueOf(letter).toUpperCase()).getValue())
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
                    .value(TILE_MAP.get(String.valueOf(letter).toUpperCase()).getValue())
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
        } else if (Arrays.asList(17, 29, 33, 43, 49, 57, 65, 71, 113, 155, 161, 169, 177, 183, 193, 197, 209)
                .contains(cellNumber)) {
            return 2;
        } else {
            return 1;
        }
    }

}
