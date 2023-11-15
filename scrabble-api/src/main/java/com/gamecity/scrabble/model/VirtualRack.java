package com.gamecity.scrabble.model;

import java.io.Serializable;
import java.util.List;

import com.gamecity.scrabble.entity.Game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Virtual representation of a rack consisting of {@link VirtualTile tiles} in a {@link Game game}
 * 
 * @author ekarakus
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(callSuper = false)
public class VirtualRack implements Serializable {

    private static final long serialVersionUID = -174263293304406460L;

    private boolean exchanged;
    private List<VirtualTile> tiles;

}
