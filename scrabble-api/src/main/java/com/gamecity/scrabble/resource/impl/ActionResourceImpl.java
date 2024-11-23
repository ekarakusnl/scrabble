package com.gamecity.scrabble.resource.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.Response;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.model.Mapper;
import com.gamecity.scrabble.model.rest.ActionDto;
import com.gamecity.scrabble.resource.ActionResource;
import com.gamecity.scrabble.service.ActionService;

@Component(value = "actionResource")
class ActionResourceImpl extends AbstractResourceImpl<Action, ActionDto, ActionService> implements ActionResource {

    @Override
    public Response get(Long gameId, Integer version) {
        // TODO add a test
        if (version < 1) {
            return Response.ok().build();
        }

        boolean hasNewAction = baseService.hasNewAction(gameId, version);
        // TODO add a test
        if (!hasNewAction) {
            return Response.ok().build();
        }

        final Action action = baseService.getAction(gameId, version);
        // TODO add a test
        if (action == null) {
            return Response.ok().build();
        }

        // TODO add a test
        return Response.ok(Mapper.toDto(action)).build();
    }

    @Override
    public Response list(Long gameId) {
        final List<Action> actions = baseService.getActions(gameId);
        // TODO add a test
        if (CollectionUtils.isEmpty(actions)) {
            return Response.ok(Collections.emptyList()).build();
        }

        final List<ActionDto> actionDtos = actions.stream().map(Mapper::toDto).collect(Collectors.toList());
        return Response.ok(actionDtos).build();
    }

}
