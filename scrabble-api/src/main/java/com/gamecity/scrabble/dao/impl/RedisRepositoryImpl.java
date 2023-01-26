package com.gamecity.scrabble.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.dao.RedisRepository;
import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.Chat;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.model.Mapper;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.rest.ChatDto;
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
        redisTemplate.convertAndSend(Constants.CacheKey.ACTION, payload);
    }

    @Override
    public void publishChat(Long gameId, Chat chat) {
        final ChatDto chatDto = Mapper.toDto(chat);
        redisTemplate.convertAndSend(Constants.CacheKey.CHATS, chatDto);
    }

    @Override
    public void updateBoard(Long gameId, VirtualBoard board) {
        redisTemplate.boundListOps(Constants.CacheKey.BOARD + ":" + gameId).rightPush(board);
    }

    @Override
    public VirtualBoard getBoard(Long gameId, Integer version) {
        final BoundListOperations<String, Object> boards =
                redisTemplate.boundListOps(Constants.CacheKey.BOARD + ":" + gameId);
        return (VirtualBoard) boards.range(version - 1, -1).stream().findFirst().orElse(null);
    }

    @Override
    public void fillRack(Long gameId, Integer playerNumber, VirtualRack rack) {
        redisTemplate.boundListOps(Constants.CacheKey.RACK + ":" + gameId + ":" + playerNumber).rightPush(rack);
    }

    @Override
    public void updateRack(Long gameId, Integer playerNumber, Integer roundNumber, VirtualRack rack) {
        redisTemplate.boundListOps(Constants.CacheKey.RACK + ":" + gameId + ":" + playerNumber)
                .set(roundNumber - 1, rack);
    }

    @Override
    public VirtualRack getRack(Long gameId, Integer playerNumber, Integer roundNumber) {
        final BoundListOperations<String, Object> racks =
                redisTemplate.boundListOps(Constants.CacheKey.RACK + ":" + gameId + ":" + playerNumber);
        return (VirtualRack) racks.range(roundNumber - 1, -1).stream().findFirst().orElse(null);
    }

}
