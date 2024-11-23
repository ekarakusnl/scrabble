package com.gamecity.scrabble.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.gamecity.scrabble.entity.GameType;
import com.gamecity.scrabble.entity.Word;
import com.gamecity.scrabble.entity.User;
import com.gamecity.scrabble.model.BoardScanFlag;
import com.gamecity.scrabble.model.Bonus;
import com.gamecity.scrabble.model.ConstructedWord;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.VirtualCell;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.model.VirtualTile;
import com.gamecity.scrabble.service.ActionService;
import com.gamecity.scrabble.service.ScoreService;
import com.gamecity.scrabble.service.PlayerService;
import com.gamecity.scrabble.service.SchedulerService;
import com.gamecity.scrabble.service.ContentService;
import com.gamecity.scrabble.service.DictionaryService;
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

import static com.gamecity.scrabble.Constants.Game.NO_SCORE;
import static com.gamecity.scrabble.Constants.Game.RACK_SIZE;
import static com.gamecity.scrabble.model.BoardScanFlag.LOG_NEW_LETTERS;
import static com.gamecity.scrabble.model.BoardScanFlag.LOG_NEW_WORDS;

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
    private SchedulerService schedulerService;

    public GameServiceImpl(final UserService userService, final PlayerService playerService,
                           final VirtualBoardService virtualBoardService, final VirtualRackService virtualRackService,
                           final VirtualBagService virtualBagService, final ContentService contentService,
                           final DictionaryService dictionaryService, final WordService wordService,
                           final ActionService actionService, final ScoreService scoreService,
                           final SchedulerService schedulerService) {
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
        this.schedulerService = schedulerService;
    }

    @Override
    public Game get(Long gameId) {
        final Game game = super.get(gameId);

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

        game.setType(GameType.USER);
        game.setOwnerId(user.getId());
        game.setStatus(GameStatus.WAITING);
        game.setActivePlayerCount(1);
        game.setVersion(1);

        final Game savedGame = super.save(game);

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

        return super.save(existingGame);
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

        log.debug("User {} joined the game {}", userId, game.getId());

        // expected player count has been reached
        if (game.getActivePlayerCount().equals(game.getExpectedPlayerCount())) {
            log.info("Expected player count has been reached, game {} is ready to start", game.getId());
            game.setStatus(GameStatus.READY_TO_START);
        }

        final Game updatedGame = super.save(game);

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

        log.debug("User {} left the game {}", userId, game.getId());

        game.setActivePlayerCount(game.getActivePlayerCount() - 1);
        game.setVersion(game.getVersion() + 1);

        final Game updatedGame = super.save(game);

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

        game.setStartDate(LocalDateTime.now());
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setCurrentPlayerNumber(1);
        game.setRoundNumber(1);
        game.setVersion(game.getVersion() + 1);

        final Integer remainingTileCount = virtualBagService.getTiles(game.getId(), game.getLanguage())
                .stream()
                .mapToInt(Tile::getCount)
                .sum();
        game.setRemainingTileCount(remainingTileCount - (game.getExpectedPlayerCount() * RACK_SIZE));

        final Game updatedGame = super.save(game);

        actionService.add(updatedGame, game.getOwnerId(), NO_SCORE, ActionType.START);
        contentService.create(updatedGame);

        log.info("Game {} is started", game.getId());

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

        GameValidationHelper.hasCurrentTurn(player.getPlayerNumber(), game.getCurrentPlayerNumber());

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

    @Override
    public ActionType determineActionType(VirtualRack virtualRack) {
        return virtualRack.getTiles().stream().anyMatch(VirtualTile::isExchanged) ? ActionType.EXCHANGE
                : virtualRack.getTiles().stream().anyMatch(VirtualTile::isSealed) ? ActionType.PLAY : ActionType.SKIP;
    }

    @Override
    public void scheduleNextRoundJobs(Game game) {
        if (GameStatus.READY_TO_END == game.getStatus()) {
            // the last round has been played, schedule the end game job
            schedulerService.scheduleEndGameJob(game.getId());
        } else {
            // skip turn timeout duration
            Integer duration = game.getDuration();
            schedulerService.scheduleSkipTurnJob(game, duration);
        }
    }

    /*
     * When the player plays word(s)
     */
    private void play(final Long userId, final Game game, final VirtualRack virtualRack,
                      final VirtualBoard virtualBoard) {
        log.info("Playing on game {} as player {}", game.getId(), game.getCurrentPlayerNumber());

        final Integer currentRoundNumber = game.getRoundNumber();

        // last played cells are cleared with the new round
        virtualBoard.clearLastPlayed();

        final List<ConstructedWord> constructedWords = scanNewWords(game, virtualRack, virtualBoard);
        // calculate the total score
        final Integer constructedWordsScore = constructedWords.stream().mapToInt(constructedWord -> {
            final Integer score = scoreService.calculateConstructedWordScore(constructedWord);
            constructedWord.setScore(score);
            return score;
        }).sum();

        final List<Bonus> bonuses = scoreService.calculateBonuses(constructedWords, virtualRack);
        final Integer bonusScore = bonuses.stream().mapToInt(Bonus::getScore).sum();

        playerService.updateScore(game.getId(), game.getCurrentPlayerNumber(), constructedWordsScore + bonusScore);

        // do this operation only after playerService#updateScore since lastPlayed property is used
        // when calculating the score
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

        final Game updatedGame = super.save(game);

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

        final Game updatedGame = super.save(game);

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

        final Game updatedGame = super.save(game);

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

        final Game updatedGame = super.save(game);

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

        game.setEndDate(LocalDateTime.now());
        game.setStatus(GameStatus.ENDED);
        game.setVersion(game.getVersion() + 1);

        // find the winning player, currentPlayerNumber shows the winner player at the end of the game
        final List<Player> players = playerService.getPlayers(id);
        final Player winningPlayer = players.stream().max(Comparator.comparing(Player::getScore)).orElse(null);
        game.setCurrentPlayerNumber(winningPlayer.getPlayerNumber());

        log.info("Game {} is ended", game.getId());

        final Game updatedGame = super.save(game);

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

        game.setEndDate(LocalDateTime.now());
        game.setStatus(GameStatus.TERMINATED);
        game.setVersion(game.getVersion() + 1);

        log.info("Game {} is terminated", game.getId());

        final Game updatedGame = super.save(game);

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
    private List<ConstructedWord> scanNewWords(final Game game, final VirtualRack updatedRack,
                                               final VirtualBoard virtualBoard) {
        locateTilesOnBoard(game.getId(),updatedRack, virtualBoard);
        GameValidationHelper.hasNonEmptyCenter(virtualBoard);

        final Set<BoardScanFlag> boardScanFlags = Set.of(LOG_NEW_LETTERS, LOG_NEW_WORDS);
        final List<ConstructedWord> constructedWords = virtualBoardService.scanWords(game.getId(), virtualBoard,
                boardScanFlags);

        // set the dictionary definitions of the words
        constructedWords.stream().forEach(constructedWord -> {
            constructedWord.setDictionaryWord(
                    dictionaryService.get(constructedWord.getBuilder().toString(), game.getLanguage()));
        });

        constructedWords.stream().forEach(constructedWord -> {
            constructedWord.getCells().stream().filter(VirtualCell::isLastPlayed).forEach(virtualCell -> {
                // set the roundNumber to last played cells 
                virtualCell.setRoundNumber(game.getRoundNumber());
            });
        });

        GameValidationHelper.hasWordsInDictionary(constructedWords, game.getLanguage());

        return constructedWords;
    }

    /*
     * Locates the tiles placed by the user to their respective cells
     */
    private void locateTilesOnBoard(final Long gameId, final VirtualRack updatedRack, final VirtualBoard virtualBoard) {
        updatedRack.getTiles().stream().filter(VirtualTile::isSealed).forEach(tile -> {
            final VirtualCell cell = virtualBoard.getCell(tile.getRowNumber(), tile.getColumnNumber());
            if (cell.isSealed()) {
                throw new GameException(GameError.CELL_IS_NOT_EMPTY,
                        Arrays.asList(tile.getRowNumber().toString(), tile.getColumnNumber().toString()));
            }

            cell.setLetter(tile.getLetter());
            cell.setValue(tile.getValue());

            log.debug("Letter '{}' with value {} on rack tile {} is located to cell[{},{}] on game {}",
                    cell.getLetter(), cell.getValue(), tile.getNumber(), cell.getRowNumber(), cell.getColumnNumber(),
                    gameId);
        });
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
