import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';
import { Avatar, Text } from 'react-native-paper';

export function RemainingTile({ remainingTileCount }) {

  const { t } = useTranslation();

  return (
    <View style={styles.remainingTileCount}>
      <Avatar.Icon
        size={48}
        style={styles.remainingTileCountIcon}
        icon="alphabetical" />
      <Text variant="bodySmall" style={styles.remainingTileCountText}>{remainingTileCount}</Text>
      <Text variant="bodySmall" style={styles.remainingTileCountLabel}>{t('game.scoreboard.tiles')}</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  remainingTileCount: {
    backgroundColor: '#3c8dbc',
    borderColor: '#3c8dbc',
    borderWidth: 0.5,
    borderRadius: 7,
    justifyContent: 'center',
    alignItems: 'center',
  },
  remainingTileCountIcon: {
    backgroundColor: '#3c8dbc',
  },
  remainingTileCountText: {
    color: '#fff',
    fontFamily: 'Gilroy-Regular',
    fontSize: 14,
    paddingLeft: 4,
    paddingRight: 4,
    paddingBottom: 2,
  },
  remainingTileCountLabel: {
    color: '#fff',
    fontFamily: 'Playball-Regular',
    fontSize: 15,
    marginTop: 4,
  },
});
