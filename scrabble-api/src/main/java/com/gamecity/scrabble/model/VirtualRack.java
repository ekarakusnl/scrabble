package com.gamecity.scrabble.model;

import java.io.Serializable;
import java.util.List;

import com.gamecity.scrabble.entity.Game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Virtual representation of a rack consisting of {@link VirtualTile tiles} in a {@link Game game}
 * 
 * @author ekarakus
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VirtualRack implements Serializable {

    private static final long serialVersionUID = -174263293304406460L;

    private boolean exchanged;
    private List<VirtualTile> tiles;

}
