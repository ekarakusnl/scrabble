import { LinearGradient } from 'expo-linear-gradient';
import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { ScrollView, StyleSheet, View } from 'react-native';
import { ActivityIndicator, PaperProvider, SegmentedButtons } from 'react-native-paper';

import { GameCard } from '../components/search/gameCard';
import { Header } from '../components/layout/header';
import { MessageBox } from '../components/layout/messageBox';
import { Notification } from '../components/layout/notification';

import GameService from '../services/game.service';
import StorageService from '../services/storage.service';

import { Game } from '../model/game';
import { GameStatus } from '../model/game-status';

export default function MyGamesScreen() {

  const { t } = useTranslation();

  const [userId, setUserId] = useState<number>();
  const [userLoaded, setUserLoaded] = useState<boolean>(false);
  const [games, setGames] = useState<Game[]>([]);
  const [statusFilter, setStatusFilter] = useState<string>(GameStatus.IN_PROGRESS);

  const notificationRef = useRef(null);
  const statusFilterRef = useRef<GameStatus>(GameStatus.IN_PROGRESS);

  useEffect(() => {
    async function loadUser(): Promise<void> {
      setUserId(await StorageService.getUserId());
      setUserLoaded(true);
    }

    loadUser();
    searchGames();

  }, []);

  function searchGames(): void {
    GameService.searchByUser().then((games: Game[]) => {
      const filteredGames = games.filter(game => game.status === statusFilterRef.current.toString());
      setGames(filteredGames);
    }).catch((error) => {
      notificationRef.current.error(error.toString());
    });
  }

  function onChangeStatusFilter(statusFilter: string): void {
    statusFilterRef.current = GameStatus[statusFilter];
    setStatusFilter(statusFilter);
    searchGames();
  }

  if (!userLoaded) {
    return <ActivityIndicator animating={true} />;
  }

  return (
    <PaperProvider>
      <LinearGradient
        colors={['#87bfcf', '#469db6']}
        style={styles.container}>
        <Header title={t('menu.game.my.title')} previousScreen='menu' />
        <View style={styles.body}>
          <SegmentedButtons
            value={statusFilter}
            onValueChange={(statusFilter: string) => onChangeStatusFilter(statusFilter)}
            buttons={[
              {
                value: GameStatus.WAITING.toString(),
                label: t('my.games.status.waiting'),
                showSelectedCheck: true,
                checkedColor: '#000',
                uncheckedColor: '#000',
                style: styles.statusFilterButton,
                labelStyle: styles.statusFilterButtonLabel,
              },
              {
                value: GameStatus.IN_PROGRESS.toString(),
                label: t('my.games.status.progress'),
                showSelectedCheck: true,
                checkedColor: '#000',
                uncheckedColor: '#000',
                style: styles.statusFilterButton,
                labelStyle: styles.statusFilterButtonLabel,
              },
              {
                value: GameStatus.ENDED.toString(),
                label: t('my.games.status.ended'),
                showSelectedCheck: true,
                checkedColor: '#000',
                uncheckedColor: '#000',
                style: styles.statusFilterButton,
                labelStyle: styles.statusFilterButtonLabel,
              },
            ]}
            style={styles.statusFilterButtons}
          />
          <ScrollView style={styles.gameList}>
            {
              games.length === 0 ?
                <MessageBox message={t('search.no.game.found')} type='info' size={18} />
                :
                games.map((game: Game) => (
                  <GameCard key={'gameCard_' + game.id} userId={userId} game={game} notificationRef={notificationRef} />
                ))
            }
          </ScrollView>
        </View>
        <Notification notificationRef={notificationRef} />
      </LinearGradient>
    </PaperProvider>
  )
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  body: {
    alignItems: 'center',
  },
  statusFilterButtons: {
    borderRadius: 20,
    fontFamily: 'Gilroy-Bold',
    margin: 20,
  },
  statusFilterButton: {
    backgroundColor: '#f7f3f9',
    color: '#000',
    borderColor: '#000',
    borderWidth: 0.5,
  },
  statusFilterButtonLabel: {
    fontFamily: 'Playball-Regular',
    fontSize: 18,
  },
  gameList: {
    width: "80%",
  },
});
