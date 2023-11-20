import { router } from 'expo-router';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';
import { Avatar, Button, Text } from 'react-native-paper';

import Icon from 'react-native-paper/src/components/Icon'

import GameService from '../../services/game.service';
import PlayerService from '../../services/player.service';

import { GameStatus } from '../../model/game-status';
import { Player } from '../../model/player';

export function PlayerList({ userId, game, lastAction, notificationRef }) {

  const { t } = useTranslation();

  const [players, setPlayers] = useState<Player[]>([]);

  useEffect(() => {
    if (!game || !lastAction || !userId) {
      return;
    }

    loadPlayers();

    return () => {
    };
  }, [lastAction]);

  function loadPlayers(): void {
    PlayerService.list(game.id, lastAction.version).then((players: Player[]) => {
      players = players.sort((previous, current) => {
        return previous.playerNumber > current.playerNumber ? 1 : previous.playerNumber < current.playerNumber ? -1 : 0;
      });

      const currentPlayer = players.find((player) => player.userId === userId);
      if (currentPlayer) {
        if (lastAction.gameStatus === GameStatus.IN_PROGRESS || lastAction.gameStatus === GameStatus.ENDED) {
          currentPlayer.allowedActions = ['ACTION_VIEW_GAME'];
        } else if (currentPlayer.userId !== game.ownerId) {
          currentPlayer.allowedActions = ['ACTION_LEAVE_GAME'];
        } else if (currentPlayer.userId === game.ownerId) {
          currentPlayer.allowedActions = ['ACTION_VIEW_GAME'];
        }
      } else {
        // add the viewing player
        players.push({ userId: userId, username: null, playerNumber: players.length + 1, score: 0, allowedActions: ['ACTION_JOIN_GAME'] });
      }

      // add the missing player icons
      while (players.length < game.expectedPlayerCount) {
        players.push({ userId: 0, username: null, playerNumber: players.length + 1, score: 0 });
      }

      setPlayers(players);
    });
  }

  function joinGame(): void {
    GameService.join(game.id).then(() => {
      router.push({ pathname: '/game', params: { id: game.id } });
    }).catch((error) => {
      notificationRef.current.error(error.toString());
    });
  }

  function leaveGame(): void {
    GameService.leave(game.id).then(() => {
      router.push('/search');
    }).catch((error) => {
      notificationRef.current.error(error.toString());
    });
  }

  function viewGame(): void {
    router.push({ pathname: '/game', params: { id: game.id } });
  }

  return (
    <View style={styles.detail}>
      {
        players.map((player: Player) => (
          <View
            key={'game_' + game.id + '_player_' + player.playerNumber}
            style={styles.player}>
            <Avatar.Image
              source={{ uri: process.env.EXPO_PUBLIC_PROFILE_PICTURE_URL + player.userId + '?' + new Date().getTime() }}
              size={40}
              style={styles.avatar} />
            {
              !player.allowedActions ?
                <Text variant="titleSmall">{player.username}</Text>
                :
                player.allowedActions.includes('ACTION_JOIN_GAME')
                  ?
                  <Button
                    icon={() => <Icon source="cursor-default-click-outline" color='#fff' size={20} />}
                    mode="contained"
                    buttonColor='#007bff'
                    labelStyle={styles.actionLabel}
                    onPress={() => joinGame()}>
                    {t('game.card.action.join')}
                  </Button>
                  :
                  player.allowedActions.includes('ACTION_LEAVE_GAME')
                    ?
                    <Button
                      icon={() => <Icon source="cursor-default-click-outline" color='#fff' size={20} />}
                      mode="contained"
                      buttonColor='#007bff'
                      labelStyle={styles.actionLabel}
                      onPress={() => leaveGame()}>
                      {t('game.card.action.leave')}
                    </Button>
                    :
                    <Button
                      icon={() => <Icon source="cursor-default-click-outline" color='#fff' size={20} />}
                      mode="contained"
                      buttonColor='#007bff'
                      labelStyle={styles.actionLabel}
                      onPress={() => viewGame()}>
                      {t('game.card.action.view')}
                    </Button>
            }
          </View>
        ))
      }
    </View>
  )
};

const styles = StyleSheet.create({
  detail: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'center',
    marginTop: 10,
  },
  player: {
    alignItems: 'center',
    marginLeft: 12,
    marginRight: 12,
    marginBottom: 8,
  },
  avatar: {
    marginBottom: 8,
  },
  actionLabel: {
    marginVertical: 4,
    marginRight: 10,
    marginLeft: 16,
  },
});
