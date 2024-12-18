package com.gamecity.scrabble.model;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.gamecity.scrabble.entity.AbstractEntity;
import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.BaseAuthority;
import com.gamecity.scrabble.entity.Chat;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.entity.GameStatus;
import com.gamecity.scrabble.entity.GameType;
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

    private Mapper() {
    }

    /**
     * Converts an entity to a dto
     * 
     * @param entity entity to convert
     * @return the dto
     */
    public static AbstractDto toDto(AbstractEntity entity) {
        if (entity instanceof Chat) {
            return toDto((Chat) entity);
        } else if (entity instanceof Game) {
            return toDto((Game) entity);
        } else if (entity instanceof User) {
            return toDto((User) entity);
        }

        throw new IllegalStateException(entity.getClass() + " is not mapped");
    }

    /**
     * Converts a dto to an entity
     * 
     * @param dto dto to convert
     * @return the entity
     */
    public static AbstractEntity toEntity(AbstractDto dto) {
        if (dto instanceof ChatDto) {
            return toEntity((ChatDto) dto);
        } else if (dto instanceof GameDto) {
            return toEntity((GameDto) dto);
        } else if (dto instanceof UserDto) {
            return toEntity((UserDto) dto);
        }

        throw new IllegalStateException(dto.getClass() + " is not mapped");
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
        return User.builder()
                .accountNonExpired(userDto.isAccountNonExpired())
                .accountNonLocked(userDto.isAccountNonLocked())
                .credentialsNonExpired(userDto.isCredentialsNonExpired())
                .email(userDto.getEmail())
                .enabled(userDto.isEnabled())
                .id(userDto.getId())
                .password(userDto.getPassword())
                .preferredLanguage(StringUtils.isEmpty(userDto.getPreferredLanguage()) ? null
                        : Language.valueOf(userDto.getPreferredLanguage()))
                .username(userDto.getUsername())
                .build();
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
                .type(game.getType().name())
                .build();
    }

    /**
     * Converts a {@link GameDto} to a {@link Game}
     * 
     * @param gameDto
     * @return entity representation of the {@link GameDto}
     */
    public static Game toEntity(GameDto gameDto) {
        return Game.builder()
                .version(gameDto.getVersion())
                .activePlayerCount(gameDto.getActivePlayerCount())
                .currentPlayerNumber(gameDto.getCurrentPlayerNumber())
                .duration(gameDto.getDuration())
                .expectedPlayerCount(gameDto.getExpectedPlayerCount())
                .id(gameDto.getId())
                .language(Language.valueOf(gameDto.getLanguage()))
                .name(gameDto.getName())
                .ownerId(gameDto.getOwnerId())
                .remainingTileCount(gameDto.getRemainingTileCount())
                .roundNumber(gameDto.getRoundNumber())
                .status(gameDto.getStatus() == null ? null : GameStatus.valueOf(gameDto.getStatus()))
                .type(gameDto.getType() == null ? null : GameType.valueOf(gameDto.getType()))
                .build();
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
     * Converts a {@link VirtualTile} to a {@link VirtualTileDto}
     * 
     * @param tile
     * @return dto representation of the {@link VirtualTile}
     */
    public static VirtualTileDto toDto(VirtualTile tile) {
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
        return Chat.builder()
                .createdDate(chatDto.getCreatedDate())
                .gameId(chatDto.getGameId())
                .message(chatDto.getMessage())
                .userId(chatDto.getUserId())
                .build();
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
        virtualRackDto.setTiles(virtualRack.getTiles().stream().map(Mapper::toDto).collect(Collectors.toList()));
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
        virtualRack.setTiles(virtualRackDto.getTiles().stream().map(Mapper::toEntity).collect(Collectors.toList()));
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

}
