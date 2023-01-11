package com.gamecity.scrabble.resource.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.gamecity.scrabble.dao.RedisRepository;
import com.gamecity.scrabble.entity.Chat;
import com.gamecity.scrabble.model.Mapper;
import com.gamecity.scrabble.model.rest.ChatDto;
import com.gamecity.scrabble.resource.ChatResource;
import com.gamecity.scrabble.service.ChatService;

@Component(value = "chatResource")
class ChatResourceImpl extends AbstractResourceImpl<Chat, ChatDto, ChatService> implements ChatResource {

    private RedisRepository redisRepository;
    private ChatService baseService;

    ChatService getBaseService() {
        return baseService;
    }

    @Autowired
    void setBaseService(ChatService baseService) {
        this.baseService = baseService;
    }

    @Autowired
    void setRedisRepository(RedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    @Override
    public Response list(Long gameId, Integer actionCounter) {
        if (actionCounter < 1) {
            return Response.ok().build();
        }

        final List<Chat> newChats = redisRepository.getChats(gameId, actionCounter);
        if (CollectionUtils.isEmpty(newChats)) {
            return Response.ok().build();
        }

        final List<ChatDto> newChatDtos = newChats.stream().map(Mapper::toDto).collect(Collectors.toList());
        return Response.ok(newChatDtos).build();
    }

}
