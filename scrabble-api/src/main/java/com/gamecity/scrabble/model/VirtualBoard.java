package com.gamecity.scrabble.model;

import java.io.Serializable;
import java.util.List;

import com.gamecity.scrabble.entity.Game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Virtual representation of a {@link Board board} in a {@link Game game}
 * 
 * @author ekarakus
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VirtualBoard implements Serializable {

    private static final long serialVersionUID = 7362737151207981041L;

    private List<VirtualCell> cells;

}
