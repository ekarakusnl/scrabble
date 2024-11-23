package com.gamecity.scrabble.resource.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.Response;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.gamecity.scrabble.entity.Chat;
import com.gamecity.scrabble.model.Mapper;
import com.gamecity.scrabble.model.rest.ChatDto;
import com.gamecity.scrabble.resource.ChatResource;
import com.gamecity.scrabble.service.ChatService;

@Component(value = "chatResource")
class ChatResourceImpl extends AbstractResourceImpl<Chat, ChatDto, ChatService> implements ChatResource {

    @Override
    public Response list(Long gameId) {
        final List<Chat> chats = baseService.getChats(gameId);
        // TODO add a test
        if (CollectionUtils.isEmpty(chats)) {
            return Response.ok(Collections.emptyList()).build();
        }

        final List<ChatDto> newChatDtos = chats.stream().map(Mapper::toDto).collect(Collectors.toList());
        return Response.ok(newChatDtos).build();
    }

}
