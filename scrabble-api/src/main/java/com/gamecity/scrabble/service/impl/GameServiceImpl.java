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
import com.gamecity.scrabble.model.DictionaryWord;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.VirtualCell;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.model.VirtualTile;
import com.gamecity.scrabble.service.ActionService;
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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static com.gamecity.scrabble.Constants.Game.BOARD_ROW_SIZE;
import static com.gamecity.scrabble.Constants.Game.BOARD_COLUMN_SIZE;
import static com.gamecity.scrabble.Constants.Game.RACK_SIZE;

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

    private enum Direction {
        VERTICAL, HORIZONTAL
    }

    @Data
    private class BoardWord {
        // cells used by the letters
        private VirtualBoard board;

        // word definition created by the tiles
        // TODO find a better name
        private StringBuilder wordDefinition;

        // the word stored in the dictionary
        private DictionaryWord dictionaryWord;

        // direction of the word
        private Direction direction;

        // whether the word is linked to existing words
        private boolean linked;

        // calculated score of the word
        private Integer score;

        public BoardWord(Direction direction) {
            this(new VirtualBoard(new ArrayList<>()), new StringBuilder(), direction, false);
        }

        public BoardWord(VirtualBoard board, StringBuilder wordDefinition, Direction direction, boolean linked) {
            this.board = board;
            this.wordDefinition = wordDefinition;
            this.direction = direction;
            this.linked = linked;
        }

        public void setDictionaryWord(final DictionaryWord dictionaryWord) {
            this.dictionaryWord = dictionaryWord;
        }
    }

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

        actionService.add(savedGame, game.getOwnerId(), ActionType.CREATE);

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

        actionService.add(updatedGame, userId, ActionType.JOIN);

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

        actionService.add(updatedGame, userId, ActionType.LEAVE);

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

        actionService.add(updatedGame, game.getOwnerId(), ActionType.START);
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

        isCurrentPlayer(player.getPlayerNumber(), currentPlayerNumber);
        isValidRack(game, player.getPlayerNumber(), virtualRack);

        final Integer boardVersion = game.getVersion() - game.getExpectedPlayerCount();
        final VirtualBoard virtualBoard = virtualBoardService.getBoard(game.getId(), boardVersion);
        final List<BoardWord> newWords = findNewWords(game, virtualRack, virtualBoard);

        playWords(game, newWords);
        assignNextPlayer(game);

        // increase the version number
        game.setVersion(game.getVersion() + 1);

        // update the remaining tile count
        final Long usedRackTileCount = virtualRack.getTiles().stream().filter(VirtualTile::isSealed).count();
        game.setRemainingTileCount(Math.max(game.getRemainingTileCount() - usedRackTileCount.intValue(), 0));

        // if the next player is the first player, then a new round starts
        if (game.getVersion() % game.getExpectedPlayerCount() == 1) {
            game.setRoundNumber(currentRoundNumber + 1);
        }

        // do the end game validations if a new round is started
        if (game.getRoundNumber() > currentRoundNumber) {
            if (GameStatus.LAST_ROUND == game.getStatus()) {
                log.info("The last round has been played, game {} is ready to end", game.getId());
                game.setStatus(GameStatus.READY_TO_END);
            } else {
                // the bag is empty, set the last round
                if (game.getRemainingTileCount().equals(0)) {
                    log.info("No tiles left in the bag, the last round is going to be played on game {}", game.getId());
                    game.setStatus(GameStatus.LAST_ROUND);
                }
            }
        }

        final Game updatedGame = baseDao.save(game);

        final Action action = actionService.add(updatedGame, userId, actionType);
        logWords(id, userId, action.getId(), currentRoundNumber, newWords);
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

        actionService.add(updatedGame, winningPlayer.getUserId(), ActionType.END);

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

        actionService.add(updatedGame, game.getOwnerId(), ActionType.TERMINATE);

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
    private void isCurrentPlayer(Integer playerNumber, Integer currentPlayerNumber) {
        if (!playerNumber.equals(currentPlayerNumber)) {
            throw new GameException(GameError.TURN_OF_ANOTHER_PLAYER);
        }
    }

    /**
     * Whether the rack of the player is the same rack as the service gets
     */
    private void isValidRack(Game game, Integer playerNumber, VirtualRack virtualRack) {

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
     * Assigns the turn to the next player in the game
     */
    private void assignNextPlayer(Game game) {
        int nextPlayerNumber = (game.getVersion() % game.getExpectedPlayerCount()) + 1;
        game.setCurrentPlayerNumber(nextPlayerNumber);
        log.info("Current player is set as player {} on game {}", nextPlayerNumber, game.getId());
    }

    private void playWords(final Game game, final List<BoardWord> newWords) {
        final Integer newWordsScore = calculateNewWordsScore(newWords);
        updatePlayerScore(game.getId(), game.getCurrentPlayerNumber(), newWordsScore);

        // mark new word cells as last played
        newWords.stream().forEach(word -> {
            word.getBoard().getCells().forEach(virtualCell -> virtualCell.setLastPlayed(true));
        });
    }

    /**
     * Does the validations and finds the words
     */
    private List<BoardWord> findNewWords(Game game, VirtualRack updatedRack, VirtualBoard virtualBoard) {
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

        final List<BoardWord> newWords = findWordsOnBoard(game.getId(), game.getCurrentPlayerNumber(),
                game.getRoundNumber(), boardMatrix);

        addWordDefinitions(newWords, game.getLanguage());
        validateWordDefinitions(newWords, game.getLanguage());
        hasValidLinks(newWords, boardMatrix);
        hasSingleLetterWords(virtualBoard);

        return newWords;
    }

    /*
     * Log the words
     */
    private void logWords(Long gameId, Long userId, Long actionId, Integer roundNumber, List<BoardWord> boardWords) {
        boardWords.forEach(word -> saveWord(gameId, userId, actionId, roundNumber, word));
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
            log.debug("Letter {} with value {} on rack tile {} is located to cell[{},{}]", cell.getLetter(),
                    cell.getValue(), tile.getNumber(), cell.getRowNumber(), cell.getColumnNumber());
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
    private List<BoardWord> findWordsOnBoard(Long gameId, Integer playerNumber, Integer roundNumber,
            VirtualCell[][] boardMatrix) {
        final List<BoardWord> words = new ArrayList<>();

        // horizontal words
        IntStream.range(1, BOARD_ROW_SIZE + 1).forEach(rowNumber -> {
            final BoardWord boardWord = new BoardWord(Direction.HORIZONTAL);
            IntStream.range(1, BOARD_COLUMN_SIZE + 1).forEach(columnNumber -> {
                final VirtualCell cell = boardMatrix[rowNumber - 1][columnNumber - 1];
                final BoardWord detectedWord = findWordsByDirection(gameId, playerNumber, roundNumber, cell,
                        Direction.HORIZONTAL, boardWord);
                if (detectedWord != null) {
                    words.add(detectedWord);
                    log.debug("Horizontal word {} has been detected on game {} by player {}",
                            detectedWord.getWordDefinition(), gameId, playerNumber);
                }
            });
        });

        // vertical words
        IntStream.range(1, BOARD_COLUMN_SIZE + 1).forEach(columnNumber -> {
            final BoardWord boardWord = new BoardWord(Direction.VERTICAL);
            IntStream.range(1, BOARD_ROW_SIZE + 1).forEach(rowNumber -> {
                final VirtualCell cell = boardMatrix[rowNumber - 1][columnNumber - 1];
                final BoardWord detectedWord = findWordsByDirection(gameId, playerNumber, roundNumber, cell,
                        Direction.VERTICAL, boardWord);
                if (detectedWord != null) {
                    words.add(detectedWord);
                    log.debug("Vertical word {} has been detected on game {} by player {}",
                            detectedWord.getWordDefinition(), gameId, playerNumber);
                }
            });
        });

        return words;
    }

    /**
     * Finds the words on the board by direction
     */
    private BoardWord findWordsByDirection(Long gameId, Integer playerNumber, Integer roundNumber, VirtualCell cell,
            Direction direction, BoardWord boardWord) {

        if (cell.getLetter() != null) {
            boardWord.getWordDefinition().append(cell.getLetter());
            boardWord.getBoard().getCells().add(cell);
            boardWord.setLinked(boardWord.isLinked() || cell.isCenter());
            if (!cell.isSealed()) {
                // letter is placed in the last round
                cell.setLastPlayed(true);
                cell.setRoundNumber(roundNumber);
            }

            log.debug("A {} {} letter is spotted on [{},{}] on game {} by player {}",
                    boardWord.isLinked() ? "new linked" : "new", direction, cell.getRowNumber(), cell.getColumnNumber(),
                    gameId, playerNumber);
        }

        boolean emptyCell = cell.getLetter() == null;
        boolean horizontalLast = Direction.HORIZONTAL == direction && cell.getColumnNumber() == BOARD_COLUMN_SIZE;
        boolean verticalLast = Direction.VERTICAL == direction && cell.getRowNumber() == BOARD_ROW_SIZE;

        // if the cell is not in the last row/column or not empty, then keep detecting the word
        if (!emptyCell && !horizontalLast && !verticalLast) {
            return null;
        }

        if (boardWord.getWordDefinition().length() <= 1) {
            resetBoardWordDefinition(boardWord);
            return null;
        }

        boolean existingWord = boardWord.getBoard().getCells().stream().allMatch(VirtualCell::isSealed);
        if (existingWord) {
            // this is an existing word on the board, don't count it
            resetBoardWordDefinition(boardWord);
            return null;
        }

        if (boardWord.isLinked()) {
            // seal the cells if the word is a linked word
            boardWord.getBoard().getCells().forEach(c -> c.setSealed(true));
        }

        final BoardWord detectedWord = new BoardWord(new VirtualBoard(boardWord.getBoard().getCells()),
                boardWord.getWordDefinition(), direction, boardWord.isLinked());

        resetBoardWordDefinition(boardWord);

        return detectedWord;
    }

    /**
     * Resets the last board word
     */
    private void resetBoardWordDefinition(BoardWord boardWord) {
        // reset the word
        boardWord.setBoard(new VirtualBoard(new ArrayList<>()));
        boardWord.setWordDefinition(new StringBuilder());
        boardWord.setDirection(null);
    }

    private void addWordDefinitions(List<BoardWord> newWords, Language language) {
        newWords.stream().map(boardWord -> {
            final DictionaryWord dictionaryWord = dictionaryService.getWord(boardWord.getWordDefinition().toString(),
                    language);
            boardWord.setDictionaryWord(dictionaryWord);
            return boardWord;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void validateWordDefinitions(List<BoardWord> newWords, Language language) {
        final List<BoardWord> invalidWords = newWords.stream()
                .filter(word -> word.getDictionaryWord() == null)
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(invalidWords)) {
            final String commaSeperatedInvalidWords = String.join(",",
                    invalidWords.stream().map(BoardWord::getWordDefinition).collect(Collectors.toList()));
            log.debug("Word(s) {} are not found in {} dictionary", commaSeperatedInvalidWords, language);
            throw new GameException(GameError.WORDS_ARE_NOT_FOUND,
                    Arrays.asList(commaSeperatedInvalidWords, language.name()));
        }
    }

    /**
     * Whether the words are linked to existing words
     */
    private void hasValidLinks(List<BoardWord> newWords, VirtualCell[][] boardMatrix) {
        final List<BoardWord> unlinkedWords = newWords.stream()
                .filter(word -> !word.isLinked())
                .collect(Collectors.toList());

        if (unlinkedWords.isEmpty()) {
            return;
        }

        int unlinkedWordCount = unlinkedWords.size();
        while (unlinkedWordCount > 0) {
            final List<BoardWord> updatedUnlinkedWords = unlinkedWords.stream().map(word -> {
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
                        unlinkedWords.stream().map(BoardWord::getWordDefinition).collect(Collectors.toList()));
                throw new GameException(GameError.WORDS_ARE_NOT_LINKED, Arrays.asList(commaSeperatedUnlinkedWords));
            }
        }
    }

    /**
     * Whether there are any words in the board with a single letter
     */
    private void hasSingleLetterWords(VirtualBoard virtualBoard) {
        // detect single word letter
        final List<String> singleLetterWords = virtualBoard.getCells()
                .stream()
                .filter(cell -> cell.getLetter() != null && !cell.isSealed())
                .map(VirtualCell::getLetter)
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(singleLetterWords)) {
            final String commaSeperatedSingleLetterWords = String.join(",", singleLetterWords);
            log.debug("Single letter word(s) {} are detected", commaSeperatedSingleLetterWords);
            throw new GameException(GameError.SINGLE_LETTER_WORDS_NOT_ALLOWED,
                    Arrays.asList(commaSeperatedSingleLetterWords));
        }
    }

    /**
     * Link the new words to the existing words
     */
    private BoardWord linkWord(BoardWord word, VirtualCell[][] boardMatrix) {
        word.getBoard().getCells().forEach(cell -> {
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
                word.getBoard().getCells().forEach(c -> c.setSealed(true));
            }
        });
        return word;
    }

    private Integer calculateNewWordsScore(List<BoardWord> words) {
        return words.stream().mapToInt(word -> {
            final Integer wordScore = word.getBoard().getCells().stream().mapToInt(cell -> {
                return !cell.isLastPlayed() ? cell.getValue() : cell.getLetterValueMultiplier() * cell.getValue();
            }).sum();

            final Integer wordScoreMultiplier = word.getBoard().getCells().stream().mapToInt(cell -> {
                return !cell.isLastPlayed() ? 1 : cell.getWordScoreMultiplier();
            }).reduce(1, Math::multiplyExact);

            final Integer totalScore = wordScore * wordScoreMultiplier;
            word.setScore(totalScore);
            return totalScore;
        }).sum();
    }

    private void saveWord(Long gameId, Long userId, Long actionId, Integer roundNumber, BoardWord boardWord) {
        final Word word = new Word();
        word.setActionId(actionId);
        word.setGameId(gameId);
        word.setUserId(userId);
        word.setRoundNumber(roundNumber);
        word.setScore(boardWord.getScore());
        word.setWord(boardWord.getDictionaryWord().getWord());
        word.setDefinition(boardWord.getDictionaryWord().getDefinition());
        wordService.save(word);
    }

    private void updatePlayerScore(Long gameId, Integer playerNumber, Integer newWordsScore) {
        final Player player = playerService.getByPlayerNumber(gameId, playerNumber);
        player.setScore(player.getScore() + newWordsScore);
        playerService.save(player);
    }

}
