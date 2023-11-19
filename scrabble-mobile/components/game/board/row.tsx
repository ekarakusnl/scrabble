import { StyleSheet, View } from 'react-native';

import { BoardCell } from './cell';

import { Cell } from '../../../model/cell';

export function BoardRow({ cells, onRemoveTile, onInitializeDroppableCell }) {

  return (
    <View style={styles.row}>
      {
        cells.map((cell: Cell) => (
          <BoardCell
            key={'column_' + cell.rowNumber + '_' + cell.columnNumber}
            cell={cell}
            onRemoveTile={onRemoveTile}
            onInitializeDroppableCell={onInitializeDroppableCell} />
        ))
      }
    </View>
  );
};

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    justifyContent: 'center',
  },
});
