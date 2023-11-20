import { LinearGradient } from 'expo-linear-gradient';
import { router, useLocalSearchParams } from 'expo-router';
import { ReactElement, useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { ActivityIndicator, StyleSheet, View } from 'react-native';
import { PaperProvider, Portal } from 'react-native-paper';

import { Board } from '../components/game/board/board';
import { GameFooter } from '../components/game/footer';
import { GameChat } from '../components/game/chat';
import { GameHistory } from '../components/game/history';
import { Header } from '../components/layout/header';
import { Notification } from '../components/layout/notification';
import { Rack } from '../components/game/rack/rack';
import { ScoreBoard } from '../components/game/scoreBoard/board';

import ActionService from '../services/action.service';
import GameService from '../services/game.service';
import PlayerService from '../services/player.service';

import { Action } from '../model/action';
import { DroppableZone } from '../model/droppable-zone';
import { Game } from '../model/game';
import { GameStatus } from '../model/game-status';
import { Player } from '../model/player';

export default function GameScreen() {

  const { t } = useTranslation();
  const { id } = useLocalSearchParams();

  // page components
  const [gamePanel, setGamePanel] = useState<ReactElement>();

  // page component states
  const scoreBoardRef = useRef<ReactElement>();
  const boardRef = useRef<ReactElement>();
  const rackRef = useRef<ReactElement>();
  const chatRef = useRef<null>();
  const historyRef = useRef<null>();
  const notificationRef = useRef<null>();
  const footerRef = useRef<null>();

  // shared variables
  const gameRef = useRef<Game>();
  const playerRef = useRef<Player>();
  const lastActionRef = useRef<Action>();
  const boardZoneRef = useRef<DroppableZone>();

  // page variables
  const syncLastActionRef = useRef<boolean>(false);
  const versionRef = useRef<number>(0);

  useEffect(() => {
    subscribeLastAction();
    loadGame(Number(id));

    return () => {
      unsubscribeLastAction();
    };
  }, []);

  function subscribeLastAction(): void {
    if (!syncLastActionRef.current) {
      syncLastActionRef.current = true;
    }
  }

  function unsubscribeLastAction(): void {
    if (syncLastActionRef.current) {
      syncLastActionRef.current = false;
    }
  }

  function loadGame(id: number): void {
    GameService.get(id).then((game: Game) => {
      if (!game || game.status === GameStatus.TERMINATED) {
        router.push('/myGames');
        return;
      }

      gameRef.current = game;

      versionRef.current = gameRef.current.version - 1;

      getPlayer();
      getLastAction();
    });
  }

  function getPlayer(): void {
    PlayerService.get(gameRef.current.id).then((player: Player) => {
      playerRef.current = player;
    });
  }

  function getLastAction(): void {
    if (!syncLastActionRef.current) {
      return;
    }

    var currentVersion = versionRef.current + 1;
    ActionService.get(gameRef.current.id, currentVersion).then((action: Action) => {
      if (action.version) {
        lastActionRef.current = action;
        versionRef.current = lastActionRef.current.version;

        if (lastActionRef.current.gameStatus === GameStatus.ENDED) {
          // game is ended, use the previous version to show the latest board and score
          versionRef.current = versionRef.current - 1;
          createGamePanel();
          return;
        }

        createGamePanel();
      }
      getLastAction();
    });
  }

  function createScoreBoard(): ReactElement {
    return (
      <ScoreBoard
        game={gameRef.current}
        lastAction={lastActionRef.current}
        viewingPlayer={playerRef.current}
        notificationRef={notificationRef} />
    );
  }

  function createBoard(): ReactElement {
    return (
      <Board
        game={gameRef.current}
        lastAction={lastActionRef.current}
        boardRef={boardRef}
        boardZoneRef={boardZoneRef}
        rackRef={rackRef}
        notificationRef={notificationRef} />
    );
  }

  function createRack(): ReactElement {
    return (
      <Rack
        game={gameRef.current}
        lastAction={lastActionRef.current}
        viewingPlayer={playerRef.current}
        rackRef={rackRef}
        boardRef={boardRef}
        boardZoneRef={boardZoneRef}
        notificationRef={notificationRef} />
    );
  }

  function createGamePanel(): void {
    scoreBoardRef.current = createScoreBoard();
    boardRef.current = createBoard();
    rackRef.current = createRack();

    const gamePanel = (
      <View style={styles.gamePanel}>
        {scoreBoardRef.current}
        {boardRef.current}
        {rackRef.current}
      </View>
    );
    setGamePanel(gamePanel);
  };

  if (!gameRef.current || !lastActionRef.current || !gamePanel) {
    return <ActivityIndicator animating={true} />;
  }

  return (
    <PaperProvider>
      <Portal>
        <GameChat
          game={gameRef.current}
          viewingPlayer={playerRef.current}
          chatRef={chatRef}
          notificationRef={notificationRef}
          footerRef={footerRef} />
      </Portal>
      <Portal>
        <GameHistory
          game={gameRef.current}
          lastAction={lastActionRef.current}
          historyRef={historyRef}
          notificationRef={notificationRef}
          footerRef={footerRef} />
      </Portal>
      <Header title={t('game.game') + ' : ' + gameRef.current.name} previousScreen='menu' />
      <LinearGradient
        colors={['#87bfcf', '#469db6']}
        style={styles.container}>
        {gamePanel}
      </LinearGradient>
      <Notification notificationRef={notificationRef} />
      <GameFooter
        lastAction={lastActionRef.current}
        rackRef={rackRef}
        chatRef={chatRef}
        historyRef={historyRef}
        footerRef={footerRef} />
    </PaperProvider>
  )
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  gamePanel: {
    marginTop: 10,
  },
});
