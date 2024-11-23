package com.gamecity.scrabble.model;

import java.io.Serializable;
import java.util.List;

import com.gamecity.scrabble.entity.Game;

import static com.gamecity.scrabble.Constants.Game.BOARD_SIZE;
import static com.gamecity.scrabble.model.ExtensionPoint.BACKWARD;
import static com.gamecity.scrabble.model.ExtensionPoint.FORWARD;
import static com.gamecity.scrabble.model.Direction.HORIZONTAL;
import static com.gamecity.scrabble.model.Direction.VERTICAL;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Virtual representation of a {@link Board board} in a {@link Game game}
 * 
 * @author ekarakus
 */
@Data
@NoArgsConstructor
public class VirtualBoard implements Serializable {

    private static final long serialVersionUID = 7362737151207981041L;

    private final VirtualCell[][] matrix = new VirtualCell[BOARD_SIZE][BOARD_SIZE];
    private List<VirtualCell> cells;

    /**
     * Creates a new virtual board with the given {@link VirtualCell cells}
     * 
     * @param cells the cells
     */
    public VirtualBoard(final List<VirtualCell> cells) {
        this.cells = cells;
        cells.stream().forEach(cell -> {
            matrix[cell.getRowNumber() - 1][cell.getColumnNumber() - 1] = cell;
        });
    }

    /**
     * Clears lastPlayed value of the cells
     */
    public void clearLastPlayed() {
        this.cells.stream().forEach(cell -> cell.setLastPlayed(false));
    }

    /**
     * Gets the board cells as a 2 dimension matrix
     * 
     * @return the board matrix
     */
    public VirtualCell[][] getMatrix() {
        return matrix;
    }

    /**
     * Gets the board cell by the given <code>rowNumber</code> and <code>columnNumber</code>
     * 
     * @param rowNumber    rowNumber of the cell
     * @param columnNumber columnNumber of the cell
     * @return the board cell
     */
    public VirtualCell getCell(Integer rowNumber, Integer columnNumber) {
        return matrix[rowNumber - 1][columnNumber - 1];
    }

    /**
     * Whether the given board cell has the available neigbour cell by the given
     * <code>direction</code> and <code>extension</code>
     * 
     * @param cell           the board cell
     * @param direction      direction of the neighbour cell
     * @param extensionPoint extension point of the cell
     * @return true if the neigbour is available
     */
    public boolean hasAvailableNeighbour(VirtualCell cell, Direction direction, ExtensionPoint extensionPoint) {
        if (!hasNeighbour(cell, direction, extensionPoint)) {
            return false;
        }

        final VirtualCell neighbourCell = getNeighbour(cell, direction, extensionPoint);

        final Direction oppositeDirection = HORIZONTAL == direction ? VERTICAL : HORIZONTAL;


        // TODO add a test
        // neighbour cell is empty and not the neighbour of an existing cell tile
        return neighbourCell != null && neighbourCell.getLetter() == null
                && hasNotUsedNeighbour(neighbourCell, direction, extensionPoint)
                && hasNotUsedNeighbour(neighbourCell, oppositeDirection, FORWARD)
                && hasNotUsedNeighbour(neighbourCell, oppositeDirection, BACKWARD);
    }

    /**
     * Whether the given board cell has the empty neigbour cell by the given <code>direction</code>
     * and <code>extension</code>
     * 
     * @param cell           the board cell
     * @param direction      direction of the neighbour cell
     * @param extensionPoint extension point of the cell
     * @return true if the neigbour is empty
     */
    public boolean hasNotUsedNeighbour(VirtualCell cell, Direction direction, ExtensionPoint extensionPoint) {
        if (!hasNeighbour(cell, direction, extensionPoint)) {
            return true;
        }

        final VirtualCell neighbourCell = getNeighbour(cell, direction, extensionPoint);

        // TODO add a test
        // neighbour cell is empty
        return neighbourCell != null && neighbourCell.getLetter() == null;
    }

    /**
     * Whether the given board cell has the sealed neigbour cell by the given <code>direction</code>
     * and <code>extension</code>
     * 
     * @param cell           the board cell
     * @param direction      direction of the neighbour cell
     * @param extensionPoint extension point of the cell
     * @return true if the neigbour is sealed
     */
    public boolean hasSealedNeighbour(VirtualCell cell, Direction direction, ExtensionPoint extensionPoint) {
        // TODO add a test
        if (!hasNeighbour(cell, direction, extensionPoint)) {
            return false;
        }

        final VirtualCell neighbourCell = getNeighbour(cell, direction, extensionPoint);

        // TODO add a test
        // neighbour cell is empty
        return neighbourCell != null && neighbourCell.isSealed();
    }

    /**
     * Get the neighbour board cell by the given <code>direction</code> and <code>extension</code>
     * 
     * @param cell           the board cell
     * @param direction      direction of the neighbour cell
     * @param extensionPoint extension point of the cell
     * @return the neighbour cell
     */
    public VirtualCell getNeighbour(VirtualCell cell, Direction direction, ExtensionPoint extensionPoint) {
        if (!hasNeighbour(cell, direction, extensionPoint)) {
            return null;
        }

        final Integer changeByPosition = (FORWARD == extensionPoint) ? 1 : -1;

        final Integer rowNumber = (HORIZONTAL == direction) ? cell.getRowNumber() - 1
                : (cell.getRowNumber() - 1) + changeByPosition;

        final Integer columnNumber = (HORIZONTAL == direction) ? (cell.getColumnNumber() - 1) + changeByPosition
                : cell.getColumnNumber() - 1;

        return matrix[rowNumber][columnNumber];
    }

    private boolean hasNeighbour(VirtualCell cell, Direction direction, ExtensionPoint extensionPoint) {
        return (cell.isHasRight() && HORIZONTAL == direction && FORWARD == extensionPoint)
                || (cell.isHasBottom() && VERTICAL == direction && FORWARD == extensionPoint)
                || (cell.isHasLeft() && HORIZONTAL == direction && BACKWARD == extensionPoint)
                || (cell.isHasTop() && VERTICAL == direction && BACKWARD == extensionPoint);
    }

}
