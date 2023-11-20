import { LinearGradient } from 'expo-linear-gradient';
import { useRef, useState } from 'react';
import { Animated, PanResponder, StyleSheet, View } from 'react-native';
import { Text } from 'react-native-paper';

import { DraggableTile } from '../../../model/draggable-tile';

const defaultCellColor = ['#fae1a6', '#d8bd72'];
const sealedCellColor = ['#f5b7b1', '#f5b7b1'];

export function RackTile({ tile, hasTurn, onDragTile, onDropTile }) {

  const [dragging, setDragging] = useState<boolean>(false);

  const animatedValue = useRef<Animated.ValueXY>(new Animated.ValueXY()).current;
  const tileLayoutRef = useRef<View>();
  const draggableTileRef = useRef<DraggableTile>();
  const initialXPosition = useRef<number>(0);
  const initialYPosition = useRef<number>(0);

  animatedValue.addListener((value) => {
    draggableTileRef.current.x = initialXPosition.current + value.x;
    draggableTileRef.current.y = initialYPosition.current + value.y;
  });

  const panResponder = useRef(
    PanResponder.create({
      onPanResponderGrant: () => {
        // start dragging
        setDragging(true);
      },
      onPanResponderMove: Animated.event(
        [
          null,
          {
            dx: animatedValue.x,
            dy: animatedValue.y,
          },
        ],
        {
          useNativeDriver: false,
          listener: (() => onDragTile(draggableTileRef.current))
        },
      ),
      onPanResponderRelease: () => {
        // stop dragging
        setDragging(false);

        // relase the tile
        onDropTile(draggableTileRef.current);

        // move the tile to its original location
        animatedValue.setValue({ x: 0, y: 0 });
        animatedValue.flattenOffset();

        // reset the current coordinates to its default
        draggableTileRef.current.x = initialXPosition.current;
        draggableTileRef.current.y = initialYPosition.current;
      },
    })
  ).current;

  panResponder.panHandlers.onStartShouldSetResponder = () => hasTurn;
  panResponder.panHandlers.onMoveShouldSetResponder = () => hasTurn;

  function onTileLayout(): void {
    if (tileLayoutRef.current) {
      tileLayoutRef.current.measure((fx, fy, width, height, px, py) => {
        initialXPosition.current = px;
        initialYPosition.current = py;
        draggableTileRef.current = {
          number: tile.number,
          letter: tile.letter,
          value: tile.value,
          x: px,
          y: py,
          width: width,
          height: height,
        };
      });
    }
  }

  return (
    <Animated.View
      style={[styles.animatedView,
      {
        transform: animatedValue.getTranslateTransform(),
        opacity: dragging ? 0.5 : 1,
      },
      ]}
      {...panResponder.panHandlers}>
      <View
        style={styles.tile}
        ref={(ref) => tileLayoutRef.current = ref}
        onLayout={() => { onTileLayout() }}>
        {
          tile.exchanged || tile.sealed ? ''
            :
            <LinearGradient
              colors={tile.sealed ? sealedCellColor : defaultCellColor}
              style={styles.tileLetter}>
              <Text style={styles.tileLetterLabel}>{tile.letter}
                <Text style={styles.tileLetterScore}>{tile.value}</Text>
              </Text>
            </LinearGradient>
        }
      </View>
    </Animated.View>
  );
};

const styles = StyleSheet.create({
  animatedView: {
    width: 40,
    height: 40,
    marginLeft: 4,
    marginRight: 4,
  },
  tile: {
    flexDirection: 'row',
    width: 40,
    height: 40,
    borderColor: '#bd9768',
    borderWidth: 0.5,
    borderRadius: 7,
    justifyContent: 'center',
    alignItems: 'center',
  },
  tileLetter: {
    width: 40,
    height: 40,
    borderColor: '#bd9768',
    borderWidth: 0.5,
    borderRadius: 7,
    alignItems: 'center',
  },
  tileLetterLabel: {
    fontSize: 34,
    fontFamily: 'AbhayaLibre-Medium',
  },
  tileLetterScore: {
    fontSize: 14,
    fontFamily: 'AbhayaLibre-Medium',
    textAlignVertical: 'bottom',
  },
});
