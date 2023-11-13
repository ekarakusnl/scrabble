import { StyleSheet, View } from 'react-native';
import { Avatar, Text } from 'react-native-paper';

export function PlayerScore({ lastAction, player, winnerPlayer }) {

  return (
    <View
      style={[
        styles.card,
        winnerPlayer && winnerPlayer.playerNumber === player.playerNumber ?
          styles.winnerPlayerCard : lastAction.currentPlayerNumber === player.playerNumber ?
            styles.currentPlayerCard : styles.defaultPlayerCard
      ]}>
      <Avatar.Image
        source={{ uri: process.env.EXPO_PUBLIC_PROFILE_PICTURE_URL + player.userId + '?' + new Date().getTime() }}
        size={48} />
      <View style={styles.details}>
        <Text variant="titleSmall" style={styles.username}>{player.username}</Text>
        <Text variant="titleSmall" style={styles.score}>{player.score}</Text>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#f7f3f9',
    borderWidth: 1,
    borderRadius: 7,
    borderStyle: 'solid',
    justifyContent: 'center',
    alignItems: 'center',
    paddingRight: 8,
    paddingLeft: 8,
    paddingTop: 4,
    paddingBottom: 4,
    marginRight: 8,
  },
  defaultPlayerCard: {
    borderColor: '#fff',
    opacity: 0.65,
  },
  currentPlayerCard: {
    borderColor: '#001f3f',
    borderWidth: 4,
  },
  winnerPlayerCard: {
    borderColor: '#3d9970',
    borderWidth: 4,
  },
  details: {
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 2,
  },
  username: {
    fontFamily: 'Gilroy-Bold',
    fontSize: 14,
    marginBottom: 2,
  },
  score: {
    fontFamily: 'Gilroy-Bold',
    fontSize: 18,
  },
});
