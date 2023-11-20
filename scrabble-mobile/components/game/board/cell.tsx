import { LinearGradient } from 'expo-linear-gradient';
import { useRef } from 'react';
import { StyleSheet, View } from 'react-native';
import { Avatar, Text, TouchableRipple } from 'react-native-paper';

import { DroppableCell } from '../../../model/droppable-cell';

const usedCellColor = ['#fae1a6', '#d8bd72'];
const playedCellColor = ['#abdbe3', '#abdbe3'];
const sealedCellColor = ['#f5b7b1', '#f5b7b1'];

const TILE_ON_CELL_OPACITY = 0.2;
const TILE_NOT_ON_CELL_OPACITY = 1;

export function BoardCell({ cell, onRemoveTile, onInitializeDroppableCell }) {

  const cellRef = useRef<View>();
  const droppableCellRef = useRef<DroppableCell>();

  function onCellLayout(): void {
    if (cellRef.current) {
      cellRef.current.measure((fx, fy, width, height, px, py) => {
        droppableCellRef.current = {
          cellNumber: cell.cellNumber,
          x: px,
          y: py,
          width: width,
          height: height,
        };
        onInitializeDroppableCell(droppableCellRef.current);
      });
    }
  }

  return (
    <TouchableRipple
      onPress={() => onRemoveTile(cell)}>
      <View
        ref={(ref) => cellRef.current = ref}
        onLayout={() => { onCellLayout() }}
        style={[styles.cell, { backgroundColor: cell.color }]}>
        {
          cell.letter ?
            <LinearGradient
              colors={cell.lastPlayed ? playedCellColor : cell.tileNumber ? sealedCellColor : usedCellColor}
              style={styles.cellLetter}>
              <Text style={styles.cellLetterLabel}>{cell.letter}
                <Text style={styles.cellLetterScore}>{cell.value}</Text>
              </Text>
            </LinearGradient>
            :
            cell.center ?
              <Avatar.Icon
                size={25}
                icon="star-outline"
                style={styles.centerIcon} />
              :
              <Text style={styles.multiplierLetter}>
                {cell.wordScoreMultiplier > 1 ? cell.wordScoreMultiplier + 'W' : cell.letterValueMultiplier > 1 ? cell.letterValueMultiplier + 'L' : ''}
              </Text>
        }
      </View>
    </TouchableRipple >
  );
};

const styles = StyleSheet.create({
  cell: {
    flexDirection: 'row',
    width: 26.5,
    height: 26.5,
    borderWidth: 0.5,
    borderRadius: 4,
    justifyContent: 'center',
    alignItems: 'center',
  },
  cellLetter: {
    width: 26.5,
    height: 26.5,
    borderStyle: 'solid',
    borderWidth: 0.5,
    borderRadius: 4,
    alignItems: 'center',
  },
  multiplierLetter: {
    fontSize: 16,
    fontFamily: 'AbhayaLibre-Bold',
  },
  cellLetterLabel: {
    fontSize: 23,
    fontFamily: 'AbhayaLibre-Medium',
  },
  cellLetterScore: {
    fontSize: 11,
    fontFamily: 'AbhayaLibre-Medium',
    textAlignVertical: 'bottom',
  },
  centerIcon: {
    backgroundColor: 'orange',
  },
});
