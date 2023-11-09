package com.gamecity.scrabble.resource.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.gamecity.scrabble.entity.Chat;
import com.gamecity.scrabble.model.Mapper;
import com.gamecity.scrabble.model.rest.ChatDto;
import com.gamecity.scrabble.resource.ChatResource;
import com.gamecity.scrabble.service.ChatService;

@Component(value = "chatResource")
class ChatResourceImpl extends AbstractResourceImpl<Chat, ChatDto, ChatService> implements ChatResource {

    private ChatService baseService;

    ChatService getBaseService() {
        return baseService;
    }

    @Autowired
    void setBaseService(ChatService baseService) {
        this.baseService = baseService;
    }

    @Override
    public Response list(Long gameId) {
        final List<Chat> chats = baseService.getChats(gameId);
        if (CollectionUtils.isEmpty(chats)) {
            return Response.ok(Collections.emptyList()).build();
        }

        final List<ChatDto> newChatDtos = chats.stream().map(Mapper::toDto).collect(Collectors.toList());
        return Response.ok(newChatDtos).build();
    }

}
