import { useEffect, useState } from 'react';
import { Trans, useTranslation } from 'react-i18next';
import { ImageSourcePropType, StyleSheet, View } from 'react-native';
import { Card, Text } from 'react-native-paper';

export function Splash({ page }) {

  const { t } = useTranslation();

  const [splashImage, setSplashImage] = useState<ImageSourcePropType>();

  useEffect(() => {
    if (!page) {
      return;
    }

    if (page === 'signin') {
      setSplashImage(require('../../assets/images/signin-splash.jpg'));
    } else if (page === 'signup') {
      setSplashImage(require('../../assets/images/signup-splash.jpg'));
    }
  });

  if (!splashImage) {
    return null;
  }

  return (
    <View style={styles.container}>
      <Card.Cover
        source={splashImage}
        style={styles.splashLogo} />
      <Text style={styles.splashTitle}>
        <Trans
          i18nKey={'splash.title.' + page}
          values={{ scrabble: t('splash.title.scrabble') }}
          parent={Text}
          variant='titleLarge'
          style={styles.splashTitleText}
          components={{
            scrabbleStyle: (
              <Text
                variant="headlineMedium"
                style={styles.gameTitle}>
              </Text>
            )
          }} />
      </Text>
    </View>
  )
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  splashLogo: {
    flex: 1,
    backgroundColor: '#fff',
    marginBottom: 20,
  },
  splashTitle: {
    flexShrink: 1,
    textAlign: 'center',
    paddingLeft: 22,
    paddingRight: 22,
    marginBottom: 26,
  },
  splashTitleText: {
    textAlign: 'center',
  },
  gameTitle: {
    color: 'blue',
    fontWeight: 'bold',
  },
});
