import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';
import { Card, PaperProvider, Text, TouchableRipple } from 'react-native-paper';

import { Header } from '../components/layout/header';

export default function Menu() {

  const { t } = useTranslation();

  function onPressMenu(pageHref: string) {
    router.push(pageHref);
  }

  return (
    <PaperProvider>
      <LinearGradient
        colors={['#87bfcf', '#469db6']}
        style={styles.container}>
        <Header title={t('menu.title')} previousScreen={null} />
        <View style={styles.body}>
          <TouchableRipple
            onPress={() => onPressMenu("create")}
            style={styles.menuItem}>
            <Card style={styles.actionCard}>
              <Card.Cover
                source={require('../assets/images/new-game.jpg')}
                style={styles.actionImage} />
              <Card.Content style={styles.action}>
                <Text
                  variant="titleLarge"
                  style={styles.actionTitle}>
                  {t('menu.game.create.title')}
                </Text>
                <Text
                  variant="bodyMedium"
                  style={styles.actionDescription}>
                  {t('menu.game.create.description')}
                </Text>
              </Card.Content>
            </Card>
          </TouchableRipple>
          <TouchableRipple
            onPress={() => onPressMenu("search")}
            style={styles.menuItem}>
            <Card style={styles.actionCard}>
              <Card.Cover
                source={require('../assets/images/search-game.jpg')}
                style={styles.actionImage} />
              <Card.Content style={styles.action}>
                <Text
                  variant="titleLarge"
                  style={styles.actionTitle}>
                  {t('menu.game.search.title')}
                </Text>
                <Text
                  variant="bodyMedium"
                  style={styles.actionDescription}>
                  {t('menu.game.search.description')}
                </Text>
              </Card.Content>
            </Card>
          </TouchableRipple>
          <TouchableRipple
            onPress={() => onPressMenu("myGames")}
            style={styles.menuItem}>
            <Card style={styles.actionCard}>
              <Card.Cover
                source={require('../assets/images/my-games.jpg')}
                style={styles.actionImage} />
              <Card.Content style={styles.action}>
                <Text
                  variant="titleLarge"
                  style={styles.actionTitle}>
                  {t('menu.game.own.title')}
                </Text>
                <Text
                  variant="bodyMedium"
                  style={styles.actionDescription}>
                  {t('menu.game.own.description')}
                </Text>
              </Card.Content>
            </Card>
          </TouchableRipple>
        </View>
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
    justifyContent: 'center',
  },
  menuItem: {
    marginTop: 10,
  },
  actionCard: {
    flexDirection: 'row',
  },
  actionImage: {
    width: 360,
    height: 140,
    margin: 8,
  },
  action: {
    marginTop: 5,
  },
  actionTitle: {
    fontFamily: 'Playball-Regular',
  },
  actionDescription: {
    marginTop: 5,
  },
});