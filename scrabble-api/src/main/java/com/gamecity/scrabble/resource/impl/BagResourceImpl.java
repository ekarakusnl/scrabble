package com.gamecity.scrabble.resource.impl;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gamecity.scrabble.entity.Bag;
import com.gamecity.scrabble.model.rest.BagDto;
import com.gamecity.scrabble.resource.BagResource;
import com.gamecity.scrabble.service.BagService;

@Component(value = "bagResource")
class BagResourceImpl extends AbstractResourceImpl<Bag, BagDto, BagService> implements BagResource {

    private BagService baseService;

    BagService getBaseService() {
        return baseService;
    }

    @Autowired
    void setBaseService(BagService baseService) {
        this.baseService = baseService;
    }

    @Override
    public Response get(Long id) {
        return super.get(id);
    }

}
