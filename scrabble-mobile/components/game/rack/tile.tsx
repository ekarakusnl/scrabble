import { LinearGradient } from 'expo-linear-gradient';
import { StyleSheet, View } from 'react-native';
import { Text, TouchableRipple } from 'react-native-paper';

const defaultCellColor = ['#fae1a6', '#d8bd72'];
const sealedCellColor = ['#f5b7b1', '#f5b7b1'];

export function RackTile({ tile, onSelectTile }) {

  return (
    <TouchableRipple
      style={styles.tileRipple}
      onPress={() => onSelectTile(tile)}>
      <View style={styles.tile}>
        <LinearGradient
          colors={tile.selected || tile.sealed ? sealedCellColor : defaultCellColor}
          style={styles.tileLetter}>
          <Text style={styles.tileLetterLabel}>{tile.letter}
            <Text style={styles.tileLetterScore}>{tile.value}</Text>
          </Text>
        </LinearGradient>
      </View>
    </TouchableRipple>
  );
};

const styles = StyleSheet.create({
  tileRipple: {
    marginLeft: 4,
    marginRight: 4,
  },
  tile: {
    flexDirection: 'row',
    width: 44,
    height: 44,
    borderColor: '#bd9768',
    borderWidth: 0.5,
    borderRadius: 7,
    justifyContent: 'center',
    alignItems: 'center',
  },
  tileLetter: {
    width: 44,
    height: 44,
    borderColor: '#bd9768',
    borderWidth: 0.5,
    borderRadius: 7,
    alignItems: 'center',
  },
  tileLetterLabel: {
    fontSize: 35,
    fontFamily: 'AbhayaLibre-Medium',
  },
  tileLetterScore: {
    fontSize: 15,
    fontFamily: 'AbhayaLibre-Medium',
    textAlignVertical: 'bottom',
  },
});
