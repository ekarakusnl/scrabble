import { LinearGradient } from 'expo-linear-gradient';
import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { ScrollView, StyleSheet, View } from 'react-native';
import { ActivityIndicator, PaperProvider } from 'react-native-paper';

import { GameCard } from '../components/search/gameCard';
import { Header } from '../components/layout/header';
import { MessageBox } from '../components/layout/messageBox';
import { Notification } from '../components/layout/notification';

import GameService from '../services/game.service';
import StorageService from '../services/storage.service';

import { Game } from '../model/game';

export default function MyGamesScreen() {

  const { t } = useTranslation();

  const [userId, setUserId] = useState<number>();
  const [userLoaded, setUserLoaded] = useState<boolean>(false);
  const [games, setGames] = useState<Game[]>([]);

  const notificationRef = useRef(null);

  useEffect(() => {
    async function loadUser(): Promise<void> {
      setUserId(await StorageService.getUserId());
      setUserLoaded(true);
    }

    loadUser();

    GameService.searchByUser().then((games: Game[]) => {
      setGames(games);
    }).catch((error) => {
      notificationRef.current.error(error.toString());
    });
  }, []);

  if (!userLoaded) {
    return <ActivityIndicator animating={true} />;
  }

  return (
    <PaperProvider>
      <LinearGradient
        colors={['#87bfcf', '#469db6']}
        style={styles.container}>
        <Header title={t('menu.game.own.title')} previousScreen='menu' />
        <View style={styles.body}>
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
    backgroundColor: '#d8d8d8',
  },
  body: {
    alignItems: 'center',
  },
  gameList: {
    width: "80%",
    marginTop: 20,
  },
});
