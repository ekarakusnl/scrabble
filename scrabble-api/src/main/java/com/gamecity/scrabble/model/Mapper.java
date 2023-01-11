package com.gamecity.scrabble.model;

import java.util.stream.Collectors;

import com.gamecity.scrabble.entity.AbstractEntity;
import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.ActionType;
import com.gamecity.scrabble.entity.Bag;
import com.gamecity.scrabble.entity.BaseAuthority;
import com.gamecity.scrabble.entity.Board;
import com.gamecity.scrabble.entity.Chat;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.entity.GameStatus;
import com.gamecity.scrabble.entity.Word;
import com.gamecity.scrabble.entity.User;
import com.gamecity.scrabble.model.rest.AbstractDto;
import com.gamecity.scrabble.model.rest.ActionDto;
import com.gamecity.scrabble.model.rest.BagDto;
import com.gamecity.scrabble.model.rest.BoardDto;
import com.gamecity.scrabble.model.rest.VirtualCellDto;
import com.gamecity.scrabble.model.rest.ChatDto;
import com.gamecity.scrabble.model.rest.ExceptionDto;
import com.gamecity.scrabble.model.rest.ExceptionType;
import com.gamecity.scrabble.model.rest.GameDto;
import com.gamecity.scrabble.model.rest.PlayerDto;
import com.gamecity.scrabble.model.rest.VirtualTileDto;
import com.gamecity.scrabble.model.rest.UserDto;
import com.gamecity.scrabble.model.rest.VirtualBoardDto;
import com.gamecity.scrabble.model.rest.VirtualRackDto;
import com.gamecity.scrabble.model.rest.WordDto;
import com.gamecity.scrabble.service.exception.GameException;
import com.gamecity.scrabble.service.exception.UserException;

/**
 * Mapper class for conversions between entities and dtos
 * 
 * @author ekarakus
 */
public class Mapper {

    /**
     * Converts an entity to a dto
     * 
     * @param entity entity to convert
     * @return the dto
     */
    public static AbstractDto toDto(AbstractEntity entity) {
        if (entity instanceof Action) {
            return toDto((Action) entity);
        } else if (entity instanceof Bag) {
            return toDto((Bag) entity);
        } else if (entity instanceof Board) {
            return toDto((Board) entity);
        } else if (entity instanceof Chat) {
            return toDto((Chat) entity);
        } else if (entity instanceof Game) {
            return toDto((Game) entity);
        } else if (entity instanceof Player) {
            return toDto((Player) entity);
        } else if (entity instanceof User) {
            return toDto((User) entity);
        } else if (entity instanceof Word) {
            return toDto((Word) entity);
        } else {
            throw new IllegalStateException(entity.getClass() + " is not mapped");
        }
    }

    /**
     * Converts a dto to an entity
     * 
     * @param dto dto to convert
     * @return the entity
     */
    public static AbstractEntity toEntity(AbstractDto dto) {
        if (dto instanceof ActionDto) {
            return toEntity((ActionDto) dto);
        } else if (dto instanceof BagDto) {
            return toEntity((BagDto) dto);
        } else if (dto instanceof BoardDto) {
            return toEntity((BoardDto) dto);
        } else if (dto instanceof ChatDto) {
            return toEntity((ChatDto) dto);
        } else if (dto instanceof GameDto) {
            return toEntity((GameDto) dto);
        } else if (dto instanceof PlayerDto) {
            return toEntity((PlayerDto) dto);
        } else if (dto instanceof UserDto) {
            return toEntity((UserDto) dto);
        } else if (dto instanceof WordDto) {
            return toEntity((WordDto) dto);
        } else {
            throw new IllegalStateException(dto.getClass() + " is not mapped");
        }
    }

    /**
     * Converts a {@link User} to a {@link UserDto}
     * 
     * @param user
     * @return dto representation of the {@link User}
     */
    public static UserDto toDto(User user) {
        final UserDto userDto = UserDto.builder()
                .accountNonExpired(user.isAccountNonExpired())
                .accountNonLocked(user.isAccountNonLocked())
                .authorities(user.getAuthorities() == null ? null
                        : user.getAuthorities().stream().map(BaseAuthority::getAuthority).collect(Collectors.toList()))
                .credentialsNonExpired(user.isCredentialsNonExpired())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .id(user.getId())
                .lastUpdatedDate(user.getLastUpdatedDate())
                .password(user.getPassword())
                .username(user.getUsername())
                .build();

        return userDto;
    }

    /**
     * Converts a {@link UserDto} to a {@link User}
     * 
     * @param userDto
     * @return entity representation of the {@link UserDto}
     */
    public static User toEntity(UserDto userDto) {
        final User user = new User();
        user.setAccountNonExpired(userDto.isAccountNonExpired());
        user.setAccountNonLocked(userDto.isAccountNonLocked());
        user.setCredentialsNonExpired(userDto.isCredentialsNonExpired());
        user.setEmail(userDto.getEmail());
        user.setEnabled(userDto.isEnabled());
        user.setId(userDto.getId());
        user.setPassword(userDto.getPassword());
        user.setUsername(userDto.getUsername());

        return user;
    }

    /**
     * Converts a {@link Game} to a {@link GameDto}
     * 
     * @param game
     * @return dto representation of the {@link Game}
     */
    public static GameDto toDto(Game game) {
        final GameDto gameDto = GameDto.builder()
                .actionCounter(game.getActionCounter())
                .activePlayerCount(game.getActivePlayerCount())
                .bagId(game.getBagId())
                .boardId(game.getBoardId())
                .currentPlayerNumber(game.getCurrentPlayerNumber())
                .duration(game.getDuration())
                .expectedPlayerCount(game.getExpectedPlayerCount())
                .id(game.getId())
                .lastUpdatedDate(game.getLastUpdatedDate())
                .name(game.getName())
                .ownerId(game.getOwnerId())
                .roundNumber(game.getRoundNumber())
                .status(game.getStatus().name())
                .build();

        return gameDto;
    }

    /**
     * Converts a {@link GameDto} to a {@link Game}
     * 
     * @param gameDto
     * @return entity representation of the {@link GameDto}
     */
    public static Game toEntity(GameDto gameDto) {
        final Game game = new Game();
        game.setActionCounter(gameDto.getActionCounter());
        game.setActivePlayerCount(gameDto.getActivePlayerCount());
        game.setBagId(gameDto.getBagId());
        game.setBoardId(gameDto.getBoardId());
        game.setCurrentPlayerNumber(gameDto.getCurrentPlayerNumber());
        game.setDuration(gameDto.getDuration());
        game.setExpectedPlayerCount(gameDto.getExpectedPlayerCount());
        game.setId(gameDto.getId());
        game.setName(gameDto.getName());
        game.setOwnerId(gameDto.getOwnerId());
        game.setRoundNumber(gameDto.getRoundNumber());
        game.setStatus(gameDto.getStatus() == null ? null : GameStatus.valueOf(gameDto.getStatus()));

        return game;
    }

    /**
     * Converts a {@link Board} to a {@link BoardDto}
     * 
     * @param board
     * @return dto representation of the {@link Board}
     */
    public static BoardDto toDto(Board board) {
        final BoardDto boardDto = BoardDto.builder()
                .columnSize(board.getColumnSize())
                .id(board.getId())
                .lastUpdatedDate(board.getLastUpdatedDate())
                .name(board.getName())
                .rowSize(board.getRowSize())
                .build();

        return boardDto;
    }

    /**
     * Converts a {@link BoardDto} to a {@link Board}
     * 
     * @param boardDto
     * @return entity representation of the {@link BoardDto}
     */
    public static Board toEntity(BoardDto boardDto) {
        final Board board = new Board();
        board.setColumnSize(boardDto.getColumnSize());
        board.setId(boardDto.getId());
        board.setName(boardDto.getName());
        board.setRowSize(boardDto.getRowSize());

        return board;
    }

    /**
     * Converts a {@link Bag} to a {@link BagDto}
     * 
     * @param bag
     * @return dto representation of the {@link Bag}
     */
    public static BagDto toDto(Bag bag) {
        final BagDto bagDto = BagDto.builder()
                .id(bag.getId())
                .language(bag.getLanguage().name())
                .lastUpdatedDate(bag.getLastUpdatedDate())
                .name(bag.getName())
                .tileCount(bag.getTileCount())
                .build();

        return bagDto;
    }

    /**
     * Converts a {@link BagDto} to a {@link Bag}
     * 
     * @param bagDto
     * @return entity representation of the {@link BagDto}
     */
    public static Bag toEntity(BagDto bagDto) {
        final Bag bag = new Bag();
        bag.setId(bagDto.getId());
        bag.setLanguage(Language.valueOf(bagDto.getLanguage()));
        bag.setName(bagDto.getName());
        bag.setTileCount(bagDto.getTileCount());

        return bag;
    }

    /**
     * Converts a {@link Player} to a {@link PlayerDto}
     * 
     * @param player
     * @return dto representation of the {@link Player}
     */
    public static PlayerDto toDto(Player player) {
        final PlayerDto playerDto = PlayerDto.builder()
                .lastUpdatedDate(player.getLastUpdatedDate())
                .userId(player.getUserId())
                .playerNumber(player.getPlayerNumber())
                .score(player.getScore())
                .username(player.getUsername())
                .build();

        return playerDto;
    }

    /**
     * Converts a {@link PlayerDto} to a {@link Player}
     * 
     * @param playerDto
     * @return entity representation of the {@link PlayerDto}
     */
    public static Player toEntity(PlayerDto playerDto) {
        final Player player = new Player();
        player.setUserId(playerDto.getUserId());
        player.setPlayerNumber(playerDto.getPlayerNumber());
        player.setScore(playerDto.getScore());
        player.setUsername(playerDto.getUsername());

        return player;
    }

    /**
     * Converts a {@link VirtualTile} to a {@link VirtualTileDto}
     * 
     * @param tile
     * @return dto representation of the {@link VirtualTile}
     */
    public static VirtualTileDto toDto(VirtualTile tile) {
        if (tile == null) {
            return null;
        }
        final VirtualTileDto tileDto = VirtualTileDto.builder()
                .columnNumber(tile.getColumnNumber())
                .letter(tile.getLetter())
                .number(tile.getNumber())
                .playerNumber(tile.getPlayerNumber())
                .roundNumber(tile.getRoundNumber())
                .rowNumber(tile.getRowNumber())
                .sealed(tile.isSealed())
                .value(tile.getValue())
                .vowel(tile.isVowel())
                .build();

        return tileDto;
    }

    /**
     * Converts a {@link VirtualTileDto} to a {@link VirtualTile}
     * 
     * @param tileDto
     * @return entity representation of the {@link VirtualTileDto}
     */
    public static VirtualTile toEntity(VirtualTileDto tileDto) {
        if (tileDto == null) {
            return null;
        }
        final VirtualTile tile = VirtualTile.builder()
                .columnNumber(tileDto.getColumnNumber())
                .letter(tileDto.getLetter())
                .number(tileDto.getNumber())
                .playerNumber(tileDto.getPlayerNumber())
                .roundNumber(tileDto.getRoundNumber())
                .rowNumber(tileDto.getRowNumber())
                .sealed(tileDto.isSealed())
                .value(tileDto.getValue())
                .vowel(tileDto.isVowel())
                .build();

        return tile;
    }

    /**
     * Converts a {@link VirtualCell} to a {@link VirtualCellDto}
     * 
     * @param cell
     * @return dto representation of the {@link VirtualCell}
     */
    public static VirtualCellDto toDto(VirtualCell cell) {
        final VirtualCellDto cellDto = VirtualCellDto.builder()
                .cellNumber(cell.getCellNumber())
                .center(cell.isCenter())
                .color(cell.getColor())
                .columnNumber(cell.getColumnNumber())
                .hasBottom(cell.isHasBottom())
                .hasLeft(cell.isHasLeft())
                .hasRight(cell.isHasRight())
                .hasTop(cell.isHasTop())
                .lastPlayed(cell.isLastPlayed())
                .letter(cell.getLetter())
                .letterValueMultiplier(cell.getLetterValueMultiplier())
                .roundNumber(cell.getRoundNumber())
                .rowNumber(cell.getRowNumber())
                .sealed(cell.isSealed())
                .value(cell.getValue())
                .wordScoreMultiplier(cell.getWordScoreMultiplier())
                .build();

        return cellDto;
    }

    /**
     * Converts a {@link VirtualCellDto} to a {@link VirtualCell}
     * 
     * @param cellDto
     * @return entity representation of the {@link VirtualCellDto}
     */
    public static VirtualCell toEntity(VirtualCellDto cellDto) {
        final VirtualCell cell = VirtualCell.builder()
                .cellNumber(cellDto.getCellNumber())
                .center(cellDto.isCenter())
                .color(cellDto.getColor())
                .columnNumber(cellDto.getColumnNumber())
                .hasBottom(cellDto.isHasBottom())
                .hasLeft(cellDto.isHasLeft())
                .hasRight(cellDto.isHasRight())
                .hasTop(cellDto.isHasTop())
                .lastPlayed(cellDto.isLastPlayed())
                .letter(cellDto.getLetter())
                .letterValueMultiplier(cellDto.getLetterValueMultiplier())
                .roundNumber(cellDto.getRoundNumber())
                .rowNumber(cellDto.getRowNumber())
                .sealed(cellDto.isSealed())
                .value(cellDto.getValue())
                .wordScoreMultiplier(cellDto.getWordScoreMultiplier())
                .build();

        return cell;
    }

    /**
     * Converts a {@link Chat} to a {@link ChatDto}
     * 
     * @param chat
     * @return dto representation of the {@link Chat}
     */
    public static ChatDto toDto(Chat chat) {
        final ChatDto chatDto = ChatDto.builder()
                .createdDate(chat.getCreatedDate())
                .gameId(chat.getGameId())
                .lastUpdatedDate(chat.getLastUpdatedDate())
                .message(chat.getMessage())
                .userId(chat.getUserId())
                .username(chat.getUsername())
                .build();

        return chatDto;
    }

    /**
     * Converts a {@link ChatDto} to a {@link Chat}
     * 
     * @param chatDto
     * @return entity representation of the {@link ChatDto}
     */
    public static Chat toEntity(ChatDto chatDto) {
        final Chat chat = new Chat();
        chat.setGameId(chatDto.getGameId());
        chat.setMessage(chatDto.getMessage());
        chat.setUserId(chatDto.getUserId());
        chat.setUsername(chatDto.getUsername());

        return chat;
    }

    /**
     * Converts a {@link Action} to a {@link ActionDto}
     * 
     * @param action
     * @return dto representation of the {@link Action}
     */
    public static ActionDto toDto(Action action) {
        final ActionDto actionDto = ActionDto.builder()
                .counter(action.getCounter())
                .currentPlayerNumber(action.getCurrentPlayerNumber())
                .gameId(action.getGameId())
                .roundNumber(action.getRoundNumber())
                .status(action.getStatus().name())
                .type(action.getType().name())
                .build();

        return actionDto;
    }

    /**
     * Converts a {@link ActionDto} to a {@link Action}
     * 
     * @param actionDto
     * @return entity representation of the {@link ActionDto}
     */
    public static Action toEntity(ActionDto actionDto) {
        final Action action = new Action();
        action.setCounter(actionDto.getCounter());
        action.setCurrentPlayerNumber(actionDto.getCurrentPlayerNumber());
        // .currentStatus(actionDto.getCurrentStatus())
        action.setGameId(actionDto.getGameId());
        action.setRoundNumber(actionDto.getRoundNumber());
        action.setStatus(GameStatus.valueOf(actionDto.getStatus()));
        action.setType(ActionType.valueOf(actionDto.getType()));

        return action;
    }

    /**
     * Converts a {@link Word} to a {@link WordDto}
     * 
     * @param word
     * @return dto representation of the {@link Word}
     */
    public static WordDto toDto(Word word) {
        final WordDto wordDto = WordDto.builder()
                .gameId(word.getGameId())
                .lastUpdatedDate(word.getLastUpdatedDate())
                .playerNumber(word.getPlayerNumber())
                .roundNumber(word.getRoundNumber())
                .score(word.getScore())
                .word(word.getWord())
                .build();

        return wordDto;
    }

    /**
     * Converts a {@link WordDto} to a {@link Word}
     * 
     * @param wordDto
     * @return entity representation of the {@link WordDto}
     */
    public static Word toEntity(WordDto wordDto) {
        final Word word = new Word();
        word.setGameId(wordDto.getGameId());
        word.setPlayerNumber(wordDto.getPlayerNumber());
        word.setRoundNumber(wordDto.getRoundNumber());
        word.setScore(wordDto.getScore());
        word.setWord(wordDto.getWord());

        return word;
    }

    /**
     * Converts a {@link GameException} to a {@link ExceptionDto}
     * 
     * @param gameException
     * @return dto representation of the {@link GameException}
     */
    public static ExceptionDto toDto(GameException gameException) {
        final ExceptionDto exceptionDto = ExceptionDto.builder()
                .code(gameException.getCode())
                .message(gameException.getMessage())
                .params(gameException.getParams())
                .type(ExceptionType.GAME)
                .build();

        return exceptionDto;
    }

    /**
     * Converts a {@link UserException} to a {@link ExceptionDto}
     * 
     * @param userException
     * @return dto representation of the {@link UserException}
     */
    public static ExceptionDto toDto(UserException userException) {
        final ExceptionDto exceptionDto = ExceptionDto.builder()
                .code(userException.getCode())
                .message(userException.getMessage())
                .params(userException.getParams())
                .type(ExceptionType.USER)
                .build();

        return exceptionDto;
    }

    /**
     * Converts a {@link VirtualRack} to a {@link VirtualRackDto}
     * 
     * @param virtualRack
     * @return dto representation of the {@link VirtualRack}
     */
    public static VirtualRackDto toDto(VirtualRack virtualRack) {
        final VirtualRackDto virtualRackDto = new VirtualRackDto();
        if (virtualRack.getTiles() != null) {
            virtualRackDto.setTiles(virtualRack.getTiles().stream().map(Mapper::toDto).collect(Collectors.toList()));
        }

        return virtualRackDto;
    }

    /**
     * Converts a {@link VirtualRackDto} to a {@link VirtualRack}
     * 
     * @param virtualRackDto
     * @return entity representation of the {@link VirtualRackDto}
     */
    public static VirtualRack toEntity(VirtualRackDto virtualRackDto) {
        final VirtualRack virtualRack = new VirtualRack();
        if (virtualRackDto.getTiles() != null) {
            virtualRack.setTiles(virtualRackDto.getTiles().stream().map(Mapper::toEntity).collect(Collectors.toList()));
        }

        return virtualRack;
    }

    /**
     * Converts a {@link VirtualBoard} to a {@link VirtualBoardDto}
     * 
     * @param virtualBoard
     * @return dto representation of the {@link VirtualBoard}
     */
    public static VirtualBoardDto toDto(VirtualBoard virtualBoard) {
        final VirtualBoardDto virtualBoardDto = new VirtualBoardDto();
        virtualBoardDto.setCells(virtualBoard.getCells().stream().map(Mapper::toDto).collect(Collectors.toList()));

        return virtualBoardDto;
    }

    /**
     * Converts a {@link VirtualBoardDto} to a {@link VirtualBoard}
     * 
     * @param virtualBoardDto
     * @return entity representation of the {@link VirtualBoardDto}
     */
    public static VirtualBoard toEntity(VirtualBoardDto virtualBoardDto) {
        final VirtualBoard virtualBoard = new VirtualBoard();
        virtualBoard.setCells(virtualBoardDto.getCells().stream().map(Mapper::toEntity).collect(Collectors.toList()));

        return virtualBoard;
    }

}
