import { ReactElement, useEffect, useImperativeHandle, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';

import { BoardRow } from './row';

import VirtualBoardService from '../../../services/virtual-board.service';

import { Cell } from '../../../model/cell';
import { DraggableTile } from '../../../model/draggable-tile';
import { DroppableCell } from '../../../model/droppable-cell';
import { GameStatus } from '../../../model/game-status';
import { VirtualBoard } from '../../../model/virtual-board';

const ROW_SIZE = 15;
const COLUMN_SIZE = 15;

export function Board({ game, lastAction, boardRef, boardZoneRef, rackRef, notificationRef }) {

  const { t } = useTranslation();

  const [rows, setRows] = useState<ReactElement[]>();

  const rowsRef = useRef<ReactElement[]>();
  const boardLayoutRef = useRef<View>();
  const virtualBoardRef = useRef<VirtualBoard>();
  const droppableCellsRef = useRef<DroppableCell[]>([]);

  useEffect(() => {
    if (!game || !lastAction || !(lastAction.gameStatus === GameStatus.IN_PROGRESS || lastAction.gameStatus === GameStatus.ENDED)) {
      return;
    }

    loadCells();

    return () => {
    };
  }, [lastAction]);

  useImperativeHandle(boardRef, () => ({
    onDragTile: (draggableTile: DraggableTile) => { onDragTile(draggableTile) },
    onDropTile: (draggableTile: DraggableTile) => { onDropTile(draggableTile) },
  }));

  function loadCells(): void {
    const boardVersion = lastAction.version - game.expectedPlayerCount;
    VirtualBoardService.get(game.id, lastAction.gameStatus === GameStatus.ENDED ? boardVersion - 1 : boardVersion).then((board: VirtualBoard) => {
      virtualBoardRef.current = board;
      createRows();
    }).catch(error => {
      notificationRef.current.error(error);
    });
  };

  function createRows(): void {
    const rows: ReactElement[] = [];
    for (var number = 0; number < ROW_SIZE; number++) {
      const startColumn = number * COLUMN_SIZE;
      const endColumn = startColumn + COLUMN_SIZE;
      rows.push(
        <BoardRow
          key={'row_' + number}
          cells={virtualBoardRef.current.cells.slice(startColumn, endColumn)}
          onRemoveTile={onRemoveTile}
          onInitializeDroppableCell={onInitializeDroppableCell} />
      );
    }
    rowsRef.current = rows;
    setRows(rows);
  }

  function updateRows(cell: Cell): void {
    const rowNumber = (cell.rowNumber - 1);
    const startColumn = rowNumber * COLUMN_SIZE;
    const endColumn = startColumn + COLUMN_SIZE;

    const updatedRow: ReactElement =
      <BoardRow
        key={'row_' + rowNumber}
        cells={virtualBoardRef.current.cells.slice(startColumn, endColumn)}
        onRemoveTile={onRemoveTile}
        onInitializeDroppableCell={onInitializeDroppableCell} />;

    const updatedRows = [];
    for (let rowNumber = 0; rowNumber < ROW_SIZE; rowNumber++) {
      if (cell.rowNumber === rowNumber + 1) {
        updatedRows.push(updatedRow);
      } else {
        updatedRows.push(rowsRef.current[rowNumber]);
      }
    }
    rowsRef.current = updatedRows;
    setRows(updatedRows);
  }

  function onBoardLayout(): void {
    if (boardLayoutRef.current && !boardZoneRef.current) {
      const paddingHorizontal = 0;
      const paddingVertical = 0;
      boardLayoutRef.current.measure((fx, fy, width, height, px, py) => {
        boardZoneRef.current = {
          x: px + paddingHorizontal,
          y: py + paddingVertical,
          width: width - paddingHorizontal,
          height: height - paddingVertical
        };
      });
    }
  }

  function onInitializeDroppableCell(droppableCell: DroppableCell): void {
    droppableCellsRef.current.push(droppableCell);
  }

  function isTileBetweenHorizontal(currentx: number, start: number, end: number): boolean {
    return start <= currentx && currentx <= end;
  }

  function isTileBetweenVertical(currenty: number, start: number, end: number): boolean {
    return start <= currenty && currenty <= end;
  }

  function onDragTile(draggableTile: DraggableTile): void {
    
  }

  function onDropTile(draggableTile: DraggableTile): void {
    const horizontalCenter = draggableTile.x + (draggableTile.width / 2);
    const verticalCenter = draggableTile.y + (draggableTile.height / 2);

    const droppableCell = droppableCellsRef.current.find((droppableCell: DroppableCell) => {
      return isTileBetweenHorizontal(horizontalCenter, droppableCell.x, droppableCell.x + droppableCell.width)
        && isTileBetweenVertical(verticalCenter, droppableCell.y, droppableCell.y + droppableCell.height);
    });

    if (!droppableCell) {
      return;
    }

    const selectedTile = draggableTile.tile;
    const cell = droppableCell.cell;
    // put the tile to the board
    if (!cell.letter) {
      // update the cell with the values of the selected tile
      cell.letter = selectedTile.letter;
      cell.value = selectedTile.value;
      cell.selectedTile = selectedTile;

      selectedTile.cellNumber = cell.cellNumber;
      selectedTile.rowNumber = cell.rowNumber;
      selectedTile.columnNumber = cell.columnNumber;
      selectedTile.sealed = true;

      // update the board
      updateRows(cell);

      // update the rack
      rackRef.current.update();
    } else if (cell.letter) {
      notificationRef.current.warning(t('error.2010', { 0: cell.rowNumber, 1: cell.columnNumber }));
      selectedTile.sealed = false;
      selectedTile.selected = false;

      // update the rack
      rackRef.current.update();
    }
  }

  function onRemoveTile(cell: Cell): void {
    if (!cell.selectedTile) {
      return;
    }

    // remove the tile from the board
    cell.selectedTile.sealed = false;
    cell.selectedTile.selected = false;
    cell.selectedTile.cellNumber = null;
    cell.selectedTile.rowNumber = null;
    cell.selectedTile.columnNumber = null;

    // reset the cell
    cell.letter = null;
    cell.selectedTile = null;
    cell.value = null;

    // update the board
    updateRows(cell);

    // update the rack
    rackRef.current.update();
  }

  if (!rows || !lastAction || !(lastAction.gameStatus === GameStatus.IN_PROGRESS || lastAction.gameStatus === GameStatus.ENDED)) {
    return null;
  }

  return (
    <View
      style={styles.board}
      ref={(ref) => boardLayoutRef.current = ref}
      onLayout={() => { onBoardLayout() }}>
      {rows}
    </View>
  )
};

const styles = StyleSheet.create({
  board: {
    marginTop: 10,
    marginBottom: 10,
  },
});
