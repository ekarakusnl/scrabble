package com.gamecity.scrabble.dao.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.dao.TileDao;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.Tile;

@Repository(value = "tileDao")
class TileDaoImpl extends AbstractDaoImpl<Tile> implements TileDao {

    @Override
    public List<Tile> getTiles(Language language) {
        return listByNamedQuery(Constants.NamedQuery.getTiles, Arrays.asList(Pair.of("language", language)));
    }

}
