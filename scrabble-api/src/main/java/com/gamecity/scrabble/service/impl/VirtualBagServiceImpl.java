package com.gamecity.scrabble.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.dao.TileDao;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.Tile;
import com.gamecity.scrabble.service.VirtualBagService;

@Service(value = "virtualBagService")
class VirtualBagServiceImpl implements VirtualBagService {

    private TileDao tileDao;

    @Autowired
    void setTileDao(TileDao tileDao) {
        this.tileDao = tileDao;
    }

    @Cacheable(value = Constants.CacheKey.TILES, key = "#gameId")
    @Override
    public List<Tile> getTiles(Long gameId, Language language) {
        return tileDao.getTiles(language);
    }

    @Override
    @CachePut(value = Constants.CacheKey.TILES, key = "#gameId")
    public List<Tile> updateTiles(Long gameId, List<Tile> tiles) {
        return tiles;
    }

}
