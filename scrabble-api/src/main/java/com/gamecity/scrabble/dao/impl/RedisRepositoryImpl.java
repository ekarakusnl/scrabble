package com.gamecity.scrabble.dao.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.dao.RedisRepository;
import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.Chat;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.model.VirtualScoreboard;
import com.gamecity.scrabble.model.Mapper;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.rest.ActionDto;
import com.gamecity.scrabble.model.rest.ChatDto;
import com.gamecity.scrabble.model.rest.PlayerDto;
import com.gamecity.scrabble.util.JsonUtils;

@Repository(value = "redisRepository")
class RedisRepositoryImpl implements RedisRepository {

    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void publishAction(Long gameId, Action action) {
        final String payload = JsonUtils.toJson(Mapper.toDto(action));
        redisTemplate.boundListOps(Constants.CacheKey.ACTION + ":" + gameId).rightPush(payload);
        redisTemplate.convertAndSend(Constants.CacheKey.ACTION, payload);
    }

    @Override
    public Action getAction(Long gameId, Integer counter) {
        final BoundListOperations<String, Object> actions =
                redisTemplate.boundListOps(Constants.CacheKey.ACTION + ":" + gameId);
        final String actionPayload = (String) actions.range(counter - 1, -1).stream().findFirst().orElse(null);
        return actionPayload == null ? null : Mapper.toEntity(JsonUtils.toDto(actionPayload, ActionDto.class));
    }

    @Override
    public void updatePlayers(Long gameId, List<Player> players) {
        final List<PlayerDto> playerDtos = players.stream().map(Mapper::toDto).collect(Collectors.toList());
        redisTemplate.boundListOps(Constants.CacheKey.PLAYERS + ":" + gameId)
                .rightPush(new VirtualScoreboard(playerDtos));
        redisTemplate.convertAndSend(Constants.CacheKey.PLAYERS, playerDtos);
    }

    @Override
    public List<Player> getPlayers(Long gameId, Integer actionCounter) {
        final BoundListOperations<String, Object> players =
                redisTemplate.boundListOps(Constants.CacheKey.PLAYERS + ":" + gameId);
        final VirtualScoreboard virtualScoreboard =
                (VirtualScoreboard) players.range(actionCounter - 1, -1).stream().findFirst().orElse(null);
        return virtualScoreboard == null ? Collections.emptyList()
                : virtualScoreboard.getPlayers().stream().map(Mapper::toEntity).collect(Collectors.toList());
    }

    @Override
    public void publishChat(Long gameId, Chat chat) {
        final ChatDto chatDto = Mapper.toDto(chat);
        redisTemplate.boundListOps(Constants.CacheKey.CHATS + ":" + gameId).rightPush(chatDto);
        redisTemplate.convertAndSend(Constants.CacheKey.CHATS, chatDto);
    }

    @Override
    public List<Chat> getChats(Long gameId, Integer actionCounter) {
        final BoundListOperations<String, Object> chats =
                redisTemplate.boundListOps(Constants.CacheKey.CHATS + ":" + gameId);
        return chats.range(actionCounter - 1, -1)
                .stream()
                .map(chat -> Mapper.toEntity((ChatDto) chat))
                .collect(Collectors.toList());
    }

    @Override
    public void updateBoard(Long gameId, VirtualBoard board) {
        redisTemplate.boundListOps(Constants.CacheKey.BOARD + ":" + gameId).rightPush(board);
        redisTemplate.convertAndSend(Constants.CacheKey.BOARD, board);
    }

    @Override
    public VirtualBoard getBoard(Long gameId, Integer actionCounter) {
        final BoundListOperations<String, Object> boards =
                redisTemplate.boundListOps(Constants.CacheKey.BOARD + ":" + gameId);
        return (VirtualBoard) boards.range(actionCounter - 1, -1).stream().findFirst().orElse(null);
    }

    @Override
    public void refreshRack(Long gameId, Integer playerNumber, VirtualRack rack) {
        redisTemplate.boundListOps(Constants.CacheKey.RACK + ":" + gameId + ":" + playerNumber).rightPush(rack);
        redisTemplate.convertAndSend(Constants.CacheKey.RACK, rack);
    }

    @Override
    public void updateRack(Long gameId, Integer playerNumber, Integer roundNumber, VirtualRack rack) {
        redisTemplate.boundListOps(Constants.CacheKey.RACK + ":" + gameId + ":" + playerNumber)
                .set(roundNumber - 1, rack);
        redisTemplate.convertAndSend(Constants.CacheKey.RACK, rack);
    }

    @Override
    public VirtualRack getRack(Long gameId, Integer playerNumber, Integer roundNumber) {
        final BoundListOperations<String, Object> racks =
                redisTemplate.boundListOps(Constants.CacheKey.RACK + ":" + gameId + ":" + playerNumber);
        return (VirtualRack) racks.range(roundNumber - 1, -1).stream().findFirst().orElse(null);
    }

}
