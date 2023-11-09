package com.gamecity.scrabble.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gamecity.scrabble.dao.BagDao;
import com.gamecity.scrabble.dao.TileDao;
import com.gamecity.scrabble.entity.Bag;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.Tile;
import com.gamecity.scrabble.service.BagService;

@Service(value = "bagService")
class BagServiceImpl extends AbstractServiceImpl<Bag, BagDao> implements BagService {

    private TileDao tileDao;

    @Autowired
    void setTileDao(TileDao tileDao) {
        this.tileDao = tileDao;
    }

    @Override
    public List<Tile> getTiles(Language language) {
        return tileDao.getTiles(language);
    }

}
