package com.gamecity.scrabble.model;

import java.util.Collections;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.gamecity.scrabble.entity.AbstractEntity;
import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.ActionType;
import com.gamecity.scrabble.entity.BaseAuthority;
import com.gamecity.scrabble.entity.Chat;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.entity.GameStatus;
import com.gamecity.scrabble.entity.Word;
import com.gamecity.scrabble.entity.User;
import com.gamecity.scrabble.model.rest.AbstractDto;
import com.gamecity.scrabble.model.rest.ActionDto;
import com.gamecity.scrabble.model.rest.VirtualCellDto;
import com.gamecity.scrabble.model.rest.ChatDto;
import com.gamecity.scrabble.model.rest.ExceptionDto;
import com.gamecity.scrabble.model.rest.ExceptionType;
import com.gamecity.scrabble.model.rest.GameDto;
import com.gamecity.scrabble.model.rest.PlayerDto;
import com.gamecity.scrabble.model.rest.UserDto;
import com.gamecity.scrabble.model.rest.VirtualTileDto;
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
        return UserDto.builder()
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
                .preferredLanguage(user.getPreferredLanguage() != null ? user.getPreferredLanguage().name() : null)
                .username(user.getUsername())
                .build();
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
        user.setPreferredLanguage(StringUtils.isEmpty(userDto.getPreferredLanguage()) ? null
                : Language.valueOf(userDto.getPreferredLanguage()));
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
        return GameDto.builder()
                .version(game.getVersion())
                .activePlayerCount(game.getActivePlayerCount())
                .currentPlayerNumber(game.getCurrentPlayerNumber())
                .duration(game.getDuration())
                .expectedPlayerCount(game.getExpectedPlayerCount())
                .id(game.getId())
                .language(game.getLanguage().name())
                .createdDate(game.getCreatedDate())
                .lastUpdatedDate(game.getLastUpdatedDate())
                .name(game.getName())
                .ownerId(game.getOwnerId())
                .remainingTileCount(game.getRemainingTileCount())
                .roundNumber(game.getRoundNumber())
                .status(game.getStatus().name())
                .build();
    }

    /**
     * Converts a {@link GameDto} to a {@link Game}
     * 
     * @param gameDto
     * @return entity representation of the {@link GameDto}
     */
    public static Game toEntity(GameDto gameDto) {
        final Game game = new Game();
        game.setVersion(gameDto.getVersion());
        game.setActivePlayerCount(gameDto.getActivePlayerCount());
        game.setCurrentPlayerNumber(gameDto.getCurrentPlayerNumber());
        game.setDuration(gameDto.getDuration());
        game.setExpectedPlayerCount(gameDto.getExpectedPlayerCount());
        game.setId(gameDto.getId());
        game.setLanguage(Language.valueOf(gameDto.getLanguage()));
        game.setName(gameDto.getName());
        game.setOwnerId(gameDto.getOwnerId());
        game.setRemainingTileCount(gameDto.getRemainingTileCount());
        game.setRoundNumber(gameDto.getRoundNumber());
        game.setStatus(gameDto.getStatus() == null ? null : GameStatus.valueOf(gameDto.getStatus()));
        return game;
    }

    /**
     * Converts a {@link Player} to a {@link PlayerDto}
     * 
     * @param player
     * @return dto representation of the {@link Player}
     */
    public static PlayerDto toDto(Player player) {
        return PlayerDto.builder()
                .lastUpdatedDate(player.getLastUpdatedDate())
                .userId(player.getUserId())
                .playerNumber(player.getPlayerNumber())
                .score(player.getScore())
                .username(player.getUsername())
                .build();
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
        return VirtualTileDto.builder()
                .columnNumber(tile.getColumnNumber())
                .letter(tile.getLetter())
                .number(tile.getNumber())
                .playerNumber(tile.getPlayerNumber())
                .roundNumber(tile.getRoundNumber())
                .rowNumber(tile.getRowNumber())
                .exchanged(tile.isExchanged())
                .sealed(tile.isSealed())
                .value(tile.getValue())
                .vowel(tile.isVowel())
                .build();
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
        return VirtualTile.builder()
                .columnNumber(tileDto.getColumnNumber())
                .letter(tileDto.getLetter())
                .number(tileDto.getNumber())
                .playerNumber(tileDto.getPlayerNumber())
                .roundNumber(tileDto.getRoundNumber())
                .rowNumber(tileDto.getRowNumber())
                .sealed(tileDto.isSealed())
                .exchanged(tileDto.isExchanged())
                .value(tileDto.getValue())
                .vowel(tileDto.isVowel())
                .build();
    }

    /**
     * Converts a {@link VirtualCell} to a {@link VirtualCellDto}
     * 
     * @param cell
     * @return dto representation of the {@link VirtualCell}
     */
    public static VirtualCellDto toDto(VirtualCell cell) {
        return VirtualCellDto.builder()
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
    }

    /**
     * Converts a {@link VirtualCellDto} to a {@link VirtualCell}
     * 
     * @param cellDto
     * @return entity representation of the {@link VirtualCellDto}
     */
    public static VirtualCell toEntity(VirtualCellDto cellDto) {
        return VirtualCell.builder()
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
    }

    /**
     * Converts a {@link Chat} to a {@link ChatDto}
     * 
     * @param chat
     * @return dto representation of the {@link Chat}
     */
    public static ChatDto toDto(Chat chat) {
        return ChatDto.builder()
                .createdDate(chat.getCreatedDate())
                .id(chat.getId())
                .gameId(chat.getGameId())
                .lastUpdatedDate(chat.getLastUpdatedDate())
                .message(chat.getMessage())
                .userId(chat.getUserId())
                .username(chat.getUsername())
                .build();
    }

    /**
     * Converts a {@link ChatDto} to a {@link Chat}
     * 
     * @param chatDto
     * @return entity representation of the {@link ChatDto}
     */
    public static Chat toEntity(ChatDto chatDto) {
        final Chat chat = new Chat();
        chat.setCreatedDate(chatDto.getCreatedDate());
        chat.setGameId(chatDto.getGameId());
        chat.setMessage(chatDto.getMessage());
        chat.setUserId(chatDto.getUserId());
        return chat;
    }

    /**
     * Converts a {@link Action} to a {@link ActionDto}
     * 
     * @param action
     * @return dto representation of the {@link Action}
     */
    public static ActionDto toDto(Action action) {
        return ActionDto.builder()
                .version(action.getVersion())
                .currentPlayerNumber(action.getCurrentPlayerNumber())
                .gameId(action.getGameId())
                .id(action.getId())
                .lastUpdatedDate(action.getLastUpdatedDate())
                .roundNumber(action.getRoundNumber())
                .gameStatus(action.getGameStatus().name())
                .remainingTileCount(action.getRemainingTileCount())
                .score(action.getScore())
                .type(action.getType().name())
                .userId(action.getUserId())
                .build();
    }

    /**
     * Converts a {@link ActionDto} to a {@link Action}
     * 
     * @param actionDto
     * @return entity representation of the {@link ActionDto}
     */
    public static Action toEntity(ActionDto actionDto) {
        final Action action = new Action();
        action.setVersion(actionDto.getVersion());
        action.setCurrentPlayerNumber(actionDto.getCurrentPlayerNumber());
        action.setGameId(actionDto.getGameId());
        action.setGameStatus(GameStatus.valueOf(actionDto.getGameStatus()));
        action.setLastUpdatedDate(actionDto.getLastUpdatedDate());
        action.setRemainingTileCount(actionDto.getRemainingTileCount());
        action.setRoundNumber(actionDto.getRoundNumber());
        action.setScore(actionDto.getScore());
        action.setType(ActionType.valueOf(actionDto.getType()));
        action.setUserId(actionDto.getUserId());
        return action;
    }

    /**
     * Converts a {@link Word} to a {@link WordDto}
     * 
     * @param word
     * @return dto representation of the {@link Word}
     */
    public static WordDto toDto(Word word) {
        return WordDto.builder()
                .id(word.getId())
                .actionId(word.getActionId())
                .gameId(word.getGameId())
                .lastUpdatedDate(word.getLastUpdatedDate())
                .userId(word.getUserId())
                .roundNumber(word.getRoundNumber())
                .score(word.getScore())
                .word(word.getWord())
                .definition(word.getDefinition())
                .build();
    }

    /**
     * Converts a {@link WordDto} to a {@link Word}
     * 
     * @param wordDto
     * @return entity representation of the {@link WordDto}
     */
    public static Word toEntity(WordDto wordDto) {
        return Word.builder()
                .actionId(wordDto.getActionId())
                .gameId(wordDto.getGameId())
                .userId(wordDto.getUserId())
                .roundNumber(wordDto.getRoundNumber())
                .score(wordDto.getScore())
                .word(wordDto.getWord())
                .definition(wordDto.getDefinition())
                .build();
    }

    /**
     * Converts a {@link GameException} to a {@link ExceptionDto}
     * 
     * @param gameException
     * @return dto representation of the {@link GameException}
     */
    public static ExceptionDto toDto(GameException gameException) {
        return ExceptionDto.builder()
                .code(gameException.getCode())
                .message(gameException.getMessage())
                .params(gameException.getParams())
                .type(ExceptionType.GAME)
                .build();
    }

    /**
     * Converts a {@link UserException} to a {@link ExceptionDto}
     * 
     * @param userException
     * @return dto representation of the {@link UserException}
     */
    public static ExceptionDto toDto(UserException userException) {
        return ExceptionDto.builder()
                .code(userException.getCode())
                .message(userException.getMessage())
                .params(userException.getParams())
                .type(ExceptionType.USER)
                .build();
    }

    /**
     * Converts a {@link VirtualRack} to a {@link VirtualRackDto}
     * 
     * @param virtualRack
     * @return dto representation of the {@link VirtualRack}
     */
    public static VirtualRackDto toDto(VirtualRack virtualRack) {
        final VirtualRackDto virtualRackDto = new VirtualRackDto();
        if (!CollectionUtils.isEmpty(virtualRack.getTiles())) {
            virtualRackDto.setTiles(virtualRack.getTiles().stream().map(Mapper::toDto).collect(Collectors.toList()));
        } else {
            virtualRackDto.setTiles(Collections.emptyList());
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
