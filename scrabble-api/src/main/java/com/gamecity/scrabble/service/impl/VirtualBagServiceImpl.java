package com.gamecity.scrabble.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.entity.Tile;
import com.gamecity.scrabble.service.BagService;
import com.gamecity.scrabble.service.VirtualBagService;

@Service(value = "virtualBagService")
class VirtualBagServiceImpl implements VirtualBagService {

    private BagService bagService;

    @Autowired
    void setBagService(BagService bagService) {
        this.bagService = bagService;
    }

    @Cacheable(value = Constants.CacheKey.TILES, key = "#gameId")
    @Override
    public List<Tile> getTiles(Long gameId, Long bagId) {
        return bagService.getTiles(bagId);
    }

    @Override
    @CachePut(value = Constants.CacheKey.TILES, key = "#gameId")
    public List<Tile> updateTiles(Long gameId, List<Tile> tiles) {
        return tiles;
    }

}
