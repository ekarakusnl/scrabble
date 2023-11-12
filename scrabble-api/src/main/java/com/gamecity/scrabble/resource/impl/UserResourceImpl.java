package com.gamecity.scrabble.resource.impl;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gamecity.scrabble.entity.User;
import com.gamecity.scrabble.model.Mapper;
import com.gamecity.scrabble.model.rest.UserDto;
import com.gamecity.scrabble.resource.UserResource;
import com.gamecity.scrabble.service.UserService;

@Component(value = "userResource")
class UserResourceImpl extends AbstractResourceImpl<User, UserDto, UserService> implements UserResource {

    private UserService baseService;

    @Override
    UserService getBaseService() {
        return baseService;
    }

    @Autowired
    void setBaseService(UserService baseService) {
        this.baseService = baseService;
    }

    @Override
    public Response findByUsername(String username) {
        final User user = baseService.loadUserByUsername(username);
        final UserDto userDto = Mapper.toDto(user);
        return Response.ok(userDto).tag(createETag(userDto)).build();
    }

    @Override
    public Response getUser(Long id) {
        final User user = baseService.get(id);
        final UserDto userDto = Mapper.toDto(user);
        return Response.ok(userDto).tag(createETag(userDto)).build();
    }

}
