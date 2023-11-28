package com.gamecity.scrabble.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.gamecity.scrabble.dao.GameDao;
import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.ActionType;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.entity.Tile;
import com.gamecity.scrabble.entity.GameStatus;
import com.gamecity.scrabble.entity.Word;
import com.gamecity.scrabble.entity.User;
import com.gamecity.scrabble.model.Bonus;
import com.gamecity.scrabble.model.ConstructedWord;
import com.gamecity.scrabble.model.ConstructedWord.Direction;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.VirtualCell;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.model.VirtualTile;
import com.gamecity.scrabble.service.ActionService;
import com.gamecity.scrabble.service.ScoreService;
import com.gamecity.scrabble.service.DictionaryService;
import com.gamecity.scrabble.service.PlayerService;
import com.gamecity.scrabble.service.ContentService;
import com.gamecity.scrabble.service.GameService;
import com.gamecity.scrabble.service.UserService;
import com.gamecity.scrabble.service.VirtualBagService;
import com.gamecity.scrabble.service.VirtualBoardService;
import com.gamecity.scrabble.service.VirtualRackService;
import com.gamecity.scrabble.service.WordService;
import com.gamecity.scrabble.service.exception.GameException;
import com.gamecity.scrabble.service.exception.error.GameError;
import com.gamecity.scrabble.service.helper.GameValidationHelper;

import lombok.extern.slf4j.Slf4j;

import static com.gamecity.scrabble.Constants.Game.BOARD_ROW_SIZE;
import static com.gamecity.scrabble.Constants.Game.BOARD_COLUMN_SIZE;
import static com.gamecity.scrabble.Constants.Game.NO_SCORE;
import static com.gamecity.scrabble.Constants.Game.RACK_SIZE;
import static com.gamecity.scrabble.model.ConstructedWord.Direction.HORIZONTAL;
import static com.gamecity.scrabble.model.ConstructedWord.Direction.VERTICAL;

@Service(value = "gameService")
@Slf4j
class GameServiceImpl extends AbstractServiceImpl<Game, GameDao> implements GameService {

    private UserService userService;
    private PlayerService playerService;
    private VirtualBoardService virtualBoardService;
    private VirtualRackService virtualRackService;
    private VirtualBagService virtualBagService;
    private ContentService contentService;
    private DictionaryService dictionaryService;
    private WordService wordService;
    private ActionService actionService;
    private ScoreService scoreService;

    public GameServiceImpl(UserService userService, PlayerService playerService,
                           VirtualBoardService virtualBoardService, VirtualRackService virtualRackService,
                           VirtualBagService virtualBagService, ContentService contentService,
                           DictionaryService dictionaryService, WordService wordService, ActionService actionService,
                           ScoreService scoreService) {
        this.userService = userService;
        this.playerService = playerService;
        this.virtualBoardService = virtualBoardService;
        this.virtualRackService = virtualRackService;
        this.virtualBagService = virtualBagService;
        this.contentService = contentService;
        this.dictionaryService = dictionaryService;
        this.wordService = wordService;
        this.actionService = actionService;
        this.scoreService = scoreService;
    }

    @Override
    public Game get(Long gameId) {
        final Game game = baseDao.get(gameId);

        if (game == null || GameStatus.TERMINATED == game.getStatus()) {
            throw new GameException(GameError.NOT_FOUND);
        }

        return game;
    }

    @Override
    @Transactional
    public Game save(Game game) {
        if (game.getId() != null) {
            return update(game);
        }

        final User user = userService.get(game.getOwnerId());

        game.setOwnerId(user.getId());
        game.setStatus(GameStatus.WAITING);
        game.setActivePlayerCount(1);
        game.setVersion(1);

        final Game savedGame = baseDao.save(game);

        playerService.add(game.getId(), user.getId(), game.getActivePlayerCount());
        actionService.add(savedGame, game.getOwnerId(), NO_SCORE, ActionType.CREATE);

        log.debug("Game {} is created", game.getId());

        return savedGame;
    }

    private Game update(Game game) {
        final Game existingGame = get(game.getId());

        if (existingGame.getVersion() > 1) {
            throw new GameException(GameError.CANNOT_UPDATE_GAME);
        }

        existingGame.setName(game.getName());
        existingGame.setExpectedPlayerCount(game.getExpectedPlayerCount());
        existingGame.setDuration(game.getDuration());

        return baseDao.save(existingGame);
    }

    @Override
    @Transactional
    public Game join(Long id, Long userId) {
        Assert.notNull(id, "id cannot be null");
        Assert.notNull(userId, "userId cannot be null");

        userService.get(userId);
        final Game game = get(id);

        if (GameStatus.WAITING != game.getStatus()) {
            throw new GameException(GameError.IN_PROGRESS);
        }

        final Player player = playerService.getByUserId(game.getId(), userId);
        if (player != null) {
            throw new GameException(GameError.IN_THE_GAME);
        }

        game.setActivePlayerCount(game.getActivePlayerCount() + 1);
        game.setVersion(game.getVersion() + 1);

        playerService.add(game.getId(), userId, game.getActivePlayerCount());

        // expected player count has been reached
        if (game.getActivePlayerCount().equals(game.getExpectedPlayerCount())) {
            log.info("Expected player count has been reached, game {} is ready to start", game.getId());
            game.setStatus(GameStatus.READY_TO_START);
        }

        final Game updatedGame = baseDao.save(game);

        actionService.add(updatedGame, userId, NO_SCORE, ActionType.JOIN);

        return updatedGame;
    }

    @Override
    @Transactional
    public Game leave(Long id, Long userId) {
        Assert.notNull(id, "id cannot be null");
        Assert.notNull(userId, "userId cannot be null");

        userService.get(userId);
        final Game game = get(id);

        if (GameStatus.WAITING != game.getStatus()) {
            throw new GameException(GameError.IN_PROGRESS);
        }

        final Player player = playerService.getByUserId(game.getId(), userId);
        if (player == null) {
            throw new GameException(GameError.NOT_IN_THE_GAME);
        }

        if (game.getOwnerId().equals(userId)) {
            throw new GameException(GameError.OWNER_CANNOT_LEAVE);
        }

        playerService.remove(player);

        game.setActivePlayerCount(game.getActivePlayerCount() - 1);
        game.setVersion(game.getVersion() + 1);

        final Game updatedGame = baseDao.save(game);

        actionService.add(updatedGame, userId, NO_SCORE, ActionType.LEAVE);

        return updatedGame;
    }

    @Override
    @Transactional
    public Game start(Long id) {
        Assert.notNull(id, "id cannot be null");

        final Game game = get(id);

        if (GameStatus.IN_PROGRESS == game.getStatus()) {
            throw new GameException(GameError.IN_PROGRESS);
        }

        if (GameStatus.WAITING == game.getStatus()) {
            throw new GameException(GameError.WAITING);
        }

        game.setStartDate(new Date());
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setCurrentPlayerNumber(1);
        game.setRoundNumber(1);
        game.setVersion(game.getVersion() + 1);

        final Integer remainingTileCount = virtualBagService.getTiles(game.getId(), game.getLanguage())
                .stream()
                .mapToInt(Tile::getCount)
                .sum();
        game.setRemainingTileCount(remainingTileCount - (game.getExpectedPlayerCount() * RACK_SIZE));

        log.info("Game {} is started", game.getId());

        final Game updatedGame = baseDao.save(game);

        actionService.add(updatedGame, game.getOwnerId(), NO_SCORE, ActionType.START);
        contentService.create(updatedGame);

        return updatedGame;
    }

    @Override
    @Transactional
    public Game play(Long id, Long userId, VirtualRack virtualRack, ActionType actionType) {
        Assert.notNull(id, "id cannot be null");
        Assert.notNull(userId, "userId cannot be null");
        Assert.notNull(virtualRack, "rack cannot be null");

        final Game game = getAndLock(id);

        if (game == null || GameStatus.TERMINATED == game.getStatus() || GameStatus.ENDED == game.getStatus()) {
            throw new GameException(GameError.NOT_FOUND);
        } else if (GameStatus.WAITING == game.getStatus()) {
            throw new GameException(GameError.WAITING);
        }

        final Player player = playerService.getByUserId(game.getId(), userId);
        final Integer currentRoundNumber = game.getRoundNumber();

        GameValidationHelper.hasCurrentPlayerTurn(player.getPlayerNumber(), game.getCurrentPlayerNumber());
        virtualRackService.validateRack(game.getId(), player.getPlayerNumber(), game.getRoundNumber(), virtualRack);

        // TODO create a service/utility to calculate board version
        final Integer boardVersion = game.getVersion() - game.getExpectedPlayerCount();
        final VirtualBoard virtualBoard = virtualBoardService.getBoard(game.getId(), boardVersion);

        if (ActionType.SKIP == actionType || ActionType.TIMEOUT == actionType) {
            skip(userId, game, actionType);
        } else if (ActionType.EXCHANGE == actionType) {
            exchange(userId, game, player, virtualRack);
        } else {
            play(userId, game, virtualRack, virtualBoard);
        }

        contentService.update(game, virtualRack, virtualBoard, player.getPlayerNumber(), currentRoundNumber);
        return game;
    }

    /*
     * When the player plays word(s)
     */
    private void play(final Long userId, final Game game, final VirtualRack virtualRack,
                      final VirtualBoard virtualBoard) {
        log.info("Playing on game {} as player {}", game.getId(), game.getCurrentPlayerNumber());

        final Integer currentRoundNumber = game.getRoundNumber();

        final List<ConstructedWord> constructedWords = findNewWords(game, virtualRack, virtualBoard);
        // calculate the total score
        final Integer constructedWordsScore = constructedWords.stream().mapToInt(constructedWord -> {
            final Integer score = scoreService.calculateConstructedWordScore(constructedWord);
            constructedWord.setScore(score);
            return score;
        }).sum();

        final List<Bonus> bonuses = scoreService.calculateBonuses(constructedWords, virtualRack);
        final Integer bonusScore = bonuses.stream().mapToInt(Bonus::getScore).sum();

        playerService.updateScore(game.getId(), game.getCurrentPlayerNumber(), constructedWordsScore + bonusScore);
        markNewWordCellsAsPlayed(constructedWords);

        assignNextPlayer(game);
        increaseVersion(game);
        increaseRoundNumber(game);

        // get the remaining tile count before filling the rack again
        final Integer previousRemainingTileCount = game.getRemainingTileCount();

        // update the remaining tile count
        final Long usedRackTileCount = virtualRack.getTiles().stream().filter(VirtualTile::isSealed).count();
        game.setRemainingTileCount(Math.max(game.getRemainingTileCount() - usedRackTileCount.intValue(), 0));

        // end the game if there are no tiles left in the bag at the start of this round and
        // the current player used all tiles in the rack
        if (previousRemainingTileCount.equals(0) && virtualRack.getTiles().stream().allMatch(VirtualTile::isSealed)) {
            // there are no tiles left in the bag and the current player finished the tiles on the rack
            log.info("The last round has been played, game {} is ready to end", game.getId());
            game.setStatus(GameStatus.READY_TO_END);
        }

        final Game updatedGame = baseDao.save(game);

        // create a play action
        final Action action = actionService.add(updatedGame, userId, constructedWordsScore, ActionType.PLAY);

        // create actions for bonuses
        bonuses.stream().forEach(bonus -> {
            actionService.add(updatedGame, userId, bonus.getScore(), bonus.getActionType());
        });

        // log the words
        logWords(game.getId(), userId, action.getId(), currentRoundNumber, constructedWords);
    }

    /*
     * When the player skips round
     */
    private void skip(final Long userId, final Game game, final ActionType actionType) {
        log.info("Skipping on game {} as player {}", game.getId(), game.getCurrentPlayerNumber());

        assignNextPlayer(game);
        increaseVersion(game);
        increaseRoundNumber(game);

        final Game updatedGame = baseDao.save(game);

        // create a skip action
        actionService.add(updatedGame, userId, NO_SCORE, actionType);

        final boolean isMaximumSkipCountReached = actionService.isMaximumSkipCountReached(game.getId(),
                game.getExpectedPlayerCount());
        if (isMaximumSkipCountReached) {
            // maximum skip count in a row has been reached, the game is going to end
            game.setStatus(GameStatus.READY_TO_END);
        }
    }

    /*
     * When the player exchanges letters
     */
    private void exchange(final Long userId, final Game game, final Player player, final VirtualRack virtualRack) {
        log.info("Exchanging on game {} as player {}", game.getId(), game.getCurrentPlayerNumber());

        virtualRackService.exchange(game.getId(), game.getLanguage(), player.getPlayerNumber(), game.getRoundNumber(),
                virtualRack);

        assignNextPlayer(game);
        increaseVersion(game);
        increaseRoundNumber(game);

        final Game updatedGame = baseDao.save(game);

        // create an exchange action
        actionService.add(updatedGame, userId, NO_SCORE, ActionType.EXCHANGE);
    }

    @Override
    @Transactional
    public Game delete(Long id) {
        final Game game = get(id);

        if (GameStatus.IN_PROGRESS == game.getStatus()) {
            throw new GameException(GameError.IN_PROGRESS);
        }

        game.setStatus(GameStatus.DELETED);
        game.setVersion(game.getVersion() + 1);

        final Game updatedGame = baseDao.save(game);

        actionService.add(updatedGame, game.getOwnerId(), NO_SCORE, ActionType.DELETE);

        return updatedGame;
    }

    @Override
    @Transactional
    public Game end(Long id) {
        Assert.notNull(id, "id cannot be null");

        final Game game = get(id);

        if (GameStatus.WAITING == game.getStatus()) {
            throw new GameException(GameError.WAITING);
        }

        if (GameStatus.READY_TO_END != game.getStatus()) {
            throw new GameException(GameError.IN_PROGRESS);
        }

        game.setEndDate(new Date());
        game.setStatus(GameStatus.ENDED);
        game.setVersion(game.getVersion() + 1);

        // find the winning player, currentPlayerNumber shows the winner player at the end of the game
        final List<Player> players = playerService.getPlayers(id);
        final Player winningPlayer = players.stream().max(Comparator.comparing(Player::getScore)).orElse(null);
        game.setCurrentPlayerNumber(winningPlayer.getPlayerNumber());

        log.info("Game {} is ended", game.getId());

        final Game updatedGame = baseDao.save(game);

        actionService.add(updatedGame, winningPlayer.getUserId(), NO_SCORE, ActionType.END);

        return updatedGame;
    }

    @Override
    @Transactional
    public Game terminate(Long id) {
        Assert.notNull(id, "id cannot be null");

        final Game game = get(id);

        if (GameStatus.WAITING != game.getStatus()) {
            throw new GameException(GameError.IN_PROGRESS);
        }

        game.setEndDate(new Date());
        game.setStatus(GameStatus.TERMINATED);
        game.setVersion(game.getVersion() + 1);

        log.info("Game {} is terminated", game.getId());

        final Game updatedGame = baseDao.save(game);

        actionService.add(updatedGame, game.getOwnerId(), NO_SCORE, ActionType.TERMINATE);

        return updatedGame;
    }

    @Override
    public List<Game> search(Long userId, boolean includeUser) {
        return baseDao.search(userId, includeUser);
    }

    /*
     * Get the game and lock it on database level
     */
    private Game getAndLock(Long gameId) {
        return baseDao.getAndLock(gameId);
    }

    /*
     * Mark the new word letter cells as last played
     */
    private void markNewWordCellsAsPlayed(final List<ConstructedWord> constructedWords) {
        constructedWords.stream().forEach(word -> {
            word.getCells().forEach(virtualCell -> virtualCell.setLastPlayed(true));
        });
    }

    /*
     * Assigns the turn to the next player
     */
    private void assignNextPlayer(final Game game) {
        // TODO create a service/utility to calculate next player number
        int nextPlayerNumber = game.getCurrentPlayerNumber() < game.getExpectedPlayerCount()
                ? game.getCurrentPlayerNumber() + 1
                : 1;

        game.setCurrentPlayerNumber(nextPlayerNumber);
        log.info("Current player is set as player {} on game {}", nextPlayerNumber, game.getId());
    }

    /*
     * Increases the version number by one
     */
    private void increaseVersion(final Game game) {
        game.setVersion(game.getVersion() + 1);
    }

    /*
     * Increases the round number by one if the next player is the first player
     */
    private void increaseRoundNumber(final Game game) {
        // TODO create a service/utility to calculate round number
        if (game.getCurrentPlayerNumber().equals(1)) {
            game.setRoundNumber(game.getRoundNumber() + 1);
        }
    }

    /*
     * Does the validations and finds the words
     */
    private List<ConstructedWord> findNewWords(final Game game, final VirtualRack updatedRack,
                                               final VirtualBoard virtualBoard) {
        final VirtualCell[][] boardMatrix = new VirtualCell[BOARD_ROW_SIZE][BOARD_COLUMN_SIZE];
        virtualBoard.getCells().stream().forEach(cell -> {
            boardMatrix[cell.getRowNumber() - 1][cell.getColumnNumber() - 1] = cell;
            cell.setLastPlayed(false);
        });

        locateTilesOnBoard(updatedRack, boardMatrix);
        GameValidationHelper.hasNonEmptyCenter(boardMatrix);

        final List<ConstructedWord> constructedWords = findWordsOnBoard(game, boardMatrix);

        GameValidationHelper.hasWordsInDictionary(constructedWords, game.getLanguage());
        GameValidationHelper.hasValidBoardLinks(constructedWords, boardMatrix);
        GameValidationHelper.hasNoSingleLetterWords(virtualBoard);

        return constructedWords;
    }

    /*
     * Locates the tiles placed by the user to their respective cells
     */
    private void locateTilesOnBoard(final VirtualRack updatedRack, final VirtualCell[][] boardMatrix) {
        updatedRack.getTiles().stream().filter(VirtualTile::isSealed).forEach(tile -> {
            final VirtualCell cell = boardMatrix[tile.getRowNumber() - 1][tile.getColumnNumber() - 1];
            if (cell.isSealed()) {
                throw new GameException(GameError.CELL_IS_NOT_EMPTY,
                        Arrays.asList(tile.getRowNumber().toString(), tile.getColumnNumber().toString()));
            }
            cell.setLetter(tile.getLetter());
            cell.setValue(tile.getValue());
            log.debug("Letter {} with value {} on rack tile {} is located to cell[{},{}]", cell.getLetter(),
                    cell.getValue(), tile.getNumber(), cell.getRowNumber(), cell.getColumnNumber());
        });
    }

    /*
     * Finds and returns the words on the board
     */
    private List<ConstructedWord> findWordsOnBoard(final Game game, final VirtualCell[][] boardMatrix) {
        final List<ConstructedWord> words = new ArrayList<>();

        // horizontal words
        IntStream.range(1, BOARD_ROW_SIZE + 1).forEach(rowNumber -> {
            final ConstructedWord constructedWord = ConstructedWord.builder().direction(HORIZONTAL).build();
            IntStream.range(1, BOARD_COLUMN_SIZE + 1).forEach(columnNumber -> {
                final VirtualCell cell = boardMatrix[rowNumber - 1][columnNumber - 1];
                final ConstructedWord detectedWord = findWordsByDirection(game, cell, HORIZONTAL, constructedWord);
                if (detectedWord != null) {
                    words.add(detectedWord);
                    log.debug("Horizontal word {} has been detected on game {} by player {}",
                            detectedWord.getWordBuilder(), game.getId(), game.getCurrentPlayerNumber());
                }
            });
        });

        // vertical words
        IntStream.range(1, BOARD_COLUMN_SIZE + 1).forEach(columnNumber -> {
            final ConstructedWord constructedWord = ConstructedWord.builder().direction(VERTICAL).build();
            IntStream.range(1, BOARD_ROW_SIZE + 1).forEach(rowNumber -> {
                final VirtualCell cell = boardMatrix[rowNumber - 1][columnNumber - 1];
                final ConstructedWord detectedWord = findWordsByDirection(game, cell, VERTICAL, constructedWord);
                if (detectedWord != null) {
                    words.add(detectedWord);
                    log.debug("Vertical word {} has been detected on game {} by player {}",
                            detectedWord.getWordBuilder(), game.getId(), game.getCurrentPlayerNumber());
                }
            });
        });

        return words;
    }

    /*
     * Finds the words on the board by direction
     */
    private ConstructedWord findWordsByDirection(final Game game, final VirtualCell cell, final Direction direction,
                                                 final ConstructedWord constructedWord) {
        if (cell.getLetter() != null) {
            constructedWord.getWordBuilder().append(cell.getLetter());
            constructedWord.getCells().add(cell);
            constructedWord.setLinked(constructedWord.isLinked() || cell.isCenter());
            if (!cell.isSealed()) {
                // letter is placed in the last round
                cell.setLastPlayed(true);
                cell.setRoundNumber(game.getRoundNumber());
            }

            log.debug("A {} {} letter is spotted on [{},{}] on game {} by player {}",
                    constructedWord.isLinked() ? "new linked" : "new", direction, cell.getRowNumber(),
                    cell.getColumnNumber(), game.getId(), game.getCurrentPlayerNumber());
        }

        boolean emptyCell = cell.getLetter() == null;
        boolean horizontalLast = HORIZONTAL == direction && cell.getColumnNumber() == BOARD_COLUMN_SIZE;
        boolean verticalLast = VERTICAL == direction && cell.getRowNumber() == BOARD_ROW_SIZE;

        // if the cell is neither in the last row/column nor empty, then keep detecting the word
        if (!emptyCell && !horizontalLast && !verticalLast) {
            return null;
        }

        if (constructedWord.getWordBuilder().length() <= 1) {
            // char sequence is less than 2 letters to create a word, reset it
            constructedWord.reset();
            return null;
        }

        boolean existingWord = constructedWord.getCells().stream().allMatch(VirtualCell::isSealed);
        if (existingWord) {
            // this is an existing word on the board, don't count it
            constructedWord.reset();
            return null;
        }

        if (constructedWord.isLinked()) {
            // seal the cells if the word is a linked word
            constructedWord.getCells().forEach(c -> c.setSealed(true));
        }

        // a word is found, build it
        final ConstructedWord detectedWord = ConstructedWord.builder()
                .cells(constructedWord.getCells())
                .wordBuilder(constructedWord.getWordBuilder())
                .direction(direction)
                .linked(constructedWord.isLinked())
                .dictionaryWord(
                        dictionaryService.getWord(constructedWord.getWordBuilder().toString(), game.getLanguage()))
                .build();

        // a new word is detected, reset the last constructed word
        constructedWord.reset();

        return detectedWord;
    }

    /*
     * Logs the words
     */
    private void logWords(final Long gameId, final Long userId, final Long actionId, final Integer roundNumber,
                          final List<ConstructedWord> constructedWords) {
        final List<Word> words = constructedWords.stream().map(constructedWord -> {
            return Word.builder()
                    .gameId(gameId)
                    .userId(userId)
                    .actionId(actionId)
                    .roundNumber(roundNumber)
                    .score(constructedWord.getScore())
                    .word(constructedWord.getDictionaryWord().getWord().toUpperCase())
                    .definition(constructedWord.getDictionaryWord().getDefinition())
                    .build();
        }).collect(Collectors.toList());
        wordService.saveAll(words);
    }

}
