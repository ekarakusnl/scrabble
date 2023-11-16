package com.gamecity.scrabble.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

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
import com.gamecity.scrabble.model.Bonus;
import com.gamecity.scrabble.model.ConstructedWord;
import com.gamecity.scrabble.model.ConstructedWord.Direction;
import com.gamecity.scrabble.model.DictionaryWord;
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

    @Autowired
    void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Autowired
    void setVirtualBoardService(VirtualBoardService virtualBoardService) {
        this.virtualBoardService = virtualBoardService;
    }

    @Autowired
    void setVirtualRackService(VirtualRackService virtualRackService) {
        this.virtualRackService = virtualRackService;
    }

    @Autowired
    void setVirtualBagService(VirtualBagService virtualBagService) {
        this.virtualBagService = virtualBagService;
    }

    @Autowired
    void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    @Autowired
    void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Autowired
    void setWordService(WordService wordService) {
        this.wordService = wordService;
    }

    @Autowired
    void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    @Autowired
    void setScoreService(ScoreService scoreService) {
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
            throw new GameException(GameError.IN_PROGRESS);
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

        if (GameStatus.WAITING == game.getStatus()) {
            throw new GameException(GameError.WAITING);
        } else if (GameStatus.ENDED == game.getStatus()) {
            throw new GameException(GameError.NOT_FOUND);
        }

        log.info("Playing on game {} as player {}", game.getId(), game.getCurrentPlayerNumber());

        final Player player = playerService.getByUserId(game.getId(), userId);
        final Integer currentPlayerNumber = game.getCurrentPlayerNumber();
        final Integer currentRoundNumber = game.getRoundNumber();

        hasTurn(player.getPlayerNumber(), currentPlayerNumber);
        hasValidRack(game, player.getPlayerNumber(), virtualRack);

        // TODO create a service/utility to calculate board version
        final Integer boardVersion = game.getVersion() - game.getExpectedPlayerCount();
        final VirtualBoard virtualBoard = virtualBoardService.getBoard(game.getId(), boardVersion);
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

        // increase the version number
        game.setVersion(game.getVersion() + 1);

        // get the remaining tile count before filling the rack again
        final Integer previousRemainingTileCount = game.getRemainingTileCount();

        // update the remaining tile count
        final Long usedRackTileCount = virtualRack.getTiles().stream().filter(VirtualTile::isSealed).count();
        game.setRemainingTileCount(Math.max(game.getRemainingTileCount() - usedRackTileCount.intValue(), 0));

        // TODO create a service/utility to calculate round number
        // if the next player is the first player, then a new round starts
        if (game.getVersion() % game.getExpectedPlayerCount() == 1) {
            game.setRoundNumber(currentRoundNumber + 1);
        }

        // end the game if there are no tiles left in the bag at the start of this round and
        // the current player used all tiles in the rack
        if (previousRemainingTileCount.equals(0) && virtualRack.getTiles().stream().allMatch(VirtualTile::isSealed)) {
            // there are no tiles left in the bag and the current player finished the tiles on the rack
            log.info("The last round has been played, game {} is ready to end", game.getId());
            game.setStatus(GameStatus.READY_TO_END);
        }

        final Game updatedGame = baseDao.save(game);

        // create actions for bonuses
        bonuses.stream().forEach(bonus -> {
            actionService.add(updatedGame, userId, bonus.getScore(), bonus.getActionType());
        });

        // create action for play/skip
        final Action action = actionService.add(updatedGame, userId, constructedWordsScore, actionType);
        logWords(id, userId, action.getId(), currentRoundNumber, constructedWords);
        contentService.update(updatedGame, virtualRack, virtualBoard, currentPlayerNumber, currentRoundNumber);

        return updatedGame;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        final Game game = get(id);

        if (GameStatus.IN_PROGRESS == game.getStatus()) {
            throw new GameException(GameError.IN_PROGRESS);
        }

        game.setStatus(GameStatus.TERMINATED);
        baseDao.save(game);
    }

    @Override
    @Transactional
    public Game end(Long id) {
        Assert.notNull(id, "id cannot be null");

        final Game game = get(id);

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

    private Game getAndLock(Long gameId) {
        final Game game = baseDao.getAndLock(gameId);

        if (game == null || GameStatus.TERMINATED == game.getStatus()) {
            throw new GameException(GameError.NOT_FOUND);
        }

        return game;
    }

    /**
     * Whether the player who is playing has the turn
     */
    private void hasTurn(Integer playerNumber, Integer currentPlayerNumber) {
        if (!playerNumber.equals(currentPlayerNumber)) {
            throw new GameException(GameError.TURN_OF_ANOTHER_PLAYER);
        }
    }

    /**
     * Whether the played rack is the same rack that was stored in the system
     */
    private void hasValidRack(Game game, Integer playerNumber, VirtualRack virtualRack) {

        final VirtualRack existingRack = virtualRackService.getRack(game.getId(), playerNumber, game.getRoundNumber());

        final Map<Integer, String> tileMap = existingRack.getTiles()
                .stream()
                .collect(Collectors.toMap(VirtualTile::getNumber, VirtualTile::getLetter));

        final Predicate<VirtualTile> filter = tile -> tileMap.containsKey(tile.getNumber())
                && tileMap.get(tile.getNumber()).equals(tile.getLetter());

        long rackMatchCount = virtualRack.getTiles().stream().filter(filter).count();
        if (rackMatchCount != virtualRack.getTiles().size()) {
            throw new GameException(GameError.RACK_DOES_NOT_MATCH);
        }
    }

    /**
     * Mark the new word cells as last played
     */
    private void markNewWordCellsAsPlayed(final List<ConstructedWord> constructedWords) {
        // mark new word cells as last played
        constructedWords.stream().forEach(word -> {
            word.getCells().forEach(virtualCell -> virtualCell.setLastPlayed(true));
        });
    }

    /**
     * Assigns the turn to the next player in the game
     */
    private void assignNextPlayer(Game game) {
        // TODO create a service/utility to calculate next player number
        int nextPlayerNumber = (game.getVersion() % game.getExpectedPlayerCount()) + 1;
        game.setCurrentPlayerNumber(nextPlayerNumber);
        log.info("Current player is set as player {} on game {}", nextPlayerNumber, game.getId());
    }

    /**
     * Does the validations and finds the words
     */
    private List<ConstructedWord> findNewWords(Game game, VirtualRack updatedRack, VirtualBoard virtualBoard) {
        boolean hasNewMove = updatedRack.getTiles().stream().anyMatch(VirtualTile::isSealed);
        if (!hasNewMove) {
            return Collections.emptyList();
        }

        final VirtualCell[][] boardMatrix = new VirtualCell[BOARD_ROW_SIZE][BOARD_COLUMN_SIZE];
        virtualBoard.getCells().stream().forEach(cell -> {
            boardMatrix[cell.getRowNumber() - 1][cell.getColumnNumber() - 1] = cell;
            cell.setLastPlayed(false);
        });

        locateTilesOnBoard(updatedRack, boardMatrix);
        hasNonEmptyCenter(boardMatrix);

        final List<ConstructedWord> constructedWords = findWordsOnBoard(game.getId(), game.getCurrentPlayerNumber(), game.getRoundNumber(),
                boardMatrix);

        addWordDefinitions(constructedWords, game.getLanguage());
        hasValidWords(constructedWords, game.getLanguage());
        hasValidLinks(constructedWords, boardMatrix);
        hasNoSingleLetterWords(virtualBoard);

        return constructedWords;
    }

    /*
     * Log the words
     */
    private void logWords(Long gameId, Long userId, Long actionId, Integer roundNumber, List<ConstructedWord> constructedWords) {
        if (constructedWords.isEmpty()) {
            return;
        }

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

    /**
     * Locates the tiles placed by the user to their respective cells
     */
    private void locateTilesOnBoard(VirtualRack updatedRack, VirtualCell[][] boardMatrix) {
        updatedRack.getTiles().stream().filter(VirtualTile::isSealed).forEach(tile -> {
            final VirtualCell cell = boardMatrix[tile.getRowNumber() - 1][tile.getColumnNumber() - 1];
            if (cell.isSealed()) {
                throw new GameException(GameError.CELL_IS_NOT_EMPTY,
                        Arrays.asList(tile.getRowNumber().toString(), tile.getColumnNumber().toString()));
            }
            cell.setLetter(tile.getLetter());
            cell.setValue(tile.getValue());
            log.debug("Letter {} with value {} on rack tile {} is located to cell[{},{}]", cell.getLetter(), cell.getValue(),
                    tile.getNumber(), cell.getRowNumber(), cell.getColumnNumber());
        });
    }

    /**
     * Whether the centre cell has been used
     */
    private void hasNonEmptyCenter(VirtualCell[][] boardMatrix) {
        final boolean isCenterUsed = Arrays.stream(boardMatrix)
                .flatMap(Arrays::stream)
                .anyMatch(cell -> cell.isCenter() && cell.getLetter() != null);
        if (!isCenterUsed) {
            throw new GameException(GameError.CENTER_CANNOT_BE_EMPTY);
        }
    }

    /**
     * Finds and returns the words on the board
     */
    private List<ConstructedWord> findWordsOnBoard(Long gameId, Integer playerNumber, Integer roundNumber, VirtualCell[][] boardMatrix) {
        final List<ConstructedWord> words = new ArrayList<>();

        // horizontal words
        IntStream.range(1, BOARD_ROW_SIZE + 1).forEach(rowNumber -> {
            final ConstructedWord constructedWord = ConstructedWord.builder().direction(HORIZONTAL).build();
            IntStream.range(1, BOARD_COLUMN_SIZE + 1).forEach(columnNumber -> {
                final VirtualCell cell = boardMatrix[rowNumber - 1][columnNumber - 1];
                final ConstructedWord detectedWord = findWordsByDirection(gameId, playerNumber, roundNumber, cell, HORIZONTAL,
                        constructedWord);
                if (detectedWord != null) {
                    words.add(detectedWord);
                    log.debug("Horizontal word {} has been detected on game {} by player {}", detectedWord.getWordBuilder(), gameId,
                            playerNumber);
                }
            });
        });

        // vertical words
        IntStream.range(1, BOARD_COLUMN_SIZE + 1).forEach(columnNumber -> {
            final ConstructedWord constructedWord = ConstructedWord.builder().direction(VERTICAL).build();
            IntStream.range(1, BOARD_ROW_SIZE + 1).forEach(rowNumber -> {
                final VirtualCell cell = boardMatrix[rowNumber - 1][columnNumber - 1];
                final ConstructedWord detectedWord = findWordsByDirection(gameId, playerNumber, roundNumber, cell, VERTICAL,
                        constructedWord);
                if (detectedWord != null) {
                    words.add(detectedWord);
                    log.debug("Vertical word {} has been detected on game {} by player {}", detectedWord.getWordBuilder(), gameId,
                            playerNumber);
                }
            });
        });

        return words;
    }

    /**
     * Finds the words on the board by direction
     */
    private ConstructedWord findWordsByDirection(Long gameId, Integer playerNumber, Integer roundNumber, VirtualCell cell,
                                                 Direction direction, ConstructedWord constructedWord) {

        if (cell.getLetter() != null) {
            constructedWord.getWordBuilder().append(cell.getLetter());
            constructedWord.getCells().add(cell);
            constructedWord.setLinked(constructedWord.isLinked() || cell.isCenter());
            if (!cell.isSealed()) {
                // letter is placed in the last round
                cell.setLastPlayed(true);
                cell.setRoundNumber(roundNumber);
            }

            log.debug("A {} {} letter is spotted on [{},{}] on game {} by player {}", constructedWord.isLinked() ? "new linked" : "new",
                    direction, cell.getRowNumber(), cell.getColumnNumber(), gameId, playerNumber);
        }

        boolean emptyCell = cell.getLetter() == null;
        boolean horizontalLast = HORIZONTAL == direction && cell.getColumnNumber() == BOARD_COLUMN_SIZE;
        boolean verticalLast = VERTICAL == direction && cell.getRowNumber() == BOARD_ROW_SIZE;

        // if the cell is not in the last row/column or not empty, then keep detecting the word
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
                .build();

        // a new word is detected, reset the last constructed word
        constructedWord.reset();

        return detectedWord;
    }

    private void addWordDefinitions(List<ConstructedWord> constructedWords, Language language) {
        constructedWords.stream().map(constructedWord -> {
            final DictionaryWord dictionaryWord = dictionaryService.getWord(constructedWord.getWordBuilder().toString(), language);
            constructedWord.setDictionaryWord(dictionaryWord);
            return constructedWord;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void hasValidWords(List<ConstructedWord> constructedWords, Language language) {
        final List<ConstructedWord> invalidWords = constructedWords.stream()
                .filter(word -> word.getDictionaryWord() == null)
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(invalidWords)) {
            final String commaSeperatedInvalidWords = String.join(",",
                    invalidWords.stream().map(ConstructedWord::getWordBuilder).collect(Collectors.toList()));
            log.debug("Word(s) {} are not found in {} dictionary", commaSeperatedInvalidWords, language);
            throw new GameException(GameError.WORDS_ARE_NOT_FOUND, Arrays.asList(commaSeperatedInvalidWords, language.name()));
        }
    }

    /**
     * Whether the words are linked to existing words
     */
    private void hasValidLinks(List<ConstructedWord> constructedWords, VirtualCell[][] boardMatrix) {
        final List<ConstructedWord> unlinkedWords = constructedWords.stream().filter(word -> !word.isLinked()).collect(Collectors.toList());

        if (unlinkedWords.isEmpty()) {
            return;
        }

        int unlinkedWordCount = unlinkedWords.size();
        while (unlinkedWordCount > 0) {
            final List<ConstructedWord> updatedUnlinkedWords = unlinkedWords.stream().map(word -> {
                return linkWord(word, boardMatrix);
            }).filter(word -> !word.isLinked()).collect(Collectors.toList());

            if (updatedUnlinkedWords.isEmpty()) {
                // all words are linked
                return;
            } else if (updatedUnlinkedWords.size() < unlinkedWordCount) {
                // some words are linked, update the unlinked word count then try to link the remaining words
                unlinkedWordCount = updatedUnlinkedWords.size();
            } else {
                // since unlinked words count didn't change this means that trying one more time doesn't make any
                // difference
                final String commaSeperatedUnlinkedWords = String.join(",",
                        unlinkedWords.stream().map(ConstructedWord::getWordBuilder).collect(Collectors.toList()));
                throw new GameException(GameError.WORDS_ARE_NOT_LINKED, Arrays.asList(commaSeperatedUnlinkedWords));
            }
        }
    }

    /**
     * Whether there are any words in the board with a single letter
     */
    private void hasNoSingleLetterWords(VirtualBoard virtualBoard) {
        // detect single word letter
        final List<String> singleLetterWords = virtualBoard.getCells()
                .stream()
                .filter(cell -> cell.getLetter() != null && !cell.isSealed())
                .map(VirtualCell::getLetter)
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(singleLetterWords)) {
            final String commaSeperatedSingleLetterWords = String.join(",", singleLetterWords);
            log.debug("Single letter word(s) {} are detected", commaSeperatedSingleLetterWords);
            throw new GameException(GameError.SINGLE_LETTER_WORDS_NOT_ALLOWED, Arrays.asList(commaSeperatedSingleLetterWords));
        }
    }

    /**
     * Link the new words to the existing words
     */
    private ConstructedWord linkWord(ConstructedWord word, VirtualCell[][] boardMatrix) {
        word.getCells().forEach(cell -> {
            if (word.isLinked()) {
                return;
            }

            final Integer rowIndex = cell.getRowNumber() - 1;
            final Integer columnIndex = cell.getColumnNumber() - 1;

            if ((cell.isSealed() || (cell.isHasRight() && boardMatrix[rowIndex][columnIndex + 1].isSealed()))
                    || (cell.isHasLeft() && boardMatrix[rowIndex][columnIndex - 1].isSealed())
                    || (cell.isHasTop() && boardMatrix[rowIndex - 1][columnIndex].isSealed())
                    || (cell.isHasBottom() && boardMatrix[rowIndex + 1][columnIndex].isSealed())) {
                word.setLinked(true);

                // seal the cells if the word is a linked word
                word.getCells().forEach(c -> c.setSealed(true));
            }
        });
        return word;
    }

}
