import { ReactElement, useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';

import { PassedTime } from './passedTime';
import { PlayerScore } from './playerScore';
import { RemainingTime } from './remainingTime';
import { RemainingTile } from './remainingTileCount';

import PlayerService from '../../../services/player.service';

import { GameStatus } from '../../../model/game-status';
import { Player } from '../../../model/player';

export function ScoreBoard({ game, lastAction, viewingPlayer, notificationRef }) {

  const { t } = useTranslation();

  const [playerCards, setPlayerCards] = useState<ReactElement[]>([]);
  const [timer, setTimer] = useState<ReactElement>();
  const [remainingTile, setRemainingTile] = useState<ReactElement>();

  const winnerPlayerRef = useRef<Player>();

  useEffect(() => {
    if (!game || !lastAction || !viewingPlayer) {
      return;
    }

    if (lastAction.gameStatus === GameStatus.ENDED) {
      setTimer(null);
      setRemainingTile(<RemainingTile remainingTileCount={lastAction.remainingTileCount} />);
    } else if (lastAction.gameStatus === GameStatus.IN_PROGRESS) {
      if (lastAction.remainingTileCount === 0) {
        notificationRef.current.info(t('game.turn.last.round'));
      }
      setTimer(
        <RemainingTime
          totalDurationInSeconds={game.duration}
          lastUpdatedDate={lastAction.lastUpdatedDate} />
      );
      setRemainingTile(<RemainingTile remainingTileCount={lastAction.remainingTileCount} />);
    } else if (lastAction.gameStatus === GameStatus.WAITING) {
      setTimer(
        <PassedTime
          key={'timer_' + lastAction.version}
          createdDate={game.createdDate} />
      );
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

      if (lastAction.gameStatus === GameStatus.WAITING) {
        // add missing players for remaining slots
        while (players.length < game.expectedPlayerCount) {
          const player: Player = { userId: 0, username: '?', playerNumber: players.length + 1, score: 0 };
          players.push(player);
        }
      } else if (lastAction.gameStatus === GameStatus.ENDED) {
        winnerPlayerRef.current = players.reduce((previous, current) => {
          return previous.score > current.score ? previous : current;
        });
        if (winnerPlayerRef.current.playerNumber === viewingPlayer.playerNumber) {
          notificationRef.current.success(t('game.turn.viewing.player.won'));
        } else {
          notificationRef.current.info(t('game.turn.another.player.won', { 0: winnerPlayerRef.current.username }));
        }
      } else if (lastAction.gameStatus === GameStatus.IN_PROGRESS) {
        if (lastAction.currentPlayerNumber === viewingPlayer.playerNumber) {
          notificationRef.current.info(t('game.turn.viewing.player'));
        } else {
          const currentPlayer = players.find((player) => {
            return player.playerNumber === lastAction.currentPlayerNumber;
          });
          notificationRef.current.info(t('game.turn.another.player', { 0: currentPlayer.username }));
        }
      }
      setPlayerCards(createPlayerCards(players));
    });
  }

  function createPlayerCards(players: Player[]): ReactElement[] {
    const playerCards: ReactElement[] = [];
    players.map((player: Player) => (
      playerCards.push(
        <PlayerScore
          key={'playerNumber_' + player.playerNumber}
          lastAction={lastAction}
          player={player}
          winnerPlayer={winnerPlayerRef.current} />
      )
    ));
    return playerCards;
  }

  if (!playerCards) {
    return null;
  }

  return (
    <View style={styles.scoreBoard}>
      {timer}
      {playerCards}
      {remainingTile}
    </View>
  )
};

const styles = StyleSheet.create({
  scoreBoard: {
    flexDirection: 'row',
    borderRadius: 7,
    justifyContent: 'center',
    paddingTop: 2,
    paddingBottom: 2,
    marginRight: 8,
    marginLeft: 8,
  },
});
