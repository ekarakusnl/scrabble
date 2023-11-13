import { StyleSheet, View } from 'react-native';

import { BoardCell } from './cell';
import { Cell } from '../../../model/cell';

export function BoardRow({ cells, onPutTile }) {

  return (
    <View style={styles.row}>
      {
        cells.map((cell: Cell) => (
          <BoardCell
            key={'column_' + cell.rowNumber + '_' + cell.columnNumber}
            cell={cell}
            onPutTile={onPutTile} />
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
