import { ReactElement, useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';

import VirtualBoardService from '../../../services/virtual-board.service';

import { VirtualBoard } from '../../../model/virtual-board';
import { Cell } from '../../../model/cell';
import { BoardRow } from './row';
import { GameStatus } from '../../../model/game-status';

const ROW_SIZE = 15;
const COLUMN_SIZE = 15;

export function Board({ game, lastAction, selectedTileRef, rackRef, notificationRef }) {

  const { t } = useTranslation();

  const [rows, setRows] = useState<ReactElement[]>();
  const rowsRef = useRef<ReactElement[]>();

  const virtualBoardRef = useRef<VirtualBoard>();

  useEffect(() => {
    if (!game || !lastAction || !(lastAction.gameStatus === GameStatus.IN_PROGRESS || lastAction.gameStatus === GameStatus.ENDED)) {
      return;
    }

    loadCells();

    return () => {
    };
  }, [lastAction]);

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
          onPutTile={onPutTile} />
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
        onPutTile={onPutTile} />;

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

  function onPutTile(cell: Cell) {
    if (selectedTileRef.current) {
      // put the tile to the board
      if (!cell.letter) {
        // update the cell with the values of the selected tile
        cell.letter = selectedTileRef.current.letter;
        cell.value = selectedTileRef.current.value;
        cell.selectedTile = selectedTileRef.current;

        selectedTileRef.current.cellNumber = cell.cellNumber;
        selectedTileRef.current.rowNumber = cell.rowNumber;
        selectedTileRef.current.columnNumber = cell.columnNumber;
        selectedTileRef.current.sealed = true;

        // update the board
        updateRows(cell);

        // reset the selected tile
        selectedTileRef.current = null;

        // update the rack
        rackRef.current.update();
      } else if (cell.letter) {
        notificationRef.current.warning(t('error.2010', { 0: cell.rowNumber, 1: cell.columnNumber }));
        selectedTileRef.current.sealed = false;
        selectedTileRef.current.selected = false;

        // reset the selected tile
        selectedTileRef.current = null;

        // update the rack
        rackRef.current.update();
      }
    } else if (cell.letter && cell.selectedTile) {
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

      selectedTileRef.current = cell.selectedTile;
      // update the rack
      rackRef.current.update();
    }
  }

  if (!rows || !lastAction || !(lastAction.gameStatus === GameStatus.IN_PROGRESS || lastAction.gameStatus === GameStatus.ENDED)) {
    return null;
  }

  return (
    <View style={styles.board}>
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
