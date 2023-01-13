package com.gamecity.scrabble.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.gamecity.scrabble.dao.GameDao;
import com.gamecity.scrabble.entity.Bag;
import com.gamecity.scrabble.entity.Board;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.entity.Tile;
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
import com.gamecity.scrabble.service.UpdaterService;
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

@Service(value = "gameService")
@Slf4j
class GameServiceImpl extends AbstractServiceImpl<Game, GameDao> implements GameService {

    private UserService userService;
    private BagService bagService;
    private BoardService boardService;
    private PlayerService playerService;
    private VirtualBoardService virtualBoardService;
    private VirtualRackService virtualRackService;
    private VirtualBagService virtualBagService;
    private UpdaterService updaterService;
    private DictionaryService dictionaryService;
    private WordService wordService;

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
    }

    @Autowired
    void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    void setBagService(BagService bagService) {
        this.bagService = bagService;
    }

    @Autowired
    void setBoardService(BoardService boardService) {
        this.boardService = boardService;
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
    void setUpdaterService(UpdaterService updaterService) {
        this.updaterService = updaterService;
    }

    @Autowired
    void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Autowired
    void setWordService(WordService wordService) {
        this.wordService = wordService;
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

        boardService.get(game.getBoardId());
        final User user = userService.get(game.getOwnerId());

        game.setOwnerId(user.getId());
        game.setStatus(GameStatus.WAITING);
        game.setActivePlayerCount(1);
        game.setActionCounter(1);

        final Game savedGame = baseDao.save(game);

        playerService.add(game.getId(), user.getId(), game.getActivePlayerCount());

        return savedGame;
    }

    private Game update(Game game) {
        final Game existingGame = get(game.getId());

        if (existingGame.getActionCounter() > 1) {
            throw new GameException(GameError.IN_PROGRESS);
        }

        existingGame.setName(game.getName());
        existingGame.setBoardId(game.getBoardId());
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

        final Player player = playerService.loadByUserId(game.getId(), userId);
        if (player != null) {
            throw new GameException(GameError.IN_THE_GAME);
        }

        game.setActivePlayerCount(game.getActivePlayerCount() + 1);
        game.setActionCounter(game.getActionCounter() + 1);

        playerService.add(game.getId(), userId, game.getActivePlayerCount());

        // expected player count has been reached
        if (game.getActivePlayerCount().equals(game.getExpectedPlayerCount())) {
            log.debug("Expected player count has been reached, game {} is ready to start.", game.getId());
            game.setStatus(GameStatus.READY_TO_START);
        }

        return baseDao.save(game);
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

        final Player player = playerService.loadByUserId(game.getId(), userId);
        if (player == null) {
            throw new GameException(GameError.NOT_IN_THE_GAME);
        }

        if (game.getOwnerId().equals(userId)) {
            throw new GameException(GameError.OWNER_CANNOT_LEAVE);
        }

        playerService.remove(player);

        game.setActivePlayerCount(game.getActivePlayerCount() - 1);
        game.setActionCounter(game.getActionCounter() + 1);

        return baseDao.save(game);
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
        game.setActionCounter(game.getActionCounter() + 1);

        log.debug("Game {} has been started.", game.getId());

        return baseDao.save(game);
    }

    @Override
    @Transactional
    public Game play(Long id, Long userId, VirtualRack virtualRack) {
        Assert.notNull(id, "id cannot be null");
        Assert.notNull(userId, "userId cannot be null");
        Assert.notNull(virtualRack, "rack cannot be null");

        final Game game = get(id);

        if (GameStatus.IN_PROGRESS != game.getStatus() && GameStatus.LAST_ROUND != game.getStatus()) {
            throw new GameException(GameError.WAITING);
        }

        log.info("Playing on game {} as player {}.", game.getId(), game.getCurrentPlayerNumber());

        final Player player = playerService.loadByUserId(game.getId(), userId);
        final Integer currentPlayerNumber = game.getCurrentPlayerNumber();
        final Integer currentRoundNumber = game.getRoundNumber();

        isCurrentPlayer(player.getPlayerNumber(), currentPlayerNumber);
        isValidRack(game, player.getPlayerNumber(), virtualRack);

        final Integer boardActionCounter = game.getActionCounter() - game.getExpectedPlayerCount();

        final Bag bag = bagService.get(game.getBagId());
        final Board board = boardService.get(game.getBoardId());

        final VirtualCell[][] boardMatrix = new VirtualCell[board.getRowSize()][board.getColumnSize()];
        final VirtualBoard virtualBoard = virtualBoardService.getBoard(game.getId(), boardActionCounter);
        virtualBoard.getCells().stream().forEach(cell -> {
            boardMatrix[cell.getRowNumber() - 1][cell.getColumnNumber() - 1] = cell;
            cell.setLastPlayed(false);
        });

        makeMove(game, bag, board, virtualRack, boardMatrix);
        assignNextPlayer(game);

        game.setActionCounter(game.getActionCounter() + 1);

        boolean newRound = false;
        // if the next user is the owner, then a new round starts
        if (game.getActionCounter() % game.getExpectedPlayerCount() == 1) {
            game.setRoundNumber(currentRoundNumber + 1);
            newRound = true;
        }

        updaterService.run(game, virtualRack, virtualBoard, currentPlayerNumber, currentRoundNumber);

        if (newRound) {
            if (GameStatus.LAST_ROUND == game.getStatus()) {
                log.debug("The last round has been played, game {} is ready to end.", game.getId());
                game.setStatus(GameStatus.READY_TO_END);
            } else {
                final List<Tile> tiles = virtualBagService.getTiles(game.getId(), game.getBagId());
                // the bag is empty, set the last round
                if (tiles.isEmpty()) {
                    log.debug("No tiles left in the bag, the last round is going to be played on game {}.",
                            game.getId());
                    game.setStatus(GameStatus.LAST_ROUND);
                }
            }
        }

        return baseDao.save(game);
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
    public List<Game> list() {
        return baseDao.getLastGames(3);
    }

    @Override
    @Transactional
    public Game end(Long id) {
        Assert.notNull(id, "id cannot be null");

        final Game game = get(id);

        if (GameStatus.READY_TO_END != game.getStatus()) {
            throw new GameException(GameError.NOT_STARTED);
        }

        game.setEndDate(new Date());
        game.setStatus(GameStatus.ENDED);
        game.setActionCounter(game.getActionCounter() + 1);

        log.debug("Game {} has been ended.", game.getId());

        return baseDao.save(game);
    }

    @Override
    public List<Game> listByUser(Long userId) {
        return baseDao.getByUser(userId);
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

        final Predicate<VirtualTile> filter =
                tile -> tileMap.containsKey(tile.getNumber()) && tileMap.get(tile.getNumber()).equals(tile.getLetter());

        long rackMatchCount = virtualRack.getTiles().stream().filter(filter).count();
        if (rackMatchCount != virtualRack.getTiles().size()) {
            throw new GameException(GameError.RACK_DOES_NOT_MATCH);
        }
    }

    /**
     * Assigns the turn to the next player in the game
     */
    private void assignNextPlayer(Game game) {
        int nextPlayerNumber = (game.getActionCounter() % game.getExpectedPlayerCount()) + 1;
        game.setCurrentPlayerNumber(nextPlayerNumber);
        log.debug("Current player is set as player {} on game {}.", nextPlayerNumber, game.getId());
    }

    /**
     * Does the validations and makes the move
     */
    private void makeMove(Game game, Bag bag, Board board, VirtualRack updatedRack, VirtualCell[][] boardMatrix) {
        boolean hasNewMove = updatedRack.getTiles().stream().anyMatch(VirtualTile::isSealed);
        if (!hasNewMove) {
            return;
        }

        locateTilesOnBoard(updatedRack, boardMatrix);
        hasNonEmptyCenter(boardMatrix);

        final List<BoardWord> newWords = findWordsOnBoard(game.getId(), game.getCurrentPlayerNumber(),
                game.getRoundNumber(), board, boardMatrix);

        final List<BoardWord> invalidWords = newWords.stream()
                .filter(word -> !isValidWord(word.getWordDefinition().toString(), bag.getLanguage()))
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(invalidWords)) {
            final String commaSeperatedInvalidWords = String.join(",",
                    invalidWords.stream().map(BoardWord::getWordDefinition).collect(Collectors.toList()));
            log.debug("Word(s) {} are not found in {} dictionary.", commaSeperatedInvalidWords, bag.getLanguage());
            throw new GameException(GameError.WORDS_ARE_NOT_FOUND,
                    Arrays.asList(commaSeperatedInvalidWords, bag.getLanguage().name()));
        }

        hasValidLink(newWords, boardMatrix);

        final Integer newWordsScore = calculateNewWordsScore(newWords);
        updatePlayerScore(game.getId(), game.getCurrentPlayerNumber(), newWordsScore);

        newWords.stream().forEach(word -> {
            word.getBoard().getCells().forEach(virtualCell -> virtualCell.setLastPlayed(true));
        });
        logWords(game.getId(), game.getCurrentPlayerNumber(), game.getRoundNumber(), newWords);
    }

    /*
     * Log the words
     */
    private void logWords(Long gameId, Integer playerNumber, Integer roundNumber, List<BoardWord> boardWords) {
        boardWords.forEach(word -> saveWord(gameId, playerNumber, roundNumber, word));
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
    private List<BoardWord> findWordsOnBoard(Long gameId, Integer playerNumber, Integer roundNumber, Board board,
            VirtualCell[][] boardMatrix) {
        final List<BoardWord> words = new ArrayList<>();

        // horizontal words
        IntStream.range(1, board.getRowSize() + 1).forEach(rowNumber -> {
            final BoardWord boardWord = new BoardWord(Direction.HORIZONTAL);
            IntStream.range(1, board.getColumnSize() + 1).forEach(columnNumber -> {
                final VirtualCell cell = boardMatrix[rowNumber - 1][columnNumber - 1];
                final BoardWord detectedWord = findWordsByDirection(gameId, playerNumber, roundNumber, board, cell,
                        Direction.HORIZONTAL, boardWord);
                if (detectedWord != null) {
                    words.add(detectedWord);
                    log.debug("Horizontal word {} has been detected on game {} by player {}.",
                            detectedWord.getWordDefinition(), gameId, playerNumber);
                }
            });
        });

        // vertical words
        IntStream.range(1, board.getColumnSize() + 1).forEach(columnNumber -> {
            final BoardWord boardWord = new BoardWord(Direction.VERTICAL);
            IntStream.range(1, board.getRowSize() + 1).forEach(rowNumber -> {
                final VirtualCell cell = boardMatrix[rowNumber - 1][columnNumber - 1];
                final BoardWord detectedWord = findWordsByDirection(gameId, playerNumber, roundNumber, board, cell,
                        Direction.VERTICAL, boardWord);
                if (detectedWord != null) {
                    words.add(detectedWord);
                    log.debug("Vertical word {} has been detected on game {} by player {}.",
                            detectedWord.getWordDefinition(), gameId, playerNumber);
                }
            });
        });

        return words;
    }

    /**
     * Finds the words on the board by direction
     */
    private BoardWord findWordsByDirection(Long gameId, Integer playerNumber, Integer roundNumber, Board board,
            VirtualCell cell, Direction direction, BoardWord boardWord) {

        if (cell.getLetter() != null) {
            boardWord.getWordDefinition().append(cell.getLetter());
            boardWord.getBoard().getCells().add(cell);
            boardWord.setLinked(boardWord.isLinked() || cell.isCenter());
            if (!cell.isSealed()) {
                cell.setRoundNumber(roundNumber);
            }

            log.debug("A {} {} letter is spotted on [{},{}] on game {} by player {}.",
                    boardWord.isLinked() ? "new linked" : "new", direction, cell.getRowNumber(), cell.getColumnNumber(),
                    gameId, playerNumber);
        }

        boolean emptyCell = cell.getLetter() == null;
        boolean horizontalLast = Direction.HORIZONTAL == direction && cell.getColumnNumber() == board.getColumnSize();
        boolean verticalLast = Direction.VERTICAL == direction && cell.getRowNumber() == board.getRowSize();

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

    /**
     * Whether the word exists in a dictionary
     */
    private boolean isValidWord(String word, Language language) {
        return dictionaryService.hasWord(word, language);
    }

    /**
     * Whether the words are linked to existing words
     */
    private void hasValidLink(List<BoardWord> newWords, VirtualCell[][] boardMatrix) {
        final List<BoardWord> unlinkedWords =
                newWords.stream().filter(word -> !word.isLinked()).collect(Collectors.toList());

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
                return cell.getLetterValueMultiplier() * cell.getValue();
            }).sum();

            final Integer wordScoreMultiplier = word.getBoard().getCells().stream().mapToInt(cell -> {
                return cell.getWordScoreMultiplier();
            }).reduce(1, Math::multiplyExact);

            final Integer totalScore = wordScore * wordScoreMultiplier;
            word.setScore(totalScore);
            return totalScore;
        }).sum();
    }

    private void saveWord(Long gameId, Integer playerNumber, Integer roundNumber, BoardWord boardWord) {
        final Word word = new Word();
        word.setGameId(gameId);
        word.setPlayerNumber(playerNumber);
        word.setRoundNumber(roundNumber);
        word.setScore(boardWord.getScore());
        word.setWord(boardWord.getWordDefinition().toString());
        wordService.save(word);
    }

    private void updatePlayerScore(Long gameId, Integer playerNumber, Integer newWordsScore) {
        final Player player = playerService.loadByPlayerNumber(gameId, playerNumber);
        player.setScore(player.getScore() + newWordsScore);
        playerService.save(player);
    }

}
